package org.jacorb.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A simple implementation of an IdentityHashMap, as it is introduced in
 * JDK 1.4.  This version here is not fully implemented and not particularly
 * efficient, and serves only as a substitute when running under earlier JDKs.
 * 
 * (In an IdentityHashMap, two keys k1 and k2 are considered equal if and only
 * if k1 == k2.)
 * 
 * @author <a href="mailto:spiegel@gnu.org">Andre Spiegel</a>
 * @version $Id$
 */
public class IdentityHashMap extends HashMap
{
    public Object put(Object key, Object value)
    {
        return super.put(new IdentityWrapper(key), value); 
    }

    public Object get(Object key)
    {
        return super.get(new IdentityWrapper(key));
    }

    public Object remove(Object key)
    {
        return super.remove(new IdentityWrapper(key));
    }

    public boolean containsKey(Object key)
    {
        return super.containsKey(new IdentityWrapper(key));
    }

    public boolean containsValue(Object value)
    {
        throw new RuntimeException("containsValue() not implemented");
    }

    public Set entrySet()
    {
        throw new RuntimeException("entrySet() not implemented");
    }

    public Set keySet()
    {
        throw new RuntimeException("keySet() not implemented");
    }

    public void putAll(Map m)
    {
        throw new RuntimeException("putAll() not implemented");
    }

    /**
     * A wrapper around an object that redefines the equals method
     * as an identity test.
     */
    private static class IdentityWrapper
    {
        private Object value;

        public IdentityWrapper(Object value)
        {
            this.value = value;
        }

        public boolean equals(Object other)
        {
            if (other instanceof IdentityWrapper)
                return this.value == ((IdentityWrapper)other).value;
            else
                return false;
        }
        
        public int hashCode()
        {
            return value.hashCode();   
        }
    }

}
