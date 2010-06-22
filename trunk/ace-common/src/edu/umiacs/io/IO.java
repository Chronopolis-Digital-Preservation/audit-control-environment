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
// $Id: IO.java 3192 2010-06-22 16:54:09Z toaster $

package edu.umiacs.io;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;

/*******************************************************************************
 * 
 * Standard IO utilties.
 * 
 * @version {@code $Revision: 3192 $ $Date$}
 * 
 ******************************************************************************/
public final class IO
{
    private IO()
    {
    }

    /**
     * Closes a possibly null IO resource without throwing an exception. 
     * This should only be used when an exception has already been thrown 
     * which renders the resource useless, or when an exception on close
     * is otherwise meaningless (on a close when reading input after all 
     * input has been read). Be careful--output may be buffered! Example:
     * 
     * <pre>
     * OutputStream os = null; 
     * try
     * {
     *     os = new FileOutputStream(...);
     *     // do work
     *     os.close();
     * }
     * catch ( IOException ie )
     * {
     *     IO.release(os);
     *     // handle exception
     * }
     * </pre>
     *  
     * @param closeable resource to close; if null, this method does nothing.
     */
    public static void release(Closeable closeable)
    {
        if ( closeable != null )
        {
            try
            {
                closeable.close();
            }
            catch ( IOException ie )
            {
            }
        }
    }
    
    /**
     * Closes a possibly null socket without throwing an exception. 
     *  
     * @see #release(java.io.Closeable)
     * @param socket socket to close; if null, this method does nothing.
     */    
    public static void release(Socket socket)
    {
        if ( socket != null )
        {
            try
            {
                socket.close();
            }
            catch ( IOException ie )
            {
            }
        }
    }
}
