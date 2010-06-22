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
// $Id: StringListBuilder.java 3192 2010-06-22 16:54:09Z toaster $

package edu.umiacs.util;

/*******************************************************************************
 * 
 * Constructs a string of delimited values. 
 * 
 * <p>
 * If a delimiter isn't specified, a comma followed by a space is used. The 
 * string is constructed in a {@code StringBuilder} as values are appended. 
 * Changing the delimiter only effects subsequent appends. The {@code append}
 * and {@code setDelimiter} methods return this object for method call chaining.
 * Typical use: 
 * <pre>
 *     new StringListBuilder() 
 *             .setDelimiter(":")
 *             .append("foo")
 *             .append("bar")
 *             .append("baz")
 *             .toString();
 * </pre>
 * creates the string {@code foo:bar:baz}
 * </p>
 * 
 * @version {@code $Revision: 3192 $ $Date$}
 * 
 ******************************************************************************/
public final class StringListBuilder 
{
    /**
     * Default delimiter used if none is specified. 
     */
    public static final String DEFAULT_DELIMITER = ", ";
    
    // Builder used to construct the string
    private StringBuilder sb = new StringBuilder();
    
    // Delimiter used between values
    private String delimiter = DEFAULT_DELIMITER;
    
    /**
     * Empty default constructor.
     */
    public StringListBuilder()
    {
    }
    
    /**
     * Creates a builder and appends the given values.
     * 
     * <p>
     * An array can be quickly printed out with the following:
     * <pre>
     * System.out.println(new StringListBuilder(values))
     * </pre>

     * Using this constructor is the same as using the default constructor
     * followed by a call to {@code append(values)}
     * </p>
     * 
     * @see #append(Object[])
     * @param values object values to append. 
     */
    public StringListBuilder(Object[] values)
    {
        append(values);
    }
    
    /**
     * Creates a builder and appends the given values.
     * 
     * <p>
     * An Iterable can be quickly printed out with the following:
     * <pre>
     * System.out.println(new StringListBuilder(values))
     * </pre>
     * 
     * Using this constructor is the same as using the default constructor
     * follwed by a call to {@code append(values)}
     * 
     * @see #append(Iterable)
     * @param values object values to append. 
     */
    public StringListBuilder(Iterable<?> values)
    {
        append(values);
    }
    
    /**
     * Gets the current value delimiter.
     * 
     * @return the value set by calling {@code setDelimiter} or {@code ", "}
     * if the delimiter has not been set. 
     */
    public String getDelimiter()
    {
        return this.delimiter;
    }

    /**
     * Sets the delimiter used to separate values in the string. 
     * 
     * @param delimiter the string to use as a delimiter. If null, the empty
     * string is used. 
     * @return this object. 
     */
    public StringListBuilder setDelimiter(String delimiter)
    {        
        this.delimiter = ( delimiter == null ) ? "" : delimiter;
        return this;
    }

    /**
     * Appends the object's value to the string list. 
     * 
     * @param value the object's value to append using {@code String.valueOf}
     * @return this object. 
     */
    public StringListBuilder append(Object value)
    {
        if ( sb.length() > 0 )
        {
            sb.append(delimiter);
        }
        sb.append(String.valueOf(value));
        
        return this;
    }
    
    /**
     * Appends all items returned by the iterator to the string list. 
     *
     * @param iterable the values to be appended to the string list. If null, 
     * the string {@code null} will be appended instead. 
     * @return this object. 
     */
    public StringListBuilder append(Iterable<?> iterable)
    {
        if ( iterable == null )
        {
            append("null");
        }
        else 
        {
            for ( Object value: iterable )
            {
                append(value);
            }
        }
        return this;
    }
    
    /**
     * Appends all items in an array to the string list.
     * 
     * @param values the value sto be appended to the string list. If null, 
     * the string {@code null} will be appended instead. 
     * @return this object. 
     */
    public StringListBuilder append(Object[] values)
    {
        if ( values == null )
        {
            append("null");
        }
        else
        {
            for ( Object value: values )
            {
                append(value);
            }
        }
        return this;
    }
    
    /**
     * Gets the constructed string list.
     * 
     * @return the current string of values. 
     */
    @Override
    public String toString()
    {
        return sb.toString();
    }
}
