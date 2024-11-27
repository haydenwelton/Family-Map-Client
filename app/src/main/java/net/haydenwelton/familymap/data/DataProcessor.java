package net.haydenwelton.familymap.data;

import net.haydenwelton.familymap.model.FamilyMember;
import net.haydenwelton.familymap.model.SearchResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import model.Event;
import model.Person;

public class DataProcessor {

    // Find family members for a given person
    public static ArrayList<FamilyMember> findFamily(Person rootPerson) {
        ArrayList<FamilyMember> familyMembers = new ArrayList<>();
        for (Person person : DataCache.getInstance().getPersons()) {
            // Check if person is father or mother
            if (rootPerson.getMotherID() != null &&
                    rootPerson.getFatherID().equals(person.getPersonID())) {
                familyMembers.add(new FamilyMember("Father", person));
            }
            else if (rootPerson.getMotherID() != null &&
                    rootPerson.getMotherID().equals(person.getPersonID())) {
                familyMembers.add(new FamilyMember("Mother", person));
            }
            // Check if person is spouse
            else if (rootPerson.getSpouseID() != null &&
                    rootPerson.getSpouseID().equals(person.getPersonID())) {
                familyMembers.add(new FamilyMember("Spouse", person));
            }
            // Check if person is child
            else if (person.getFatherID() != null &&
                    person.getFatherID().equals(rootPerson.getPersonID()) ||
                    person.getMotherID() != null &&
                            person.getMotherID().equals(rootPerson.getPersonID())) {
                familyMembers.add(new FamilyMember("Child", person));
            }
        }
        // Sort the family members by age using the FamilyMemberComparator
        familyMembers.sort(new FamilyMember.FamilyMemberComparator());
        return familyMembers;
    }

    // Search for persons that match the query
    public static void searchPersons(List<SearchResult> results, String query) {
        if (query.isEmpty() || query.trim().isEmpty()) return;
        for (Person person : DataCache.getInstance().getPersons()) {
            // Check if person's name contains the query
            if (containsIgnoreCase(person.getFirstName() + " " + person.getLastName(), query)) {
                results.add(new SearchResult(person));
            }
        }
    }

    // Search for events that match the query
    public static void searchEvents(List<SearchResult> results, String query) {
        if (query.isEmpty() || query.trim().isEmpty()) return;
        DataCache dataCache = DataCache.getInstance();
        for (Event event : DataCache.getInstance().getEvents()) {
            // Check if event's properties contain the query
            if (containsIgnoreCase(event.getCountry(), query) ||
                    containsIgnoreCase(event.getCity(), query) ||
                    containsIgnoreCase(event.getEventType(), query) ||
                    containsIgnoreCase(String.valueOf(event.getYear()), query)) {
                // Get the corresponding person and add the SearchResult
                Person person = dataCache.getPersonByID(event.getPersonID());
                assert person != null;
                results.add(new SearchResult(event, person.getFirstName(), person.getLastName()));
            }
        }
    }


    public static boolean containsIgnoreCase(String src, String theThing) {
        if (src == null) return false;
        final int length = theThing.length();
        if (length == 0)
            return true; // Empty string is contained

        final char firstLo = Character.toLowerCase(theThing.charAt(0));
        final char firstUp = Character.toUpperCase(theThing.charAt(0));

        for (int i = src.length() - length; i >= 0; i--) {
            // Quick check before calling the more expensive regionMatches() method:
            final char ch = src.charAt(i);
            if (ch != firstLo && ch != firstUp)
                continue;

            if (src.regionMatches(true, i, theThing, 0, length))
                return true;
        }

        return false;
    }

    public static HashMap<String, Person> generatePersonMap(Person[] filteredPersons) {
        HashMap<String, Person> personMap = new HashMap<>();
        for (Person person : filteredPersons) {
            personMap.put(person.getPersonID(), person);
        }
        return personMap;
    }

    public static HashMap<String, TreeSet<Event>> generateSortedEventMap(Event[] filteredEvents,
                                                                         HashMap<String, Person> personMap) {
        HashMap<String, TreeSet<Event>> sortedEventsByPerson = new HashMap<>();
        for (Event event : filteredEvents) {
            Person person = personMap.get(event.getPersonID());
            assert person != null;
            if (sortedEventsByPerson.get(person.getPersonID()) == null) {
                TreeSet<Event> personEvents = new TreeSet<>(new EventComparator());
                personEvents.add(event);
                sortedEventsByPerson.put(person.getPersonID(), personEvents);
            }
            else {
                TreeSet<Event> sortedEvents = sortedEventsByPerson.get(person.getPersonID());
                assert sortedEvents != null;
                sortedEvents.add(event);
            }
        }
        return sortedEventsByPerson;
    }

    private static class EventComparator implements Comparator<Event> {
        @Override
        public int compare(Event event1, Event event2) {
            // Compare events by year
            return Integer.compare(event1.getYear(), event2.getYear());
        }
    }
}