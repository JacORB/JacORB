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

import java.util.Properties;
import org.jacorb.test.common.PatternWrapper;


/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class Add_1_0_ProfilesTest extends AbstractGIOPMinorVersionTestCase
{
    protected String getGIOPMinorVersionString()
    {
        return "2";
    }

    protected void doMorePatching(Properties props)
    {
        props.setProperty("jacorb.giop.add_1_0_profiles", "on");
    }

    protected void verifyPrintIOROutput(String printIOROutput)
    {
       PatternWrapper re = PatternWrapper.init("IIOP Version:\\s+1\\.2");

       assertTrue(re.match(printIOROutput) != 0);

        re = PatternWrapper.init("IIOP Version:\\s+1\\.0");

        assertTrue(re.match(printIOROutput) != 0);
    }
}
