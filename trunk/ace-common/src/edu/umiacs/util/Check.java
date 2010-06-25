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
 * Standard checks for method parameters.
 * 
 * <p>
 * This class provides commonly used checks for method parameters.  All 
 * methods throw an {@code IllegalArgumentException} if the assertion is false.  
 * </p>
 * 
 * @version {@code $Revision$ $Date$}
 * 
 ******************************************************************************/
public final class Check
{
    private Check()
    {
    }

    /**
     * Checks that a parameter value is not null.
     * 
     * @param name parameter name that is being checked. If an exception is
     * thrown, this name will be included in the exception message. 
     * @param object object to check for null. 
     * @return {@code object} if not null.
     * @throws IllegalArgumentException if {@code object} is null.
     */
    public static <T> T notNull(String name, T object)
    {
        if ( object == null )
        {
            throw new IllegalArgumentException(name + ": null");
        }
        return object;
    }

    /**
     * Checks that a string value is not null and is not empty.
     * 
     * <p>
     * The string value is empty if {@code Strings.isEmpty()} returns true. 
     * </p>
     * 
     * @see edu.umiacs.util.Strings#isEmpty
     * @param name parameter name that is being checked. If an exception is
     * thrown, this name will be included in the exception message. 
     * @param string string value to check for emptiness. 
     * @return the string value if not empty. 
     * @throws IllegalArgumentException is {@code string} is null or empty. 
     */
    public static String notEmpty(String name, String string)
    {
        if ( Strings.isEmpty(string) )
        {
            throw new IllegalArgumentException(name + ": empty");
        }
        return string;
    }

    /**
     * Checks that a boolean expression is true.
     * 
     * @param condition textual representation of the condition being checked. 
     * This string is used in the exception message if this assertion fails.
     * @param expression the value of the boolean expression to check.
     * @throws IllegalArgumentException if {@code expression} is false. 
     */
    public static void isTrue(String condition, boolean expression)
    {
        if ( expression == false )
        {
            throw new IllegalArgumentException(condition + ": false");
        }
    }       
    
    /**
     * Checks that a boolean expression is false.
     * 
     * @param condition textual representatino of the conding being checked.
     * This string is used in the exception message if this assertion fails.
     * @param expression the value of the boolean expression to check. 
     * @throws IllegalArgumentException if {@code expression} is true.
     */
    public static void isFalse(String condition, boolean expression)
    {
        if ( expression == true )
        {
            throw new IllegalArgumentException(condition + ": true");
        }
    }
    
    /**
     * Checks that a numeric value is not a negative number. 
     * 
     * @param name parameter name that is being checked. If an exception is
     * thrown, this name will be included in the exception message. 
     * @param value the numeric value to check. 
     * @return the value checked.
     * @throws IllegalArgumentException if value is null or if value is less
     * than zero. 
     */
    public static <T extends Number> T notNegative(String name, T value)
    {
        if ( value == null || value.doubleValue() < 0 )
        {
            throw new IllegalArgumentException(name + " < 0: " + value);
        }
        return value;
    }
    
    /**
     * Checks that a numeric value is positive. 
     * 
     * @param name parameter name that is being checked. If an exception is
     * thrown, thsi name will be included in the exception message. 
     * @param value the numeric value to check. 
     * @return the checked value.
     * @throws IllegalArgumentExceptino if the value is null or if the value
     * is less than or equal to zero. 
     */
    public static <T extends Number> T isPositive(String name, T value)
    {
        if ( value == null || value.doubleValue() <= 0 )
        {
            throw new IllegalArgumentException(name + " <= 0: " + value);
        }
        return value;
    }
}
