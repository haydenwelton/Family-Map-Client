package net.haydenwelton.familymap;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import android.widget.TextView;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import net.haydenwelton.familymap.adapter.PersonExpandableListAdapter;
import net.haydenwelton.familymap.data.DataCache;
import net.haydenwelton.familymap.data.DataProcessor;
import net.haydenwelton.familymap.model.FamilyMember;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import model.Event;
import model.Person;

public class PersonActivity extends AppCompatActivity {

    private static final String PERSON_KEY = "personID";
    private static final String EVENT_KEY = "eventID";
    PersonExpandableListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the layout for this activity
        setContentView(R.layout.person_activity);

        // Set the toolbar as the activity's action bar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Enable the up button in the action bar to navigate up to parent activity
        ActionBar supportActionBar = getSupportActionBar();
        assert supportActionBar != null;
        supportActionBar.setDisplayHomeAsUpEnabled(true);

        // Retrieve the personID from the intent that started this activity
        Intent intent = getIntent();
        String personID = intent.getStringExtra(PERSON_KEY);

        // Retrieve the person object with the given personID from the data cache
        DataCache dataCache = DataCache.getInstance();
        Person person = dataCache.getPersonByID(personID);

        // Set the person's basic information (first name, last name, and gender) on the activity layout
        setPersonInfo(person);

        // Create a single-threaded executor to retrieve the list of events and family members associated with the person
        ExecutorService executor = Executors.newSingleThreadExecutor();

        // Create a Callable task to retrieve the list of events associated with the person and submit it to the executor
        Callable<List<Event>> eventCall = new EventTask(personID);
        Future<List<Event>> eventFuture = executor.submit(eventCall);

        // Create a Callable task to retrieve the list of family members associated with the person and submit it to the executor
        Callable<List<FamilyMember>> familyCall = new FamilyTask(personID);
        Future<List<FamilyMember>> familyFuture = executor.submit(familyCall);

        try {
            // Once both the event and family member lists are retrieved, create the ExpandableListView
            createExpList(eventFuture.get(), familyFuture.get(), person);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Private static class that retrieves the list of events associated with a person in a separate thread
    private static class EventTask implements Callable<List<Event>> {
        private final String personID;

        // Constructor
        public EventTask(String personID) {
            this.personID = personID;
        }

        // Call method for executing in a thread
        @Override
        public List<Event> call() {
            // Get an instance of DataCache class
            DataCache dataCache = DataCache.getInstance();

            // Create an ArrayList to store person's events
            ArrayList<Event> personEvents = new ArrayList<>();

            // Iterate through all events
            for (Event event : dataCache.getEvents()) {
                // Check if the event is for the current person
                if (event.getPersonID().equals(personID)) {
                    personEvents.add(event);
                }
            }

            // Sort the personEvents by chronological order
            personEvents.sort(new Event.EventComparator());

            // Return the list of person's events
            return personEvents;
        }
    }

    private static class FamilyTask implements Callable<List<FamilyMember>> {
        private final String personID;

        // Constructor
        public FamilyTask(String personID) {
            this.personID = personID;
        }

        // Call method for executing in a thread
        @Override
        public List<FamilyMember> call() {
            // Get an instance of DataCache class
            DataCache dataCache = DataCache.getInstance();

            // Get the root person based on the personID
            Person rootPerson = dataCache.getPersonByID(personID);

            // Find the family members of the root person
            List<FamilyMember> familyMembers = DataProcessor.findFamily(rootPerson);

            // Return the list of family members
            return familyMembers;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            // Handle the up button in the action bar
            case R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    // Method to set the person's information on the UI
    private void setPersonInfo(Person person) {
        // Find the TextViews for the first name, last name and gender
        TextView firstNameText = findViewById(R.id.firstNamePerson);
        TextView lastNameText = findViewById(R.id.lastNamePerson);
        TextView genderText = findViewById(R.id.genderPerson);

        // Set the text of the TextViews to the corresponding values
        firstNameText.setText(person.getFirstName());
        lastNameText.setText(person.getLastName());
        genderText.setText(person.getGender().equals("f") ? R.string.female : R.string.male);
    }

    // Method to create the expandable list view
    private void createExpList(List<Event> events, List<FamilyMember> family, Person rootPerson) {
        // Get a reference to the expandable list view in the layout
        ExpandableListView listView = findViewById(R.id.expList);

        // Define the titles of the two sections of the list view
        String[] titles = { "LIFE EVENTS", "FAMILY" };

        // Create an instance of the custom expandable list adapter, passing in the activity, titles,
        // events, family members, and root person's name to use as the group headers
        adapter = new PersonExpandableListAdapter(this, titles, events,
                family, rootPerson.getFirstName(), rootPerson.getLastName());

        // Set the adapter on the list view
        listView.setAdapter(adapter);

        // Expand both sections of the list view by default
        listView.expandGroup(0);
        listView.expandGroup(1);

        // Set a listener to handle clicks on child items in the list view
        listView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            // If the clicked item is in the "Life Events" section, start a new EventActivity to display
            // details of the clicked event
            if (titles[groupPosition].equals("LIFE EVENTS")) {
                Intent newEventIntent = new Intent(this, EventActivity.class);
                newEventIntent.putExtra(EVENT_KEY, events.get(childPosition).getEventID());
                startActivity(newEventIntent);
            }
            // If the clicked item is in the "Family" section, start a new PersonActivity to display
            // details of the clicked family member
            else {
                Intent newPersonIntent = new Intent(this, PersonActivity.class);
                newPersonIntent.putExtra(PERSON_KEY,
                        family.get(childPosition).getPerson().getPersonID());
                startActivity(newPersonIntent);
            }
            return false;
        });
    }
}