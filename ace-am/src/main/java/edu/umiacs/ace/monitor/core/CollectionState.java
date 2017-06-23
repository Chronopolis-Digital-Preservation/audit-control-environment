package edu.umiacs.ace.monitor.core;

/**
 * A test to see how we can do this
 *
 * Created by shake on 3/30/17.
 */
public enum CollectionState {

    ACTIVE, INTERRUPTED, NEVER, ERROR;

    public static CollectionState fromChar(char state) {
        switch (state) {
            case 'A':
                return ACTIVE;
            case 'I':
                return INTERRUPTED;
            case 'N':
                return NEVER;
            case 'E':
                return ERROR;
            default:
                throw new IllegalArgumentException("Unknown state " + state);
        }
    }

    public char asChar() {
        return this.name().charAt(0);
    }
}
