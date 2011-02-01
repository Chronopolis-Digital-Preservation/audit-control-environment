package edu.umiacs.ace.driver2;

import edu.umiacs.ace.driver.FileBean;
import java.security.MessageDigest;

/**
 * Core driver for reading files and returning their current state
 *
 * @author toaster
 */
public abstract class FileReader<T> {

    private byte[] buffer;
    private MessageDigest digest;

    public FileReader( int bufferSize ) {
        buffer = new byte[bufferSize];
    }

    void setDigest( MessageDigest digest ) {
        this.digest = digest;
    }

    /**
     * Implementing classes should use this digest when calculating the digest
     * for a filebean. Please note, reset is called on the digest prior to returning
     * @return digest to use for filebean
     */
    protected MessageDigest getDigest() {
        digest.reset();
        return digest;
    }


    /**
     * implementing classes may use this buffer rather than tracking and creating their
     * own.
     * @return
     */
    protected byte[] getBuffer() {
        return buffer;
    }

    public abstract FileBean readFile( T nextFile );
}
