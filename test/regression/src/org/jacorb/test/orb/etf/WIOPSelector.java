package org.jacorb.test.orb.etf;

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
 *   Software Foundation, 51 Franklin Street, Fifth Floor, Boston,
 *   MA 02110-1301, USA.
 */

import java.util.Iterator;
import java.util.List;
import org.jacorb.orb.ProfileSelector;
import org.jacorb.orb.giop.ClientConnectionManager;
import org.omg.ETF.Profile;

/**
 * A ProfileSelector that always selects the WIOPProfile, no matter
 * where it is in the list.
 *
 * @author Andre Spiegel spiegel@gnu.org
 */
public class WIOPSelector implements ProfileSelector
{
    public Profile selectProfile (List profiles, ClientConnectionManager ccm)
    {
        for (Iterator i=profiles.iterator(); i.hasNext();)
        {
            Profile p = (Profile)i.next();
            if (p instanceof org.jacorb.test.orb.etf.wiop.WIOPProfile)
                return p;
        }
        return null;
    }

    public Profile selectNextProfile (List profiles, Profile lastProfile)
    {
        return null;
    }
}
