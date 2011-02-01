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
 * Assists in constructing hash code values.
 * 
 * <p>
 * From http://java.sun.com/docs/books/effective/index.html
 * </p>
 * 
 * <p>
 * Typical use:
 * <pre>
 * public int hashCode()
 * {
 *     return new HashCodeBuilder()
 *          .hash(foo)
 *          .hash(bar)
 *          .hash(baz)
 *          .hashCode();
 * }
 * </pre>
 *     
 * <p>
 * The array hash methods call the non-array hash methods on each array 
 * element. 
 * </p>
 * 
 * @version {@code $Revision$ $Date$}
 * 
 ******************************************************************************/
public final class HashCodeBuilder
{
    /**
     * Initial value of the hash code. 
     */
    private static final int INITIAL_PRIME      = 17;
    
    /**
     * Previous hash value is multiplied by this amount and then added to 
     * the supplied value. 
     */
    private static final int PRIME_FACTOR       = 37;
    
    /**
     * Current hash result. 
     */
    private int result = INITIAL_PRIME;
    
    /**
     * Creates a new hash code builder. 
     */
    public HashCodeBuilder()
    {
    }
    
    /**
     * Hashes in the specified value. 
     */ 
    public HashCodeBuilder hash(boolean b)
    {
        update(b ? 0 : 1);
        return this;
    }
    
    /**
     * Hashes in the specified value. 
     */   
    public HashCodeBuilder hash(boolean[] ab)
    {
        if ( ab != null )
        {
            for ( int x = 0; x < ab.length; x++ )
                hash(ab[x]);
        }
        return this;
    }
    
    /**
     * Hashes in the specified value. 
     */     
    public HashCodeBuilder hash(byte b)
    {
        update((int)b);
        return this;
    }
    
    /**
     * Hashes in the specified value. 
     */     
    public HashCodeBuilder hash(byte[] ab)
    {
        if ( ab != null )
        {
            for ( int x = 0; x < ab.length; x++ )
                hash(ab[x]);
        }
        return this;
    }
    
    /**
     * Hashes in the specified value. 
     */     
    public HashCodeBuilder hash(char c)
    {
        update((int)c);
        return this;
    }
    
    /**
     * Hashes in the specified value. 
     */     
    public HashCodeBuilder hash(char[] ac)
    {
        if ( ac != null )
        {
            for ( int x = 0; x < ac.length; x++ )
                hash(ac[x]);
        }
        return this;
    }
    
    /**
     * Hashes in the specified value. 
     */     
    public HashCodeBuilder hash(short s)
    {
        update(s);
        return this;
    }
    
    /**
     * Hashes in the specified value. 
     */     
    public HashCodeBuilder hash(short[] as)
    {
        if ( as != null )
        {
            for ( int x = 0; x < as.length; x++ )
                hash(as[x]);
        }
        return this;
    }
    
    /**
     * Hashes in the specified value. 
     */     
    public HashCodeBuilder hash(int i)
    {
        update(i);
        return this;
    }
    
    /**
     * Hashes in the specified value. 
     */     
    public HashCodeBuilder hash(int[] ai)
    {
        if ( ai != null )
        {
            for ( int x = 0; x < ai.length; x++ )
                hash(ai[x]);
        }
        return this;
    }
    
    /**
     * Hashes in the specified value. 
     */     
    public HashCodeBuilder hash(long l)
    {
        update((int)(l ^ (l >> 32)));
        return this;
    }
    
    /**
     * Hashes in the specified value. 
     */     
    public HashCodeBuilder hash(long[] al)
    {
        if ( al != null )
        {
            for ( int x = 0; x < al.length; x++ )
                hash(al[x]);
        }
        return this;
    }
    
    /**
     * Hashes in the specified value. 
     */     
    public HashCodeBuilder hash(float f)
    {
        update(Float.floatToIntBits(f));
        return this;
    }
    
    /**
     * Hashes in the specified value. 
     */     
    public HashCodeBuilder hash(float[] af)
    {
        if ( af != null )
        {
            for ( int x = 0; x < af.length; x++ )
                hash(af[x]);
        }
        return this;
    }
    
    /**
     * Hashes in the specified value. 
     */     
    public HashCodeBuilder hash(double d)
    {
        return hash(Double.doubleToLongBits(d));
    }
    
    /**
     * Hashes in the specified value. 
     */     
    public HashCodeBuilder hash(double[] ad)
    {
        if ( ad != null )
        {
            for ( int x = 0; x < ad.length; x++ )
                hash(ad[x]);
        }
        return this;
    }
    
    /**
     * Hashes in the specified value. 
     */     
    public HashCodeBuilder hash(Object o)
    {
        if ( o != null )
        {
            update(o.hashCode());
        }
        return this;
    }
    
    /**
     * Hashes in the specified value. 
     */     
    public HashCodeBuilder hash(Object[] ao)
    {
        if ( ao != null )
        {
            for ( int x = 0; x < ao.length; x++ )
                hash(ao[x]);
        }
        return this;
    }
    
    private void update(int c)
    {
        result = PRIME_FACTOR * result + c;
    }
    
    /**
     * Gets the computed hash code from this builder. 
     * 
     * @return the computed hash code. 
     */
    @Override
    public int hashCode()
    {
        return result;
    }
    
    /**
     * Identity comparison. 
     * 
     * @param o object to compare. 
     * @return true, if and only if, o and this object are the same instance. 
     */
    @Override
    public boolean equals(Object o)
    {
        return this == o;
    }
    
    /**
     * String representatino of the hash code. 
     * 
     * @return the same as {@code String.valueOf(hashCode())}
     */
    @Override
    public String toString()
    {
        return String.valueOf(result);
    }
    
}
