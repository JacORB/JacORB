package org.jacorb.orb;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2006 Gerald Brose.
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

import java.util.*;
import org.omg.ETF.Profile;
import org.jacorb.orb.giop.ClientConnectionManager;
import org.omg.IOP.TAG_INTERNET_IOP;
import org.jacorb.orb.iiop.IIOPProfile;

/**
 * @author Steve Osselton
 * @version $Id$
 */
public class SpecificProfileSelector implements ProfileSelector
{
   private final org.omg.RTCORBA.Protocol[] protocols;

   public SpecificProfileSelector (org.omg.RTCORBA.Protocol[] protocols)
   {
      this.protocols = protocols;
   }

   /**
    *  Select IOP profile that matches protocol
    */
   public Profile selectProfile (List profiles, ClientConnectionManager ccm)
   {
      final Iterator iter = profiles.iterator();

      while (iter.hasNext ())
      {
         final Profile profile = (Profile) iter.next();
         final int profileTag = profile.tag ();

         for (int i = 0; i < protocols.length; i++)
         {
            final int tagToMatch = protocols[i].protocol_type;

            if (profileTag == tagToMatch)
            {
               return profile;
            }

            if (profileTag == TAG_INTERNET_IOP.value)
            {
                if (profile instanceof IIOPProfile)
                {
                    // Special case check for IIOP profile supporting SSL
                    IIOPProfile iiopProfile = (IIOPProfile) profile;
                    if
                    (
                            (tagToMatch == ORBConstants.JAC_SSL_PROFILE_ID) &&
                            (iiopProfile.getSSL () != null)
                    )
                    {
                        return profile;
                    }

                    // Special case check for IIOP profile not supporting SSL
                    if
                    (
                            (tagToMatch == ORBConstants.JAC_NOSSL_PROFILE_ID) &&
                            ((iiopProfile.getSSL () == null) ||
                                    // SSL port contains a valid value but further check is required
                                    // see if protection is enabled.
                                    (((iiopProfile.getSSL()).target_requires &
                                            org.omg.Security.NoProtection.value) != 0))
                    )
                    {
                        return profile;
                    }
                }
            }
         }
      }

      return null;
   }
}
