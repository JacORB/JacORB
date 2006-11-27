package org.jacorb.test.bugs.bug228;

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

import org.jacorb.test.common.ORBTestCase;

/**
 * Test for bug 228, checks whether factory methods do end up in the Helper
 * class of a value type.  Also tests whether the ORB automatically finds
 * the DefaultFactory.
 *
 * @author <a href="mailto:spiegel@gnu.org">Andre Spiegel</a>
 * @version $Id$
 */
public class Bug228Test extends ORBTestCase
{
    public void testFactories()
    {
        Sample s = SampleHelper.init_1 (orb);
        assertNotNull(s);

        s = SampleHelper.init_2 (orb, 1, 2.0, "blabla");
        assertNotNull(s);
        assertEquals (1, s.alpha);
        assertEquals (2.0, s.beta, 0.0);
        assertEquals ("blabla", s.gamma);
    }
}
