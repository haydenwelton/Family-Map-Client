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
import responses.LoginResponse;
import responses.PersonResponse;
import responses.RegisterResponse;

public class PersonsTest {
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
    public void getPersonsPass() {
        ServerProxy proxy = new ServerProxy();
        LoginRequest request = new LoginRequest("username", "password");
        LoginResponse loginResponse = proxy.login(request, HOST, PORT);

        DataCache dataCache = DataCache.getInstance();
        dataCache.setAuthToken(new AuthToken(loginResponse.getAuthtoken(), loginResponse.getUsername()));

        PersonResponse personResponse = proxy.people(HOST, PORT);

        assertNotNull(personResponse);
        assertTrue(personResponse.getSuccess());
        assertNotNull(personResponse.getData());
        assertEquals(31, personResponse.getData().length);
    }

    @Test
    public void getPersonsInvalidAuthToken() {
        ServerProxy proxy = new ServerProxy();
        DataCache dataCache = DataCache.getInstance();
        dataCache.setAuthToken(new AuthToken("NULL", "username"));
        PersonResponse personResponse = proxy.people(HOST, PORT);

        assertNotNull(personResponse);
        assertFalse(personResponse.getSuccess());
        assertNull(personResponse.getData());
    }

    @AfterClass
    public static void cleanUp() {
        ServerProxy proxy = new ServerProxy();
        proxy.clear(HOST, PORT);
    }
}
