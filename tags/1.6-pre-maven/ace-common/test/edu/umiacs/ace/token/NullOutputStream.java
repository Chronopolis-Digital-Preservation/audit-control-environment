package edu.umiacs.ace.token;

import java.io.IOException;
import java.io.OutputStream;


/**
 *
 * @author toaster
 */
public class NullOutputStream extends OutputStream
{

    @Override
    public void write( byte[] b ) throws IOException {

    }

    @Override
    public void write( byte[] b, int off, int len ) throws IOException {
    }

    @Override
    public void write( int b ) throws IOException {
    }

}
