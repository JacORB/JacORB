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

package org.jacorb.test.bugs.bugjac513;

import static org.junit.Assert.assertTrue;
import java.util.regex.Pattern;
import org.jacorb.test.harness.TestUtils;

/**
 * @author Alphonse Bendt
 */
public class GIOP_1_1_Test extends AbstractGIOPMinorVersionTestCase
{
    protected String getGIOPMinorVersionString()
    {
        return "1";
    }

    protected void verifyPrintIOROutput(String printIOROutput)
    {
       Pattern re = Pattern.compile("IIOP Version:\\s+1\\.1");
       assertTrue(TestUtils.patternMatcher(re, printIOROutput) != 0);
    }
}
