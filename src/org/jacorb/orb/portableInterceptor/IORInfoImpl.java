package org.jacorb.orb.portableInterceptor;

import org.omg.PortableInterceptor.*;
import org.omg.IOP.*;
import org.omg.CORBA.*;
import org.jacorb.orb.ORB;
import org.jacorb.poa.POA;
import java.util.Vector;
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

  private ORB orb = null;
  private POA poa = null;
  
  public IORInfoImpl(ORB orb, POA poa,
		     Vector components_iiop_profile,
		     Vector components_multi_profile) {

    this.components_iiop_profile = components_iiop_profile;
    this.components_multi_profile = components_multi_profile;

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
    
    return poa.getPolicy(type);
  }
} // IORInfoImpl






