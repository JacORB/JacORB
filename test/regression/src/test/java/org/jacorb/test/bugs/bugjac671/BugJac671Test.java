package org.jacorb.test.bugs.bugjac671;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2014  Gerald Brose.
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

import java.util.Properties;
import org.jacorb.test.harness.CommonSetup;
import org.jacorb.test.harness.FixedPortORBTestCase;
import org.junit.Test;

/**
 * <code>BugJac671Test</code> verifies that we can start two ORB/POAs
 * on different ports and that they do not clash.
 *
 * @author <a href="mailto:Nick.Cross@prismtech.com">Nick Cross</a>
 * @version 1.0
 */
public class BugJac671Test extends FixedPortORBTestCase
{
    @Override
    protected void patchORBProperties(Properties props) throws Exception
    {
        props.setProperty ("OAPort", Integer.toString(getNextAvailablePort()));
    }

    @Test
    public void test_orb_port() throws Exception
    {
        Properties props2 = new Properties();
        props2.putAll(CommonSetup.loadSSLProps("jsse_server_props", "jsse_server_ks"));

        getAnotherORB(props2);
    }
}
