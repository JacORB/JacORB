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
package org.jacorb.orb.portableInterceptor;

import org.omg.PortableInterceptor.*;
import org.omg.IOP.*;
import org.omg.CORBA.*;
import org.jacorb.orb.ORB;
import org.jacorb.poa.POA;
import java.util.Vector;
import java.util.Hashtable;
/**
 * This class represents the type of info object
 * that will be passed to the IORInterceptors. <br>
 * See PI Spec p.7-64f
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public class IORInfoImpl extends org.omg.CORBA.LocalObject 
  implements IORInfo{

  Vector components_iiop_profile = null;
  Vector components_multi_profile = null;

  private Hashtable policy_overrides = null;
  private ORB orb = null;
  private POA poa = null;
  
  public IORInfoImpl(ORB orb, POA poa,
		     Vector components_iiop_profile,
		     Vector components_multi_profile,
                     Hashtable policy_overrides) {

    this.components_iiop_profile = components_iiop_profile;
    this.components_multi_profile = components_multi_profile;
    this.policy_overrides = policy_overrides;

    this.orb = orb;
    this.poa = poa;
  }

  // implementation of org.omg.PortableInterceptor.IORInfoOperations interface
  public void add_ior_component(TaggedComponent component) {
    components_iiop_profile.addElement(component);
    components_multi_profile.addElement(component);
  }

  public void add_ior_component_to_profile(TaggedComponent component, int id){
    if (id == TAG_INTERNET_IOP.value)
      components_iiop_profile.addElement(component);

    else if (id == TAG_MULTIPLE_COMPONENTS.value)
      components_multi_profile.addElement(component);
  }

  /**
   * @return a policy of the given type, or null,
   * if no policy of that type is present.
   */
  public Policy get_effective_policy(int type) {
    if (! orb.hasPolicyFactoryForType(type))
      throw new INV_POLICY("No PolicyFactory for type " + type + 
			   " has been registered!", 2,
			   CompletionStatus.COMPLETED_MAYBE);
    Policy policy = null;
    if (policy_overrides != null)
	policy = (Policy)policy_overrides.get(new Integer(type));
    return (policy != null) ? policy : poa.getPolicy(type);
  }
} // IORInfoImpl






