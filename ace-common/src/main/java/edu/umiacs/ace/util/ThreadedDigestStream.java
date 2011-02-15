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
 * This class parallelizes file reading and digesting. It maintains a set of
 * byte buffers which are transfered between free and fille queues. A dedicated
 * digest thread will read all filled blocks, update a digest, then return the
 * block to the free queue.
 *
 * All users of this class must call shutdown or abort when no more calls to
 * readStream are expected or the digest thread will not terminate
 * 
 * @author toaster
 */
public final class ThreadedDigestStream {

    private LinkedBlockingQueue<ByteWrapper> filledBlocks = new LinkedBlockingQueue<ByteWrapper>();
    private LinkedBlockingQueue<ByteWrapper> emptyBlocks = new LinkedBlockingQueue<ByteWrapper>();
    private int processBlocks = 5;
    private boolean running = true;
    private MessageDigest md;
    private IOException delayedException = null;
    private ThreadedDigestStreamListener listener;
    private Thread digestThread;
    private boolean digestDone = false;

    /**
     * Create a new threaded digest stream. The queues will be initialized
     * with 5 buffers of the supplied block size.
     *
     * @param md digest to update
     * @param blockSize block size to use in queues
     */
    public ThreadedDigestStream(MessageDigest md, int blockSize) {
        this.md = md;

        for (int i = 0; i < processBlocks; i++) {
            ByteWrapper block = new ByteWrapper(blockSize);
            if (!emptyBlocks.offer(block)) {
                throw new RuntimeException("Could not add empty block to queue");
            }
        }
    }

    /**
     * stop this queue. Any open files will be given a chance to finish. If the
     * this stream is currently idle, the digest thread will exit, otherwise it
     * will finish the current open file.
     *
     * After a shutdown call, future calls to readstream will error.
     *
     */
    public void shutdown() {
        running = false;
        // if a digest is not running, toss a shutdown message into queue
        if (digestDone) {
            ByteWrapper closeBlock = new ByteWrapper(0);
            closeBlock.len = -1;
            filledBlocks.offer(closeBlock);
        }
    }

    /**
     * Set a listener to receive notifications of bit processing.
     * 
     * @param listener
     */
    public void setListener(ThreadedDigestStreamListener listener) {
        this.listener = listener;
    }

    /**
     * Read the given inputstream and update the stored digest. If a listener
     * has been set, it will be notified when bytes are read. Also, if an
     * exception is thrown, the digest thread will not be shutdown and additional
     * files can still be processed.
     * 
     * @param is inputstream to read
     * @return total bytes read
     * @throws IOException if there was an issue reading the file.
     */
    public long readStream(InputStream is) throws IOException {
        checkThread();

        if (!filledBlocks.isEmpty()) {
            throw new IllegalStateException("Read in progress");
        }
        if (!running) {
            throw new IllegalStateException("Stream shutdown");
        }

        digestDone = false;
        int read = 0;
        delayedException = null;

        long totalBytes = 0;

        try {
            ByteWrapper bw = emptyBlocks.take();
            try {
                while (running && (read = is.read(bw.block)) != -1) {
                    if (listener != null) {
                        listener.bytesRead(read);
                    }
                    bw.len = read;
                    filledBlocks.put(bw);
                    totalBytes += read;
                    bw = emptyBlocks.take();
                }
            } finally {
                emptyBlocks.put(bw);
            }

        } catch (InterruptedException e) {
            throw new IOException(e);
        } finally {
            ByteWrapper closeBlock = new ByteWrapper(0);
            closeBlock.len = -1;
            if (!filledBlocks.offer(closeBlock)) {
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

    /**
     * Abort processing the current file.
     *
     * @return if abort was successful
     *
     */
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

    private void checkThread() {
        if (digestThread == null) {
            DigestRunnable dr = new DigestRunnable();
            digestThread = new Thread(dr);
            digestThread.setName("Digest Thread");
            digestThread.start();
        }
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
                    }
                }
            } catch (InterruptedException e) {
                if (delayedException == null) {
                    delayedException = new IOException(e);
                }
            } finally {
                synchronized (ThreadedDigestStream.this) {
                    running = false;
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
