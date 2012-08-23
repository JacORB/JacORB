/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2012 Gerald Brose / The JacORB Team.
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
 */

package org.jacorb.orb;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;
import org.omg.CORBA.BAD_PARAM;

/**
 * @author Alphonse Bendt
 */
public class ObjectKeyMap
{
    private final ORB orb;
    private final Map objectKeyMap = new HashMap();

    public ObjectKeyMap(ORB orb)
    {
        this.orb = orb;
    }

    /**
     * Map an object key to another, as defined by the value
     * of a corresponding configuration property in the properties
     * file, e.g. map "NameService" to "StandardNS/NameServer-POA/_root"
     *
     * @param originalKey a <code>byte[]</code> value containing the original
     * key.
     * @return a <code>byte[]</code> value containing the mapped key, if a
     * mapping is defined, originalKey otherwise.
     */
    public synchronized byte[] mapObjectKey(byte[] originalKey)
    {
       byte []result = originalKey;

       if (objectKeyMap.size () > 0)
       {
          String origKey = new String (originalKey);
          Object found = objectKeyMap.get (origKey);

          if( found != null )
          {
             if (found instanceof String)
             {
                if ( ParsedIOR.isParsableProtocol ( (String)found ) )
                {
                   // We have found a file reference. Use ParsedIOR to get
                   // the byte key.
                   try
                   {
                      ParsedIOR ior = new ParsedIOR( orb, (String)found );

                      result = ior.get_object_key();
                   }
                   catch ( IllegalArgumentException e )
                   {
                      throw new BAD_PARAM("could not extract object_key from IOR: " + e);
                   }
                }
                else
                {
                   result = org.jacorb.orb.util.CorbaLoc.parseKey((String)found);
                }
                // This 'hack' does the following - we cannot parse the key in configuration
                // as the service may not have been started yet so the files may not exist. So we have
                // to do it on demand - as an optimisation we then overwrite the original value with the
                // parsed form to save speed on future lookups.
                objectKeyMap.put (origKey, result);
             }
             else
             {
                // Must be a byte - already parsed it
                result = (byte[])found;
             }
          }
       }
       return result;
    }


    /**
     * a helper method supplied to initialize the object key map. This
     * replaces functionality from the defunct Environment class to populate
     * a hash map based on the names starting with "jacorb.orb.ObjectKeyMap"
     */
    public synchronized void configureObjectKeyMap(Configuration config)
    {
        final String prefix = "jacorb.orb.objectKeyMap.";
        final List names = config.getAttributeNamesWithPrefix(prefix);

        try
        {
            for (Iterator i = names.iterator(); i.hasNext(); )
            {
                final String property = (String) i.next();
                final String key_name = property.substring(prefix.length());
                final String full_path = config.getAttribute(property);
                addObjectKey(key_name, full_path);
            }
        }
        catch (ConfigurationException e)
        {
            throw new RuntimeException("should never happen", e);
        }
    }

    /**
     * <code>addObjectKey </code> is a proprietary method that allows the
     * internal objectKeyMap to be altered programmatically. The objectKeyMap
     * allows more readable corbaloc URLs by mapping the actual object key to
     * an arbitary string. See the jacorb.properties file for more information.
     *
     * @param key_name a <code>String</code> value e.g. NameService
     * @param full_path an <code>String</code> value e.g. file:/home/rnc/NameSingleton.ior
     */
    public synchronized void addObjectKey(String key_name, String full_path)
    {
        objectKeyMap.put(key_name, full_path);
    }

    /**
     * @see #addObjectKey(String, String)
     * @param key_name a <code>String</code> value e.g. NameService
     * @param object an a reference to a object whose object key should be used.
     */
    public void addObjectKey(String key_name, org.omg.CORBA.Object object)
    {
        addObjectKey(key_name, orb.object_to_string(object));
    }
}
