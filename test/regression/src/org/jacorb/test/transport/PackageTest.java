package org.jacorb.test.transport;

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

import org.jacorb.transport.IIOPTransportCurrentInitializer;
import org.jacorb.transport.TransportCurrentInitializer;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class PackageTest extends TestCase {
    
	public PackageTest(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite("org.jacorb.transport");
                suite.addTest(new PackageTest("testInitializers"));
		suite.addTest(FrameworkClientTest.suite());
		suite.addTest(FrameworkServerTest.suite());
		suite.addTest(IIOPClientTest.suite());
		suite.addTest(IIOPServerTest.suite());
		return suite;
	}
    
    public void testInitializers() throws Exception {
        // This is only to confirm that instantiation is possible, i.e. classes are
        // not missing due to some classpath issue.
        new TransportCurrentInitializer();
        new IIOPTransportCurrentInitializer();
    }
}
