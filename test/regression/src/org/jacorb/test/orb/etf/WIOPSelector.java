package org.jacorb.test.orb.etf;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose.
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

import org.jacorb.orb.ProfileSelector;
import org.jacorb.orb.connection.ClientConnectionManager;
import org.omg.ETF.Profile;

/**
 * A ProfileSelector that always selects the WIOPProfile, no matter
 * where it is in the list.
 *
 * @author <a href="mailto:spiegel@gnu.org">Andre Spiegel</a>
 * @version $Id$
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
}
