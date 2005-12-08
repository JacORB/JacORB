package org.jacorb.test.orb;

import java.util.*;

import org.omg.CORBA.*;
import org.omg.IOP.*;
import org.omg.PortableInterceptor.IORInfo;
import org.omg.PortableInterceptor.IORInterceptor;

import org.jacorb.orb.iiop.IIOPAddress;
import org.jacorb.orb.portableInterceptor.IORInfoExt;
import org.jacorb.orb.iiop.IIOPProfile;

/**
 * An IOR Interceptor that adds alternate addresses to IIOP Profiles,
 * using the special JacORB mechanism via IORInfoExt.
 *
 * @author Marc Heide
 * @version $Id$
 */
public class IIOPProfileInterceptor
    extends LocalObject
    implements IORInterceptor
{
   public List alternateAddresses = IIOPAddressInterceptor.alternateAddresses;

    public void establish_components(IORInfo info)
    {
       IORInfoExt infoExt = (IORInfoExt) info;
       // ORB should have added already 1 profile with IOP tag
       int nrOfProf = infoExt.get_number_of_profiles(TAG_INTERNET_IOP.value);
       if( nrOfProf != 1 )
       {
          throw new RuntimeException ("unexpected number of IOP Profiles: " + nrOfProf);
       }

       IIOPProfile primaryProf =
           (IIOPProfile) infoExt.get_profile(TAG_INTERNET_IOP.value, 0);
       try
       {
          IIOPProfile cloneOfPrimary = (IIOPProfile) primaryProf.clone();

          // now add alternate addresses to primary profile
           for (Iterator i = alternateAddresses.iterator(); i.hasNext();) {

             IIOPAddress addr = (IIOPAddress)i.next();
             primaryProf.addComponent( TAG_ALTERNATE_IIOP_ADDRESS.value, addr.toCDR() );
          }

           // now add a secondary and third profile like used e.g. by
           // Visibroker 4.5
          for (Iterator i = alternateAddresses.iterator(); i.hasNext();)
          {
              IIOPAddress addr = (IIOPAddress)i.next();

              IIOPProfile additionalProfile =
                  (IIOPProfile) primaryProf.clone();

              additionalProfile.patchPrimaryAddress(addr);

              infoExt.add_profile(additionalProfile);
          }
       }
       catch ( CloneNotSupportedException ex )
       {
          throw new RuntimeException ("Exception during cloning of profile: "
                                      + ex);
       }

       nrOfProf = infoExt.get_number_of_profiles(TAG_INTERNET_IOP.value);
       if( nrOfProf != 1 + alternateAddresses.size())
       {
          throw new RuntimeException ("unexpected number of IOP Profiles after addition: "
                                      + nrOfProf
                                      + ", where number of alternates was: "
                                      + alternateAddresses.size());
       }

       // check access functions
       primaryProf =
           (IIOPProfile) infoExt.get_profile(TAG_INTERNET_IOP.value, 0);
       IIOPProfile primaryProf2 =
           (IIOPProfile) infoExt.get_profile(TAG_INTERNET_IOP.value);
       // they should be equal to primary
       if ( ! primaryProf.equals(primaryProf2) )
       {
          throw new RuntimeException ("difference between "
                                      + "get_profile(tag, idx) and "
                                      + "get_profile(tag): ");
       }

    }

    public String name()
    {
      return "IIOPProfileInterceptor";
    }

    public void destroy()
    {
      alternateAddresses.clear();
    }

}
