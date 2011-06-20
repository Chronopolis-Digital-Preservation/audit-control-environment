/*
 * Copyright (c) 2007-2011, University of Maryland
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
package edu.umiacs.ace.monitor.compare;

import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.ace.monitor.core.MonitoredItem;
import edu.umiacs.ace.util.PersistUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.apache.log4j.Logger;

/**
 * Currently does all comparison in memory, this limits collections to a million
 * items or so, but prevents db thrashing.
 * 
 * 
 * @author toaster
 */
public final class CollectionCompare2 {

    private Map<String, String> sourceMap = new HashMap<String, String>();
    private Map<String, String> sourceReverseMap = new HashMap<String, String>();
    private static final Logger LOG = Logger.getLogger(CollectionCompare2.class);
    private List<String> parseErrors = new ArrayList<String>();

    public CollectionCompare2(InputStream sourceFile, String prefix) {
        try {
            parseInputStream(sourceFile, prefix);
        } catch (IOException e) {
            LOG.error("Error reading digest source", e);
            throw new RuntimeException(e);
        }
    }

    Map<String, String> getSourceMap() {
        return sourceMap;
    }

    public List<String> getParseErrors() {
        return Collections.unmodifiableList(parseErrors);
    }

    public void compareTo(CompareResults cr, Collection c, MonitoredItem item) {
        EntityManager em = PersistUtil.getEntityManager();
        long time = System.currentTimeMillis();
        long total = 0;

        try {
            LOG.info("Starting collection compare on " + c.getName() + " source size: " + sourceMap.size());

            Query q = em.createNamedQuery("MonitoredItem.listFilesInCollection");
            q.setParameter("coll", c);
            List items = q.getResultList();

            for (Object o : items) {
                total++;
                MonitoredItem aceItem = (MonitoredItem) o;
                String acePath = aceItem.getPath();
                String aceDigest = aceItem.getFileDigest();

                if (sourceMap.containsKey(acePath)) {
                    cr.fileExistsAtTarget(acePath);

                    if (sourceMap.get(acePath).matches(aceDigest)) {
//Perfect file, 
                    } else {
                        cr.mismatchedDigests(acePath, sourceMap.get(acePath), aceDigest);
                    }
                } else if (sourceReverseMap.containsKey(aceDigest)) {
                    cr.fileExistsAtTarget(sourceReverseMap.get(aceDigest));
                    cr.mismatchedNames(aceDigest, acePath, sourceReverseMap.get(aceDigest));
                } else {
                    cr.sourceFileNotFound(acePath);
                }
            }
        } catch (Exception e) {
            LOG.error("Error during load and compare: ", e);

        } finally {
            LOG.info("Finished collection compare on: "
                    + c.getName() + " time: " + (System.currentTimeMillis() - time) + " tested: " + total);
            cr.finished();
            em.close();
        }

    }

    private void parseInputStream(InputStream sourceFile, String prefix) throws IOException {

        BufferedReader input = new BufferedReader(new InputStreamReader(sourceFile));
        String line = input.readLine();

        if (line != null && line.matches("^[A-Z0-9\\-]+:.+$")) {
            line = input.readLine();
        }

        while (line != null) {
            String tokens[] = line.split("\\s+", 2);
            if (tokens == null || tokens.length != 2) {
                LOG.error("Error processing line: " + line);
                parseErrors.add("Corrupt Line: " + line);
            } else {
                sourceMap.put(tokens[1], tokens[0]);
                sourceReverseMap.put(tokens[0], tokens[1]);
            }
            line = input.readLine();
        }
    }
}
