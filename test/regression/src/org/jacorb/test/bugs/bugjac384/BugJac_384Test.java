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

package org.jacorb.test.bugs.bugjac384;

import java.io.File;
import java.util.Arrays;
import java.util.Properties;
import junit.framework.TestCase;
import org.jacorb.test.common.TestUtils;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

/**
 * @author Alphonse Bendt
 */
public class BugJac_384Test extends TestCase
{
    public void testThereShouldBeOnlyOneLogfile() throws Exception
    {
        File dir = TestUtils.createTempDir("bugjac384");

        Properties props = new Properties();

        props.setProperty("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
        props.setProperty("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");
        props.setProperty("jacorb.log.default.verbosity", "4");
        props.setProperty("jacorb.logfile", dir.getAbsolutePath() + "/logfile.txt");
        props.setProperty("jacorb.logfile.append", "on");
        props.setProperty("jacorb.logfile.maxLogSize", "1500");
        props.setProperty("jacorb.logfile.rotateCount", "2");

        ORB orb = ORB.init(new String[0], props);
        orb.shutdown(true);
        ((org.jacorb.config.JacORBConfiguration)((org.jacorb.orb.ORB)orb).getConfiguration()).shutdownLogging();
        orb = ORB.init(new String[0], props);
        POA poa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
        poa.destroy(true, true);
        orb.shutdown(true);

        ((org.jacorb.config.JacORBConfiguration)((org.jacorb.orb.ORB)orb).getConfiguration()).shutdownLogging();

        final String[] list = dir.list();
        assertEquals(Arrays.asList(list).toString(), 2, list.length);
    }
}
