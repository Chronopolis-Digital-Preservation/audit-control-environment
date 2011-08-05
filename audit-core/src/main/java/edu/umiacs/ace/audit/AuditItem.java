/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.umiacs.ace.audit;

import edu.umiacs.ace.util.ThrottledInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author toaster
 */
public abstract class AuditItem {

    private String[] pathList;
    private long created;
    private AuditSource source;

    public AuditItem(String[] pathList, AuditSource source) {
        this.pathList = pathList;
        this.created = System.currentTimeMillis();
        this.source = source;
    }

    public final AuditSource getSource() {
        return source;
    }

    public abstract InputStream openStream() throws IOException;

    public final String[] getPathList() {
        return pathList;
    }

    public final long getCreated() {
        return created;
    }
}
