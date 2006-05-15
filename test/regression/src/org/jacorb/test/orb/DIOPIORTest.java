package org.jacorb.test.orb;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2001  Gerald Brose.
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

import junit.framework.*;
import junit.extensions.TestSetup;

import org.apache.avalon.framework.logger.Logger;
import org.easymock.MockControl;
import org.jacorb.test.common.ORBSetup;
import org.jacorb.orb.ParsedIOR;
import java.util.List;


/**
 * <code>DIOPIORTest</code> tests that JacORB can decode a DIOP IOR - this
 * are a GIOP UDP protocol.
 *
 * @author Nick Cross
 * @version $Id$
 */
public class DIOPIORTest extends TestCase
{
    private static final MockControl loggerControl = MockControl.createNiceControl(Logger.class);
    private static final Logger loggerMock = (Logger) loggerControl.getMock();

    private static org.omg.CORBA.ORB orb;

    /**
     * <code>testIOR</code> is a test DIOP ior to decode.
     */
    private static final String ior = "IOR:0064d0820000002a49444c3a6e6f6465624361744361742f436174426173655265717565737448616e646c65723a312e3000531d0000000154414f0400000025000100540000000a3132372e302e302e310004010000000d654f524208000041c630303030";

    public DIOPIORTest (String name)
    {
        super (name);
    }

    /**
     * <code>suite</code> lists the tests for Junit to run.
     *
     * @return a <code>Test</code> value
     */
    public static Test suite ()
    {
        TestSuite suite = new TestSuite ("DIOP Test");
        Setup setup = new Setup( suite );
        ORBSetup osetup = new ORBSetup( setup );

        suite.addTest (new DIOPIORTest ("testDecode1"));

        return osetup;
    }


    /**
     * <code>testDecode1</code> tests that JacORB can decode a DIOP IOR. To do
     * this we create a ParsedIOR with the known IOR and test that the number
     * of profile bodies is greater than zero.
     */
    public void testDecode1 ()
    {
        ParsedIOR pior = new ParsedIOR( ior, orb, loggerMock);

        List bodies = pior.getProfiles();

        assertNotNull("did not get bodies", bodies);
        assertTrue("did not get bodies", bodies.size() > 0);
    }


    /**
     * <code>Setup</code> is an inner class to initialize the ORB.
     */
    private static class Setup extends TestSetup
    {
        /**
         * Creates a new <code>Setup</code> instance.
         *
         * @param test a <code>Test</code> value
         */
        public Setup (Test test)
        {
            super (test);
        }

        /**
         * <code>setUp</code> sets the orb variable.
         */
        protected void setUp ()
        {
            orb = ORBSetup.getORB ();
        }

        /**
         * <code>tearDown</code> does nothing for this test.
         */
        protected void tearDown ()
        {
        }
    }
}
