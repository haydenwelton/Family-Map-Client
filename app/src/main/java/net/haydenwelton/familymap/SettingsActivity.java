package net.haydenwelton.familymap;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceFragmentCompat;

import net.haydenwelton.familymap.data.DataCache;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Enable the up button in the action bar
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Set up the logout button
        Button logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> {
            // Invalidate the data cache and close the activity
            DataCache dataCache = DataCache.getInstance();
            dataCache.invalidate();
            finish();
        });

        // Display the settings fragment
        getSupportFragmentManager().beginTransaction().replace(R.id.settingsLayout, new SettingsFragment()).commit();
    }

    // saves state of previous fragment/activity
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle the up button in the action bar
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            setResult(RESULT_OK, intent);
            finish();
        }

        return true;
    }

    // A class that extends PreferenceFragmentCompat, a fragment that displays a hierarchy of preference items
    public static class SettingsFragment extends PreferenceFragmentCompat {

        // The method that is called when preferences are being created
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            // Sets preferences from an XML resource
            setPreferencesFromResource(R.xml.preferences, rootKey);
        }
    }
}