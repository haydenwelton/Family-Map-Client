package net.haydenwelton.familymap.model;

import java.util.Locale;

import model.Event;
import model.Person;

public class SearchResult {
    public enum Type { PERSON, EVENT }

    private final Type type;
    private final String id;
    private final String mainInfo;
    private final String subInfo;
    private final String gender;

    public SearchResult(Person person) {
        this.type = Type.PERSON;
        this.id = person.getPersonID();
        this.mainInfo = person.getFirstName() + " " + person.getLastName();
        this.subInfo = null;
        this.gender = person.getGender();
    }

    public SearchResult(Event event, String firstName, String lastName) {
        this.type = Type.EVENT;
        this.id = event.getEventID();
        this.mainInfo = event.getEventType().toUpperCase(Locale.ROOT) + ": " +
                event.getCity() + ", " + event.getCountry() + " (" +
                event.getYear() + ")";
        this.subInfo = firstName + " " + lastName;
        this.gender = null;
    }

    public Type getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public String getMainInfo() {
        return mainInfo;
    }

    public String getSubInfo() {
        return subInfo;
    }

    public String getGender() {
        return gender;
    }
}
