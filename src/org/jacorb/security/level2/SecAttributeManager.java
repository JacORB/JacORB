/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2002 Gerald Brose
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
package org.jacorb.security.level2;

/**
 * SecAttributeManager.java
 *
 *
 * Created: Mon Sep  4 16:32:39 2000
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

import java.util.*;

import org.omg.Security.*;

public class SecAttributeManager
{
    private Hashtable attributes = null;
    private int id = Integer.MIN_VALUE + 1;

    private static SecAttributeManager instance = null;

    public SecAttributeManager()
    {
        attributes = new Hashtable();
    }

    public static SecAttributeManager getInstance()
    {
        if( instance == null )
        {
            instance = new SecAttributeManager();
        }

        return instance;
    }
        
    public KeyAndCert getAttributeCertValue( SecAttribute attribute )
    {
        return ( KeyAndCert ) getAttributeValue( attribute );
    }
    
    public SecAttribute createAttribute( Object attrib_value, 
                                         AttributeType attribute_type )
    {
        //value is id in byte array
        byte[] value = new byte[]{ (byte) (( id >>> 24 ) & 0xff),
                                   (byte) (( id >>> 16 ) & 0xff),
                                   (byte) (( id >>>  8 ) & 0xff),
                                   (byte)  ( id & 0xff) };

        attributes.put( new Integer( id++ ), attrib_value );

        return new SecAttribute( attribute_type,
                                 new byte[0], //no defining auth
                                 value );
        
    }

    public Object getAttributeValue( SecAttribute attribute )
    {
        if( attribute.value.length != 4 )
        {
            throw new Error( "Value of SecAttribute unknown!" );
        }

        int the_id = 
            ((attribute.value[0] & 0xff) << 24 ) +
            ((attribute.value[1] & 0xff) << 16 ) +
            ((attribute.value[2] & 0xff) << 8 ) +
            (attribute.value[3] & 0xff);

        return attributes.get( new Integer( the_id ));
    }

} // SecAttributeManager






