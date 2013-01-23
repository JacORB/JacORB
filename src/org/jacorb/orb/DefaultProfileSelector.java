package org.jacorb.orb;

/*
 *        JacORB  - a free Java ORB
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
    private Profile currentProfile = null;

    /**
     *
     * @param profiles
     * @param ccm
     * @return the first profile in the list if present
     */
    public Profile selectProfile (List profiles, ClientConnectionManager ccm)
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
     * If lastProfile is null, the first profile will be returned regardless.
     */
    public Profile selectNextProfile(List profileList, Profile lastProfile)
    {
        //sanity check
        if (profileList == null || profileList.isEmpty())
        {

            return null;
        }

        // locate the last profile in the list
        Iterator iterator;
        for (iterator = profileList.iterator(); iterator.hasNext();)
        {
            Profile profile_x = (Profile) iterator.next();

            if (lastProfile != null)
            {
                if (lastProfile.equals(profile_x))
                {

                    break;
                }
            }
            else
            {
                currentProfile = profile_x;

                return profile_x;
            }
        }

        // return the next profile, which is next to the last profile.
        while (true)
        {
            if (iterator == null || !iterator.hasNext())
            {
                iterator = profileList.iterator();
            }
            Profile profile_y = (Profile) iterator.next();


                currentProfile = profile_y;

                return profile_y;
        }
    }

    /**
     *
     * @param profileList
     * @param originalProfile
     * @param lastProfile
     * @return the next profile on the list that is next to lastProfile until
     * originalProfile is found which will cause a null to be returned.
     * If lastProfile is null, the first profile will be returned.
     * If originalProfile is null, the next profile will be returned regardless.
     */
    public Profile selectNextProfile(List<Profile> profileList, Profile originalProfile, Profile lastProfile)
    {
        //sanity check
        if (profileList == null || profileList.isEmpty())
        {

            return null;
        }

        // locate lastProfile in the list
        Iterator iterator;
        for (iterator = profileList.iterator(); iterator.hasNext();)
        {
            Profile profile_x = (Profile) iterator.next();

            if (lastProfile != null)
            {
                if (lastProfile.equals(profile_x))
                {

                    break;
                }
            }
            else
            {
                currentProfile = profile_x;

                return profile_x;
            }
        }

        // return the next profile, which is next to lastProfile.
        // if originalProfile is encountered, null will be turned since the
        // entire list has been iternated.
        while (true)
        {
            if (iterator == null || !iterator.hasNext())
            {
                iterator = profileList.iterator();
            }
            Profile profile_y = (Profile) iterator.next();

            if (originalProfile != null) {
                if (!originalProfile.equals(profile_y) )
                {
                    currentProfile = profile_y;

                    return profile_y;
                }
                else
                {

                    currentProfile = null;
                    return null;
                }
            }
            else
            {
                currentProfile = profile_y;

                return profile_y;
            }
        }
    }

    /* for debug purposes */
    public Profile getCurrentProfile()
    {
        return currentProfile;
    }
 }
