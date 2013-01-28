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
     * @see org.jacorb.orb.ProfileSelector#selectProfile(java.util.List, org.jacorb.orb.giop.ClientConnectionManager)
     */
    public Profile selectProfile (List profiles, ClientConnectionManager ccm)
    {
        // always return the first profile in the list
        if (profiles.size() > 0)
        {
            return (Profile)profiles.get(0);
        }

        return null;
    }
}
