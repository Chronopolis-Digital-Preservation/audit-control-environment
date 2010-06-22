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
// $Id: ToStringBuilder.java 3192 2010-06-22 16:54:09Z toaster $

package edu.umiacs.util;

/*******************************************************************************
 * 
 * Utility for creating {@code toString} values useful for debugging.
 * 
 * <p>
 * Typical use is to pass in an instance of the object and all the fields as
 * in the following:
 * <pre>
 * package a.b.c;
 * 
 * class Clazz
 * {
 *    String foo = "text";
 *    int bar = 42;
 *    Object[] baz = new Object[] { "something", 21, 'c' }
 * 
 *    public String toString()
 *    {
 *        new ToStringBuilder(this)
 *                .append("foo", foo)
 *                .append("bar", bar)
 *                .append("baz", baz)
 *                .toString();
 *    }
 * }
 * </pre>
 * 
 * {@code toString} will return the following string:
 * <pre>
 * a.b.c.Clazz: foo="text", bar=42, baz=["something", 21, 'c']
 * </pre>
 * 
 * Field values are formatted using the {@code StandardTypeFormatter}
 * </p>
 * 
 * @see TypeFormatter
 * @version {@code $Revision: 3192 $ $Date$}
 * 
 ******************************************************************************/
public final class ToStringBuilder 
{
    /** 
     * If the constructor with an object instance is used, this will contain
     * the name of the object's class. 
     */ 
    private String prefix = "";
    
    /** 
     * Field names and values are separated by commas
     */
    private StringListBuilder sb = new StringListBuilder();
    
    /**
     * Make the values pretty. 
     */ 
    private static final TypeFormatter formatter = new TypeFormatter();
    
    /**
     * Creates a ToStringBuilder that doesn't add the class name of the object.
     */
    public ToStringBuilder()
    {}
    
    /**
     * Creates a ToStringBuilder which contains the name of an object's class. 
     * 
     * @param instance the resulting toString is prepended with the class name
     * of this object. If null, this is the same as calling the empty 
     * constructor. 
     */
    public ToStringBuilder(Object instance)
    {
        if ( instance != null )
        {
            this.prefix = instance.getClass().getName() + ": ";
        }
    }
    
    /**
     * Appends the name and value of a field. 
     * 
     * @param name name of the field.
     * @param value value of the field. 
     * @return this object. 
     */
    public ToStringBuilder append(String name, Object value)
    {
        sb.append(String.valueOf(name) + "=" + formatter.format(value));
        return this;
    }
    
    /**
     * Appends the name and value of an array field. 
     * 
     * @param name name of the field.
     * @param values values of the field. 
     * @return this object. 
     */
    public ToStringBuilder append(String name, Object[] values)
    { 
        sb.append(String.valueOf(name) + "=" + formatter.format(values));        
        return this;
    }
      
    /**
     * Appends the name and values of an Iterable. 
     * 
     * @param name name of the field. 
     * @param values values of the field. 
     * @return this object. 
     */
    public ToStringBuilder append(String name, Iterable<?> values)
    {
        sb.append(String.valueOf(name) + "=" + formatter.format(values));
        return this;
    }
        
    /**
     * Gets the constructed {@code toString} value. 
     * 
     * @return the constructed string. 
     */
    @Override
    public String toString()
    {
        return prefix + sb.toString();
    }
}
