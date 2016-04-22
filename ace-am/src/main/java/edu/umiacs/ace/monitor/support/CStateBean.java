package edu.umiacs.ace.monitor.support;

/**
 * A Bean to hold each collection status as well as its definition
 *
 * Created by shake on 4/5/16.
 */
public enum CStateBean {

    NO_FILTER(""),
    ACTIVE("A"),
    ERROR("E"),
    NEVER_SCANNED("N");

    private final String state;

    CStateBean(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }
}
