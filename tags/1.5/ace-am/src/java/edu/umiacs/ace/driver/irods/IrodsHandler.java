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

package edu.umiacs.ace.driver.irods;

import edu.umiacs.ace.driver.FileBean;
import edu.umiacs.ace.driver.filter.PathFilter;
import edu.umiacs.ace.util.HashValue;
import edu.umiacs.irods.operation.BulkFileHandler;
import edu.umiacs.srb.util.StringUtil;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.log4j.Logger;

/**
 *
 * @author toaster
 */
public class IrodsHandler implements BulkFileHandler {

    private LinkedBlockingQueue<FileBean> readyList;
    private PathFilter filter;
    private MessageDigest digest;
//    private boolean ignore = false;
    private String workingFile;
    private long fileSize;
    private static final Logger LOG = Logger.getLogger(IrodsHandler.class);

    public IrodsHandler( LinkedBlockingQueue<FileBean> readyList,
            PathFilter filter, String digestAlgorithm ) {
        this.readyList = readyList;
        this.filter = filter;
        try {
            digest = MessageDigest.getInstance(digestAlgorithm);
        } catch ( NoSuchAlgorithmException ex ) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void openFile( String path, long length ) throws IOException {
        LOG.trace("Opening file for digesting: " + path);
        digest.reset();
        fileSize = 0;
        workingFile = path;

    }

    @Override
    public void ioError( IOException ioe ) {
        FileBean fb = new FileBean();
        fb.setError(true);
        fb.setPathList(extractPathList(workingFile));
        fb.setFileSize(fileSize);
        fb.setErrorMessage(StringUtil.exceptionAsString(ioe));
        LOG.trace("Error on file " + workingFile + ioe.getLocalizedMessage());
        readyList.add(fb);
    }

    /**
     * calculate digest, create file bean, add to queue
     */
    @Override
    public void closeFile() {
        byte[] hashValue = digest.digest();

        FileBean fb = new FileBean();
        fb.setError(false);
        fb.setPathList(extractPathList(workingFile));
        fb.setHash(HashValue.asHexString(hashValue));
        fb.setFileSize(fileSize);

        LOG.trace("Closing file " + workingFile + " digest " + fb.getHash());
        readyList.add(fb);
    }

    @Override
    public boolean processItem( String path, boolean isDirectory ) {
        return filter.process(extractPathList(path), isDirectory);
    }

    @Override
    public boolean mkdir( String directory ) {
        return true;
    }

    /**
     * 
     */
    @Override
    public void writeBytes( byte[] bytes, long offset, int length ) {
//        LOG.trace("digesting chunk, offset: " + offset + " length " + length);
        digest.update(bytes, 0, length);
        fileSize += length;
    }

    /**
     * in irods, the path does not have a starting / 
     * @param file
     * @return
     */
    private String[] extractPathList( String file ) {
        String[] tmpPathList = file.split("/");

        if ( tmpPathList.length == 1 ) {
            return tmpPathList;
        }
//        LOG.trace("length: " + tmpPathList.length);
        String pathList[] = new String[tmpPathList.length - 1];

        int i = tmpPathList.length - 3;
        pathList[tmpPathList.length - 2] = "/" + tmpPathList[1];
//        LOG.trace(
//                "idx: " + (tmpPathList.length - 2) + " " + pathList[tmpPathList.length - 2]);

        for ( int j = 2; j < tmpPathList.length; j++ ) {
            pathList[i] = pathList[i + 1] + "/" + tmpPathList[j];
//            LOG.trace("idx: " + i + " " + pathList[i]);
            i--;
        }
        return pathList;
    }
}
