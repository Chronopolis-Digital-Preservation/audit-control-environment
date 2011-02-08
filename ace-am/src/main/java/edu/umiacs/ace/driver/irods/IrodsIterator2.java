/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.umiacs.ace.driver.irods;

import edu.umiacs.ace.driver.DriverStateBean;
import edu.umiacs.ace.driver.DriverStateBean.State;
import edu.umiacs.ace.driver.FileBean;
import edu.umiacs.ace.driver.QueryThrottle;
import edu.umiacs.ace.driver.StateBeanDigestListener;
import edu.umiacs.ace.driver.filter.PathFilter;
import edu.umiacs.ace.monitor.core.MonitoredItem;
import edu.umiacs.ace.util.HashValue;
import edu.umiacs.ace.util.ThreadedDigestStream;
import edu.umiacs.ace.util.ThrottledInputStream;
import edu.umiacs.io.IO;
import edu.umiacs.irods.api.IRodsRequestException;
import edu.umiacs.irods.api.pi.ErrorEnum;
import edu.umiacs.irods.api.pi.GenQueryEnum;
import edu.umiacs.irods.api.pi.RodsObjStat_PI;
import edu.umiacs.irods.operation.ConnectOperation;
import edu.umiacs.irods.operation.IrodsOperations;
import edu.umiacs.irods.operation.IrodsProxyInputStream;
import edu.umiacs.irods.operation.QueryBuilder;
import edu.umiacs.irods.operation.QueryResult;
import edu.umiacs.util.Strings;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import org.apache.log4j.Logger;

/**
 *
 * @author toaster
 */
public class IrodsIterator2 implements Iterator<FileBean> {

    private static final Logger LOG = Logger.getLogger(IrodsIterator2.class);
    private FileBean next;
    private Queue<String> dirsToProcess = new LinkedList<String>();
    private Queue<String> filesToProcess = new LinkedList<String>();
    private MessageDigest digest;
    private static final int BLOCK_SIZE = 1048576;
//    private byte[] buffer = new byte[BLOCK_SIZE];
    private String rootFile;
    private PathFilter filter;
    private DriverStateBean statebean;
    private boolean cancel = false;
    private ConnectOperation co;
    private double lastDelay = 0;
    ThreadedDigestStream reader;

    public IrodsIterator2(MonitoredItem[] startPath, PathFilter filter,
            String digestAlgorithm, DriverStateBean statebean,
            String collectionDirectory, ConnectOperation co) {
        this.statebean = statebean;
        this.filter = filter;
        this.co = co;
        try {
            digest = MessageDigest.getInstance(digestAlgorithm);
            reader = new ThreadedDigestStream(digest, BLOCK_SIZE);
            rootFile = collectionDirectory;

            statebean.setRunningThread(Thread.currentThread());

            statebean.setStateAndReset(State.LISTING);
            if (startPath != null) {
                for (MonitoredItem mi : startPath) {
                    String startFile;
                    startFile = collectionDirectory + startPath[0].getPath();
                    if (mi.isDirectory()) {
                        dirsToProcess.add(startFile);

                    } else {
                        filesToProcess.add(startFile);

                    }
                }

            } else {
                dirsToProcess.add(rootFile);
            }
            loadNext();
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
        statebean.setStateAndReset(State.IDLE);
    }

    public void cancel() {
        this.cancel = true;
        reader.abort();
    }

    @Override
    public boolean hasNext() {
        boolean hasNext = (!cancel && next != null);
        if (!hasNext) {
            reader.shutdown();
        }
        return hasNext;
//        return !cancel && next != null;
    }

    @Override
    public FileBean next() {
        FileBean retValue = next;

        loadNext();

        return retValue;
    }

    @Override
    public void remove() {
    }

    private void loadNext() {
        statebean.setStateAndReset(State.LISTING);

        // see if wee need to process a directory or if there are files in queue
        while (filesToProcess.isEmpty() && !dirsToProcess.isEmpty()) {
            String directory = dirsToProcess.poll();
            statebean.setFile(directory);
            LOG.trace("Popping directory: " + directory);
            scanForDirectories(directory);
            scanForFiles(directory);
        }

        // now see why we ended loop
        // we have files
        if (!filesToProcess.isEmpty() && !cancel) {
            next = processFile(filesToProcess.poll());
        } else {
            next = null;
        }
        statebean.setStateAndReset(State.IDLE);

    }
//private void testConnection() {
//        if (opCount > MAXOPCOUNT) {
//            opCount = 0;
//            try {
//                co.reconnect();
//            } catch (IOException ex) {
//                LOG.error("Error reconnecting", ex);
//            }
//        }
//        opCount++;
//    }

    private void scanForFiles(String parent) {
        QueryBuilder qb;
        QueryResult qr;
        try {
            qb =
                    new QueryBuilder(GenQueryEnum.COL_DATA_NAME);

            qb.eq(GenQueryEnum.COL_COLL_NAME, parent);
//            testConnection();
            qr = qb.execute(co.getConnection());

            while (qr.next()) {
                String file = parent + "/" + qr.getValue(GenQueryEnum.COL_DATA_NAME);
                if (filter.process(extractPathList(file), false)) {
                    filesToProcess.add(file);
                }
            }

        } catch (IOException ex) {
            LOG.error("Exception", ex);
        }
    }

    private void scanForDirectories(String parent) {

        QueryBuilder qb;
        QueryResult qr;
        try {

            qb = new QueryBuilder(GenQueryEnum.COL_COLL_NAME);
            qb.eq(GenQueryEnum.COL_COLL_PARENT_NAME, parent);
//            testConnection();
            qr = qb.execute(co.getConnection());

            while (qr.next()) {
                if (qr.getValue(GenQueryEnum.COL_COLL_NAME).equals(parent)) {
                    continue;
                }
                String dir = qr.getValue(GenQueryEnum.COL_COLL_NAME);
                if (filter.process(extractPathList(dir), true)) {
                    dirsToProcess.add(dir);
                }
            }

        } catch (IRodsRequestException ex) {
            if (ex.getErrorCode() != ErrorEnum.CAT_NO_ROWS_FOUND) {
                //TODO: handle this
                LOG.error("irods error ", ex);
            }
        } catch (IOException ex) {
            LOG.error("IOException ", ex);
            //TODO: handle this
        }

    }

    private String[] extractPathList(String file) {
        int substrLength = rootFile.length();

        // build directory path
        List<String> dirPathList = new ArrayList<String>();
        String currFile = file;
        while (!currFile.equals(rootFile)) {
//            LOG.trace("Adding dir to path: " + currFile.substring(substrLength));
            dirPathList.add(currFile.substring(substrLength));
            currFile = currFile.substring(0, currFile.lastIndexOf('/'));
        }
        return dirPathList.toArray(new String[dirPathList.size()]);
    }

    @SuppressWarnings("empty-statement")
    private FileBean processFile(String file) {


        DigestInputStream dis = null;
        FileBean fb = new FileBean();
        IrodsOperations io = new IrodsOperations(co);
        fb.setPathList(extractPathList(file));

        digest.reset();

        try {
            RodsObjStat_PI stat = io.stat(file);
            LOG.trace("Processing file: " + file);
            statebean.setStateAndReset(State.OPENING_FILE);
            statebean.setRead(0);
            statebean.setTotalSize(stat.getObjSize());
            statebean.setFile(file);


            statebean.setState(State.THROTTLE_WAIT);
            QueryThrottle.waitToRun();
            statebean.setState(State.READING);
            statebean.updateLastChange();

//            long fileSize = 0;
            IrodsProxyInputStream iis = new IrodsProxyInputStream(file, co.getConnection());
            ThrottledInputStream tis =
                    new ThrottledInputStream(iis, QueryThrottle.getMaxBps(), lastDelay);
//            dis = new DigestInputStream(tis, digest);
//            int read = 0;
//            while ((read = dis.read(buffer)) >= 0 && !cancel) {
//                fileSize += read;
//                statebean.updateLastChange();
//                statebean.setRead(fileSize);
//            }

            reader.setListener(new StateBeanDigestListener(statebean));
            long fileSize = reader.readStream(tis);
            lastDelay = tis.getSleepTime();

            byte[] hashValue = digest.digest();
//            dis.close();
            tis.close();
            fb.setHash(HashValue.asHexString(hashValue));
            fb.setFileSize(fileSize);

        } catch (IOException ie) {
            LOG.error("Error reading file: " + file, ie);
            fb.setError(true);
            fb.setErrorMessage(Strings.exceptionAsString(ie));
        } finally {
            IO.release(dis);
            statebean.setStateAndReset(State.IDLE);
            if (cancel) {
                return null;
            } else {
                return fb;
            }
        }
    }
}
