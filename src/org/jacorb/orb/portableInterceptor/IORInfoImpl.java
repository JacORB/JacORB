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
package org.jacorb.orb.portableInterceptor;

import org.omg.PortableInterceptor.*;
import org.omg.IOP.*;
import org.omg.CORBA.*;
import org.omg.ETF.Profile;

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
                         implements IORInfoExt
{
    /**
     * Maps profile tags to component lists (Integer -> TaggedComponentList).
     */
    private Map components = null;
    
    private Map policy_overrides = null;
  
    private ORB orb = null;
    private POA poa = null;
    private List _profiles = null;
  
    public IORInfoImpl (ORB orb, POA poa, 
                        Map components, Map policy_overrides,
                        List profiles)
    {
        this.orb = orb;
        this.poa = poa;
        this.components = components;
        this.policy_overrides = policy_overrides;
        this._profiles = profiles;
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

    /**
     * This method adds a further profile to an IOR.
     * By using this method it is possible to append e.g. further IIOP
     * profiles. The added profile is marshalled after all profiles
     * already existing in profile list.
     * @param profile       the profile to add
     */
    public void add_profile(Profile profile)
    {
       if( _profiles != null )
       {
          _profiles.add(profile);
       }

    }

    /**
     * This method returns the number of profiles of the given type.
     * The returned value can be used to iterate over the existing
     * profiles of given type (get_profile()).
     * @param tag     profile tag, e.g. TAG_INTERNET_IOP.value
     * @return        number of profiles of given tag
     */
    public int get_number_of_profiles(int tag)
    {
       int retVal = 0;
       for (int i=0; i < _profiles.size(); i++)
       {
           Profile p = (Profile) _profiles.get(i);
           if ( p.tag() == tag )
              retVal++;
       }
       return retVal;
    }

    /**
     * Returns the profile with the given tag at the given position.
     * Following rule must apply to parameter position:<p>
     * <code> 0 <= position < get_number_of_profiles(tag) </code><p>
     * @param tag        tag of profile, e.g. TAG_INTERNET_IOP.value
     * @param position   position in IOR
     * @return           profile
     * @exception       ArrayIndexOutOfBoundsException if position is
     *                   out of range
     */
    public org.omg.ETF.Profile get_profile(int tag, int position)
    {
       int cnt = position;
       Profile retVal = null;
       for (int i=0; i < _profiles.size(); i++)
       {
           Profile p = (Profile) _profiles.get(i);
           if ( p.tag() == tag && cnt == 0)
           {
              retVal = p;
              break;
           }
           else
           {
              cnt--;
           }
       }
       if( retVal == null )
         throw new ArrayIndexOutOfBoundsException("no profile with tag=" + tag + " at position" + position);

       return retVal;
    }

    /**
     * Returns the first profile with the given tag (position == 0).
     * If no profile with given tag exists, null is returned.
     * @param tag        tag of profile, e.g. TAG_INTERNET_IOP.value
     * @return           first profile or null if no profile with given
     *                   tag exists
     */
    public org.omg.ETF.Profile get_profile(int tag)
    {
       Profile retVal = null;
       for (int i=0; i < _profiles.size(); i++)
       {
           Profile p = (Profile) _profiles.get(i);
           if ( p.tag() == tag )
           {
              retVal = p;
              break;
           }
       }
       return retVal;

    }

}






