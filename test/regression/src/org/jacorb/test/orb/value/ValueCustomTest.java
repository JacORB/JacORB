package org.jacorb.test.orb.value;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2004  Gerald Brose.
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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.jacorb.test.common.TestUtils;

/**
 * <code>ValueCustomTest</code> tests that JacORB handles custom valuetypes.
 *
 * @author <a href="mailto:rnc@prismtechnologies.com"></a>
 * @version $Id$
 */
public class ValueCustomTest extends ClientServerTestCase
{
    private CustomValueExchange server;

    /**
     * Creates a new <code>ValueCustomTest</code> instance.
     *
     * @param name a <code>String</code> value
     * @param setup a <code>ClientServerSetup</code> value
     */
    public ValueCustomTest(String name, ClientServerSetup setup)
    {
        super(name, setup);
    }

    /**
     * <code>setUp</code> initilizes for the Junit tests.
     *
     * @exception Exception if an error occurs
     */
    public void setUp() throws Exception
    {
        server = CustomValueExchangeHelper.narrow( setup.getServerObject());
    }

    protected void tearDown() throws Exception
    {
        server = null;
    }

    /**
     * <code>suite</code> is called by JUnit.
     *
     * @return a <code>Test</code> value
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite("Valuetype Custom client/server tests");
        ClientServerSetup setup =
            new ClientServerSetup(suite,
                                  "org.jacorb.test.orb.value.CustomValueExchangeImpl");

        TestUtils.addToSuite(suite, setup, ValueCustomTest.class);

        return setup;
    }

    /**
     * <code>test_value_custom_good1</code> tests that I can send a custom
     * marshalled value.
     */
    public void test_value_custom_good1()
    {
        CustomValueExampleImpl valueType = new CustomValueExampleImpl();

        valueType.name_state = "Example : hello";

        valueType.number_state = 100;

        server.sendValueExample( valueType );

        System.out.println("test_value_custom number: " + valueType.number());
    }


    /**
     * <code>test_value_custom_fail1</code> tests that the custom marshalling code
     * can catch and throw an exception.
     */
    public void test_value_custom_fail1()
    {
        try
        {
            CustomValueExampleImpl valueType = new CustomValueExampleImpl();

            valueType.name_state = "Example : hello";

            valueType.number_state = -1000;

            server.sendValueExample( valueType );

            System.out.println("test_value_custom number: " + valueType.number());
            fail();
        }
        catch (org.omg.CORBA.MARSHAL ex)
        {
            // expected
        }
    }
}
