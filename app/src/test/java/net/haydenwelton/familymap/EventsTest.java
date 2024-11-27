package net.haydenwelton.familymap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import net.haydenwelton.familymap.data.DataCache;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import model.AuthToken;
import requests.LoginRequest;
import requests.RegisterRequest;
import responses.EventResponse;
import responses.LoginResponse;
import responses.RegisterResponse;

public class EventsTest {
    private static final String HOST = "localhost";
    private static final String PORT = "8080";

    @BeforeClass
    public static void setup() {
        ServerProxy proxy = new ServerProxy();
        RegisterRequest request = new RegisterRequest("username", "password", "email@gmail.com",
                "firstName", "lastName", "m");
        RegisterResponse response = proxy.register(request, HOST, PORT);
    }

    @Test
    public void getEventsPass() {
        ServerProxy proxy = new ServerProxy();
        LoginRequest request = new LoginRequest("username", "password");
        LoginResponse loginResponse = proxy.login(request, HOST, PORT);

        DataCache dataCache = DataCache.getInstance();
        dataCache.setAuthToken(new AuthToken(loginResponse.getAuthtoken(), loginResponse.getUsername()));

        EventResponse eventResponse = proxy.events(HOST, PORT);

        assertNotNull(eventResponse);
        assertTrue(eventResponse.getSuccess());
        assertNotNull(eventResponse.getData());
        assertEquals(91, eventResponse.getData().length);
    }

    @Test
    public void getEventsInvalidAuthToken() {
        ServerProxy proxy = new ServerProxy();
        DataCache dataCache = DataCache.getInstance();
        dataCache.setAuthToken(new AuthToken("NULL", "username"));
        EventResponse eventResponse = proxy.events(HOST, PORT);

        assertNotNull(eventResponse);
        assertFalse(eventResponse.getSuccess());
        assertNull(eventResponse.getData());
    }

    @AfterClass
    public static void cleanUp() {
        ServerProxy proxy = new ServerProxy();
        proxy.clear(HOST, PORT);
    }
}
