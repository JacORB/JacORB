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
import org.omg.Messaging.RELATIVE_RT_TIMEOUT_POLICY_TYPE;
import org.omg.Messaging._RelativeRoundtripTimeoutPolicyLocalBase;

/**
 * Specifies a relative timeout for a CORBA roundtrip.  It is an upper bound
 * for the time it may take for a request to reach the server, be processed,
 * and the reply delivered back to the client.
 * 
 * @author Andre Spiegel spiegel@gnu.org
 */
public class RelativeRoundtripTimeoutPolicy
    extends _RelativeRoundtripTimeoutPolicyLocalBase
{
    /**
     * The length of this timeout, in CORBA time units
     * (100 nanosecond resolution).
     */
    private final long relative_expiry;

    /**
     * Constructs a new RelativeRoundtripTimeoutPolicy object from
     * an Any value.  This is the official CORBA way of constructing
     * this policy (via orb.create_policy()), but JacORB also has a
     * convenience constructor that directly takes the timeout value
     * as a parameter.
     * 
     * @param value an Any that contains the timeout as a CORBA
     * "unsigned long long" value (use Any.insert_ulonglong()).
     * The timeout is specified in CORBA time units (100 nanosecond resolution).
     * If you have a value in milliseconds, multiply that by 10,000.
     */
    public RelativeRoundtripTimeoutPolicy (org.omg.CORBA.Any value)
    {
        super();
        this.relative_expiry = value.extract_ulonglong();
    }

    /**
     * Convenience constructor for RelativeRoundtripTimeoutPolicy.  This
     * constructor is JacORB-specific, non-portable, but it allows you to
     * create a policy object in a single line of code, rather than going
     * via the ORB and stuffing the timeout value into an Any.
     * @param relative_expiry the duration of this timeout, in CORBA
     * time units (100 nanosecond resolution).  If you have a value in
     * milliseconds, multiply that by 10,000.
     */
    public RelativeRoundtripTimeoutPolicy (long relative_expiry)
    {
        super();
        this.relative_expiry = relative_expiry;
    }
 
    /**
     * Returns the duration of this timeout, in CORBA time units (100 nanosecond
     * resolution).  To convert it to milliseconds, divide by 10,000.
     */
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
        return this;
    }

    public void destroy()
    {
    }
}
