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
import org.omg.ETF.Profile;

/**
 * When the client connects to the server, an instance of this class selects
 * one of potentially many profiles in the IOR that the server published.
 * This class is the default ProfileSelector in JacORB, it always selects
 * the first profile in the list, no matter what.
 *
 * @author Andre Spiegel spiegel@gnu.org
 */
public class DefaultProfileSelector implements ProfileSelector
{
    /**
     *
     * @param profiles
     * @param ccm
     * @return the first profile in the list if present
     */
    public Profile selectProfile (List<Profile> profiles, ClientConnectionManager ccm)
    {
        if (profiles == null || profiles.isEmpty())
        {
            return null;
        }

        return selectNextProfile(profiles, null);
    }

    /**
     *
     * @param profileList
     * @param lastProfile
     * @return the next profile on the list that is next to lastProfile.
     * If lastProfile is null, or points to the last profile in the list,
     * the first profile will be returned.
     */
    public Profile selectNextProfile(List<Profile> profileList, Profile lastProfile)
    {
        Profile currentProfile = null;

        //sanity check
        if (profileList == null || profileList.isEmpty())
        {
            return null;
        }

        // locate the last profile in the list
        Iterator<Profile> iterator;
        for (iterator = profileList.iterator(); iterator.hasNext();)
        {
            currentProfile = iterator.next();
            if (lastProfile == null)
            {
                return currentProfile;
            }

            if (lastProfile.equals(currentProfile))
            {
                break;
            }
        }

        if (!iterator.hasNext())
        {
            iterator = profileList.iterator();
        }
        currentProfile = iterator.next();

        // ensure the next profile is not the same as lastProfile
        if (lastProfile.equals(currentProfile))
        {
            currentProfile = null;
        }

        return currentProfile;
    }

}
