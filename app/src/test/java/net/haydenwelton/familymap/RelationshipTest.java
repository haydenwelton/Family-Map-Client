package net.haydenwelton.familymap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import net.haydenwelton.familymap.data.DataCache;
import net.haydenwelton.familymap.data.DataProcessor;
import net.haydenwelton.familymap.model.FamilyMember;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import model.AuthToken;
import model.Person;
import model.User;
import requests.RegisterRequest;
import responses.EventResponse;
import responses.PersonResponse;
import responses.RegisterResponse;

public class RelationshipTest {
    private static final String HOST = "localhost";
    private static final String PORT = "8080";


    @BeforeClass
    public static void setup() {
        ServerProxy proxy = new ServerProxy();
        DataCache dataCache = DataCache.getInstance();


        RegisterRequest request = new RegisterRequest("username", "password", "email@gmail.com",
                "firstName", "lastName", "m");
        RegisterResponse response = proxy.register(request, HOST, PORT);
        dataCache.setUser(new User(response.getUsername(), "password", "email@gmail.com",
                "firstName", "lastName", "m", response.getPersonID()));
        dataCache.setAuthToken(new AuthToken(response.getAuthtoken(), response.getUsername()));

        PersonResponse personResponse = proxy.people(HOST, PORT);
        dataCache.setPersons(personResponse.getData());

        EventResponse eventResponse = proxy.events(HOST, PORT);
        dataCache.setEvents(eventResponse.getData());
    }

    @Test
    public void testCorrectFamily() {
        DataCache dataCache = DataCache.getInstance();
        HashMap<String, Person> personMap = dataCache.getPersonMap();
        Person rootPerson = personMap.get(dataCache.getUser().getPersonID());
        List<FamilyMember> familyMembers = DataProcessor.findFamily(rootPerson);

        int totalFamilyMembers = 0;
        assert rootPerson != null;
        if (rootPerson.getMotherID() != null) {
            assertEquals(rootPerson.getMotherID(),
                    getFamilyMember(familyMembers, "mother").getPerson().getPersonID());
            totalFamilyMembers++;
        }
        if (rootPerson.getFatherID() != null) {
            assertEquals(rootPerson.getFatherID(),
                    getFamilyMember(familyMembers, "father").getPerson().getPersonID());
            totalFamilyMembers++;
        }
        if (rootPerson.getSpouseID() != null) {
            assertEquals(rootPerson.getSpouseID(),
                    getFamilyMember(familyMembers, "spouse").getPerson().getPersonID());
            totalFamilyMembers++;
        }

        List<FamilyMember> children = getChildren(familyMembers);
        for (FamilyMember familyMember : children) {
            Person childOnMap = personMap.get(familyMember.getPerson().getPersonID());
            assertTrue(childOnMap.getFatherID().equals(rootPerson.getPersonID()) ||
                    childOnMap.getMotherID().equals(rootPerson.getPersonID()));
        }

        // Check if all children were found
        for (Person person : dataCache.getPersons()) {
            if (rootPerson.getPersonID().equals(person.getMotherID()) ||
                    rootPerson.getPersonID().equals(person.getFatherID())) {
                totalFamilyMembers++;
            }
        }

        assertEquals(totalFamilyMembers, familyMembers.size());
    }

    @Test
    public void testCorrectFamilyFail() {
        DataCache dataCache = DataCache.getInstance();
        HashMap<String, Person> personMap = dataCache.getPersonMap();
        Person rootPerson = personMap.get(dataCache.getUser().getPersonID());
        List<FamilyMember> familyMembers = DataProcessor.findFamily(rootPerson);

        int totalFamilyMembers = 0;
        assert rootPerson != null;
        if (rootPerson.getMotherID() != null) {
            assertEquals(rootPerson.getMotherID(),
                    getFamilyMember(familyMembers, "mother").getPerson().getPersonID());
            totalFamilyMembers++;
        }
        if (rootPerson.getFatherID() != null) {
            assertEquals(rootPerson.getFatherID(),
                    getFamilyMember(familyMembers, "father").getPerson().getPersonID());
            totalFamilyMembers++;
        }
        if (rootPerson.getSpouseID() != null) {
            assertEquals(rootPerson.getSpouseID(),
                    getFamilyMember(familyMembers, "spouse").getPerson().getPersonID());
            totalFamilyMembers++;
        }

        List<FamilyMember> children = getChildren(familyMembers);
        for (FamilyMember familyMember : children) {
            Person childOnMap = personMap.get(familyMember.getPerson().getPersonID());
            assertTrue(childOnMap.getFatherID().equals(rootPerson.getPersonID()) ||
                    childOnMap.getMotherID().equals(rootPerson.getPersonID()));
        }

        // Check if all children were found
        for (Person person : dataCache.getPersons()) {
            if (rootPerson.getPersonID().equals(person.getMotherID()) ||
                    rootPerson.getPersonID().equals(person.getFatherID())) {
                totalFamilyMembers++;
            }
        }

        assertEquals(totalFamilyMembers, familyMembers.size());
        assertFalse(familyMembers.size() > 10); //Returns a different size of family than expected
    }




    private FamilyMember getFamilyMember(List<FamilyMember> familyMembers, String relation) {
        for (FamilyMember familyMember : familyMembers) {
            if (familyMember.getRelationship().equalsIgnoreCase(relation)) {
                return familyMember;
            }
        }
        return null;
    }

    private List<FamilyMember> getChildren(List<FamilyMember> familyMembers) {
        List<FamilyMember> children = new ArrayList<>();
        for (FamilyMember familyMember : familyMembers) {
            if (familyMember.getRelationship().equalsIgnoreCase("child")) {
                children.add(familyMember);
            }
        }
        return children;
    }

    @AfterClass
    public static void cleanUp() {
        ServerProxy proxy = new ServerProxy();
        proxy.clear(HOST, PORT);
    }
}
