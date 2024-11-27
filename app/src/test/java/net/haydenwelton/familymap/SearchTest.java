package net.haydenwelton.familymap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import net.haydenwelton.familymap.data.DataCache;
import net.haydenwelton.familymap.data.DataProcessor;
import net.haydenwelton.familymap.model.SearchResult;

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

public class SearchTest {
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

        PersonResponse personResult = proxy.people(HOST, PORT);
        dataCache.setPersons(personResult.getData());

        EventResponse eventResult = proxy.events(HOST, PORT);
        dataCache.setEvents(eventResult.getData());
    }

    @Test
    public void searchPersons() {
        List<SearchResult> results = new ArrayList<>();
        HashMap<String, Person> personMap = DataCache.getInstance().getPersonMap();
        DataProcessor.searchPersons(results, "us");

        for (SearchResult result : results) {
            assertTrue(DataProcessor.containsIgnoreCase(result.getMainInfo(), "us") ||
                    DataProcessor.containsIgnoreCase(result.getSubInfo(), "us"));
            Person person = personMap.get(result.getId());
            assertNotNull(person);
            assertTrue(DataProcessor.containsIgnoreCase(person.getFirstName(), "us") ||
                    DataProcessor.containsIgnoreCase(person.getLastName(), "us"));
        }
    }

    @Test
    public void searchEvents() {
        List<SearchResult> results = new ArrayList<>();
        DataProcessor.searchEvents(results, "e");

        for (SearchResult result : results) {
            assertTrue(DataProcessor.containsIgnoreCase(result.getMainInfo(), "e") ||
                    DataProcessor.containsIgnoreCase(result.getSubInfo(), "e"));
        }
    }

    @Test
    public void searchPersonWithSpaces() {
        List<SearchResult> results = new ArrayList<>();
        DataProcessor.searchPersons(results, "firstname lastname");
        assertFalse(results.isEmpty());
    }

    @Test
    public void searchNoResults() {
        List<SearchResult> results = new ArrayList<>();
        DataProcessor.searchPersons(results, "`");
        DataProcessor.searchEvents(results, "=");
        DataProcessor.searchPersons(results, "");
        DataProcessor.searchEvents(results, "");
        DataProcessor.searchPersons(results, " ");
        DataProcessor.searchEvents(results, " ");

        assertTrue(results.isEmpty());
    }

    @AfterClass
    public static void cleanUp() {
        ServerProxy proxy = new ServerProxy();
        proxy.clear(HOST, PORT);
    }
}
