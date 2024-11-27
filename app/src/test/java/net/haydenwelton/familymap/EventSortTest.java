package net.haydenwelton.familymap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import net.haydenwelton.familymap.data.DataCache;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import model.AuthToken;
import model.Event;
import model.Person;
import model.User;
import requests.RegisterRequest;
import responses.EventResponse;
import responses.PersonResponse;
import responses.RegisterResponse;

public class EventSortTest {
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
    public void testSort() {
        DataCache dataCache = DataCache.getInstance();
        HashMap<String, Person> personMap = dataCache.getPersonMap();
        HashMap<Person, List<Event>> dataMap = new HashMap<>();

        for (Event event : dataCache.getEvents()) {
            Person person = personMap.get(event.getPersonID());
            if (!dataMap.containsKey(person)) {
                dataMap.put(person, new ArrayList<>());
            }
            dataMap.get(person).add(event);
        }

        for (Map.Entry<Person, List<Event>> entry : dataMap.entrySet()) {
            entry.getValue().sort(new Event.EventComparator());
        }

        for (Map.Entry<Person, List<Event>> entry : dataMap.entrySet()) {
            int min = -1;
            for (Event event : entry.getValue()) {
                assertTrue(event.getYear() > min);
                min = event.getYear();
            }
        }
    }

    @Test
    public void testBirthAfterDeath() {
        Event bestevent1 = new Event("00000", "username", "99999", 10f,
                10f, "USA", "Riverton", "Birth", 2012);
        Event bestevent2 = new Event("00000", "username", "99999", 10f,
                10f, "USA", "Riverton", "Death", 2001);

        List<Event> events = Stream.of(bestevent2, bestevent1).collect(Collectors.toList());
        events.sort(new Event.EventComparator());

        assertEquals("Birth", events.get(0).getEventType());
        assertEquals("Death", events.get(1).getEventType());
    }

    @Test
    public void testSortSameYear() {
        Event bestevent1 = new Event("00000", "username", "99999", 10f,
                10f, "USA", "Riverton", "Birth", 2001);
        Event bestevent2 = new Event("00000", "username", "99999", 10f,
                10f, "USA", "Riverton", "Test", 2001);
        Event bestevent3 = new Event("00000", "username", "99999", 10f,
                10f, "USA", "Riverton", "Death", 2001);

        List<Event> events = Stream.of(bestevent1, bestevent3, bestevent2).collect(Collectors.toList());
        events.sort(new Event.EventComparator());

        assertEquals("Birth", events.get(0).getEventType());
        assertEquals("Test", events.get(1).getEventType());
        assertEquals("Death", events.get(2).getEventType());
    }



    @AfterClass
    public static void cleanUp() {
        ServerProxy proxy = new ServerProxy();
        proxy.clear(HOST, PORT);
    }
}
