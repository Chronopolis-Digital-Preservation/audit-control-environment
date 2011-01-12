package edu.umiacs.ace.monitor.register;

import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.ace.monitor.core.MonitoredItem;
import edu.umiacs.ace.token.TokenStoreEntry;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author toaster
 */
public class IngestSessionBean {

    private List<TokenStoreEntry> entries;
    private Collection collection;
    private MonitoredItem baseDirectory;
    private int subset;
    private int shortestEntry;

    public MonitoredItem getBaseDirectory() {
        return baseDirectory;
    }

    public Collection getCollection() {
        return collection;
    }

    public List<TokenStoreEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public int getShortestEntry() {
        return shortestEntry;
    }

    public int getSubset() {
        return subset;
    }

    public void setSubset( int subset ) {
        this.subset = subset;
    }

}
