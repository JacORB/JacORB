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

package org.jacorb.test.idl;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import junit.framework.Test;
import org.jacorb.test.common.TestUtils;
import org.omg.CORBA.TypeCode;

/**
 * this test will try to process and compile all idl files included
 * in the directory <TEST_HOME>/idl/compiler/succeed.
 * this test assumes the idl files to be correct and
 * will fail if JacIDL or javac causes an error during processing.
 *
 * additionally if you'd like to verify the compiled classes
 * you can optionally define a method that must adhere to the following
 * signature: public void verify_<FILENAME>(ClassLoader cl) {...}
 * (dots in filename will be converted to _). inside the method you can then
 * load and inspect the compiled classes.
 *
 * @author Alphonse Bendt
 */
public class ParseValidIDLTest extends AbstractIDLTestcase
{
    public ParseValidIDLTest(File file)
    {
        super("testCanParseValidIDL", file);
    }

    /**
     * this is the main test method. it will be invoked
     * for every .idl file that is found in the source directory
     */
    public void testCanParseValidIDL() throws Exception
    {
        // decide wether to spawn an extra process for JacIDL.
        // defaults to false.
        runJacIDL(false, shouldSpawnJacIDLProcess(idlFile));

        ClassLoader cl = compileGeneratedSources(false);

        invokeVerifyMethod(cl);

        // if a test fails the directory
        // will not be deleted. this way
        // the contents can be inspected.
        TestUtils.deleteRecursively(dirCompilation);
        TestUtils.deleteRecursively(dirGeneration);
    }

    /**
     *  should the IDL file idlFile be parsed in its own Process?
     *
     *  Bit horrible here - compiling some standalone IDL's always seemed to work. Maybe its an interaction
     *  with the threading in the parser/interfacebody. However if I execute this as a separate process it works
     *  fine - so for a test its still ok.
     */
    private boolean shouldSpawnJacIDLProcess(File idlFile)
    {
//        if (idlFile.getName().endsWith("basetypes.idl"))
//        {
//            return true;
//        }

        return false;
    }

    /**
     * related to RT#1445. forward declarations in idl led
     * to incorrectly generated classes.
     */
    public void verify_rt1445_idl(ClassLoader cl) throws Exception
    {
        Class nodeClazz = cl.loadClass("tree.Node");

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
        Class clazz = cl.loadClass("bugrtj999.THIS_DOESNT_WORK");
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
        Class clazz = cl.loadClass("PragmaBug.TestHelper");
        Method method = clazz.getMethod("id", (Class[])null);
        assertEquals("IDL:acme.com/PragmaBug/Test:1.0", method.invoke(null, (Object[])null));
    }

    public void verify_valueTest_idl(ClassLoader cl) throws Exception
    {
        Class clazz = cl.loadClass("test.ValueTestHelper");
        Method method = clazz.getMethod("type", new Class[0]);
        TypeCode result = (TypeCode) method.invoke(null, new Object[0]);

        assertEquals(2, result.member_count());
        assertEquals("member1", result.member_name(0));
        assertEquals("member2", result.member_name(1));
    }

    public static Test suite()
    {
        final String dir = TestUtils.testHome() + "/idl/compiler/succeed";
        return suite(dir, ParseValidIDLTest.class);
    }
}
