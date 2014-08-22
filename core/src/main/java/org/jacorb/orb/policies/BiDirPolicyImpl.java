/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2012 Gerald Brose / The JacORB Team.
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
package org.jacorb.orb.policies;


public class BiDirPolicyImpl
    extends org.omg.CORBA.LocalObject
    implements org.omg.BiDirPolicy.BidirectionalPolicy
{
    private final short value;

    public BiDirPolicyImpl(short value)
    {
        super();

        this.value = value;
    }

    public short value()
    {
        return value;
    }

    public int policy_type()
    {
        return org.omg.BiDirPolicy.BIDIRECTIONAL_POLICY_TYPE.value;
    }

    public org.omg.CORBA.Policy copy()
    {
        return this;
    }

    public void destroy()
    {
    }
}
