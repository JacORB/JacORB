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

/**
 * An Object that maps String Keys to Values.<br> 
 * A WildcardMap cannot contain duplicate keys.
 * Each Key has exactly one Entry associated. A Key can contain the Wildcard Character '*' which
 * matches zero or more characters. The WildcardMap supports two semantics of accessing
 * the entries. The first way is to ignore the special meaning of the Wildcard character and to just
 * return the entries as they were inserted. <br>
 * This way you could put some entries in a WildcardMap and fetch them again using the Operation
 * {@link #getNoExpansion(Object) getNoExpansion()}:
 * 
 * <pre> 
 *   WildcardMap wc = new WildcardMap();
 *   wc.put(&quot;abc&quot;, new Integer(1));
 *   wc.put(&quot;a*&quot;, new Integer(2));
 *   wc.getNoExpansion(&quot;abc&quot;) =&gt; 1
 *   wc.getNoExpansion(&quot;a*&quot;) =&gt; 2
 *   wc.getNoExpansion(&quot;xyz&quot;) =&gt; null
 * </pre>
 * 
 * This behaviour is similiar to a {@link java.util.Map Map}.<br>
 * The other way using the WildcardMap is to use the Operation {@link #getWithExpansion(Object)
 * getWithExpansion()}. This Operations matches the requested Key to all contained Keys. If the Key
 * of an Entry contains the Wildcard Character '*' it is matched as expected by the semantic of '*'.
 * The Operations returns an array of all matching entries:
 * 
 * <pre>
 *   wc.getWithExpansion(&quot;abc&quot;) =&gt; [1,2]
 *   wc.getWithExpansion(&quot;a&quot;) =&gt; [2]
 *   wc.getWithExpansion(&quot;abcd&quot;) =&gt; [2]
 *   wc.getWithExpansion(&quot;xyz&quot;) =&gt; []
 * </pre>
 * 
 * @author Alphonse Bendt
 * @version $Id$
 */
public interface WildcardMap
{
    /**
     * clear this map
     */
    void clear();

    /**
     * remove the specified key from this Map.
     */
    Object remove(Object key);

    /**
     * The operation <code>put</code> associates the specified value with the specified key in
     * this map. The String representation of the Key {@link java.lang.Object#toString() toString()}
     * is used. If the map previously contained a mapping for this key, the old value is replaced by
     * the specified value.
     * 
     * @param key
     *            key with which String representation the specified value is to be associated.
     * @param value
     *            value to be associated with the specified key.
     * @return previous value associated with specified key, or null if there was no mapping for
     *         key.
     */
    Object put(Object key, Object value);

    /**
     * Returns the value to which this map maps the specified key. Returns null if the map contains
     * no mapping for this key.
     * 
     * @param key
     *            key whose associated value is to be returned
     * @return the value to which this map maps the specified key, or null if the map contains no
     *         mapping for this key.
     */
    Object getNoExpansion(Object key);

    /**
     * Returns the value to which this map maps the specified key. Additionaly return all Values
     * which keys contain a Wildcard and match the requested key. Returns null if the map contains
     * no mapping for this key.
     * 
     * @param key
     *            key whose associated value is to be returned
     * @return an Array of all Matching entries or null if no matching entry could be found.
     */
    Object[] getWithExpansion(Object key);
}