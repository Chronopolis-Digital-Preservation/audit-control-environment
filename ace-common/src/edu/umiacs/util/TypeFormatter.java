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

package edu.umiacs.util;

/*******************************************************************************
 * 
 * Formats a value based on its type.
 * 
 * <p>
 * Values that match the following are formatted as follows:
 * <table border="1">
 * <tr><th>Type</th><th>Input</th><th>Output</th></tr>
 * <tr><td>Number</td><td>42</td><td>42</td></tr>
 * <tr><td>Boolean</td><td>true</td><td>true</td></tr>
 * <tr><td>Character</td><td>x</td><td>'x'</td></tr>
 * <tr><td>CharSequence</td><td>Foo</td><td>"Foo"</td></tr>
 * <tr><td>Enum</td><td>AnEnum.VALUE</td><td>VALUE</td></tr>
 * </table>
 * </p>
 * 
 * <p>
 * In addition:
 * <ul>
 * <li>If the value is null, the returned string will be the text {@code null}.
 * <li>If the value is an array, the returned string will be the formatted
 * values of each item, separated by commas, and enclosed by brackets. 
 * For example: {@code [2, "Foo", 'x']}
 * <li>If the value is an {@code Iterable}, the returned string will be the
 * formatted values of each item, separated by commas, and enclosed by 
 * parenthesis. For example: {@code (2, "Foo", 'x')}   
 * <li>If the value does not match any of the above, the returned string
 * will the the {@code toString} value enclosed by braces.
 * </ul>
 * </p>
 * 
 * @version {@code $Revision$ $Date$}
 * 
 ******************************************************************************/
public class TypeFormatter 
{
    /**
     * Default empty constructor.
     */
    public TypeFormatter()
    {}
    
    /**
     * Formats a value according to the class description above. 
     * 
     * @param value object's value to format.
     * @return the formatted value. 
     */
    public String format(Object value)
    {  
        if ( value == null )
        {
            return "null";
        }
        else if ( value instanceof Number )
        {
            return value.toString();
        }
        else if ( value instanceof Boolean )
        {
            return value.toString();
        }
        else if ( value instanceof Character )
        {
            return "'" + value.toString() + "'";
        }
        else if ( value instanceof CharSequence )
        {
            return "\"" + value + "\"";
        }
        else if ( value instanceof Enum )    
        {
            return value.toString();
        }
        else if ( value instanceof Iterable )
        {
            Iterable<?> iter = (Iterable<?>)value;
            StringListBuilder slb = new StringListBuilder();
            for ( Object object: iter )
            {
                slb.append(format(object));
            }
            return "(" + slb + ")";
        }
        return "{" + value + "}";
    }
    
    /**
     * Formats the values of an array according to the class description 
     * above. 
     * 
     * @param values array values to format.
     * @return the formatted values. 
     */
    public String format(Object[] values)
    {
        if ( values == null )
        {
            return "null";
        }
        StringListBuilder slb = new StringListBuilder();
        for ( Object object: values )
        {
            slb.append(format(object));
        }
        return "[" + slb + "]";
    }
}
