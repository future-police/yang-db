package com.kayhut.fuse.model;

import com.kayhut.fuse.model.ontology.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.kayhut.fuse.model.OntologyTestUtils.Color.TYPE_COLOR;
import static com.kayhut.fuse.model.OntologyTestUtils.Gender.TYPE_GENDER;
import static com.kayhut.fuse.model.ontology.Property.Builder.get;
import static java.util.Collections.emptyList;

/**
 * Created by liorp on 4/27/2017.
 */
public class OntologyTestUtils {

    public static final String DATE = "date";
    public static final String INT = "int";
    public static final String STRING = "string";
    public static final String CM = "cm";

    public static Property FIRST_NAME = new Property("firstName", STRING, 1);
    public static Property LAST_NAME = new Property("lastName", STRING, 2);
    public static Property GENDER = new Property("gender", TYPE_GENDER, 3);
    public static Property BIRTH_DATE = new Property("birthDate", STRING, 4);
    public static Property DEATH_DATE = new Property("deathDate", STRING, 5);
    public static Property HEIGHT = new Property("height", INT, 6);
    public static Property NAME = new Property("name", STRING, 7);
    public static Property COLOR = new Property("color", TYPE_COLOR, 8);

    public static Property START_DATE = new Property("startDate", DATE, 9);
    public static Property END_DATE = new Property("endDate", DATE, 10);
    public static Property TEMPERATURE = new Property("temperature", INT, 11);
    public static Property TIMESTAMP = new Property("timestamp", DATE, 12);


    public static final RelationshipType OWN = new RelationshipType("own", 101, true).withProperty(START_DATE.type, END_DATE.type);
    public static final RelationshipType MEMBER_OF = new RelationshipType("memberOf", 102, true).withProperty(START_DATE.type, END_DATE.type);
    public static final RelationshipType FIRE = new RelationshipType("fire", 103, true).withProperty(START_DATE.type, END_DATE.type,TEMPERATURE.type,TIMESTAMP.type);
    public static final RelationshipType FREEZE = new RelationshipType("freeze", 104, true).withProperty(START_DATE.type, END_DATE.type,TEMPERATURE.type);
    public static final RelationshipType ORIGIN = new RelationshipType("origin", 105, true).withProperty(START_DATE.type, END_DATE.type);
    public static final RelationshipType SUBJECT = new RelationshipType("subject", 106, true).withProperty(START_DATE.type, END_DATE.type);
    public static final RelationshipType REGISTERED = new RelationshipType("registered", 107, true).withProperty(START_DATE.type, END_DATE.type);


    public interface Entity {
        String name();

        int type();

        List<RelationshipType> relations();

        List<Property> properties();

    }

    public static class Property {
        public String name;
        public String className;
        public int type;

        public Property(String name, String className, int type) {
            this.name = name;
            this.className = className;
            this.type = type;
        }
    }

    public static class DRAGON implements Entity {
        public static String name = "Dragon";
        public static int type = 2;
        public static List<Property> propertyList = Arrays.asList(NAME, GENDER, COLOR);

        public static List<RelationshipType> relationshipList = Arrays.asList(
                REGISTERED.addPair(new EPair(type, GUILD.type)),
                FIRE.addPair(new EPair(type, DRAGON.type)),
                FREEZE.addPair(new EPair(type, DRAGON.type)),
                ORIGIN.addPair(new EPair(type, KINGDOM.type)));

        @Override
        public String name() {
            return name;
        }

        @Override
        public int type() {
            return type;
        }

        @Override
        public List<Property> properties() {
            return propertyList;
        }

        @Override
        public List<RelationshipType> relations() {
            return relationshipList;
        }
    }

    public static class HORSE implements Entity {
        public static String name = "Horse";
        public static int type = 3;
        public static List<Property> propertyList = Arrays.asList(NAME, GENDER);

        public static List<RelationshipType> relationshipList = Collections.singletonList(
                REGISTERED.addPair(new EPair(type, GUILD.type)));

        @Override
        public String name() {
            return name;
        }

        @Override
        public int type() {
            return type;
        }

        @Override
        public List<Property> properties() {
            return propertyList;
        }

        @Override
        public List<RelationshipType> relations() {
            return relationshipList;
        }
    }

    public static class GUILD implements Entity {
        public static String name = "Guild";
        public static int type = 4;
        public static List<Property> propertyList = Arrays.asList(NAME);

        public static List<RelationshipType> relationshipList = Collections.singletonList(
                REGISTERED.addPair(new EPair(type, KINGDOM.type)));

        @Override
        public String name() {
            return name;
        }

        @Override
        public int type() {
            return type;
        }

        @Override
        public List<Property> properties() {
            return propertyList;
        }

        @Override
        public List<RelationshipType> relations() {
            return relationshipList;
        }

    }

    public static class KINGDOM implements Entity {
        public static String name = "Kingdom";
        public static int type = 5;
        public static List<Property> propertyList = Arrays.asList(NAME);

        @Override
        public String name() {
            return name;
        }

        @Override
        public int type() {
            return type;
        }

        @Override
        public List<Property> properties() {
            return propertyList;
        }

        @Override
        public List<RelationshipType> relations() {
            return emptyList();
        }
    }

    public static class PERSON implements Entity {
        public static String name = "Person";
        public static int type = 1;


        public static List<Property> propertyList = Arrays.asList(FIRST_NAME, LAST_NAME, GENDER, BIRTH_DATE, DEATH_DATE, HEIGHT, NAME);

        public static List<RelationshipType> relationshipList = Arrays.asList(
                SUBJECT.addPair(new EPair(type, KINGDOM.type)),
                OWN.addPair(new EPair(type, DRAGON.type)),
                OWN.addPair(new EPair(type, HORSE.type)),
                MEMBER_OF.addPair(new EPair(type, GUILD.type)));


        @Override
        public String name() {
            return name;
        }

        @Override
        public int type() {
            return type;
        }

        @Override
        public List<Property> properties() {
            return propertyList;
        }

        @Override
        public List<RelationshipType> relations() {
            return relationshipList;
        }

    }

    public static Ontology createDragonsOntologyShort() {
        //no real use of partial ontology under no validation
        return createDragonsOntologyLong();
    }

    public static Ontology createDragonsOntologyLong() {
        Ontology ontologyShortObj = new Ontology();
        ontologyShortObj.setOnt("Dragons");
        //enums
        ontologyShortObj.setEnumeratedTypes(Arrays.asList(
                EnumeratedType.from(TYPE_GENDER, Gender.values()),
                EnumeratedType.from(TYPE_COLOR, Color.values())));

        //properties
        ontologyShortObj.setProperties(Arrays.asList(
                get().build(FIRST_NAME.type,FIRST_NAME.name,STRING),
                get().build(LAST_NAME.type,LAST_NAME.name,STRING),
                get().build(GENDER.type,GENDER.name,TYPE_GENDER),
                get().build(BIRTH_DATE.type,BIRTH_DATE.name,DATE),
                get().build(DEATH_DATE.type,DEATH_DATE.name,DATE),
                get().build(HEIGHT.type,HEIGHT.name,INT,CM),
                get().build(NAME.type,NAME.name,STRING),
                get().build(START_DATE.type,START_DATE.name,DATE),
                get().build(END_DATE.type,END_DATE.name,DATE),
                get().build(TIMESTAMP.type,TIMESTAMP.name,DATE),
                get().build(TEMPERATURE.type,TEMPERATURE.name,INT)));

        ontologyShortObj.setRelationshipTypes(Arrays.asList(
                REGISTERED,
                SUBJECT,
                ORIGIN,
                FREEZE,
                FIRE,
                MEMBER_OF,
                OWN));

        //entities
        ontologyShortObj.getEntityTypes().addAll(Arrays.asList(
                new EntityType(PERSON.type, PERSON.name, PERSON.propertyList.stream().map(p1 -> p1.type).collect(Collectors.toList())),
                new EntityType(HORSE.type, HORSE.name, HORSE.propertyList.stream().map(p1 -> p1.type).collect(Collectors.toList())),
                new EntityType(DRAGON.type, DRAGON.name, DRAGON.propertyList.stream().map(p1 -> p1.type).collect(Collectors.toList())),
                new EntityType(KINGDOM.type, KINGDOM.name, KINGDOM.propertyList.stream().map(p1 -> p1.type).collect(Collectors.toList())),
                new EntityType(GUILD.type, GUILD.name, GUILD.propertyList.stream().map(p1 -> p1.type).collect(Collectors.toList()))));

        return OntologyFinalizer.finalize(ontologyShortObj);
    }

    public static enum Gender {
        MALE, FEMALE, OTHER;
        public static final String TYPE_GENDER = "TYPE_Gender";

    }

    public static enum Color {
        RED, BLUE, GREEN, YELLOW;
        public static final String TYPE_COLOR = "TYPE_Color";

    }

    public static Property getPropertyByName(List<Property> properties, String name) {
        return properties.stream().filter(p -> p.name.equals(name)).findFirst().get();
    }

    public static Property getPropertyByType(List<Property> properties, int type) {
        return properties.stream().filter(p -> p.type == type).findFirst().get();
    }

}
