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

package org.jacorb.test.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;
import org.jacorb.test.harness.TestUtils;
import org.jacorb.util.ObjectUtil;
import org.junit.Test;

/**
 * @author Alphonse Bendt
 */
public class ObjectUtilTest
{
    @Test
    public void testClassForNameWithNull() throws Exception
    {
        try
        {
            ObjectUtil.classForName(null);
            fail();
        }
        catch(IllegalArgumentException e)
        {
        }
    }

    @Test
    public void testClassForNameWithoutCtxCL() throws Exception
    {
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();

        try
        {
            Thread.currentThread().setContextClassLoader(null);

            Class<?> clazz = ObjectUtil.classForName("org.jacorb.orb.ORB");
            assertNotNull(clazz);
        }
        finally
        {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }

    @Test
    public void testClassForNameWithCtxCL() throws Exception
    {
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();

        try
        {
            Thread.currentThread().setContextClassLoader(new URLClassLoader(new URL[] {new File(TestUtils.jacorbHome(), "/classes").toURI().toURL()}, null));

            Class<?> clazz = ObjectUtil.classForName("org.jacorb.orb.ORB");
            assertNotNull(clazz);
        }
        finally
        {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }

    @Test
    public void testArgsToProps()
    {
        String[] args = {"-Dkey1=value1", "-Dkey2=value2"};

        Properties props = ObjectUtil.argsToProps(args);

        assertEquals("value1", props.get("key1"));
        assertEquals("value2", props.get("key2"));
    }

    @Test
    public void testBufToString()
    {
        byte[] buffer = new byte[32];

        for (int i = 0; i < buffer.length; i++)
        {
            buffer[i] = (byte) i;
        }

        String str = ObjectUtil.bufToString(buffer, 0, buffer.length);

        assertNotNull(str);
    }
}
