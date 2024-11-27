package net.haydenwelton.familymap;

import static org.junit.Assert.assertEquals;

import android.content.SharedPreferences;

import com.github.ivanshafran.sharedpreferencesmock.SPMockBuilder;

import net.haydenwelton.familymap.data.DataCache;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;

import model.AuthToken;
import model.Event;
import model.Person;
import model.User;
import requests.RegisterRequest;
import responses.EventResponse;
import responses.PersonResponse;
import responses.RegisterResponse;

public class FilterTest {
    private static final String HOST = "localhost";
    private static final String PORT = "8080";
    private static HashMap<String, Person> personMap = null;


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

        personMap = dataCache.getPersonMap();
    }

    @Test
    public void filterMales() {
        SharedPreferences prefs = new SPMockBuilder().createSharedPreferences();
        prefs.edit().putBoolean("male", true).commit();
        DataCache dataCache = DataCache.getFilteredInstance(prefs);

        for (Event event : dataCache.getEvents()) {
            Person person = personMap.get(event.getPersonID());
            assert person != null;
            assertEquals("m", person.getGender());
        }
    }

    @Test
    public void filterFemales() {
        SharedPreferences prefs = new SPMockBuilder().createSharedPreferences();
        prefs.edit().putBoolean("female", true).commit();
        DataCache dataCache = DataCache.getFilteredInstance(prefs);

        for (Event event : dataCache.getEvents()) {
            Person person = personMap.get(event.getPersonID());
            assert person != null;
            assertEquals("f", person.getGender());
        }
    }


    @Test
    public void filterMalesAndFemales() {
        SharedPreferences prefs = new SPMockBuilder().createSharedPreferences();
        prefs.edit().putBoolean("female", false).commit();
        prefs.edit().putBoolean("male", false).commit();
        DataCache dataCache = DataCache.getFilteredInstance(prefs);

        assertEquals(0, dataCache.getEvents().length);
    }


    @AfterClass
    public static void cleanUp() {
        ServerProxy proxy = new ServerProxy();
        proxy.clear(HOST, PORT);
    }

}
