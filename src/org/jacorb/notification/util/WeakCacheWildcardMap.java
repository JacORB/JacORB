/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Library General Public
 *   License as published by the Free Software Foundation; either
 *   version 2 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this library; if not, write to the Free
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */

package org.jacorb.notification.util;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class WeakCacheWildcardMap implements WildcardMap
{
    private final WildcardMap delegate_;
    private final Map cache_ = new WeakHashMap();
    
    public WeakCacheWildcardMap(WildcardMap delegate)
    {
        delegate_ = delegate;
    }
    
    public WeakCacheWildcardMap()
    {
        this(new DefaultWildcardMap());
    }
    
    public void clear()
    {
        delegate_.clear();
        cache_.clear();
    }
    
    public Object getNoExpansion(Object key)
    {  
        return delegate_.getNoExpansion(key);
    }
    
    public Object[] getWithExpansion(Object key)
    {
        Object[] result = (Object[]) cache_.get(key.toString());
        
        if (result != null)
        {
            return result;
        }
        
        result = delegate_.getWithExpansion(key);
        cache_.put(key.toString(), result);
        
        return result;
    }
    
    public Object put(Object key, Object value)
    {
        cache_.remove(key.toString());
        
        return delegate_.put(key, value);
    }
    
    public Object remove(Object key)
    {
        cache_.remove(key.toString());
        
        return delegate_.remove(key);
    }
}