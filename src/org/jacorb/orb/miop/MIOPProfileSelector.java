package org.jacorb.orb.miop;

import java.util.Iterator;
import java.util.List;
import org.jacorb.orb.ProfileSelector;
import org.jacorb.orb.giop.ClientConnectionManager;
import org.omg.ETF.Profile;
import org.omg.IOP.TAG_INTERNET_IOP;
import org.omg.IOP.TAG_UIPMC;


/**
 * Profile selector that considers the UMIOP object reference.
 *
 * @author Alysson Neves Bessani
 * @version 1.0
 */
public class MIOPProfileSelector implements ProfileSelector
{
   /**
    * Selects a profile of a reference. If there's a miop profile then it is
    * selected. Otherwise, a gateway profile is returned (or null).
    *
    * @param profiles
    * @param ccm
    * @return the selected profile
    */
   public Profile selectProfile (List profiles, ClientConnectionManager ccm)
   {
      Profile miop = null, iiop = null;

      for (Iterator i = profiles.iterator (); i.hasNext ();)
      {
         Profile profile = (Profile)i.next ();

         switch (profile.tag ())
         {
            case TAG_UIPMC.value:
            {
               miop = profile;
               break;
            }
            case TAG_INTERNET_IOP.value:
            {
               iiop = profile;
               break;
            }
         }
      }

      return (miop != null) ? miop : iiop;
   }

}
