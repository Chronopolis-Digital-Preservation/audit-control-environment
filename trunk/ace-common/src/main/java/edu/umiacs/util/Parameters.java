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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class Parameters 
{
    private Map<String,String> data = new HashMap<String,String>();
    
    public Parameters()
    {}
    
    public boolean exists(String name)
    {
        return data.keySet().contains(name);
    }
    
    public boolean hasValue(String name)
    {
        return !Strings.isEmpty(data.get(name));
    }
    
    public String getString(String name, String defaultValue)
    {
        Check.notEmpty("name", name);
        
        String svalue = data.get(name);
        return ( svalue == null ) ? defaultValue: svalue;
    }
    
    public String getString(String name)
    {
        String svalue = getString(name, null);
        if ( svalue == null )
        {
            throw new ParameterException(name);
        }
        return svalue;
    }
    
    public boolean getBoolean(String name, boolean defaultValue)
    {
        String svalue = getString(name, null);
        return ( svalue == null ) ? defaultValue : parseBoolean(name, svalue);
    }
    
    public boolean getBoolean(String name)
    {
        return parseBoolean(name, getString(name));
    }
    
    public int getInt(String name, int defaultValue)
    {
        String svalue = getString(name, null);
        return ( svalue == null ) ? defaultValue : parseInt(name, svalue); 
    }
    
    public int getInt(String name)
    {
        return parseInt(name, getString(name));
    }
    
    public long getLong(String name, long defaultValue)
    {
        String svalue = getString(name, null);
        return ( svalue == null ) ? defaultValue : parseLong(name, svalue); 
    }
    
    public long getLong(String name)
    {
        return parseLong(name, getString(name));
    }
    
    public double getDouble(String name, double defaultValue)
    {
        String svalue = getString(name, null);
        return ( svalue == null ) ? defaultValue : parseDouble(name, svalue);
    }
                
    public double getDouble(String name)
    {
        return parseDouble(name, getString(name));
    }
        
    public void put(String name, Object value)
    {
        Check.notEmpty("name", name);
        
        data.put(name, (value == null ? null : value.toString()));
    }
       
    public Properties cloneAsProperties()
    {
        Properties prop = new Properties();
        for (String key : data.keySet())
        {   
            prop.setProperty(key, data.get(key));
        }
        return prop;
    }
    
    public Set<String> keySet()
    {
        return data.keySet();
        
    }
    private boolean parseBoolean(String name, String value)
    {
        if ( value.equals("true") )
        {
            return true;
        }
        else if ( value.equals("false") )
        {
            return false;
        }
        throw new ParameterException(name, value);
    }
    
    private int parseInt(String name, String value)
    {
        try
        {
            return Integer.parseInt(value);
        }
        catch ( NumberFormatException nfe )
        {
            throw new ParameterException(name, value);
        }
    }
    
    private long parseLong(String name, String value)
    {
        try
        {
            return Long.parseLong(value);
        }
        catch ( NumberFormatException nfe )
        {
            throw new ParameterException(name, value);
        }
    }
    
    private double parseDouble(String name, String value)
    {
        try
        {
            return Double.parseDouble(value);
        }
        catch ( NumberFormatException nfe )
        {
            throw new ParameterException(name, value);
        }
    }
             
}
