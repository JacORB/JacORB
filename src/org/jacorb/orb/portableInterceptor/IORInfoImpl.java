/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2003 Gerald Brose
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
package org.jacorb.orb.portableInterceptor;

import org.omg.PortableInterceptor.*;
import org.omg.IOP.*;
import org.omg.CORBA.*;

import org.jacorb.orb.ORB;
import org.jacorb.orb.MinorCodes;
import org.jacorb.orb.TaggedComponentList;
import org.jacorb.poa.POA;

import java.util.*;

/**
 * This class represents the type of info object
 * that will be passed to the IORInterceptors. <br>
 * See PI Spec p.7-64f
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public class IORInfoImpl extends org.omg.CORBA.LocalObject 
                         implements IORInfo
{
    /**
     * Maps profile tags to component lists (Integer -> TaggedComponentList).
     */
    private Map components = null;
    
    private Map policy_overrides = null;
  
    private ORB orb = null;
    private POA poa = null;
  
    public IORInfoImpl (ORB orb, POA poa, 
                        Map components, Map policy_overrides)
    {
        this.orb = orb;
        this.poa = poa;
        this.components = components;
        this.policy_overrides = policy_overrides;
    }

    /**
     * Adds component to all profiles.
     */
    public void add_ior_component (TaggedComponent component) 
    {
        for (Iterator i = components.values().iterator(); i.hasNext();)
        {
            TaggedComponentList l = (TaggedComponentList)i.next();
            l.addComponent (component);
        }
    }

    /**
     * Adds the component to the profile with the given tag.
     */
    public void add_ior_component_to_profile(TaggedComponent component, int id)
    {
        TaggedComponentList l = 
            (TaggedComponentList)components.get (new Integer (id));
        if (l == null)
        {
            throw new org.omg.CORBA.BAD_PARAM
            (
                "unknown profile tag: " + id,
                MinorCodes.NO_SUCH_PROFILE,
                CompletionStatus.COMPLETED_MAYBE
            );
        }
        else
        {
            l.addComponent (component);
        }
    }

    /**
     * @return a policy of the given type, or null,
     * if no policy of that type is present.
     */
    public Policy get_effective_policy(int type)
    {
        if (!orb.hasPolicyFactoryForType(type))
        {
            throw new org.omg.CORBA.INV_POLICY 
            (
                "No PolicyFactory for type " + type + 
			    " has been registered!", 
                MinorCodes.NO_SUCH_POLICY, 
                CompletionStatus.COMPLETED_MAYBE
            );
        }
        else
        {
            Policy policy = null;
            if (policy_overrides != null)
            {
	           policy = (Policy)policy_overrides.get (new Integer(type));
            }
            return (policy != null) ? policy : poa.getPolicy(type);
        }
    }

}






