package edu.umiacs.ace.monitor.core;

import javax.persistence.AttributeConverter;

/**
 * Converter to go from CollectionState enum to a Char
 *
 * Created by shake on 3/30/17.
 */
public class CollectionStateConverter implements AttributeConverter<CollectionState, String> {
    @Override
    public String convertToDatabaseColumn(CollectionState collectionState) {
        switch (collectionState) {
            case ACTIVE:
                return "A";
            case NEVER:
                return "N";
            case INTERRUPTED:
                return "I";
            case ERROR:
                return "E";
            default:
                throw new IllegalArgumentException("Unknown state " + collectionState);
        }
    }

    @Override
    public CollectionState convertToEntityAttribute(String s) {
        switch (s) {
            case "A":
                return CollectionState.ACTIVE;
            case "N":
                return CollectionState.NEVER;
            case "I":
                return CollectionState.INTERRUPTED;
            case "E":
                return CollectionState.ERROR;
            default:
                throw new IllegalArgumentException("Unknown state " + s);
        }
    }
}
