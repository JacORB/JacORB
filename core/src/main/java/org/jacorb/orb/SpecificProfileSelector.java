package org.jacorb.orb;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2014 Gerald Brose / The JacORB Team.
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

import java.util.Iterator;
import java.util.List;
import org.jacorb.orb.giop.ClientConnectionManager;
import org.jacorb.orb.iiop.IIOPProfile;
import org.omg.ETF.Profile;
import org.omg.IOP.TAG_INTERNET_IOP;

/**
 * @author Steve Osselton
 */
public class SpecificProfileSelector implements ProfileSelector
{
    private final org.omg.RTCORBA.Protocol[] protocols;
    private Profile currentProfile = null;


    public SpecificProfileSelector (org.omg.RTCORBA.Protocol[] protocols)
    {
        this.protocols = protocols;
    }

    /**
     *  Select first IOP profile that matches protocol
     */
    public Profile selectProfile (List<Profile> profiles, ClientConnectionManager ccm)
    {
        return selectNextProfile (profiles, null);
    }

    private boolean validate (Profile profile)
    {
        final int profileTag = profile.tag ();

        for (int i = 0; i < protocols.length; i++)
        {
            final int tagToMatch = protocols[i].protocol_type;

            if (profileTag == tagToMatch)
            {
                return true;
            }

            if (profile instanceof IIOPProfile)
            {
                // Special case check for IIOP profile supporting SSL
                IIOPProfile iiopProfile = (IIOPProfile) profile;
                if (tagToMatch == ORBConstants.JAC_SSL_PROFILE_ID &&
                    iiopProfile.getSSL () != null)
                {
                    return true;
                }

                // Special case check for IIOP profile not supporting SSL
                if (tagToMatch == ORBConstants.JAC_NOSSL_PROFILE_ID &&
                    (iiopProfile.getSSL () == null ||
                     // SSL port contains a valid value but further check is required
                     // see if protection is enabled.
                     (((iiopProfile.getSSL()).target_requires &
                       org.omg.Security.NoProtection.value) != 0)))
                {
                    return true;
                }
            }
        }
        return false;
    }

    public Profile selectNextProfile(List<Profile> profileList, Profile lastProfile)
    {
        //sanity check
        if (profileList == null || profileList.isEmpty())
        {
            return null;
        }

        // locate the last profile in the list
        Iterator<Profile> iterator;
        for (iterator = profileList.iterator(); iterator.hasNext();)
        {
            Profile p = iterator.next();

            if (lastProfile != null)
            {
                if (lastProfile.equals(p))
                {
                    break;
                }
            }
            else if (validate (p))
            {
                currentProfile = p;
                return p;
            }
        }

        // if we exit the loop but lastProfile is null, that means no
        // valid profiles were found.
        if (lastProfile == null)
        {
            currentProfile = null;
            return null;
        }

        // return the next profile, which is next to the last profile.
        while (true)
        {
            if (!iterator.hasNext())
            {
                iterator = profileList.iterator();
            }
            Profile p = iterator.next();
            if (lastProfile.equals (p))
            {
                // came all the way back around
                break;
            }

            if (validate (p))
            {
                currentProfile = p;
                return p;
            }
        }
        return null;
    }
}
