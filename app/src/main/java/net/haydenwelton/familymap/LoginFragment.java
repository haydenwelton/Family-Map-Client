package net.haydenwelton.familymap;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import net.haydenwelton.familymap.data.DataCache;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import model.AuthToken;
import model.Person;
import model.User;
import requests.LoginRequest;
import requests.RegisterRequest;
import responses.EventResponse;
import responses.LoginResponse;
import responses.PersonResponse;
import responses.RegisterResponse;


public class LoginFragment extends Fragment {
    enum Field { HOST, PORT, USERNAME, PASSWORD, FIRST_NAME, LAST_NAME, EMAIL, GENDER }
    protected Listener listener;

    public interface Listener {
        void notifyWhenCompleted();
    }

    public void registerListener(Listener listener) { this.listener = listener; }

    private static final String FIRST_NAME_KEY = "firstName";
    private static final String LAST_NAME_KEY = "lastName";
    private static final String STATUS_KEY = "success";

    public LoginFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View theView = inflater.inflate(R.layout.login_fragment, container, false);
        Button loginButton = theView.findViewById(R.id.loginButton);
        Button registerButton = theView.findViewById(R.id.registerButton);

        RadioGroup radioGroup = theView.findViewById(R.id.genderSelection);
        loginButton.setEnabled(false);
        registerButton.setEnabled(false);

        Map<Field, EditText> loginFields = new EnumMap<>(Field.class);
        Map<Field, EditText> registerFields = new EnumMap<>(Field.class);
        Map<Field, Boolean> activeFields = new EnumMap<>(Field.class);

        for (Field field : Field.values()) {
            activeFields.put(field, false);
        }
        // Find all of the views by ID
        //Field for host
        loginFields.put(Field.HOST, theView.findViewById(R.id.hostField));
        //Field for port
        loginFields.put(Field.PORT, theView.findViewById(R.id.portField));
        //Field for username
        loginFields.put(Field.USERNAME, theView.findViewById(R.id.usernameField));
        //Field for password
        loginFields.put(Field.PASSWORD, theView.findViewById(R.id.passwordField));
        //Field for firstName
        registerFields.put(Field.FIRST_NAME, theView.findViewById(R.id.firstNameField));
        //Field for lastName
        registerFields.put(Field.LAST_NAME, theView.findViewById(R.id.lastNameField));
        //Field for email
        registerFields.put(Field.EMAIL, theView.findViewById(R.id.emailField));

        for (Map.Entry<Field, EditText> entry : Stream.concat(loginFields.entrySet().stream(),
                registerFields.entrySet().stream()).collect(Collectors.toSet())) {
            entry.getValue().addTextChangedListener(new TextWatcher() {
                boolean changeButtons = false;
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // If field is empty, buttons will change on text update
                    if (s.toString().trim().length() == 0) {
                        changeButtons = true;
                    }
                }
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.toString().trim().length() > 0) {
                        activeFields.put(entry.getKey(), true);
                    }
                    else {
                        activeFields.put(entry.getKey(), false);
                        changeButtons = true;
                    }
                    if (changeButtons) {
                        updateButtons(activeFields, loginFields, loginButton, registerButton);
                    }
                }
                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            boolean changeButtons = false;
            if (activeFields.containsKey(Field.GENDER)) {
                changeButtons = true;
            }
            activeFields.put(Field.GENDER, true);
            if (changeButtons) {
                updateButtons(activeFields, loginFields, loginButton, registerButton);
            }
        });
        Context currentContext = getContext();

        //Login button pressed
        loginButton.setOnClickListener(v -> {
            Log.d("Login", "Login button pressed!");
            //Field for host number
            String host = Objects.requireNonNull(loginFields.get(Field.HOST)).getText().toString();
            //Field for port number
            String port = Objects.requireNonNull(loginFields.get(Field.PORT)).getText().toString();
            //Field for username
            String username = Objects.requireNonNull(loginFields.get(Field.USERNAME)).getText().toString();
            //Field for password
            String password = Objects.requireNonNull(loginFields.get(Field.PASSWORD)).getText().toString();

            LoginData loginData = new LoginData(host, port, username, password);

            Handler uiThreadHandler = new Handler() {
                @Override
                public void handleMessage(Message message) {
                    Bundle bundle = message.getData();
                    String bundleStatus = bundle.getString(STATUS_KEY);
                    Log.d("Login", "Handling login message...");
                    if (bundleStatus != null && bundleStatus.equals("success")) {
                        listener.notifyWhenCompleted();
                        Toast.makeText(currentContext, bundle.getString(FIRST_NAME_KEY) + " " +
                                bundle.getString(LAST_NAME_KEY), Toast.LENGTH_LONG).show();
                    }
                    else {
                        Log.d("Login", "About to send login failure toast...");
                        Toast.makeText(currentContext, "Login failed", Toast.LENGTH_LONG).show();
                    }
                }
            };

            LoginTask loginTask = new LoginTask(uiThreadHandler, loginData);
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(loginTask);
        });

        //Register button pressed
        registerButton.setOnClickListener(v -> {
            Log.d("Register", "Register button pressed!");
            //Field for host number
            String host = Objects.requireNonNull(loginFields.get(Field.HOST)).getText().toString();
            //Field for port number
            String port = Objects.requireNonNull(loginFields.get(Field.PORT)).getText().toString();
            //Field for username
            String username = Objects.requireNonNull(loginFields.get(Field.USERNAME)).getText().toString();
            //Field for password
            String password = Objects.requireNonNull(loginFields.get(Field.PASSWORD)).getText().toString();
            //Field for firstName
            String firstName = Objects.requireNonNull(registerFields.get(Field.FIRST_NAME)).getText().toString();
            //Field for lastName
            String lastName = Objects.requireNonNull(registerFields.get(Field.LAST_NAME)).getText().toString();
            //Field for email
            String email = Objects.requireNonNull(registerFields.get(Field.EMAIL)).getText().toString();
            //Field for gender
            String gender = null;

            int radioButtonID = radioGroup.getCheckedRadioButtonId();
            if (radioButtonID == R.id.male) {
                gender = "m";
            }
            else if (radioButtonID == R.id.female) {
                gender = "f";
            }
            LoginData loginData = new LoginData(host, port, username, password, firstName,
                    lastName, email, gender);

            Handler uiThreadHandler = new Handler() {
                @Override
                public void handleMessage(Message message) {
                    Bundle bundle = message.getData();
                    String status = bundle.getString(STATUS_KEY);
                    Log.d("Register", "Handling register message...");
                    if (status != null && status.equals("success")) {
                        listener.notifyWhenCompleted();
                        Toast.makeText(currentContext, bundle.getString(FIRST_NAME_KEY) + " " +
                                bundle.getString(LAST_NAME_KEY), Toast.LENGTH_LONG).show();
                    }
                    else {
                        Log.d("Register", "About to send failure toast...");
                        Toast.makeText(currentContext, "Register failed", Toast.LENGTH_LONG).show();
                    }
                }
            };

            //Register
            RegisterTask registerTask = new RegisterTask(uiThreadHandler, loginData);
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.submit(registerTask);
        });

        //Return the view
        return theView;
    }

     //A class representing a login task that will be executed in a background thread.
     //This task attempts to log in a user with the provided login credentials
     //and sends a response to the given handler once the login is completed.
    private static class LoginTask implements Runnable {

        // The handler that will receive messages regarding the login status.
        private final Handler handler;

        // The data required to log in the user.
        private final LoginData loginData;


        //Constructs a new LoginTask object with the given handler and login data.


        public LoginTask(Handler handler, LoginData loginData) {
            this.handler = handler;
            this.loginData = loginData;
        }

        //Runs the login task in a background thread.

        @Override
        public void run() {

            // Create a login request with the provided credentials.
            LoginRequest request = new LoginRequest(loginData.username, loginData.password);

            // Create a new server proxy to handle the login request.
            ServerProxy proxy = new ServerProxy();

            // Attempt to log in the user with the given credentials.
            LoginResponse response = proxy.login(request, loginData.host, loginData.port);

            // Log the response received from the server.
            Log.d("Login", String.format("Login response: %s, %s",
                    response.getSuccess() ? "success" : "failure", response.getMessage()));

            // If the login was unsuccessful, send a message to the handler indicating failure.
            if (!response.getSuccess()) {
                Message message = handler.obtainMessage();
                Bundle messageBundle = new Bundle();
                messageBundle.putString(STATUS_KEY, "failure");
                message.setData(messageBundle);
                handler.sendMessage(message);
                return;
            }
            // Otherwise, cache the user's data and send a message indicating success.
            else {
                DataCache dataCache = DataCache.getInstance();
                dataCache.setUser(new User(response.getUsername(), loginData.password, loginData.email,
                        loginData.firstName, loginData.lastName, loginData.gender,
                        response.getPersonID()));
                dataCache.setAuthToken(new AuthToken(response.getAuthtoken(), response.getUsername()));
            }

            // Create a new data task to download data for the logged-in user.
            DataTask dataTask = new DataTask(handler, loginData.host, loginData.port, response.getPersonID());

            // Create a single thread executor to handle the data task.
            ExecutorService executor = Executors.newSingleThreadExecutor();

            // Submit the data task to the executor to be run.
            executor.submit(dataTask);

        }
    }



     //A background task to handle registration process.

    private static class RegisterTask implements Runnable {

        private final Handler handler;
        private final LoginData loginData;


         //Constructs a new RegisterTask object with the given parameters.

        public RegisterTask(Handler handler, LoginData loginData) {
            this.handler = handler;
            this.loginData = loginData;
        }


         //Performs the registration process in the background thread.
         //Sends a RegisterRequest to the server to register a new user.
         //If the response is successful, it sets the user's data in the DataCache and sets the auth token.
         //If the response fails, it sends a failure message to the UI thread via the handler.
         //Finally, it initiates a data retrieval task via DataTask.

        @Override
        public void run() {
            RegisterRequest request = new RegisterRequest(loginData.username, loginData.password,
                    loginData.email, loginData.firstName, loginData.lastName, loginData.gender);
            ServerProxy proxy = new ServerProxy();
            RegisterResponse response = proxy.register(request, loginData.host, loginData.port);
            Log.d("Register", String.format("Register response: %s, %s",
                    response.getSuccess()?"success":"failure", response.getMessage()));
            if (!response.getSuccess()) {
                Message message = handler.obtainMessage();
                Bundle messageBundle = new Bundle();
                messageBundle.putString(STATUS_KEY, "failure");
                message.setData(messageBundle);
                handler.sendMessage(message);
                return;
            }
            else {
                DataCache dataCache = DataCache.getInstance();
                dataCache.setUser(new User(response.getUsername(), loginData.password, loginData.email,
                        loginData.firstName, loginData.lastName, loginData.gender,
                        response.getPersonID()));
                dataCache.setAuthToken(new AuthToken(response.getAuthtoken(), response.getUsername()));
            }
            Log.d("Register", "About to send register message...");

            DataTask dataTask = new DataTask(handler, loginData.host, loginData.port, response.getPersonID());
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(dataTask);
        }
    }


     //A background task that downloads user data from the server and sends a message to the main thread
     //with the result.
    private static class DataTask implements Runnable {

        // Handler to send message to main thread
        private final Handler handler;

        // Host and port of the server
        private final String host;
        private final String port;

        // ID of the user whose data is being downloaded
        private final String personID;

        public DataTask(Handler handler, String host, String port, String personID) {
            this.handler = handler;
            this.host = host;
            this.port = port;
            this.personID = personID;
        }


         //Runs the background task to download user data from the server.
         //If successful, updates the local cache with the downloaded data and sends a success message to the main thread.
         //If unsuccessful, logs an error message and sends a failure message to the main thread.

        @Override
        public void run() {
            DataCache dataCache = DataCache.getInstance();
            ServerProxy proxy = new ServerProxy();
            PersonResponse personResponse = proxy.people(host, port);
            EventResponse eventResponse = proxy.events(host, port);
            Log.d("Data", String.format("Persons result: %s, %s",
                    personResponse.getSuccess()?"success":"failure", personResponse.getMessage()));
            Log.d("Data", String.format("Events result: %s, %s",
                    personResponse.getSuccess()?"success":"failure", eventResponse.getMessage()));
            if (personResponse.getSuccess() && eventResponse.getSuccess()) {
                dataCache.setPersons(personResponse.getData());
                dataCache.setEvents(eventResponse.getData());
            }
            else {
                Log.e("Data", "Unable to download user data from server");
            }
            sendMessage(personResponse, eventResponse);
        }

         //Sends a message to the main thread with the result of the data download.
         //If successful, includes the user's first and last name in the message.
        private void sendMessage(PersonResponse personResponse, EventResponse eventResponse) {
            Message message = Message.obtain();
            Bundle messageBundle = new Bundle();
            boolean success = personResponse.getSuccess() && eventResponse.getSuccess();
            messageBundle.putString(STATUS_KEY, success ? "success" : "failure");

            if (success) {
                DataCache dataCache = DataCache.getInstance();
                Person userPerson = dataCache.getPersonByID(personID);
                messageBundle.putString(FIRST_NAME_KEY, userPerson.getFirstName());
                messageBundle.putString(LAST_NAME_KEY, userPerson.getLastName());
            }
            message.setData(messageBundle);
            handler.sendMessage(message);
        }

    }

    private static class LoginData {
        private final String host;
        private final String port;
        private final String username;
        private final String password;
        private final String firstName;
        private final String lastName;
        private final String email;
        private final String gender;

        public LoginData(String host, String port, String username, String password) {
            this.host = host;
            this.port = port;
            this.username = username;
            this.password = password;
            this. firstName = null;
            this. lastName = null;
            this.email = null;
            this.gender = null;
        }

        public LoginData(String host, String port, String username, String password,
                         String firstName, String lastName, String email, String gender) {
            this.host = host;
            this.port = port;
            this.username = username;
            this.password = password;
            this. firstName = firstName;
            this. lastName = lastName;
            this.email = email;
            this.gender = gender;
        }
    }

    private boolean registerActive(Map<Field, Boolean> activeFields) {
        boolean valid = true;
        // Iterate through each field in the map
        for (Boolean filled : activeFields.values()) {
            // If a field isn't filled, set the valid flag to false and break out of the loop
            if (!filled) {
                valid = false;
                break;
            }
        }
        // Return whether all fields are filled or not
        return valid;
    }

    private boolean loginActive(Map<Field, Boolean> activeFields, Map<Field, EditText> loginFields) {
        boolean valid = true;
        // Iterate through each login field
        for (Field field : loginFields.keySet()) {
            // If a field isn't active or isn't filled, set the valid flag to false and break out of the loop
            if (activeFields.get(field) == null || !activeFields.get(field)) {
                valid = false;
                break;
            }
        }
        // Return whether all login fields are active and filled or not
        return valid;
    }

    private void updateButtons(Map<Field, Boolean> activeFields, Map<Field, EditText> loginFields,
                               Button loginButton, Button registerButton) {
        // Enable/disable the login button based on whether all login fields are active and filled or not
        loginButton.setEnabled(loginActive(activeFields, loginFields));
        // Enable/disable the register button based on whether all fields are filled or not
        registerButton.setEnabled(registerActive(activeFields));
    }

}