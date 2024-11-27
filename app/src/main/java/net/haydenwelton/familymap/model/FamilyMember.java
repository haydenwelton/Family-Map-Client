package net.haydenwelton.familymap.model;

import java.util.Comparator;

import model.Person;

public class FamilyMember {
    private final String relationship;
    private final Person person;

    public FamilyMember(String relationship, Person person) {
        this.relationship = relationship;
        this.person = person;
    }

    public String getRelationship() {
        return relationship;
    }

    public Person getPerson() {
        return person;
    }

    public static class FamilyMemberComparator implements Comparator<FamilyMember> {
        @Override
        public int compare(FamilyMember o1, FamilyMember o2) {
            if (o1 == o2) {
                return 0;
            }
            if (o1.relationship.equals("Father") ||
                    o1.relationship.equals("Mother") && !o2.relationship.equals("Father") ||
                    o1.relationship.equals("Spouse") && (!o2.relationship.equals("Father") &&
                            !o2.relationship.equals("Mother"))) {
                return -1;
            }
            else {
                return 1;
            }
        }
    }
}

