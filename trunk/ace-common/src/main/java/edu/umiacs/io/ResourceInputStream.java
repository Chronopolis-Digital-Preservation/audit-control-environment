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

package edu.umiacs.io;

import edu.umiacs.util.Check;
import java.io.FilterInputStream;
import java.io.InputStream;

/*******************************************************************************
 * 
 * Input stream for reading files embedded on the classpath. 
 * 
 * @version {@code $Revision$ $Date$}
 * 
 ******************************************************************************/

public class ResourceInputStream extends FilterInputStream
{
    /**
     * Creates a new input stream from a resource using the ClassLoader of the 
     * provided class. 
     *  
     * @param class0 the ClassLoader of this class to use in finding the 
     * resource. 
     * @param resourceName name of the resource file to read.
     * @throws IllegalArgumentException if {@code class0} is null, or if
     * {@code resourceName} is empty. 
     * @throws ResourceNotFoundException if the file specified in the 
     * {@code resourceName} cannot be found. 
     */
    public ResourceInputStream(Class<?> class0, String resourceName)
    {
        super(openResourceStream(class0, resourceName));
    }
    
    /**
     * Creats a new input stream from a resource using the ClassLoader of the
     * provided object. 
     * 
     * <p>
     * This is the same as constructing with:
     * <pre>
     * new ResourceInputStream(object.getClass(), resourceName)
     * </pre>
     * </p>
     * 
     * @param object the ClassLoader ofthis object to use in finding the
     * resource. 
     * @param resourceName name of the resource file to read.
     * @throws IllegalArgumentException if {@code class0} is null, or if
     * {@code resourceName} is empty. 
     * @throws ResourceNotFoundException if the file specified in the 
     * {@code resourceName} cannot be found.  
     */
    public ResourceInputStream(Object object, String resourceName)
    {
        this(Check.notNull("object", object).getClass(), resourceName);
    }
    
    private static InputStream openResourceStream(Class<?> class0, 
            String resourceName)
    {
        Check.notNull("class0", class0);
        Check.notEmpty("resourceName", resourceName);
        
        InputStream inputStream = class0.getResourceAsStream(resourceName);
        if ( inputStream == null )
        {
            throw new ResourceNotFoundException(resourceName);
        }
        return inputStream;
    }
}
