package net.haydenwelton.familymap;

import android.util.Log;
import com.google.gson.Gson;
import net.haydenwelton.familymap.data.DataCache;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import requests.LoginRequest;
import requests.RegisterRequest;
import responses.ClearResponse;
import responses.EventResponse;
import responses.LoginResponse;
import responses.PersonResponse;
import responses.RegisterResponse;

public class ServerProxy {

    // Sends a login request to the server and returns a LoginResponse object.
    public LoginResponse login(LoginRequest request, String hostNum, String portNum) {
        try {
            // Check that the host and port are valid.
            if (hostNum.isEmpty() || hostNum == null) {
                throw new MalformedURLException("Invalid host");
            }
            if (portNum.isEmpty() || portNum == null) {
                throw new MalformedURLException("Invalid port number");
            }

            // Create a URL object from the host and port numbers.
            URL url = new URL("HTTP", hostNum, Integer.parseInt(portNum), "/user/login");
            // Open an HTTP connection to the server.
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            // Set the request method to POST.
            connection.setRequestMethod("POST");
            // Allow output from the connection.
            connection.setDoOutput(true);
            // Set the Accept header to JSON.
            connection.addRequestProperty("Accept", "application/json");
            // Connect to the server.
            connection.connect();

            // Create a GSON object to convert between JSON and Java objects.
            Gson gson = new Gson();
            // Convert the request object to a JSON string.
            String jsonRequest = gson.toJson(request);
            // Get an output stream from the connection and write the JSON string to it.
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(jsonRequest.getBytes(StandardCharsets.UTF_8));
            outputStream.close();

            // If the response code indicates success, parse the response body and return a LoginResponse object.
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream is = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(is);
                LoginResponse response = gson.fromJson(reader, LoginResponse.class);
                is.close();
                return response;
            }
            // If the response code indicates failure, return a LoginResponse object with an error message.
            else {
                return new LoginResponse(null, null, null,
                        false, "Unable to connect to server");
            }
        }
        catch (IOException e) {
            // If there is an exception, log an error and return a LoginResponse object with an error message.
            Log.e("Proxy", "Login: " + e.getMessage());
            return new LoginResponse(null, null, null,
                    false, "Invalid URL");
        }
    }

    // Sends a register request to the server and returns a RegisterResponse object.
    public RegisterResponse register(RegisterRequest request, String hostNumber, String portNumber) {
        try {
            // Check that the host and port are valid.
            if (hostNumber.isEmpty() || hostNumber == null) {
                throw new MalformedURLException("Invalid host");
            }
            if (portNumber.isEmpty() || portNumber == null) {
                throw new MalformedURLException("Invalid port number");
            }

            // Create a URL object from the host and port numbers.
            URL url = new URL("HTTP", hostNumber, Integer.parseInt(portNumber), "/user/register");

            // Open an HTTP connection to the server.
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.addRequestProperty("Accept", "application/json");
            connection.connect();

            Gson gson = new Gson();

            // Convert the RegisterRequest object to a JSON string.
            String jsonRequest = gson.toJson(request);

            // Get the output stream from the connection and write the JSON string to it.
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(jsonRequest.getBytes(StandardCharsets.UTF_8));
            outputStream.close();

            // Check if the connection was successful.
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                Log.d("Proxy", "Opening response stream...");

                // Get the input stream from the connection and create a reader for it.
                InputStream inputStream = connection.getInputStream();
                InputStreamReader streamReader = new InputStreamReader(inputStream);

                // Convert the JSON response to a RegisterResponse object.
                RegisterResponse registerResponse = gson.fromJson(streamReader, RegisterResponse.class);

                Log.d("Proxy", "Response stream closed");
                inputStream.close();
                return registerResponse;
            } else {
                // If the connection was not successful, return a RegisterResponse with error information.
                return new RegisterResponse(null, null, null, false, "Unable to connect to server");
            }

        }
        catch (IOException e) {
            // If an exception is caught during the registration process, return a RegisterResponse with error information.
            Log.e("Proxy", "Register: " + e.getMessage());
            return new RegisterResponse(null, null, null, false,
                    e.getMessage());
        }
    }

    public PersonResponse people(String hostNumber, String portNumber) {
        try {
            // Check if hostNumber is empty or null
            if (hostNumber.isEmpty() || hostNumber == null) {
                throw new MalformedURLException("Invalid host");
            }
            // Check if portNumber is empty or null
            if (portNumber.isEmpty() || portNumber == null) {
                throw new MalformedURLException("Invalid port number");
            }

            // Get an instance of the DataCache singleton class
            DataCache dataCache = DataCache.getInstance();

            // Create a new URL object using the provided hostNumber and portNumber
            URL url = new URL("HTTP", hostNumber, Integer.parseInt(portNumber), "/person/");

            // Open a connection to the URL using HTTP GET method
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Set the Authorization header in the HTTP request using the authentication token
            // retrieved from the DataCache singleton
            connection.setRequestProperty("Authorization", dataCache.getAuthToken().getAuthToken());

            // Establish the connection
            connection.connect();

            // Check if the response code is HTTP_OK (200)
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                // Create a new Gson object for JSON parsing
                Gson gson = new Gson();

                // Get an input stream from the connection and wrap it in an InputStreamReader
                // for character decoding
                InputStream inputStream = connection.getInputStream();
                InputStreamReader streamReader = new InputStreamReader(inputStream);

                // Parse the JSON response using Gson and create a new PersonResponse object
                PersonResponse personResponse = gson.fromJson(streamReader, PersonResponse.class);

                // Close the input stream
                inputStream.close();

                // Return the PersonResponse object
                return personResponse;
            }
            else {
                // If the response code is not HTTP_OK, return a new PersonResponse object with
                // an error message
                return new PersonResponse(null, "Unable to connect to server", false);
            }
        }
        catch (IOException e) {
            // If an IOException is caught, return a new PersonResponse object with an error message
            // indicating an invalid URL
            Log.e("Proxy", "Persons: " + e.getMessage());
            return new PersonResponse(null, "Invalid URL", false);
        }
    }

    public EventResponse events(String hostNumber, String portNumber) {
        try {
            // Validate the host number and port number inputs
            if (hostNumber.isEmpty() || hostNumber == null) {
                throw new MalformedURLException("Invalid host");
            }
            if (portNumber.isEmpty() || portNumber == null) {
                throw new MalformedURLException("Invalid port number");
            }

            // Get the authentication token from the cache
            DataCache dataCache = DataCache.getInstance();

            // Create the URL for the event API endpoint
            URL url = new URL("HTTP", hostNumber, Integer.parseInt(portNumber), "/event/");

            // Open the HTTP connection to the URL
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            // Set the request method and add the authentication token to the header
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Authorization", dataCache.getAuthToken().getAuthToken());

            // Connect to the API endpoint
            urlConnection.connect();

            // If the response code is OK, parse the JSON response
            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                Gson gson = new Gson();
                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader streamReader = new InputStreamReader(inputStream);

                // Log a message to indicate that the response stream has been closed
                Log.d("Proxy", "Events: Response stream closed");

                // Parse the response JSON into an EventResponse object
                EventResponse eventResponse = gson.fromJson(streamReader, EventResponse.class);

                // Close the input stream
                inputStream.close();

                // Return the EventResponse object
                return eventResponse;
            }
            else {
                // If the response code is not OK, return a new EventResponse object with an error message
                return new EventResponse(null, "Unable to connect to server", false);
            }
        }
        catch (IOException e) {
            // If an exception occurs, log the error message and return a new EventResponse object with an error message
            Log.e("Proxy", "Events: " + e.getMessage());
            return new EventResponse(null, "Invalid URL", false);
        }
    }


    public ClearResponse clear(String hostNumber, String portNumber) {
        try {
            // Validate the host number and port number inputs
            if (hostNumber.isEmpty() || hostNumber == null) {
                throw new MalformedURLException("Invalid host");
            }
            if (portNumber.isEmpty() || portNumber == null) {
                throw new MalformedURLException("Invalid port number");
            }
            // Create URL object for clear command
            URL url = new URL("HTTP", hostNumber, Integer.parseInt(portNumber), "/clear/");
            // Create HttpURLConnection object and send POST request to clear command URL
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.connect();
            // Check response code for success
            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                // Create Gson object for parsing JSON response
                Gson gson = new Gson();
                // Get input stream and create input stream reader
                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader streamReader = new InputStreamReader(inputStream);
                // Parse JSON response into ClearResponse object
                ClearResponse clearResponse = gson.fromJson(streamReader, ClearResponse.class);
                // Close input stream
                inputStream.close();
                // Log successful clear command
                Log.d("Proxy", "Clear: clear command successful");
                return clearResponse;
            }
            else {
                // Return error message for failed connection
                return new ClearResponse("Unable to connect to server", false);
            }
        }
        catch (IOException e) {
            // Log and return error message for invalid URL
            Log.e("Proxy", e.getMessage());
            return new ClearResponse("Invalid URL", false);
        }
    }
}
