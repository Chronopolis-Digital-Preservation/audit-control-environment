package edu.umiacs.ace.rest;

import java.util.Date;

/**
 * Bean for displaying certain MonitoredItem properties in our api
 * todo: java.time???
 *
 * Created by shake on 11/14/16.
 */
public class MonitoredItemBean {

    private Long id;
    private String path;
    private String state;
    private String fileDigest;
    private Long size;
    private Date lastSeen;
    private Date stateChange;
    private Date lastVisited;

    public MonitoredItemBean() {
    }

    public MonitoredItemBean(Long id, String path, char state, String fileDigest, Long size , Date lastSeen, Date stateChange, Date lastVisited) {
        this.id = id;
        this.path = path;
        this.state = String.valueOf(state);
        this.fileDigest = fileDigest;
        this.size = size;
        this.lastSeen = lastSeen;
        this.stateChange = stateChange;
        this.lastVisited = lastVisited;
    }


    public Long getId() {
        return id;
    }

    public MonitoredItemBean setId(Long id) {
        this.id = id;
        return this;
    }

    public String getPath() {
        return path;
    }

    public MonitoredItemBean setPath(String path) {
        this.path = path;
        return this;
    }

    public String getState() {
        return state;
    }

    public MonitoredItemBean setState(String state) {
        this.state = state;
        return this;
    }

    public String getFileDigest() {
        return fileDigest;
    }

    public MonitoredItemBean setFileDigest(String fileDigest) {
        this.fileDigest = fileDigest;
        return this;
    }

    public Long getSize() {
        return size;
    }

    public MonitoredItemBean setSize(Long size) {
        this.size = size;
        return this;
    }

    public Date getLastSeen() {
        return lastSeen;
    }

    public MonitoredItemBean setLastSeen(Date lastSeen) {
        this.lastSeen = lastSeen;
        return this;
    }

    public Date getStateChange() {
        return stateChange;
    }

    public MonitoredItemBean setStateChange(Date stateChange) {
        this.stateChange = stateChange;
        return this;
    }

    public Date getLastVisited() {
        return lastVisited;
    }

    public MonitoredItemBean setLastVisited(Date lastVisited) {
        this.lastVisited = lastVisited;
        return this;
    }
}
