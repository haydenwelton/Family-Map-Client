package net.haydenwelton.familymap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import requests.LoginRequest;
import requests.RegisterRequest;
import responses.LoginResponse;
import responses.RegisterResponse;

public class LoginTest {
    private static final String HOST = "localhost";
    private static final String PORT = "8080";

    @BeforeClass
    public static void setup() {
        ServerProxy proxy = new ServerProxy();
        RegisterRequest request = new RegisterRequest("username", "password", "email@gmail.com",
                "firstName", "lastName", "m");
        RegisterResponse result = proxy.register(request, HOST, PORT);
    }


    @Test
    public void loginPass() {
        ServerProxy proxy = new ServerProxy();
        LoginRequest request = new LoginRequest("username", "password");
        LoginResponse result = proxy.login(request, HOST, PORT);

        assertNotNull(result);
        assertTrue(result.getSuccess());
        assertNotNull(result.getAuthtoken());
        assertNotNull(result.getUsername());
        assertNotNull(result.getPersonID());
    }

    @Test
    public void loginFail() {
        ServerProxy proxy = new ServerProxy();
        LoginRequest request = new LoginRequest("username", "INVALIDPASSWORD");
        LoginResponse result = proxy.login(request, HOST, PORT);

        assertNotNull(result);
        assertFalse(result.getSuccess());
    }

    @AfterClass
    public static void cleanUp() {
        ServerProxy proxy = new ServerProxy();
        proxy.clear(HOST, PORT);
    }
}
