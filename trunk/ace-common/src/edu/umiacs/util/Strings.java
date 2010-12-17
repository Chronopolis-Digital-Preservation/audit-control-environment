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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

/*******************************************************************************
 * 
 * General string utilities.
 * 
 * @version {@code $Revision$ $Date$}
 * 
 ******************************************************************************/
public final class Strings
{
    private Strings()
    {
    }
    /** Holder of all illegal XML chars. **/
    private static byte[] ILLEGAL_XML_1_0_CHARS;

    static
    {
        final StringBuffer buff = new StringBuffer();
        for ( char i = 0x0000; i < 0x0020; i++ )
        {
            if ( i != 0x0009 && i != 0x000A && i != 0x000D )
            {
                buff.append(i);
            }
        }
        ILLEGAL_XML_1_0_CHARS = buff.toString().getBytes();
        Arrays.sort(ILLEGAL_XML_1_0_CHARS);
    }

    /** 
     * Cleans a given String, so that it can be safely used in XML. 
     * All Invalid characters, will be replaced with the given replace character. 
     * Valid XML characters are described here: 
     * {@link "http://www.w3c.org/TR/2000/REC-xml-20001006#dt-character"} 
     * 
     * from http://markmail.org/message/7ez6nb3i5e7get7l
     * 
     * @param pString the string to clean 
     * @param pReplacement the char to use to replace the invalid characters 
     * @return the string, cleaned for XML. 
     */
    public static String cleanStringForXml(String pString, char pReplacement)
    {
        
        if (pString == null)
            return null;
        
        final byte[] bytes = pString.getBytes();
        for ( int i = 0; i < bytes.length; i++ )
        {
            byte aByte = bytes[i];
            if ( Arrays.binarySearch(ILLEGAL_XML_1_0_CHARS, aByte) >= 0 )
            {
                bytes[i] = (byte) pReplacement;
            }
        }
        return new String(bytes);
    }
    
    /**
     * Determines if a string is a valid integer.
     *
     * @param val string value to check.
     * @return true if {@code val} can successfully be parsed into an integer.
     */
    public static boolean isValidLong(Object val)
    {
        if ( val == null )
            return false;
        
        try
        {
            long i = Long.parseLong(val.toString());
            return true;
        }
        catch ( NumberFormatException e )
        {
            return false;
        }
    }

        /**
     * Determines if a string is a valid integer.
     *
     * @param val string value to check.
     * @return true if {@code val} can successfully be parsed into an integer.
     */
    public static boolean isValidInt(Object val)
    {
        if ( val == null )
            return false;
        
        try
        {
            int i = Integer.parseInt(val.toString());
            return true;
        }
        catch ( NumberFormatException e )
        {
            return false;
        }
    }

     /**
     * Return exception and contained stack trace as string
     * 
     * @param e exception to process
     */
    public static String exceptionAsString(Throwable e)
    {
        if (e == null)
            return null;
        
        try
        {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            return sw.toString();
        }
        catch(Exception e2)
        {
            return "bad stack2string: " + e2.getMessage();
        }
    }

    /**
     * Determines if an object's string value is empty. 
     * 
     * @param value the value to check. 
     * @return true if value is null or the trimmed length returned by 
     * {@code toString} is zero, otherwise returns false. 
     */
    public static boolean isEmpty(Object value)
    {
        if ( value != null )
        {
            String sv = value.toString();
            if ( sv != null )
            {
                return sv.trim().length() == 0;
            }
        }
        return true;
    }
    
    public static String repeat(char value, int times)
    {
        Check.notNegative("times", times);
        
        if ( times == 0 )
            return "";
        char[] c = new char[times];
        for ( int i = 0; i < times; i++ )
        {
            c[i] = value;
        }
        return new String(c);
    }

    public static <T> String join(char delimiter, T ... items)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(items[0]);
        for (int i = 1; i < items.length; i++)
        {
            sb.append(delimiter);
            sb.append(items[i]);
        }
        return sb.toString();
    }

    /**
     * Returns a new string that is a substring of another string. Use this
     * method when it is more natural to get a substring based on an offset
     * and a length--otherwise, use the standard String.substring method. 
     * 
     * @param string the string to create a substring from. 
     * @param offset the starting index in the string to create the substring
     * from. If the offset equals the length of the string, the empty string
     * will be returned. 
     * @param length the length of the new substring. If the length from the
     * offset exceeds the length of the string, the entire string starting
     * from the offset is returned. 
     * @return the substring
     * @throws IllegalArgumentException if string is null, the offset is 
     * less than zero or is greater than the string length, or if the length
     * is less than zero. 
     */
//    public static String mid(String string, int offset, int length)
//    {
//        Check.notNull("string", string);
//        Check.notNegative("offset", offset);
//        Check.notNegative("length", length);
//        if ( offset > string.length() )
//        {
//            throw new IllegalArgumentException("offset > string.length(): " + 
//                    offset);
//        }
//        if ( length > string.length() - offset )
//        {
//            length = string.length() - offset;
//        }
//        return string.substring(offset, offset + length);
//    }

}
