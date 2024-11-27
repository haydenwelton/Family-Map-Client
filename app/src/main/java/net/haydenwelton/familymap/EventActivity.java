package net.haydenwelton.familymap;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

// Define the EventActivity class
public class EventActivity extends AppCompatActivity {
    private static final String EVENT_KEY = "eventID"; // Define the key for the event ID in the intent

    // Override the onCreate method to set up the activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_activity); // Set the layout for the activity
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); // Set up the toolbar

        ActionBar supportActionBar = getSupportActionBar();
        assert supportActionBar != null;
        supportActionBar.setDisplayHomeAsUpEnabled(true); // Enable the "home" button on the toolbar

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (savedInstanceState == null) {
            Fragment mapsFragment = new MapsFragment(); // Create a new MapsFragment
            Intent intent = getIntent(); // Get the intent that started the activity
            Bundle bundle = new Bundle();
            bundle.putString(EVENT_KEY, intent.getStringExtra(EVENT_KEY)); // Retrieve the event ID from the intent and add it to the bundle
            mapsFragment.setArguments(bundle); // Pass the bundle as an argument to the MapsFragment

            fragmentManager.beginTransaction()
                    .replace(R.id.root_layout, mapsFragment, "map_fragment") // Add the MapsFragment to the fragment manager
                    .commit();
        }
    }

    // Override the onOptionsItemSelected method to handle toolbar item clicks
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home: // If the home button is pressed
                NavUtils.navigateUpFromSameTask(this); // Navigate up to the parent activity
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
