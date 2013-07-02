/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2012 Gerald Brose / The JacORB Team.
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
 *
 */

package org.jacorb.test.notification.common;

import java.util.Properties;
import junit.extensions.TestSetup;
import junit.framework.Test;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.CommonSetup;
import org.jacorb.test.common.TestUtils;
import org.jacorb.test.ir.IFRServerSetup;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Object;

/**
 * setup class for TypedEventChannel integration tests.
 *
 * @author Alphonse Bendt
 */
public class TypedServerTestSetup extends TestSetup
{
    private final static String IGNORED = "ignored";

    private ClientServerSetup clientServerSetup;
    private IFRServerSetup ifrSetup;

    public TypedServerTestSetup(Test test)
    {
    	super(test);
    }

    public void setUp() throws Exception
    {
    	ifrSetup = new IFRServerSetup(fTest, TestUtils.testHome() + "/idl/TypedNotification.idl", null, null);

    	ifrSetup.setUp();

    	Properties props = new Properties();
    	props.setProperty("ORBInitRef.InterfaceRepository", ifrSetup.getRepository().toString());
        // FIXME: bugzilla #820 - disabled security for some regression tests
    	props.setProperty (CommonSetup.JACORB_REGRESSION_DISABLE_SECURITY, "true");
    	clientServerSetup = new ClientServerSetup(fTest, TypedServerTestRunner.class.getName(), IGNORED, props, props);
    	clientServerSetup.setUp();
    }

    public void tearDown() throws Exception
    {
    	clientServerSetup.tearDown();
    	ifrSetup.tearDown();
    }

	public Object getServerObject()
	{
		return clientServerSetup.getServerObject();
	}

	public ORB getClientOrb()
	{
		return clientServerSetup.getClientOrb();
	}
}
