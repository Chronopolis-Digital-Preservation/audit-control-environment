/*
 * Copyright (c) 2007-2010, University of Maryland
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 * and the following disclaimer in the documentation and/or other materials provided with the
 * distribution.
 *
 * Neither the name of the University of Maryland nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ACE Components were written in the ADAPT Project at the University of
 * Maryland Institute for Advanced Computer Study.
 */
// $Id: DirectoryTree.java 3181 2010-06-16 20:46:16Z toaster $

package edu.umiacs.ace.monitor.access.browse;

import edu.umiacs.ace.monitor.core.MonitoredItem;
import edu.umiacs.ace.monitor.core.MonitoredItemManager;
import edu.umiacs.ace.util.PersistUtil;
import edu.umiacs.ace.monitor.core.Collection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import org.apache.log4j.Logger;

/**
 * Object to track a directory tree for display in directorytree.jsp
 * @author toaster
 */
public class DirectoryTree {

    private static final Logger LOG = Logger.getLogger(DirectoryTree.class);
    List<DirectoryNode> roots = new ArrayList<DirectoryNode>();
    private Map<Long, DirectoryNode> items = new HashMap<Long, DirectoryNode>();
    private Collection collection;

    public DirectoryTree( Collection parent ) {
        this.collection = parent;
        EntityManager em = PersistUtil.getEntityManager();
        MonitoredItemManager mem = new MonitoredItemManager(em);
        try {


            for ( MonitoredItem mi : mem.getCollectionRoots(parent) ) {

                DirectoryNode dn = new DirectoryNode();
                dn.id = mi.getId();
                dn.directory = mi.isDirectory();
                dn.expanded = false;
                dn.children = null;
                dn.path = mi.getPath();
                dn.status = mi.getState();
                items.put(mi.getId(), dn);
                roots.add(dn);
            }
            Collections.sort(roots);
        } finally {
            em.close();
        }
    }

    public List<DirectoryNode> getRoots() {
        return new ArrayList<DirectoryNode>(roots);
    }

    public DirectoryNode getDirectoryNode( long item ) {
        return items.get(item);
    }

    public void toggleItem( long item ) {
        DirectoryNode dn = items.get(item);
        if ( dn == null || !dn.directory ) {
            LOG.debug("Null directory id: " + item);
            return;
        }

        if ( dn.expanded ) {
            closeItem(dn);
        } else {
            expandItem(dn);
        }
    }

    public Collection getCollection() {
        return collection;
    }

    private void expandItem( DirectoryNode dn ) {
        LOG.trace("Expanding directory: " + dn.getName());

        EntityManager em = PersistUtil.getEntityManager();
        MonitoredItemManager mem = new MonitoredItemManager(em);

        dn.expanded = true;
        dn.children = new ArrayList<DirectoryNode>();

        try {
            for ( MonitoredItem mi : mem.listChildren(collection, dn.getPath()) ) {
                DirectoryNode d = new DirectoryNode();
                d.id = mi.getId();
                d.directory = mi.isDirectory();
                d.expanded = false;
                d.children = null;
                d.path = mi.getPath();
                d.status = mi.getState();
                dn.children.add(d);
                items.put(d.getId(), d);


            }
            Collections.sort(dn.children);
        } finally {
            em.close();

        }
    }

    private void closeItem( DirectoryNode dn ) {
        LOG.trace("Closing directory: " + dn.getName());

        if ( dn.expanded ) {
            for ( DirectoryNode d : dn.children ) {
                closeItem(d);
                items.remove(d.getId());
            }
        }
        dn.expanded = false;
//        items.remove(dn.id);
        dn.children = null;

    }

    public class DirectoryNode implements Comparable {

        private boolean expanded = false;
        private boolean directory = false;
        private String path;
        private long id;
        List<DirectoryNode> children = new ArrayList<DirectoryNode>();
        private char status;

        public boolean isExpanded() {
            return expanded;
        }

        public boolean isDirectory() {
            return directory;
        }

        public long getId() {
            return id;
        }

        public String getPath() {
            return path;
        }

        public String getName() {
            return path.substring(path.lastIndexOf('/') + 1);
        }

        public List<DirectoryNode> getChildren() {
            return children;
        }

        public String getStatus() {
            return String.valueOf(status);
        }

        public int compareTo( Object o ) {
            if ( o instanceof DirectoryNode ) {
                DirectoryNode n = (DirectoryNode) o;
                if ( directory && !n.directory ) {
                    return -1;
                }
                if ( n.directory && !directory ) {
                    return 1;
                }

                return getName().compareTo(n.getName());
            }
            throw new IllegalArgumentException("Cannot compare unlike items");
        }
    }
}
