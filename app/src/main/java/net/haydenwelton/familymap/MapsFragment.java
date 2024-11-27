package net.haydenwelton.familymap;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import net.haydenwelton.familymap.data.DataCache;
import net.haydenwelton.familymap.data.DataProcessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import model.Event;
import model.Person;

public class MapsFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback {
    private GoogleMap map;

    private static final String EVENT_KEY = "eventID";
    private static final String PERSON_KEY = "personID";
    private static final String SUCCESS_KEY = "success";
    private static final String SPOUSE_KEY = "spouse";
    private static final String FAMILY_KEY = "family";
    private static final String LIFE_KEY = "life";
    private Person[] filteredPersons = null;
    private Event[] filteredEvents = null;
    ArrayList<LatLng> spouseLine = null;
    ArrayList<ArrayList<LatLng>> lifeLines = null;
    ArrayList<FamilyLine> familyLines = null;
    ArrayList<Polyline> polylines = new ArrayList<>();
    protected HashMap<Marker, Event> eventsOnMap = new HashMap<>();

    private Event selectedEvent = null;
    private Person selectedPerson = null;

    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        // Set a callback to be invoked when the map has finished rendering
        map.setOnMapLoadedCallback(this);

        // Create a FilterHandler object and pass it the current fragment and GoogleMap object
        FilterHandler filterHandler = new FilterHandler(this, googleMap);

        // Create a FilterTask object and pass it the filter handler and context
        FilterTask filterTask = new FilterTask(filterHandler, getContext());

        // Create a single-thread executor and submit the filter task to it
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(filterTask);

        // Set a marker click listener on the map
        googleMap.setOnMarkerClickListener(marker -> {
            // Get the event associated with the clicked marker
            selectedEvent = eventsOnMap.get(marker);
            assert selectedEvent != null;

            // Get the person associated with the selected event
            DataCache dataCache = DataCache.getInstance();
            selectedPerson = dataCache.getPersonByID(selectedEvent.getPersonID());
            assert selectedPerson != null;

            // Update the info bar with the selected event and person
            setInfoBar();

            // Create a LineHandler object and pass it the current fragment and GoogleMap object
            LineHandler lineHandler = new LineHandler(this, googleMap);

            // Create a LineTask object and pass it the line handler and context
            LineTask lineTask = new LineTask(lineHandler, getContext());

            // Submit the line task to the executor
            executor.submit(lineTask);

            return false;
        });

        // Get the bundle of arguments passed to the fragment
        Bundle bundle = getArguments();
        if (bundle != null) {
            // If the bundle is not null, get the selected event and person from the DataCache
            DataCache dataCache = DataCache.getInstance();
            selectedEvent = dataCache.getEventByID(bundle.getString(EVENT_KEY));
            selectedPerson = dataCache.getPersonByID(selectedEvent.getPersonID());

            // Update the info bar with the selected event and person
            setInfoBar();
        }

        // If there is a selected event, animate the camera to its location
        if (selectedEvent != null) {
            LatLng location = new LatLng(selectedEvent.getLatitude(), selectedEvent.getLongitude());
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 5f));

            // Create a LineHandler object and pass it the current fragment and GoogleMap object
            LineHandler lineHandler = new LineHandler(this, googleMap);

            // Create a LineTask object and pass it the line handler and context
            LineTask lineTask = new LineTask(lineHandler, getContext());

            // Submit the line task to the executor
            executor.submit(lineTask);
        }
    }


    // Called when the state of the fragment needs to be saved
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        // Save the IDs of the currently selected event and person (if any) to the bundle
        outState.putString(EVENT_KEY, selectedEvent == null ? null : selectedEvent.getEventID());
        outState.putString(PERSON_KEY, selectedPerson == null ? null : selectedPerson.getPersonID());
        super.onSaveInstanceState(outState);
    }

    // Called when the activity has been created
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    // Called when the fragment view needs to be created
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the maps_fragment layout
        View view = inflater.inflate(R.layout.maps_fragment, container, false);

        // Set the default person image
        ImageView personImage = view.findViewById(R.id.personImage);
        personImage.setImageResource(R.drawable.default_person);

        // Set the default info text
        TextView infoText = view.findViewById(R.id.infoText);
        infoText.setText(R.string.default_info);
        return view;
    }

    // Called when the view has been created
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Find the map fragment and set the onMapReady callback
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Enable options menu for the fragment
        setHasOptionsMenu(true);
    }

    // Called when the options menu needs to be created
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        // If the parent activity is the MainActivity, inflate the main_map menu
        if (requireActivity().getClass() == MainActivity.class) {
            inflater.inflate(R.menu.main_map, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    // This method handles options menu item selection
    // If searchButton is clicked, it starts the SearchActivity
    // If settingsButton is clicked, it starts the SettingsActivity
    // If an unrecognized item is clicked, it returns super.onOptionsItemSelected(item)
    @Override

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.searchButton:
                Intent searchIntent = new Intent(getActivity(), SearchActivity.class);
                startActivity(searchIntent);
                return true;
            case R.id.settingsButton:
                Intent settingsIntent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // This method sets the info bar based on the selected person and event
    // It retrieves the TextView and ImageView from the view and updates them accordingly
    // It sets the LinearLayout to start the PersonActivity when clicked
    private void setInfoBar() {
        View view = getView();
        assert view != null;
        TextView infoText = view.findViewById(R.id.infoText);
        ImageView personImage = view.findViewById(R.id.personImage);

        personImage.setImageResource(selectedPerson.getGender().equals("f") ? R.drawable.female :
                R.drawable.male);
        infoText.setText(getResources().getString(R.string.info_bar_text,
                getResources().getString(R.string.person_name, selectedPerson.getFirstName(),
                        selectedPerson.getLastName()), getResources().getString(R.string.event_info,
                        selectedEvent.getEventType().toUpperCase(Locale.ROOT), selectedEvent.getCity(),
                        selectedEvent.getCountry(), selectedEvent.getYear())));

        LinearLayout infoBar = view.findViewById(R.id.infoBar);
        infoBar.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), PersonActivity.class);
            intent.putExtra(PERSON_KEY, selectedPerson.getPersonID());
            startActivity(intent);
        });
    }


    // No implementation needed here
    @Override
    public void onMapLoaded() {

    }

    // A private static inner class that extends Handler which is used to handle messages
    // sent from a background thread to the main thread in order to update the UI of the MapsFragment.
    // It is responsible for placing the markers on the GoogleMap and adding them to the eventsOnMap HashMap.
    private static class FilterHandler extends Handler {
        private final MapsFragment fragment;
        private final GoogleMap googleMap;

        private FilterHandler(MapsFragment fragment, GoogleMap googleMap) {
            this.fragment = fragment;
            this.googleMap = googleMap;
        }

        // This method is called when a message is received by the Handler
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            // Logs that the event locations are being placed
            Log.d("Maps", "Placing event locations...");
            // Gets the DataCache singleton instance
            DataCache dataCache = DataCache.getInstance();
            // Clears the eventsOnMap HashMap
            fragment.eventsOnMap.clear();
            // Gets the Bundle from the received Message object
            Bundle bundle = msg.getData();
            // Gets the boolean value for success from the Bundle
            boolean success = bundle.getBoolean(SUCCESS_KEY);
            if (success) {
                // If success is true, adds a marker for each event to the GoogleMap
                for (Event event : fragment.filteredEvents) {
                    Log.d("Maps", String.format("Adding %s event", event.getEventType()));
                    // Adds a marker to the GoogleMap and stores the marker and its corresponding event in the eventsOnMap HashMap
                    fragment.eventsOnMap.put(googleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(event.getLatitude(), event.getLongitude()))
                            .icon(BitmapDescriptorFactory.defaultMarker(dataCache.getColor(event.getEventType())))), event);
                }
            } else {
                // If success is false, displays a toast message indicating that the markers could not be added to the map
                Toast.makeText(fragment.getContext(), "Failed to add markers to map", Toast.LENGTH_LONG).show();
            }
        }
    }

    // This is the FilterTask class which implements the Runnable interface
    private class FilterTask implements Runnable {
        // Instance variables for the FilterHandler and Context
        private final FilterHandler handler;
        private final Context context;

        // Constructor for the FilterTask
        protected FilterTask(FilterHandler handler, Context context) {
            // Assign the handler and context variables
            this.handler = handler;
            this.context = context;
        }

        // The run method is called when this thread is started
        @Override
        public void run() {
            // Get the SharedPreferences instance for the default preferences file
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            // Get the DataCache instance with the filtered data
            DataCache dataCache = DataCache.getFilteredInstance(prefs);
            // Get the filtered events and persons from the DataCache instance
            filteredEvents = dataCache.getEvents();
            filteredPersons = dataCache.getFilteredPersons();
            // Call the sendMessage method to send the results to the UI thread
            sendMessage();
        }

        // Helper method to send the results of the filter operation to the UI thread
        private void sendMessage() {
            // Create a new Message instance
            Message message = Message.obtain();
            // Create a new Bundle to hold the message data
            Bundle messageBundle = new Bundle();
            // Determine whether the filter operation was successful or not
            boolean success = filteredEvents != null;
            // Add the success value to the message data bundle
            messageBundle.putBoolean(SUCCESS_KEY, success);
            // Set the data bundle for the message
            message.setData(messageBundle);
            // Send the message to the FilterHandler to be handled by the UI thread
            handler.sendMessage(message);
        }
    }

    // Define a static class called LineHandler that extends Handler
    private static class LineHandler extends Handler {

        // Define final instance variables for MapsFragment and GoogleMap
        private final MapsFragment fragment;
        private final GoogleMap googleMap;

        // Create a constructor that takes in MapsFragment and GoogleMap and assigns them to the instance variables
        private LineHandler(MapsFragment fragment, GoogleMap googleMap) {
            this.fragment = fragment;
            this.googleMap = googleMap;
        }

        // Override the handleMessage method
        @Override
        public void handleMessage(@NonNull Message msg) {

            // Log a debug message that marker lines are being placed
            Log.d("Maps", "Placing marker lines...");

            // Clear existing polylines
            clearPolylines();

            // Get the data bundle from the message
            Bundle bundle = msg.getData();

            // Define the base weight for the polylines
            float baseWeight = 20f;

            // Check if spouse lines, life lines, and family lines are valid
            boolean spouseLineValid = bundle.getBoolean(SPOUSE_KEY);
            boolean lifeLinesValid = bundle.getBoolean(LIFE_KEY);
            boolean familyLinesValid = bundle.getBoolean(FAMILY_KEY);

            // If spouse lines are valid, add them to the map
            if (spouseLineValid) {
                this.fragment.polylines.add(
                        googleMap.addPolyline(new PolylineOptions()
                                .color(Color.BLUE)
                                .width(baseWeight)
                                .addAll(fragment.spouseLine)));
            }

            // If life lines are valid, add them to the map
            if (lifeLinesValid) {
                for (ArrayList<LatLng> points : fragment.lifeLines) {
                    this.fragment.polylines.add(
                            googleMap.addPolyline(new PolylineOptions()
                                    .color(Color.GREEN)
                                    .width(baseWeight)
                                    .addAll(points)));
                }
            }

            // If family lines are valid, add them to the map
            if (familyLinesValid) {
                for (FamilyLine familyLine : fragment.familyLines) {
                    // Determine the weight of the polyline based on the number of generations
                    float weight = baseWeight - familyLine.getGeneration() * 10f;
                    if (weight < 1f) weight = 1f;
                    this.fragment.polylines.add(
                            googleMap.addPolyline(new PolylineOptions()
                                    .color(Color.RED)
                                    .width(weight)
                                    .addAll(familyLine.getPoints())));
                }
            }
        }

        // Clear the existing polylines from the map
        private void clearPolylines() {
            for (Polyline polyline : this.fragment.polylines) {
                polyline.remove();
            }
            this.fragment.polylines.clear();
        }
    }

    private class LineTask implements Runnable {
        private final LineHandler handler;
        private final Context context;

        // Constructor that initializes the handler and context variables
        protected LineTask(LineHandler handler, Context context) {
            this.handler = handler;
            this.context = context;
        }

        // Overrides the run method to perform the task on a separate thread
        @Override
        public void run() {
            // Get the shared preferences for the app
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

            // Initialize variables for the spouse, life, and family lines
            spouseLine = null;
            lifeLines = null;
            familyLines = null;

            // Check if the spouse lines preference is set to true, and if so, find the spouse line
            if (prefs.getBoolean("spouse_lines", false)) {
                spouseLine = findSpouseLine();
            }

            // Check if the life lines preference is set to true, and if so, find the life lines
            if (prefs.getBoolean("life_lines", false)) {
                lifeLines = findLifeLines();
            }

            // Check if the family lines preference is set to true, and if so, find the family lines
            if (prefs.getBoolean("family_lines", false)) {
                familyLines = findFamilyLines();
            }

            // Send the message with the spouse, life, and family lines to the LineHandler
            sendMessage();
        }

        // Sends a message to the LineHandler with the spouse, life, and family lines
        private void sendMessage() {
            Message message = Message.obtain();
            Bundle messageBundle = new Bundle();
            messageBundle.putBoolean(SPOUSE_KEY, spouseLine != null);
            messageBundle.putBoolean(LIFE_KEY, lifeLines != null);
            messageBundle.putBoolean(FAMILY_KEY, familyLines != null);
            message.setData(messageBundle);
            handler.sendMessage(message);
        }

        private ArrayList<LatLng> findSpouseLine() {
            // Find the earliest birth event or earliest event if no birth event exists for the selected person's spouse
            Event cachedEvent = null;
            for (Event event : filteredEvents) {
                if (event.getPersonID().equals(selectedPerson.getSpouseID())) {
                    if (event.getEventType().equalsIgnoreCase("birth")) {
                        // Add the two events to an ArrayList and return it
                        ArrayList<LatLng> points = new ArrayList<>();
                        points.add(new LatLng(selectedEvent.getLatitude(), selectedEvent.getLongitude()));
                        points.add(new LatLng(event.getLatitude(), event.getLongitude()));
                        return points;
                    }
                    else {
                        // Cache the earliest event if there is no birth event
                        if (cachedEvent == null || event.compareTo(cachedEvent) < 0) {
                            cachedEvent = event;
                        }
                    }
                }
            }
            // If no events were found for the spouse, return null
            if (cachedEvent == null) return null;
            else {
                // Add the two events to an ArrayList and return it
                ArrayList<LatLng> points = new ArrayList<>();
                points.add(new LatLng(selectedEvent.getLatitude(), selectedEvent.getLongitude()));
                points.add(new LatLng(cachedEvent.getLatitude(), cachedEvent.getLongitude()));
                return points;
            }
        }

        private ArrayList<ArrayList<LatLng>> findLifeLines() {
            // Create a list of events for the selected person
            ArrayList<Event> storyEvents = new ArrayList<>();
            ArrayList<ArrayList<LatLng>> lifeLines = null;
            for (Event event : filteredEvents) {
                if (event.getPersonID().equals(selectedPerson.getPersonID())) {
                    storyEvents.add(event);
                }
            }
            // Create an ArrayList of ArrayLists, where each inner ArrayList contains the LatLng points for a line segment
            for (int i = 0; i < storyEvents.size() - 1; i++) {
                Event event1 = storyEvents.get(i);
                Event event2 = storyEvents.get(i + 1);
                if (lifeLines == null) {
                    lifeLines = new ArrayList<>();
                }
                ArrayList<LatLng> points = new ArrayList<>();
                points.add(new LatLng(event1.getLatitude(), event1.getLongitude()));
                points.add(new LatLng(event2.getLatitude(), event2.getLongitude()));
                lifeLines.add(points);
            }
            return lifeLines;
        }

        private ArrayList<FamilyLine> findFamilyLines() {
            // Create a map of Person objects keyed by person ID
            HashMap<String, Person> personMap = DataProcessor.generatePersonMap(filteredPersons);
            // Create a map of Event objects keyed by person ID and sorted by year
            HashMap<String, TreeSet<Event>> eventsByPerson = DataProcessor.generateSortedEventMap(filteredEvents, personMap);
            // Create an ArrayList of FamilyLine objects, where each object represents a lineage line
            ArrayList<FamilyLine> familyLines = new ArrayList<>();

            // Recursive function to add all lineage lines for a given person
            addLineage(familyLines, personMap, eventsByPerson, selectedPerson, 0);
            // Return null if no lineage lines were found
            if (familyLines.isEmpty()) return null;
            return familyLines;
        }

        private void addLineage(ArrayList<FamilyLine> familyLines,
                                HashMap<String, Person> personMap,
                                HashMap<String, TreeSet<Event>> eventsByPerson,
                                Person person,
                                int generation) {
            // Base case: return if the person is null
            if (person == null) return;
            // Recurse on the person's mother and father
            Person mother = personMap.get(person.getMotherID());
            Person father = personMap.get(person.getFatherID());
            addLineage(familyLines, personMap, eventsByPerson, mother, generation + 1);
            addLineage(familyLines, personMap, eventsByPerson, father, generation + 1);
            ArrayList<LatLng> points = new ArrayList<>();
            Event personEvent;

            // Get selected event, not first event of selectedPerson
            if (person == selectedPerson) {
                personEvent = selectedEvent;
            }
            else {
                personEvent = getFirstEvent(person.getPersonID(), eventsByPerson);
            }
            if (mother != null) {
                Event firstEventMother = getFirstEvent(mother.getPersonID(), eventsByPerson);
                points.add(new LatLng(personEvent.getLatitude(), personEvent.getLongitude()));
                points.add(new LatLng(firstEventMother.getLatitude(), firstEventMother.getLongitude()));
                familyLines.add(new FamilyLine(generation, points));
            }
            if (father != null) {
                Event firstEventFather = getFirstEvent(father.getPersonID(), eventsByPerson);
                points.add(new LatLng(personEvent.getLatitude(), personEvent.getLongitude()));
                points.add(new LatLng(firstEventFather.getLatitude(), firstEventFather.getLongitude()));
                familyLines.add(new FamilyLine(generation, points));
            }
        }

        private Event getFirstEvent(String personID, HashMap<String, TreeSet<Event>> eventsByPerson) {
            Set<Event> orderedEvents = eventsByPerson.get(personID);
            assert orderedEvents != null;
            return orderedEvents.iterator().next();
        }
    }

    protected static class FamilyLine {
        // This field represents the generation of the line.
        private final int generation;

        // This field is an ArrayList of LatLng points representing the location of the line.
        private final ArrayList<LatLng> points;

        // ArrayList of LatLng points representing the location of the line, and initializes the fields accordingly.
        public FamilyLine(int generation, ArrayList<LatLng> points) {
            this.generation = generation;
            this.points = points;
        }

        // This method returns the generation of the line.
        public int getGeneration() {
            return generation;
        }

        // This method returns the ArrayList of LatLng points representing the location of the line.
        public ArrayList<LatLng> getPoints() {
            return points;
        }
    }
}