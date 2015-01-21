package org.jacorb.test.orb.value;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2014 Gerald Brose / The JacORB Team.
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

import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.jacorb.test.harness.TestUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.omg.CORBA.MARSHAL;


/**
 * <code>ValueCustomTest</code> tests that JacORB handles custom valuetypes.
 *
 * @author <a href="mailto:rnc@prismtechnologies.com"></a>
 */
public class ValueCustomTest extends ClientServerTestCase
{
    private CustomValueExchange server;

    @Before
    public void setUp() throws Exception
    {
        server = CustomValueExchangeHelper.narrow( setup.getServerObject());
    }

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {
        setup = new ClientServerSetup("org.jacorb.test.orb.value.CustomValueExchangeImpl");

    }

    /**
     * <code>test_value_custom_good1</code> tests that I can send a custom
     * marshalled value.
     */
    @Test
    public void test_value_custom_good1()
    {
        CustomValueExampleImpl valueType = new CustomValueExampleImpl();

        valueType.name_state = "Example : hello";

        valueType.number_state = 100;

        server.sendValueExample( valueType );

        TestUtils.getLogger().debug("test_value_custom number: " + valueType.number());
    }


    /**
     * <code>test_value_custom_fail1</code> tests that the custom marshalling code
     * can catch and throw an exception.
     */
    @Test (expected=MARSHAL.class)
    public void test_value_custom_fail1()
    {
        CustomValueExampleImpl valueType = new CustomValueExampleImpl();

        valueType.name_state = "Example : hello";

        valueType.number_state = -1000;

        server.sendValueExample( valueType );
    }
}
