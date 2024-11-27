package net.haydenwelton.familymap.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.haydenwelton.familymap.R;
import net.haydenwelton.familymap.model.FamilyMember;

import java.util.List;
import java.util.Locale;

import model.Event;

public class PersonExpandableListAdapter extends BaseExpandableListAdapter {

    private final Context context;
    private final String[] expandableListTitle;
    private final List<Event> events;
    private final List<FamilyMember> familyMembers;
    private final String firstName;
    private final String lastName;

    public PersonExpandableListAdapter(Context context, String[] expandableListTitle,
                                       List<Event> events, List<FamilyMember> family,
                                       String firstName, String lastName) {

        this.context = context;
        this.expandableListTitle = expandableListTitle; // titles for the groups
        this.events = events; // list of life events for the person
        this.firstName = firstName;
        this.lastName = lastName;
        this.familyMembers = family; // list of family members associated with the person
    }

    @Override
    public Object getChild(int listPosition, int expandedListPosition) {
        // Return child item (Event or FamilyMember) at the specified position
        if (this.expandableListTitle[listPosition].equals("LIFE EVENTS")) {
            return events.get(expandedListPosition); // Event object
        }
        else {
            return familyMembers.get(expandedListPosition); // FamilyMember object
        }
    }

    @Override
    public long getChildId(int listPosition, int expandedListPosition) {
        // Return the unique ID for the given child
        return expandedListPosition;
    }

    @Override
    public View getChildView(int listPosition, final int expandedListPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        // Inflate child views for each child item and write their content
        final Object child = getChild(listPosition, expandedListPosition); // Event or FamilyMember object
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.info_row_event, null);
        }
        if (child.getClass() == Event.class) {
            writeEvent(convertView, (Event) child); // write content for an Event object
        }
        else {
            writeFamilyMember(convertView, (FamilyMember) child); // write content for a FamilyMember object
        }

        return convertView; // return the child view
    }

    @Override
    public int getChildrenCount(int listPosition) {
        // Return the number of child items (Events or FamilyMembers) for a given group
        if (this.expandableListTitle[listPosition].equals("LIFE EVENTS")) {
            return this.events.size();
        }
        else {
            return familyMembers.size();
        }
    }

    @Override
    public Object getGroup(int listPosition) {
        // Return the group title at the specified position
        return this.expandableListTitle[listPosition];
    }

    @Override
    public int getGroupCount() {
        // Return the number of groups
        return this.expandableListTitle.length;
    }

    // Return the group ID for a given list position
    @Override
    public long getGroupId(int listPosition) {
        return listPosition;
    }

    // Returns the view for the given group at the given position in the list
    // isExpanded indicates if the group is expanded or collapsed
    // convertView is a view that can be reused, if null then inflate a new view
    @Override
    public View getGroupView(int listPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        // Get the title of the group
        String listTitle = (String) getGroup(listPosition);
        // If convertView is null, inflate a new view using the group_event layout
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.group_event, null);
        }

        // Get the TextView for the group title and set the text to the title
        TextView listTitleTextView = convertView.findViewById(R.id.eventGroupTitle);
        listTitleTextView.setTypeface(null, Typeface.BOLD);
        listTitleTextView.setText(listTitle);

        // Return the view
        return convertView;
    }

    // Indicates whether the IDs for the groups are stable across changes to the underlying data
    @Override
    public boolean hasStableIds() {
        return false;
    }

    // Indicates whether a child view is selectable
    // Returns true for all children
    @Override
    public boolean isChildSelectable(int listPosition, int expandedListPosition) {
        return true;
    }

    // Helper method to set the contents of the view for an Event
    // Sets the image, main info text, and sub info text
    private void writeEvent(View view, Event event) {
        // Set the image to the location icon
        ImageView personImage = view.findViewById(R.id.rowImage);
        personImage.setImageResource(R.drawable.location);

        // Get the resources and set the main info text to the formatted event info
        Resources res = view.getResources();
        TextView mainText = view.findViewById(R.id.mainInfo);
        String infoText = String.format(Locale.ROOT, "%s: %s, %s (%d)",
                event.getEventType().toUpperCase(Locale.ROOT), event.getCity(),
                event.getCountry(), event.getYear());
        mainText.setText(infoText);

        // Set the sub info text to the person's name using the string resource
        TextView subText = view.findViewById(R.id.subInfo);
        subText.setText(res.getString(R.string.person_name, firstName, lastName));
    }

    // Helper method to set the contents of the view for a FamilyMember
    // Sets the image, main info text, and sub info text
    private void writeFamilyMember(View view, FamilyMember familyMember) {
        // Set the image based on the person's gender
        ImageView personImage = view.findViewById(R.id.rowImage);
        personImage.setImageResource(familyMember.getPerson().getGender().equals("f") ? R.drawable.female
                : R.drawable.male);

        // Set the main info text to the person's full name
        TextView mainText = view.findViewById(R.id.mainInfo);
        String infoText = String.format(Locale.ROOT, "%s %s", familyMember.getPerson().getFirstName(),
                familyMember.getPerson().getLastName());
        mainText.setText(infoText);

        // Set the sub info text to the person's relationship to the user
        TextView subText = view.findViewById(R.id.subInfo);
        subText.setText(familyMember.getRelationship());
    }


}