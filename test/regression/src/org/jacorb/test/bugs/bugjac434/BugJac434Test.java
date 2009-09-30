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

package org.jacorb.test.bugs.bugjac434;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.jacorb.orb.ParsedIOR;
import org.jacorb.orb.util.PrintIOR;
import org.jacorb.test.common.ORBTestCase;

public class BugJac434Test extends ORBTestCase
{
    // This is a Orbacus IOR with an Orbacus FreeSSL Profile (0x14).
    public static final String ior = "IOR:010000001d00000049444c3a546573744f524241757468656e74696361746f723a312e300000000002000000000000002d000000010100b71a0000006c616479626972642e64652e707269736d746563682e636f6d001fad0500000054657374000000001400000034000000010100b71a0000006c616479626972642e64652e707269736d746563682e636f6d009ba20500000054657374002d534800000000";


    public void testPrintIOR() throws Exception
    {
       ParsedIOR pIOR = new ParsedIOR ((org.jacorb.orb.ORB)orb, ior);

       StringWriter sw = new StringWriter();
       PrintWriter pw = new PrintWriter (sw, true);

       PrintIOR.printIOR (pIOR, pw);

       String result = sw.toString ();

//        System.err.println ("Got result" + result);

       assertTrue (result.indexOf ("Unknown profile found with tag") > 0);
    }
}
