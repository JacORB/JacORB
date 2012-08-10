package org.jacorb.orb.policies;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2012 Gerald Brose / The JacORB Team.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jacorb.config.Configuration;
import org.omg.CORBA.SetOverrideType;
import org.omg.CORBA._PolicyManagerLocalBase;
import org.slf4j.Logger;

/**
 * Implementation of the ORB-level policy management interface as per
 * CORBA 2.6, p. 4-43 to 4-45:
 *
 * This PolicyManager has operations through which a set of Policies
 * can be applied and the current overriding Policy settings can be
 * obtained.  Policies applied at the ORB level override any system
 * defaults. The ORB's PolicyManager is obtained through an invocation
 * of ORB::resolve_initial_references, specifying an identifier of
 * "ORBPolicyManager."
 *
 * @author Gerald Brose
 */

public class PolicyManager
    extends _PolicyManagerLocalBase
{
    private final static org.omg.CORBA.Policy[] EMPTY_RESULT = new org.omg.CORBA.Policy[0];
    private final Map policy_overrides;
    private final Logger logger ;

    /**
     * public c'tor
     */

    public PolicyManager(Configuration config)
    {
        super();

        policy_overrides = new HashMap();
        this.logger = config.getLogger("jacorb.orb.policies");
    }

    /**
     * Returns a PolicyList containing the overridden Polices for the
     * requested PolicyTypes.  If the specified sequence is empty, all
     * Policy overrides at this scope will be returned.  If none of
     * the requested PolicyTypes are overridden at the target
     * PolicyManager, an empty sequence is returned. This accessor
     * returns only those Policy overrides that have been set at the
     * specific scope corresponding to the target PolicyManager (no
     * evaluation is done with respect to overrides at other scopes).
     *
     * @param ts a sequence of overridden policy types identifying the
     *           policies that are to be retrieved.
     * @return the list of overridden policies of the types specified by ts
     */

    public synchronized org.omg.CORBA.Policy[] get_policy_overrides(int[] ts)
    {
        if (ts == null)
        {
            throw new IllegalArgumentException("Argument may not be null");
        }

        if (ts.length == 0)
        {
            return (org.omg.CORBA.Policy[])policy_overrides.values().toArray( new org.omg.CORBA.Policy[]{});
        }

        final List policyList;
        synchronized(policy_overrides)
        {
            if (policy_overrides.isEmpty())
            {
                return EMPTY_RESULT;
            }

            if (ts.length == 0)
            {
                final Collection values = policy_overrides.values();
                return (org.omg.CORBA.Policy[])values.toArray( new org.omg.CORBA.Policy[values.size()]);
            }

            policyList = new ArrayList(ts.length);

            for (int i = 0; i < ts.length; i++ )
            {
                final Object policy = policy_overrides.get( Integer.valueOf(ts[i]));
                if (policy != null)
                {
                    policyList.add(policy);
                }
            }
        }

        org.omg.CORBA.Policy[] result =
            (org.omg.CORBA.Policy[])policyList.toArray( new org.omg.CORBA.Policy[]{});

        if (logger.isDebugEnabled() && result.length > 0)
        {
            logger.debug("get_policy_overrides returns " + result.length + " policies");
        }

        return result;
    }

    /**
     *
     * Modifies the current set of overrides with the requested list
     * of Policy overrides. The first parameter policies is a sequence
     * of references to Policy objects. The second parameter set_add
     * of type SetOverrideType indicates whether these policies should
     * be added onto any other overrides that already exist
     * (ADD_OVERRIDE) in the PolicyManager, or they should be added to
     * a clean PolicyManager free of any other overrides
     * (SET_OVERRIDE).
     * <p>
     * Invoking set_policy_overrides with an empty sequence of
     * policies and a mode of SET_OVERRIDE removes all overrides from
     * a PolicyManager. Only certain policies that pertain to the
     * invocation of an operation at the client end can be overridden
     * using this operation. Attempts to override any other policy
     * will result in the raising of the CORBA::NO_PERMISSION
     * exception. If the request would put the set of overriding
     * policies for the target PolicyManager in an inconsistent state,
     * no policies are changed or added, and the exception
     * InvalidPolicies is raised. There is no evaluation of
     * compatibility with policies set within other PolicyManagers.
     *
     * @param policies a sequence of Policy objects that are to be
     * associated with the PolicyManager object.
     *
     * @param set_add whether the association is in addition to
     * (ADD_OVERRIDE) or as a replacement of (SET_OVERRIDE) any
     * existing overrides already associated with the PolicyManager
     * object. If the value of this parameter is SET_OVERRIDE, the
     * supplied policies completely replace all existing overrides
     * associated with the PolicyManager object. If the value of this
     * parameter is ADD_OVERRIDE, the supplied policies are added to
     * the existing overrides associated with the PolicyManager
     * object, except that if a supplied Policy object has the same
     * PolicyType value as an existing override, the supplied Policy
     * object replaces the existing override.
     *
     * @throws org.omg.CORBA.InvalidPolicies a list of indices identifying the
     * position in the input policies list that are occupied by
     * invalid policies
     *
     * @throws org.omg.CORBA.BAD_PARAM if the sequence contains two or more Policy
     * objects with the same PolicyType value, the operation raises
     * the standard sytem exception BAD_PARAM with standard minor code
     * 30.
     */

    public synchronized void set_policy_overrides(org.omg.CORBA.Policy[] policies,
                                                  org.omg.CORBA.SetOverrideType set_add)
        throws org.omg.CORBA.InvalidPolicies
    {
        if (policies == null)
        {
            throw new IllegalArgumentException("Argument may not be null");
        }

        Map newPolicies = new HashMap();
        StringBuffer sb = new StringBuffer();

        // check that the policies argument does not contain multiple
        // policies of the same type while (copying the list of policies)
        // and does not override policies that cannot be overriden
        for (int i = 0; i < policies.length; i++ )
        {
            if (!PolicyUtil.isInvocationPolicy( policies[i].policy_type() ))
            {
                throw new org.omg.CORBA.NO_PERMISSION("Not an invocation policy, type " +
                                                      policies[i].policy_type() );
            }
            // else:
            Integer key = Integer.valueOf(policies[i].policy_type());
            if ( newPolicies.put( key, policies[i] ) != null )
            {
                throw new org.omg.CORBA.BAD_PARAM( "Multiple policies of type " +
                                                   policies[i].policy_type(),
                                                   30,
                                                   org.omg.CORBA.CompletionStatus.COMPLETED_NO);
            }
            sb.append(" " + policies[i].policy_type() );
        }


        synchronized(policy_overrides)
        {
            if (set_add == org.omg.CORBA.SetOverrideType.SET_OVERRIDE )
            {
                PolicyUtil.checkValidity(Collections.unmodifiableMap(newPolicies));
                policy_overrides.clear();
                policy_overrides.putAll(newPolicies);
            }
            else if (set_add == org.omg.CORBA.SetOverrideType.ADD_OVERRIDE )
            {
                // adds policies (and replaces any existing policies)
                final Map test = new HashMap(policy_overrides);
                test.putAll(policy_overrides);
                test.putAll( newPolicies );
                PolicyUtil.checkValidity(Collections.unmodifiableMap(test));

                policy_overrides.clear();
                policy_overrides.putAll(test);
            }
        }

        if (logger.isDebugEnabled())
        {
            String prefix = set_add == SetOverrideType.ADD_OVERRIDE ? "ADD_OVERRIDE" : "SET_OVERRIDE";
            logger.debug(prefix + ", types: " + sb);
        }
    }
}
