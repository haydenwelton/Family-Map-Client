package net.haydenwelton.familymap;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import net.haydenwelton.familymap.data.DataCache;

import java.util.Map;

public class MainActivity extends AppCompatActivity implements LoginFragment.Listener {
    // Define shared preferences to store user preferences
    private SharedPreferences defaultPrefs;
    private SharedPreferences temporaryPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set default shared preferences and default values
        defaultPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // Create fragment manager and display login or map fragment based on login status
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (savedInstanceState == null) {
            if (!isLoggedIn()) {
                Fragment loginFragment = createLoginFragment();
                fragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, loginFragment, "main_fragment")
                        .commit();
            } else {
                Fragment fragment = new MapsFragment();
                fragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, fragment, "main_fragment")
                        .commit();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Set up temporary preferences to compare with default preferences
        if (temporaryPrefs == null) {
            temporaryPrefs = getSharedPreferences("tempPrefs", MODE_PRIVATE);
        }
        // Display login or map fragment based on login status and preference changes
        if (!isLoggedIn()) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frameLayout, createLoginFragment())
                    .commit();
        } else if (!preferencesAreEqual(defaultPrefs, temporaryPrefs)) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frameLayout, new MapsFragment())
                    .commit();
        }
    }

    // Compare default and temporary preferences to see if any changes were made
    private boolean preferencesAreEqual(SharedPreferences defaultPreferences, SharedPreferences temporaryPreferences) {
        Map<String, ?> defaultPrefM = defaultPreferences.getAll();
        Map<String, ?> temporaryPrefM = temporaryPreferences.getAll();
        for (Map.Entry<String, ?> entryIter : defaultPrefM.entrySet()) {
            if (!entryIter.getValue().equals(temporaryPrefM.get(entryIter.getKey()))) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Save preferences to temporary preferences
        savePreferencesToTemporary();
    }

    // Save user preferences to temporary preferences
    private void savePreferencesToTemporary() {
        SharedPreferences.Editor editor = temporaryPrefs.edit();
        for (Map.Entry<String, ?> entry : defaultPrefs.getAll().entrySet()) {
            Object entryValue = entry.getValue();
            String key = entry.getKey();
            if (entryValue instanceof Boolean) {
                editor.putBoolean(key, (Boolean) entryValue);
            } else if (entryValue instanceof Float) {
                editor.putFloat(key, (Float) entryValue);
            } else if (entryValue instanceof Integer) {
                editor.putInt(key, (Integer) entryValue);
            } else if (entryValue instanceof Long) {
                editor.putLong(key, (Long) entryValue);
            } else if (entryValue instanceof String) {
                editor.putString(key, (String) entryValue);
            }
        }
        editor.apply();
    }

    // Check if the user is logged in
    private boolean isLoggedIn() {
        DataCache dataCache = DataCache.getInstance();
        return dataCache.getAuthToken() != null;
    }

    // Creates a new instance of the LoginFragment and registers this activity
    // as a listener to be notified when the login process has completed.

    private Fragment createLoginFragment() {
        LoginFragment loginFragment = new LoginFragment();
        loginFragment.registerListener(this);
        return loginFragment;
    }

    // Implementation of the LoginFragment.
    // Listener interface that is called when the login process has completed successfully.
    // This method replaces the LoginFragment with a new instance of theMapsFragment.

    @Override
    public void notifyWhenCompleted() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = new MapsFragment();

        fragmentManager.beginTransaction()
                .replace(R.id.frameLayout, fragment)
                .commit();
    }
}