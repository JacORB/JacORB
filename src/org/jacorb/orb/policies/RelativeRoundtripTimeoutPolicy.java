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

public class RelativeRoundtripTimeoutPolicy
    extends _RelativeRoundtripTimeoutPolicyLocalBase
{
    private long relative_expiry;
    
    public RelativeRoundtripTimeoutPolicy (long relative_expiry)
    {
        this.relative_expiry = relative_expiry;
    }

    public RelativeRoundtripTimeoutPolicy (org.omg.CORBA.Any value)
    {
        this.relative_expiry = value.extract_ulonglong();
    }

    public long relative_expiry()
    {
        return relative_expiry;
    }

    public int policy_type()
    {
        return RELATIVE_RT_TIMEOUT_POLICY_TYPE.value;
    }

    public Policy copy()
    {
        return new RelativeRoundtripTimeoutPolicy (relative_expiry);
    }

    public void destroy()
    {
    }

}
