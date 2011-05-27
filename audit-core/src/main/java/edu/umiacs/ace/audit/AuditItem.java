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

    private String name;
    private long created;
    private AuditSource source;

    public AuditItem(String name, AuditSource source) {
        this.name = name;
        this.created = System.currentTimeMillis();
        this.source = source;
    }

    public final AuditSource getSource() {
        return source;
    }

    public abstract InputStream openStream() throws IOException;

    public final String getName() {
        return name;
    }

    public final long getCreated() {
        return created;
    }
}
