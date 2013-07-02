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

package org.jacorb.test.bugs.bug503;

import java.util.Properties;
import org.jacorb.test.BasicServer;
import org.jacorb.test.BasicServerHelper;
import org.jacorb.test.BasicServerPOATie;
import org.jacorb.test.common.ORBTestCase;
import org.jacorb.test.orb.BasicServerImpl;

/**
 * @author Alphonse Bendt
 */
public class Bug503Test extends ORBTestCase
{
    private BasicServer innerServer;
    private BasicServer outerServer;

    protected void patchORBProperties(Properties props)
    {
        // this is to prevent that this test picks up
        // jacorb.properties. the properties might configure some
        // orb initializers. we don't want this here
        // as otherwise this test will fail!
        props.setProperty("ORBid", "bogus");
    }

    protected void doSetUp() throws Exception
    {
        // need to remember the main thread here as we want
        // to verify that innerServer isn't called from another thread.
        final Thread mainThread = Thread.currentThread();

        innerServer =
            BasicServerHelper.narrow(rootPOA.servant_to_reference(new BasicServerImpl()
            {
                public boolean bounce_boolean(boolean x)
                {
                    assertSame(mainThread, Thread.currentThread());

                    return super.bounce_boolean(x);
                }
            }));

        // need to go via a string here to ensure that a delegate is
        // created that has the poa NOT set. this is to ensure
        // that the call between outerServer and innerServer executes
        // the Delegate.resolvePOA code.
        String innerIOR = orb.object_to_string(innerServer);
        BasicServer newDelegate = BasicServerHelper.narrow(orb.string_to_object(innerIOR));
        outerServer = BasicServerHelper.narrow(rootPOA.servant_to_reference(new BasicServerPOATie(newDelegate)));
    }

    protected void doTearDown() throws Exception
    {
        outerServer._release();
        outerServer = null;
        innerServer._release();
        innerServer = null;
    }
    
    public void testIsLocalWorks() throws Exception
    {
        assertTrue(outerServer.bounce_boolean(true));
    }
}
