package org.jacorb.orb.policies;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2004 Gerald Brose.
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

import java.util.Map;

public class PolicyUtil
{
    /** map with Integers of policy types that may not be overridden */
    // private static Map invocationPolicies = new HashMap();

    /**
     * determine if a policy applies to operation invocations or not,
     * called e.g., from PolicyManager operations to check if
     * arguments are okay.
     */

    public static boolean isInvocationPolicy(int policyType)
    {
        return true; // TODO
        //        return invocationPolicies.containsKey( new Integer( policyType ));
    }


    /**
     * determine if a given set of policies is consistent
     * called e.g., from PolicyManager operations to check if
     * arguments are okay.
     * @throws org.omg.CORBA.InvalidPolicies a list of indices identifying the
     * position in the input policies list that are occupied by
     * invalid policies
     */

    public static void checkValidity(Map policies)
        throws org.omg.CORBA.InvalidPolicies
    {
        // TODO
    }
}
