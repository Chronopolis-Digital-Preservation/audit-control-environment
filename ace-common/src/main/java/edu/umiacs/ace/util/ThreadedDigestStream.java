/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.umiacs.ace.util;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class parallelizes file reading and digesting.
 * 
 * @author toaster
 */
public class ThreadedDigestStream {

    private LinkedBlockingQueue<ByteWrapper> filledBlocks = new LinkedBlockingQueue<ByteWrapper>();
    private LinkedBlockingQueue<ByteWrapper> emptyBlocks = new LinkedBlockingQueue<ByteWrapper>();
    private int processBlocks = 5;
    private boolean running = true;
    private MessageDigest md;
    private IOException delayedException = null;
    private ThreadedDigestStreamListener listener;
    private Thread digestThread;
    private boolean digestDone = false;

    public ThreadedDigestStream(MessageDigest md, int blockSize) {
        this.md = md;

        for (int i = 0; i < processBlocks; i++) {
            ByteWrapper block = new ByteWrapper(blockSize);
            if (!emptyBlocks.offer(block)) {
                throw new RuntimeException("Could not add empty block to queue");
            }
        }
    }

    public void shutdown() {
        running = false;
        // if a digest is not running, toss a shutdown message into queue
        if (digestDone) {
            ByteWrapper closeBlock = new ByteWrapper(0);
            closeBlock.len = -1;
            filledBlocks.offer(closeBlock);
        }
    }

    public void setListener(ThreadedDigestStreamListener listener) {
        this.listener = listener;
    }

    private void checkThread() {
        if (digestThread == null) {
            DigestRunnable dr = new DigestRunnable();
            digestThread = new Thread(dr);
            digestThread.setName("Digest Thread");
            digestThread.start();
        }
    }

    public long readStream(InputStream is) throws IOException {
        checkThread();

        if (!filledBlocks.isEmpty()) {
            throw new IllegalStateException("Read in progress");
        }
        digestDone = false;
        int read = 0;
        delayedException = null;

        long totalBytes = 0;

        try {
            ByteWrapper bw = emptyBlocks.take();
            while (running && (read = is.read(bw.block)) != -1) {
                if (listener != null) {
                    listener.bytesRead(read);
                }
                bw.len = read;
//                System.out.println("len " + bw.len + bw.block.length);
                filledBlocks.put(bw);
                totalBytes += read;
                bw = emptyBlocks.take();
            }
            emptyBlocks.put(bw);
        } catch (InterruptedException e) {
            throw new IOException(e);
        } finally {
            ByteWrapper closeBlock = new ByteWrapper(0);
            closeBlock.len = -1;
            if (!filledBlocks.offer(closeBlock)) {
//                digestThread.stop(); // bad, but there is no other way this is stopping if we're here
                digestThread = null;
                throw new RuntimeException("Could not queue close block");
            }
        }

        synchronized (this) {
            while (!digestDone) {
                try {
                    this.wait();
                } catch (InterruptedException ex) {
                    delayedException = new IOException(ex);
                }
            }
        }

        if (delayedException != null) {
            throw delayedException;
        } else {
            return totalBytes;
        }

    }

    public boolean abort() {
        ByteWrapper closeBlock = new ByteWrapper(0);
        closeBlock.len = -1;
        running = false;
        if (filledBlocks.offer(closeBlock)) {
            delayedException = new IOException("Read aborted");
            return true;
        }
        return false;
    }

    private class DigestRunnable implements Runnable {

        public void run() {

            try {
                while (running) {
                    ByteWrapper bw = filledBlocks.take();
                    if (bw.len == -1) {
                        digestDone = true;
                        synchronized (ThreadedDigestStream.this) {
                            ThreadedDigestStream.this.notifyAll();
                        }
                    } else {
                        
                        md.update(bw.block, 0, bw.len);
                        emptyBlocks.put(bw);
//                        System.out.println("updating block, resetting" + emptyBlocks.size());
                    }
                }
            } catch (InterruptedException e) {
                if (delayedException == null) {
                    delayedException = new IOException(e);
                }
            } finally {
                synchronized (ThreadedDigestStream.this) {
                    running = false;
//                    System.out.println("Digest thread cleaning");
                    filledBlocks.clear();
                    emptyBlocks.clear();

                }
            }
        }
    }

    private class ByteWrapper {

        byte[] block;
        int len = 0;

        public ByteWrapper(int bSize) {
            this.block = new byte[bSize];
        }
    }
}
