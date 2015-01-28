/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2014 Gerald Brose / The JacORB Team.
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

package org.jacorb.test.idl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jacorb.test.harness.TestUtils;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;
import org.omg.CORBA.TypeCode;

/**
 * this test will try to process and compile all idl files included
 * in the directory &lt;TEST_HOME&gt;/idl/compiler/succeed.
 * this test assumes the idl files to be correct and
 * will fail if JacIDL or javac causes an error during processing.
 *
 * additionally if you'd like to verify the compiled classes
 * you can optionally define a method that must adhere to the following
 * signature: public void verify_&lt;FILENAME&gt;(ClassLoader cl) {...}
 * (dots in filename will be converted to _). inside the method you can then
 * load and inspect the compiled classes.
 *
 * @author Alphonse Bendt
 */
public class ParseValidIDLTest extends AbstractIDLTestcase
{
    final static String IDL = TestUtils.testHome() + "/src/test/idl/compiler/succeed";

    @Parameters(name="{index} + {0}")
    public static Collection<Object[]> data()
    {
        List<Object[]> params = new ArrayList<Object[]>();
        File fileNames[] = new File(IDL).listFiles(new FilenameFilter()
        {
            @Override
            public boolean accept(File dir, String name)
            {
                return name.endsWith(".idl");
            }
        });

        for (File file : fileNames) {
            params.add(new Object[] { file });
        }
        return params;
    }

    public ParseValidIDLTest(File file) throws IOException
    {
        super(file);
    }

    /**
     * this is the main test method. it will be invoked
     * for every .idl file that is found in the source directory
     */
    @Test
    public void testCanParseValidIDL() throws Exception
    {
        // decide wether to spawn an extra process for JacIDL.
        // defaults to false.
        runJacIDL(false);

        ClassLoader cl = compileGeneratedSources(false);

        invokeVerifyMethod(cl);
    }

    /**
     * related to RT#1445. forward declarations in idl led
     * to incorrectly generated classes.
     */
    public void verify_rt1445_idl(ClassLoader cl) throws Exception
    {
        Class<?> nodeClazz = cl.loadClass("tree.Node");

        nodeClazz.getDeclaredField("name");
        nodeClazz.getDeclaredField("description");
        nodeClazz.getDeclaredField("children");
    }

    /**
     * <code>verify_bugrtj999_idl</code> verifies that IDl with long constants
     * and shift/multiply/etc operators generate the correct long/int values.
     *
     * These hardcoded values come from VisiBroker8.0 which generates the values
     * inline as opposed to relying on the Java JVM.
     *
     * @param cl a <code>ClassLoader</code> value
     * @exception Exception if an error occurs
     */
    public void verify_bugrtj999_idl(ClassLoader cl) throws Exception
    {
        Class<?> clazz = cl.loadClass("bugrtj999.THIS_DOESNT_WORK");
        Field f = clazz.getDeclaredField ("value");
        assertTrue (16384L == f.getLong (null));

        clazz = cl.loadClass("bugrtj999.addlong");
        f = clazz.getDeclaredField ("value");
        assertTrue (25L == f.getLong (null));

        clazz = cl.loadClass("bugrtj999.THIS_WORKS");
        f = clazz.getDeclaredField ("value");
        assertTrue (2048L == f.getLong (null));

        clazz = cl.loadClass("bugrtj999.foo");
        f = clazz.getDeclaredField ("value");
        assertTrue (176 == f.getInt (null));
    }

    public void verify_bugpt480_idl(ClassLoader cl) throws Exception
    {
        cl.loadClass("org.jacorb.test.bugs.bugpt480.ExceptionOne");

        cl.loadClass("org.jacorb.test.bugs.bugpt480.ExceptionOneHelper");

        cl.loadClass("org.jacorb.test.bugs.bugpt480.ExceptionOneHolder");

        cl.loadClass("org.jacorb.test.bugs.bugpt480.FooPackage.ExceptionTwo");

        cl.loadClass("org.jacorb.test.bugs.bugpt480.FooPackage.ExceptionTwoHelper");

        cl.loadClass("org.jacorb.test.bugs.bugpt480.FooPackage.ExceptionTwoHolder");
    }

    public void verify_bugjac569_idl(ClassLoader cl) throws Exception
    {
        Class<?> clazz = cl.loadClass("PragmaBug.TestHelper");
        Method method = clazz.getMethod("id", (Class[])null);
        assertEquals("IDL:acme.com/PragmaBug/Test:1.0", method.invoke(null, (Object[])null));
    }

    public void verify_valueTest_idl(ClassLoader cl) throws Exception
    {
        Class<?> clazz = cl.loadClass("test.ValueTestHelper");
        Method method = clazz.getMethod("type", new Class[0]);
        TypeCode result = (TypeCode) method.invoke(null, new Object[0]);

        assertEquals(2, result.member_count());
        assertEquals("member1", result.member_name(0));
        assertEquals("member2", result.member_name(1));
    }

    public void verify_typedefstring_idl(ClassLoader cl) throws Exception
    {
        Class<?> clazz = cl.loadClass("test.MyStruct");
        Object obj = clazz.newInstance();
        Field f1 = clazz.getDeclaredField ("name");
        Field f2 = clazz.getDeclaredField ("id");
        assertTrue ("".equals(f1.get(obj)));
        assertTrue ("".equals(f2.get(obj)));
    }
}
