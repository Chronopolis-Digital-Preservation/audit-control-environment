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
// $Id$

package edu.umiacs.ace.driver.benchmark;

import edu.umiacs.ace.driver.DriverStateBean;
import edu.umiacs.ace.util.PersistUtil;
import edu.umiacs.ace.driver.AuditIterable;
import edu.umiacs.ace.driver.FileBean;
import edu.umiacs.ace.driver.filter.PathFilter;
import edu.umiacs.ace.driver.StorageDriver;
import edu.umiacs.ace.monitor.core.MonitoredItem;
import edu.umiacs.ace.monitor.core.Collection;
import edu.umiacs.ace.util.HashValue;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import org.apache.log4j.Logger;

/**
 * Simple benchmarking driver that generates a fake collection of a given depth
 * At each layer, 'dirs' directories will be created. At each leaf directory,
 * 'files' files will be created and returned.
 * 
 * This driver will NOT return input streams
 * 
 * @author toaster
 */
public final class BenchmarkAccess extends StorageDriver {

    private static final String PAGE = "benchmark.jsp";
    private BenchmarkSettings settings;
    private EntityManager em;
    private static final Logger LOG = Logger.getLogger(BenchmarkAccess.class);
    private static final long seed = 974629478;
    public static final String PARAM_BLOCK_SIZE = "block_size";
    public static final String PARAM_DO_DIGEST = "readFiles"; // should we create fake files to read, or just digests
    public static final String PARAM_LENGTH = "length";
    public static final String PARAM_DEPTH = "depth";
    public static final String PARAM_DIRS_LAYER = "dirs"; // directories per layer
    public static final String PARAM_FILES_DIR = "files"; // number of files in each root

    public BenchmarkAccess( Collection c, EntityManager em ) {

        super(c);
        try {
            this.em = em;
            Query q = em.createNamedQuery("BenchmarkSettings.getByCollection");

            q.setParameter("coll", c);
            try {
                settings = (BenchmarkSettings) q.getSingleResult();
                LOG.debug("Loaded existing srbSettings");
            } catch ( NoResultException e ) {
                settings = new BenchmarkSettings();
                settings.setBlockSize(0);
                settings.setCollection(c);
                settings.setDepth(1);
                settings.setDirs(1);
                settings.setFileLength(0);
                settings.setFiles(1);
                EntityTransaction et = em.getTransaction();
                et.begin();
                em.persist(settings);
                et.commit();
                LOG.trace("Persisted new BenchmarkSetting");
            }
        } catch ( Exception e ) {
            LOG.error("Error loading settings: " + e);
            throw new RuntimeException(e);
        }
    }

    public BenchmarkSettings getSettings() {
        return settings;
    }

    @Override
    public void setParameters( Map m ) {
        boolean newTx;

        settings.setBlockSize(Integer.parseInt(getSingleParam(m,
                PARAM_BLOCK_SIZE)));
        settings.setCollection(getCollection());
        settings.setDepth(Integer.parseInt(getSingleParam(m, PARAM_DEPTH)));
        settings.setDirs(Integer.parseInt(getSingleParam(m, PARAM_DIRS_LAYER)));
        settings.setFileLength(Long.parseLong(getSingleParam(m, PARAM_LENGTH)));
        settings.setFiles(Integer.parseInt(getSingleParam(m, PARAM_FILES_DIR)));
        settings.setReadFiles(Boolean.parseBoolean(getSingleParam(m,
                PARAM_DO_DIGEST)));

        EntityTransaction et = em.getTransaction();
        newTx = et.isActive();
        if ( !newTx ) {
            et.begin();
        }
        PersistUtil.getEntityManager().merge(settings);
        if ( !newTx ) {
            et.commit();
        }
    }

    @Override
    public String checkParameters( Map m, String path ) {
        try {
            Integer.parseInt(getSingleParam(m, PARAM_BLOCK_SIZE));
            Integer.parseInt(getSingleParam(m, PARAM_DEPTH));
            Integer.parseInt(getSingleParam(m, PARAM_DIRS_LAYER));
            Long.parseLong(getSingleParam(m, PARAM_LENGTH));
            Integer.parseInt(getSingleParam(m, PARAM_FILES_DIR));
            Boolean.parseBoolean(getSingleParam(m, PARAM_DO_DIGEST));
            return null;
        } catch ( NumberFormatException nfe ) {
            LOG.debug("Error parsing: ", nfe);

            return "Error parsing item " + nfe.getMessage();
        }

    }

    @Override
    public void remove( EntityManager em ) {
        EntityTransaction trans = em.getTransaction();
        boolean active = trans.isActive();
        if ( !active ) {
            trans.begin();
        }
        em.remove(settings);
        if ( !active ) {
            trans.commit();
        }
    }

    @Override
    public String getPage() {
        return PAGE;
    }

    private String getSingleParam( Map m, String paramName ) {
        Object o = m.get(paramName);

        if ( o == null ) {
            return null;
        }

        String[] objString = (String[]) o;
        return objString[0];
    }

    @Override
    public AuditIterable<FileBean> getWorkList( final String digestAlgorithm,
            final PathFilter filter, final MonitoredItem[] startPathList ) {
        return new AuditIterable<FileBean>() {

            @Override
            public void cancel() {
            }

            @Override
            public Iterator<FileBean> iterator() {
                if ( settings.isReadFiles() ) {
                    return new MyReadFileIterator(digestAlgorithm);
                } else {
                    return new MyFakeBeanIterator();
                }

            }

            @Override
            public DriverStateBean[] getState() {
                return new DriverStateBean[0];
            }

        };
    }

    @Override
    public InputStream getItemInputStream( String itemPath ) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private abstract class BaseIterator implements Iterator<FileBean> {

        private int[] arrayCounter;
        private int dirs;
        private int maxFiles;
        private int currFile;
        private boolean next = true;

        public BaseIterator() {
            arrayCounter = new int[settings.getDepth()];
            Arrays.fill(arrayCounter, 0);
            dirs = settings.getDirs();
            maxFiles = settings.getFiles();
            currFile = 0;
        }

        @Override
        public boolean hasNext() {
            return next;
        }

        private String[] createPathList() {
            String[] retString = new String[arrayCounter.length + 1];

            StringBuilder path = new StringBuilder();
            for ( int idx = 0; idx < arrayCounter.length; idx++ ) {
                path.append("/dir-");
                path.append(arrayCounter[idx]);
                retString[arrayCounter.length - idx] = path.toString();
            }
            retString[0] = path + "/file-" + currFile;

            return retString;
        }

        private void incrementFile() {
            // see if current file goes over limit when incremented, up directory
            if ( ++currFile >= maxFiles ) {
                currFile = 0;
                // update directory
                for ( int i = arrayCounter.length - 1; i >= 0; i-- ) {
                    if ( ++arrayCounter[i] >= dirs ) {
                        if ( i == 0 ) // entire array has rolled over
                        {
                            next = false;
                        }
                        arrayCounter[i] = 0;
                    } else {
                        i = -1;
                    }

                }
            }
        }

        @Override
        public FileBean next() {
            if ( !next ) {
                return null;
            }
            // create fileBean
            FileBean fb = new FileBean();
            fb.setError(false);
            fb.setHash(getHash());
            fb.setPathList(createPathList());
            fb.setFileSize(settings.getFileLength());

            incrementFile();
            return fb;
        }

        abstract String getHash();

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    private class MyFakeBeanIterator extends BaseIterator {

        private String fill = "0000000000000000";
        private String prefixDigest = "c5b7a40ee481dbb190544ec21b41313fb9bc6d693cf5991f";
        private long count = 0;

        @Override
        String getHash() {
            String suffix = Long.toHexString(count);
            count++;
            return prefixDigest + fill.substring(suffix.length()) + suffix;
        }
    }

    private class MyReadFileIterator extends BaseIterator {

        private byte[] block = new byte[settings.getBlockSize()];
        private int offset = 0;
        private MessageDigest digest;

        public MyReadFileIterator( String digestAlgorithm ) {
            // 1. Fill holding array
            Random r = new Random(seed);
            r.nextBytes(block);
            try {
                digest = MessageDigest.getInstance(digestAlgorithm);
            } catch ( NoSuchAlgorithmException ex ) {
                throw new RuntimeException(ex);
            }

        }

        @Override
        String getHash() {
            long start = System.currentTimeMillis();
            long remaining = settings.getFileLength();
            digest.reset();
            int initialLength = (int) ((block.length - offset) > remaining ? remaining : block.length
                    - offset);
            LOG.debug("offs " + offset + " len " + initialLength);
            digest.update(block, offset, initialLength);

            remaining = settings.getFileLength() - initialLength;
            while ( remaining > 0 ) {
                if ( block.length < remaining ) {
                    digest.update(block);
                    remaining -= block.length;
                    offset = 0;
                } else {
                    digest.update(block, 0, (int) remaining);
                    remaining -= remaining;
                    offset = (int) remaining;
                }
            }
            long totalTime = System.currentTimeMillis() - start;
            if (LOG.isTraceEnabled())
            LOG.trace(
                    "Time: " + (totalTime / 1000) + "s " + ((settings.getFileLength() / 1024)
                    / totalTime) / 1000 + "K/s");
            return HashValue.asHexString(digest.digest());
        }
    }
}
