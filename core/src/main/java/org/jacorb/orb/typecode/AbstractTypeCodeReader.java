/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2006 The JacORB project.
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

package org.jacorb.orb.typecode;

import java.util.Map;
import org.jacorb.orb.CDRInputStream;
import org.jacorb.orb.ORBSingleton;
import org.omg.CORBA.TypeCode;

/**
 * this class captures logic common to all TypeCodeReader implementations
 *
 * @author Alphonse Bendt
 */
public abstract class AbstractTypeCodeReader implements TypeCodeReader
{
    /**
     * singleton orb that's used to create the actual TypeCode's
     */
    protected final ORBSingleton orb;


    public AbstractTypeCodeReader ()
    {
       orb = (ORBSingleton)org.omg.CORBA.ORBSingleton.init();
    }


    public TypeCode readTypeCode(CDRInputStream in, Map recursiveTCMap, Map repeatedTCMap)
    {
        // TypeCode's at least consist of the Kind
        final int kind = in.read_long();
        final int start_pos = in.get_pos() - 4;
        final Integer startPosition = Integer.valueOf( start_pos );

        return doReadTypeCode(in, recursiveTCMap, repeatedTCMap, startPosition, kind);
    }

    /**
     * subclasses should provide specific implementations
     */
    protected abstract TypeCode doReadTypeCode(CDRInputStream in,
                                               Map recursiveTCMap,
                                               Map repeatedTCMap,
                                               Integer startPosition,
                                               int kind);

    protected  String validateName(String name)
    {
        if (name != null && name.length() == 0)
        {
            return null;
        }
        return name;
    }

    protected String validateID(String id)
    {
        if (id == null || id.length() == 0)
        {
            return "IDL:";
        }
        return id;
    }
}
