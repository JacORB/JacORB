/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2006 The JacORB project.
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

package org.jacorb.test.bugs.bugjac513;

import org.apache.regexp.RE;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class GIOP_1_2_Test extends AbstractGIOPMinorVersionTestCase
{
    protected String getGIOPMinorVersionString()
    {
        return "2";
    }

    protected void verifyPrintIOROutput(String printIOROutput)
    {
        RE re = new RE("IIOP Version:\\s+1\\.2");

        assertTrue(re.match(printIOROutput));
    }
}
