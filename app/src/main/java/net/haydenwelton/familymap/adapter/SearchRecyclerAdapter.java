package net.haydenwelton.familymap.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.haydenwelton.familymap.EventActivity;
import net.haydenwelton.familymap.PersonActivity;
import net.haydenwelton.familymap.R;
import net.haydenwelton.familymap.model.SearchResult;

import java.util.ArrayList;

public class SearchRecyclerAdapter extends RecyclerView.Adapter<SearchRecyclerAdapter.ViewHolder> {

    // Constants for intent extras
    private static final String PERSON_KEY = "personID";
    private static final String EVENT_KEY = "eventID";

    // The list of search results to display
    private final ArrayList<SearchResult> searchResults;

    // Constructor that sets the search results
    public SearchRecyclerAdapter(ArrayList<SearchResult> searchResults) {
        this.searchResults = searchResults;
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        // Views for each search result item in the RecyclerView
        private final LinearLayout searchItem;
        private final ImageView imageView;
        private final TextView mainInfo;
        private final TextView subInfo;

        // Constructor that sets the views
        public ViewHolder(View view) {
            super(view);
            searchItem = view.findViewById(R.id.searchItem);
            imageView = view.findViewById(R.id.searchImage);
            mainInfo = view.findViewById(R.id.mainInfo);
            subInfo = view.findViewById(R.id.subInfo);
        }

        // Getter methods for the views
        public LinearLayout getSearchItem() {
            return searchItem;
        }

        public ImageView getImageView() {
            return imageView;
        }

        public TextView getMainInfo() {
            return mainInfo;
        }

        public TextView getSubInfo() {
            return subInfo;
        }
    }

    // Create new views
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Inflate the view for a search result item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_search, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get views for the search result item at this position
        ImageView imageView = viewHolder.getImageView();
        TextView mainInfo = viewHolder.getMainInfo();
        TextView subInfo = viewHolder.getSubInfo();

        // Set the contents of the views based on the search result at this position
        SearchResult result = this.searchResults.get(position);
        assert result != null;
        if (result.getType() == SearchResult.Type.PERSON) {
            // If the search result is a person, set the image to the gender-specific icon
            if (result.getGender().equals("f")) {
                imageView.setImageResource(R.drawable.female);
            }
            else {
                imageView.setImageResource(R.drawable.male);
            }
            // Set the main info and clear the sub info
            mainInfo.setText(result.getMainInfo());
            subInfo.setText("");
        }
        else {
            // If the search result is an even set the image to the location icon
            imageView.setImageResource(R.drawable.location);
            // Set the main info and sub info
            mainInfo.setText(result.getMainInfo());
            subInfo.setText(result.getSubInfo());
        }

        // Get the search item view from the ViewHolder and set an onClickListener
        LinearLayout searchItem = viewHolder.getSearchItem();
        searchItem.setOnClickListener(v -> {
        // Create an intent to start either the PersonActivity or EventActivity depending on the SearchResult type
            Intent intent;
            if (result.getType() == SearchResult.Type.PERSON) {
                intent = new Intent(searchItem.getContext(), PersonActivity.class);
                intent.putExtra(PERSON_KEY, result.getId());
            }
            else {
                intent = new Intent(searchItem.getContext(), EventActivity.class);
                intent.putExtra(EVENT_KEY, result.getId());
            }
            // Start the activity using the context of the searchItem
            searchItem.getContext().startActivity(intent);
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return searchResults.size();
    }


}