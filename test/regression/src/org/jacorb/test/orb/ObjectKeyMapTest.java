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

package org.jacorb.test.orb;

import java.util.Arrays;
import java.util.Collections;
import org.easymock.MockControl;
import org.jacorb.config.Configuration;
import org.jacorb.orb.ObjectKeyMap;
import org.jacorb.test.common.ORBTestCase;
import org.omg.CORBA.BAD_PARAM;

/**
 * @author Alphonse Bendt
 */
public class ObjectKeyMapTest extends ORBTestCase
{
    private static final String shortKey = "ShortCut";
    private static final byte[] shortKeyBytes = shortKey.getBytes();
    private static final String longKey = "Long/Key/123456789";
    private static final byte[] longKeyBytes = longKey.getBytes();

    private MockControl configControl = MockControl.createControl(Configuration.class);
    private Configuration configMock = (Configuration) configControl.getMock();

    private ObjectKeyMap objectUnderTest;

    protected void doSetUp() throws Exception
    {
        objectUnderTest = new ObjectKeyMap((org.jacorb.orb.ORB)orb);
    }

    public void testWithoutMappingUnmodifiedKeyIsUsed()
    {
        assertEqualString(shortKeyBytes, objectUnderTest.mapObjectKey(shortKeyBytes));
    }

    public void testAddObjectKey()
    {
        objectUnderTest.addObjectKey(shortKey, longKey);

        assertEqualString(longKeyBytes, objectUnderTest.mapObjectKey(shortKeyBytes));
    }

    public void testAddObjectKeyAsBogusIORFails()
    {
        objectUnderTest.addObjectKey(shortKey, "corbaloc:localhost:1234/RootPOA/" + longKey);

        try
        {
            objectUnderTest.mapObjectKey(shortKeyBytes);
            fail();
        }
        catch(BAD_PARAM e)
        {
        }
    }

    public void testAddObjectKeyAsIOR()
    {
        objectUnderTest.addObjectKey(shortKey, "corbaloc::localhost:1234/" + longKey);

        assertEqualString(longKeyBytes, objectUnderTest.mapObjectKey(shortKeyBytes));
    }

    public void testAddObjectAsObject()
    {
        String corbaloc = "corbaloc::localhost:1234/" + longKey;

        objectUnderTest.addObjectKey(shortKey, orb.string_to_object(corbaloc));

        assertEqualString(longKeyBytes, objectUnderTest.mapObjectKey(shortKeyBytes));
    }


    public void testConfigureWithEmptyMap()
    {
        configControl.expectAndReturn(configMock.getAttributeNamesWithPrefix("jacorb.orb.objectKeyMap."), Collections.EMPTY_LIST);

        configControl.replay();

        objectUnderTest.configureObjectKeyMap(configMock);

        assertEqualString(shortKeyBytes, objectUnderTest.mapObjectKey(shortKeyBytes));

        configControl.verify();
    }

    public void testConfigureWithOneEntry() throws Exception
    {
        String configKey = "jacorb.orb.objectKeyMap." + shortKey;
        configControl.expectAndReturn(configMock.getAttributeNamesWithPrefix("jacorb.orb.objectKeyMap."), Arrays.asList(new String[] {configKey}));
        configControl.expectAndReturn(configMock.getAttribute(configKey), longKey);

        configControl.replay();

        objectUnderTest.configureObjectKeyMap(configMock);

        assertEqualString(longKeyBytes, objectUnderTest.mapObjectKey(shortKeyBytes));

        configControl.verify();
    }


    private static void assertEqualString(byte[] expected, byte[] actual)
    {
        assertEquals(new String(expected), new String(actual));
    }
}
