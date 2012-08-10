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

import org.omg.CORBA.Policy;
import org.omg.Messaging.REBIND_POLICY_TYPE;
import org.omg.Messaging._RebindPolicyLocalBase;

public class RebindPolicy extends _RebindPolicyLocalBase
{
    private final short rebind_mode;

    public RebindPolicy (org.omg.CORBA.Any value)
    {
        this.rebind_mode = value.extract_short();
    }

    public short rebind_mode()
    {
        return rebind_mode;
    }

    public int policy_type()
    {
        return REBIND_POLICY_TYPE.value;
    }

    public Policy copy()
    {
        return this;
    }

    public void destroy()
    {
    }
}
