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
import org.omg.Messaging.SYNC_SCOPE_POLICY_TYPE;
import org.omg.Messaging._SyncScopePolicyLocalBase;

public class SyncScopePolicy extends _SyncScopePolicyLocalBase
{
    private final short synchronization;

    public SyncScopePolicy (org.omg.CORBA.Any value)
    {
        super();

        this.synchronization = value.extract_short();
    }

    public short synchronization()
    {
        return synchronization;
    }

    public int policy_type()
    {
        return SYNC_SCOPE_POLICY_TYPE.value;
    }

    public Policy copy()
    {
        return this;
    }

    public void destroy()
    {
    }
}
