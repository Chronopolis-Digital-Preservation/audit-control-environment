package edu.umiacs.ace.driver2;

import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.ace.monitor.core.MonitoredItem;
import java.util.Iterator;

/**
 *
 * @author toaster
 */
public interface AceDriverFactory<T,V> {

  
    public FileReader<V> createReader(T settings);
    public V translatePath(Collection c, MonitoredItem item);
    public Iterator<V> createScanner(T settings);
}
