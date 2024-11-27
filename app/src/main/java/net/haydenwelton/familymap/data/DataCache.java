package net.haydenwelton.familymap.data;

import android.content.SharedPreferences;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

import model.AuthToken;
import model.Event;
import model.Person;
import model.User;

/**
 * The DataCache class is responsible for storing data retrieved from the server for use in the FamilyMap application.
 * This class implements the Singleton design pattern to ensure that only one instance of it can be created.
 */
public class DataCache {

    // Singleton instance
    private static DataCache instance;

    // User and auth token data
    private User user;
    private AuthToken authToken;

    // Event and person data
    private Event[] events;
    private HashMap<String, Person> personMap;
    private HashMap<String, Person> filteredPersons;
    private ArrayList<Event> filteredEvents;

    // Map of event types to their marker colors
    private final HashMap<String, Float> colorMap = new HashMap<>();
    private static final float[] MARKER_COLORS = {
            BitmapDescriptorFactory.HUE_RED,
            BitmapDescriptorFactory.HUE_YELLOW,
            BitmapDescriptorFactory.HUE_AZURE,
            BitmapDescriptorFactory.HUE_GREEN,
            BitmapDescriptorFactory.HUE_ROSE,
            BitmapDescriptorFactory.HUE_ORANGE,
            BitmapDescriptorFactory.HUE_VIOLET,
            BitmapDescriptorFactory.HUE_MAGENTA
    };

  //Returns the singleton instance of the DataCache class. If an instance does not exist, one will be created.

    public static DataCache getInstance() {
        if (instance == null) {
            instance = new DataCache();
        }
        return instance;
    }


     //Returns a filtered instance of the DataCache class, based on the user's preferences.

    public static DataCache getFilteredInstance(SharedPreferences prefs) {
        instance.filter(prefs.getBoolean("mother_side", false), prefs.getBoolean("father_side", false),
                prefs.getBoolean("female", false), prefs.getBoolean("male", false));
        return instance;
    }

    //Private constructor to prevent instantiation of the DataCache class.

    private DataCache() {}

    //Sets the current user.

    public void setUser(User user) {
        this.user = user;
    }

    //Sets the current auth token.

    public void setAuthToken(AuthToken token) {
        this.authToken = token;
    }

   //Sets the array of persons and creates a map of person IDs to Person objects.

    public void setPersons(Person[] persons) {
        this.personMap = new HashMap<>();
        this.filteredPersons = new HashMap<>();
        for (Person person : persons) {
            this.personMap.put(person.getPersonID(), person);
            this.filteredPersons.put(person.getPersonID(), person);
        }
    }

   // Sets the array of events and creates an array list of filtered events.

    public void setEvents(Event[] events) {
        this.events = events;
        this.filteredEvents = new ArrayList<>();
        filteredEvents.addAll(Arrays.asList(events));
    }


    public User getUser() {
        return this.user;
    }

    public AuthToken getAuthToken() {
        return this.authToken;
    }

    public Person[] getPersons() {
        return this.personMap.values().toArray(new Person[0]);
    }

    public Person[] getFilteredPersons() { return this.filteredPersons.values().toArray(new Person[0]); }

    public HashMap<String, Person> getPersonMap() {
        return personMap;
    }

    public Event[] getEvents() {
        return filteredEvents.toArray(new Event[0]);
    }

    public float getColor(String eventType) {
        if (!colorMap.containsKey(eventType.toLowerCase(Locale.ROOT))) {
            colorMap.put(eventType.toLowerCase(Locale.ROOT), MARKER_COLORS[colorMap.size() % MARKER_COLORS.length]);
        }
        return colorMap.get(eventType.toLowerCase(Locale.ROOT));
    }

    public void invalidate() {
        this.user = null;
        this.authToken = null;
        this.events = null;
        this.personMap = null;
        this.filteredPersons = null;
        this.filteredEvents = null;
        this.colorMap.clear();
    }

    public Person getPersonByID(String id) {
        return personMap.get(id);
    }

    public Event getEventByID(String id) {
        for (Event event : this.events) {
            if (id.equals(event.getEventID())) {
                return event;
            }
        }
        return null;
    }

   //Filters the list of persons based on the given criteria, and matches events to the filtered persons.

    private void filter(boolean motherSide, boolean fatherSide, boolean female, boolean male) {
        // Clear the current list of filtered persons.
        this.filteredPersons.clear();

        // Get the root person (the user's person) from the personMap.
        Person root = this.personMap.get(this.user.getPersonID());

        // Make sure the root person exists.
        assert root != null;

        // Get the root person's spouse from the personMap.
        Person spouse = this.personMap.get(root.getSpouseID());

        // If the "male" criteria is true, add the root person and spouse (if they exist) to the filteredPersons list if they are male.
        if (male) {
            if (root.getGender().equals("m")) {
                filteredPersons.put(root.getPersonID(), root);
            }
            if (spouse != null) {
                if (spouse.getGender().equalsIgnoreCase("m")) {
                    filteredPersons.put(spouse.getPersonID(), spouse);
                }
            }
        }

        // If the "female" criteria is true, add the root person and spouse (if they exist) to the filteredPersons list if they are female.
        if (female) {
            if (root.getGender().equals("f")) {
                filteredPersons.put(root.getPersonID(), root);
            }
            if (spouse != null) {
                if (spouse.getGender().equalsIgnoreCase("f")) {
                    filteredPersons.put(spouse.getPersonID(), spouse);
                }
            }
        }

        // Get the root person's mother and father from the personMap.
        Person mother = this.personMap.get(root.getMotherID());
        Person father = this.personMap.get(root.getFatherID());

        // If the "motherSide" criteria is true, add all ancestors on the mother's side of the family to the filteredPersons list.
        if (motherSide) {
            addLineage(mother, female, male);
        }

        // If the "fatherSide" criteria is true, add all ancestors on the father's side of the family to the filteredPersons list.
        if (fatherSide) {
            addLineage(father, female, male);
        }

        // Match events to the filtered persons.
        matchEventsToPersons();
    }




    //Recursively adds all ancestors of the given person to the `filteredPersons` map, but only if they match the desired gender filters.
    private void addLineage(Person person, boolean female, boolean male) {
        // If the person is null, there's nothing to add, so return.
        if (person == null) return;

        // Recursively add the person's mother and father to the map.
        addLineage(personMap.get(person.getMotherID()), female, male);
        addLineage(personMap.get(person.getFatherID()), female, male);

        // Add the person to the map if they match the desired gender filters.
        if (female) {
            if (person.getGender().equals("f")) {
                this.filteredPersons.put(person.getPersonID(), person);
            }
        }
        if (male) {
            if (person.getGender().equals("m")) {
                this.filteredPersons.put(person.getPersonID(), person);
            }
        }
    }

    //Clears the list of filtered events, and then iterates through all the events in the global list events
    private void matchEventsToPersons() {
        this.filteredEvents.clear();
        for (Event event : this.events) {
            if (filteredPersons.containsKey(event.getPersonID())) {
                this.filteredEvents.add(event);
            }
        }
    }
}