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
package org.jacorb.orb.policies;

import org.omg.CORBA.*;
import org.omg.Messaging.*;

public class MaxHopsPolicy extends _MaxHopsPolicyLocalBase
{
    private short max_hops;

    public MaxHopsPolicy (short max_hops)
    {
        this.max_hops = max_hops;
    }
    
    public MaxHopsPolicy (org.omg.CORBA.Any value)
    {
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
        return new MaxHopsPolicy (max_hops);
    }

    public void destroy()
    {
    }

}
