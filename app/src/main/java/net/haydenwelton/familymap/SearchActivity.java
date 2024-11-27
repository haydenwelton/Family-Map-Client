package net.haydenwelton.familymap;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.haydenwelton.familymap.adapter.SearchRecyclerAdapter;
import net.haydenwelton.familymap.data.DataProcessor;
import net.haydenwelton.familymap.model.SearchResult;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SearchActivity extends AppCompatActivity {

    private ArrayList<SearchResult> searchResults; // list of search results
    private ArrayList<SearchTask> searchTasks; // list of active search tasks

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);

        // initialize lists
        searchResults = new ArrayList<>();
        searchTasks = new ArrayList<>();

        // set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // set up back button
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        // set up RecyclerView for displaying search results
        RecyclerView recyclerView = findViewById(R.id.searchList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // set up loading panel for displaying search progress
        RelativeLayout loadingPanel = findViewById(R.id.loadingPanel);
        loadingPanel.setVisibility(View.GONE);

        // set up adapter for RecyclerView
        SearchRecyclerAdapter recyclerAdapter = new SearchRecyclerAdapter(searchResults);
        recyclerView.setAdapter(recyclerAdapter);

        // set up search box with listener for search input
        EditText searchText = findViewById(R.id.searchText);
        searchText.addTextChangedListener(new TextWatcher() {
            // when search text changes, start new search task
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ExecutorService executorService = Executors.newSingleThreadExecutor();
                SearchTask searchTask = new SearchTask(new SearchHandler(recyclerAdapter, loadingPanel),
                        s.toString().trim());
                searchTasks.add(searchTask);
                loadingPanel.setVisibility(View.VISIBLE);
                executorService.submit(searchTask);
            }

            // when search text is being changed, interrupt any running search tasks
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                for (SearchTask searchTask : searchTasks) {
                    searchTask.interrupt();
                }
            }

            // do nothing after search text is changed
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // set up clear button for search box
        ImageButton clearButton = findViewById(R.id.clearText);
        clearButton.setOnClickListener(v -> searchText.setText(""));
    }

    // handler for messages from search tasks, updates UI with new search results
    private static class SearchHandler extends Handler {
        private final SearchRecyclerAdapter recyclerAdapter; // adapter for search results RecyclerView
        private final RelativeLayout loadingPanel; // panel for displaying search progress

        // constructor takes adapter and loading panel as arguments
        private SearchHandler(SearchRecyclerAdapter recyclerAdapter, RelativeLayout loadingPanel) {
            this.recyclerAdapter = recyclerAdapter;
            this.loadingPanel = loadingPanel;
        }

        // update UI with new search results
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            loadingPanel.setVisibility(View.GONE); //Sets the visibility of the loadingPanel to GONE, hiding it from view.
            recyclerAdapter.notifyDataSetChanged(); //Notifies the SearchRecyclerAdapter that the dataset has changed, prompting it to update its views.

        }
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // If the user presses the home button, navigate up to the parent activity
            case R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private class SearchTask implements Runnable {

        // Flag to indicate whether the task is running or not
        volatile boolean running = true;

        // Handler to communicate the search results to the UI thread
        private final SearchHandler searchHandler;

        // The search query string
        private final String query;

        public SearchTask(SearchHandler searchHandler, String query) {
            this.searchHandler = searchHandler;
            this.query = query;
        }

        // Interrupt the task if it is currently running
        protected void interrupt() {
            running = false;
        }

        @Override
        public void run() {

            // Clear the search results list
            searchResults.clear();

            // If the task is no longer running, return
            if (!running) {
                return;
            }

            // Search for persons that match the query
            DataProcessor.searchPersons(searchResults, query);

            // If the task is no longer running, clear the results and return
            if (!running) {
                searchResults.clear();
                return;
            }

            // Search for events that match the query
            DataProcessor.searchEvents(searchResults, query);

            // If the task is no longer running, clear the results and return
            if (!running) {
                searchResults.clear();
                return;
            }

            // Send a message to the search handler to update the UI with the results
            sendMessage();
        }

        // Send a message to the search handler to update the UI with the results
        private void sendMessage() {
            Message message = Message.obtain();
            searchHandler.sendMessage(message);
        }

    }
}