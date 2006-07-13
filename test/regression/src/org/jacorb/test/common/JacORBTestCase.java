package org.jacorb.test.common;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 2005  Gerald Brose.
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

import junit.framework.*;

/**
 * A special TestCase that is capable of deciding whether it applies
 * to a certain client and server version.
 *
 * @author Andre Spiegel spiegel@gnu.org
 * @version $Id$
 */
public class JacORBTestCase extends TestCase implements JacORBTest
{
    private TestAnnotations annotations = null;

    public JacORBTestCase (String name)
    {
        super(name);
        String clientVersion = System.getProperty ("jacorb.test.client.version",
                                                   "cvs");
        String serverVersion = System.getProperty ("jacorb.test.server.version",
                                                   "cvs");
        if (!clientVersion.equals("cvs") || !serverVersion.equals("cvs"))
        {
            annotations = TestAnnotations.forTestCase (this);
        }
    }

    /**
     * Indicates whether this Test is applicable to a given client and
     * server ORB version.
     */
    public boolean isApplicableTo (String clientVersion, String serverVersion)
    {
        if (annotations == null)
        {
            return true;
        }

        boolean result =
            annotations.isApplicableTo (clientVersion, serverVersion);

        if (!result)
        {
            System.out.println ("not applicable: " + getName());
        }

        return result;
    }
}
