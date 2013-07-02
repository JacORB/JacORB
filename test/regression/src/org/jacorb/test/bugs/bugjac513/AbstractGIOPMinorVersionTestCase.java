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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;
import org.jacorb.orb.Delegate;
import org.jacorb.orb.ParsedIOR;
import org.jacorb.orb.util.PrintIOR;
import org.jacorb.test.BasicServer;
import org.jacorb.test.BasicServerHelper;
import org.jacorb.test.common.ORBTestCase;
import org.jacorb.test.orb.BasicServerImpl;
import org.omg.CORBA.portable.ObjectImpl;

/**
 * @author Alphonse Bendt
 */
public abstract class AbstractGIOPMinorVersionTestCase extends ORBTestCase
{
    protected void patchORBProperties(String testName, Properties props) throws Exception
    {
        String giopVersionString = getGIOPMinorVersionString();
        props.setProperty("jacorb.giop_minor_version", giopVersionString);

        doMorePatching(props);
    }

    protected void doMorePatching(Properties props)
    {
    }

    protected abstract String getGIOPMinorVersionString();

    public void testServerWorks() throws Exception
    {
        BasicServer server = BasicServerHelper.narrow(rootPOA.servant_to_reference(new BasicServerImpl()));

        long now = System.currentTimeMillis();

        assertEquals(now, server.bounce_long_long(now));
    }

    public void testServerUsesProperGIOPVersion() throws Exception
    {
        BasicServer server = BasicServerHelper.narrow(rootPOA.servant_to_reference(new BasicServerImpl()));

        ParsedIOR ior = ((Delegate)((ObjectImpl)server)._get_delegate()).getParsedIOR();

        StringWriter out = new StringWriter();
        PrintIOR.printIOR(orb, ior, new PrintWriter(out));

        String result = out.toString();
        verifyPrintIOROutput(result);
    }

    protected abstract void verifyPrintIOROutput(String printIOROutput);
}
