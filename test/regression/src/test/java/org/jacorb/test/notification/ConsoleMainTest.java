/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2014 Gerald Brose / The JacORB Team.
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
 *
 */

package org.jacorb.test.notification;

import static org.junit.Assert.assertEquals;
import org.jacorb.notification.ConsoleMain;
import org.junit.Test;

public class ConsoleMainTest
{
    @Test
    public void testSplitArgs()
    {
        assertEquals(0, ConsoleMain.splitArgs(null).length);
        assertEquals(0, ConsoleMain.splitArgs("").length);

        final String[] splitted = ConsoleMain.splitArgs("-startChannels 2");
        assertEquals(2, splitted.length);
        assertEquals("-startChannels", splitted[0]);
        assertEquals("2", splitted[1]);
    }
}
