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
package org.jacorb.orb.policies;

import org.omg.CORBA.Policy;
import org.omg.Messaging.MAX_HOPS_POLICY_TYPE;
import org.omg.Messaging._MaxHopsPolicyLocalBase;

public class MaxHopsPolicy extends _MaxHopsPolicyLocalBase
{
    private final short max_hops;

    public MaxHopsPolicy (org.omg.CORBA.Any value)
    {
        super();

        max_hops = value.extract_ushort();
    }

    public short max_hops()
    {
        return max_hops;
    }

    public int policy_type()
    {
        return MAX_HOPS_POLICY_TYPE.value;
    }

    public Policy copy()
    {
        return this;
    }

    public void destroy()
    {
    }
}
