/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose.
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

package org.jacorb.orb.connection;

import org.omg.BiDirPolicy.*;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.Policy;

import org.omg.CORBA.LocalObject;

/**
 * BiDirPolicy.java
 *
 *
 * Created: Mon Sep  3 18:39:06 2002
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public class BiDirPolicy 
    extends org.omg.CORBA.LocalObject 
    implements BidirectionalPolicy
{
    private short value;

    public BiDirPolicy( short value )
    {
        this.value = value;
    }

    public boolean useBiDirGIOP()
    {
        return value == BOTH.value;
    }

    public short value()
    {
        return value;
    }

    public int policy_type()
    {
        return BIDIRECTIONAL_POLICY_TYPE.value;
    }

    public Policy copy()
    {
        return new BiDirPolicy( value );
    }

    public void destroy()
    {
    }
}// BiDirPolicy

