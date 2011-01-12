package edu.umiacs.ace.driver2;

import edu.umiacs.ace.driver.FileBean;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.log4j.Logger;

/**
 *
 * @author toaster
 */
public class FileReaderThread implements Runnable {

    private FileReader reader;
    private LinkedBlockingQueue queue;
    private boolean cancel;
    private static final Logger LOG = Logger.getLogger(FileReaderThread.class);
    private Queue<FileBean> resultQueue;

    public FileReaderThread( FileReader reader, LinkedBlockingQueue fileQueue,
            Queue<FileBean> resultQueue ) {
        this.reader = reader;
        this.queue = fileQueue;
        this.resultQueue = resultQueue;
    }

    public void cancel() {
        cancel = true;
    }

    @Override
    public void run() {

        while ( !cancel ) {

            try {
                resultQueue.add(reader.readFile(queue.take()));

            } catch ( InterruptedException e ) {
                LOG.info("Interrupted waiting for next file, cancel: " + cancel);
            } catch ( Exception e ) {
                LOG.error("Unknown error in read thread ",e);
                // log unknown from reader
            }

        }
        LOG.info("Thread exiting on cancel");
    }
}
