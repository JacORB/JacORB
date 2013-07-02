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
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jacorb.orb.CDRInputStream;
import org.jacorb.orb.CDROutputStream;
import org.jacorb.test.common.TestUtils;
import org.jacorb.test.idl.bugjac144.BugJac144ObjectCachePlugin;
import org.omg.CORBA.Any;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;

/**
 * these tests do not fit in the standard scheme
 * of valid/invalid idl tests.
 * to be able to parse the idl files here
 * JacIDL needs more setup. in this
 * case an additional param is needed.
 * therefor the method createJacIDLArgs has
 * been overwritten.
 *
 * @author Alphonse Bendt
 */
public class ValidIDLWithExtraSetupTest extends AbstractIDLTestcase
{
    private static final String TEST_HOME = TestUtils.testHome();
    private static final String IDL_DIR = "/idl/compiler/misc/";

    private static final String[] TWO_ONE_STRINGS = new String[] {"TWO", "ONE"};
    private static final String[] ONE_TWO_STRINGS = new String[] {"ONE", "TWO"};

    private final List arguments = new ArrayList();

    public ValidIDLWithExtraSetupTest(String[] argList, String file)
    {
        super
        (
            "testMiscParseGood",
            (new File(file)).isAbsolute() ? new File(file) : new File(TEST_HOME + IDL_DIR + file)
        );
        arguments.addAll(Arrays.asList(argList));

        arguments.add("-d");
        arguments.add(dirGeneration.getAbsolutePath());
        arguments.add(idlFile.getAbsolutePath());
    }

    public ValidIDLWithExtraSetupTest(String arg, String file)
    {
        this(new String[] {arg}, file);
    }

    protected String[] createJacIDLArgs()
    {
        String args[] = new String[arguments.size()];

        for(int x=0; x<arguments.size(); ++x)
        {
            args[x] = (String) arguments.get(x);
        }

        return args;
    }

    /**
     * this is the main testmethod
     */
    public void testMiscParseGood() throws Exception
    {
        ClassLoader clOld = Thread.currentThread().getContextClassLoader();

        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

        try
        {
            runJacIDL(false, false);
            ClassLoader cl = compileGeneratedSources(false);

            invokeVerifyMethod(cl);
            TestUtils.deleteRecursively(dirGeneration);
            TestUtils.deleteRecursively(dirCompilation);
        }
        finally
        {
            Thread.currentThread().setContextClassLoader(clOld);
        }
    }

    /**
     * tests the -genEnhanced option of JacIDL
     */
    public void verify_bugJac149_idl(ClassLoader cl) throws Exception
    {
        testGeneratedToStringMethod(cl);

        testGeneratedEqualsMethod(cl);
    }

    private void testGeneratedEqualsMethod(ClassLoader cl) throws Exception
    {
        Object struct1 = newVarLengthStruct(cl, null, false, "NST1", "NST1_1", ONE_TWO_STRINGS);
        Object struct2 = newVarLengthStruct(cl, "V2", false, "NST2", "NST2_1", ONE_TWO_STRINGS);
        Object struct3 = newVarLengthStruct(cl, "", true, null, null, null);
        Object struct4 = newVarLengthStruct(cl, "V2", false, "NST2", "NST2_1", TWO_ONE_STRINGS);
        Object struct5 = newVarLengthStruct(cl, null, false, "NST1", "NST1_1", ONE_TWO_STRINGS);

        assertEquals(struct1, struct1);
        assertEquals(struct1, struct5);
        assertEquals(struct5, struct1);
        assertFalse(struct2.equals(struct4));
        assertFalse (struct4.equals(struct2));
        assertEquals(struct3, struct3);
        assertSame(struct3, struct3);
        assertFalse(struct3.equals(struct4));
    }

    private void testGeneratedToStringMethod(ClassLoader cl) throws Exception
    {
        Object struct = newVarLengthStruct(cl, "V2", false, "NST2", "NST2_1", TWO_ONE_STRINGS);

        String structString = struct.toString();

        // Hackathon - we know that the struct should have these values
        // so check they are there.
        assertTrue (structString.indexOf("V2") != -1);
        assertTrue (structString.indexOf("TWO") != -1);
        assertTrue (structString.indexOf("ONE") != -1);
        assertTrue (structString.indexOf("NST2") != -1);
        assertTrue (structString.indexOf("NST2_1") != -1);
        assertTrue (structString.indexOf("false") != -1);
        assertTrue (structString.indexOf("10") != -1);
    }

    private Object newVarLengthStruct(ClassLoader cl, String m1, boolean alarmMonitoringIndicator, String name, String value, String[] m1Seq) throws Exception
    {
        Any any = org.omg.CORBA.ORB.init().create_any();
        any.insert_short ((short)10);

        Class nameValueClazz = cl.loadClass("org.jacorb.test.bugs.bugjac149.NameAndStringValue_T");
        Constructor nameValueCTOR = nameValueClazz.getConstructor(new Class[] {String.class, String.class});
        Object nameValue = nameValueCTOR.newInstance(new Object[] {name, value});

        Class structClazz = cl.loadClass("org.jacorb.test.bugs.bugjac149.VarLengthStruct");
        Constructor structCTOR = structClazz.getConstructor(new Class[] {String.class, Boolean.TYPE, Any.class, nameValueClazz, String[].class});

        return structCTOR.newInstance(new Object[] {m1, Boolean.valueOf(alarmMonitoringIndicator), any, nameValue, m1Seq});
    }

    public void verify_bugJac44_idl(ClassLoader cl) throws Exception
    {
        Class clazz = cl.loadClass("apmInterface.SA_Connection");
        assertNotNull(clazz);
    }

    public void verify_scoping10_idl(ClassLoader cl) throws Exception
    {
        assertNotNull(cl.loadClass("myTestPackage.sMyStruct"));
    }

   public void verify_bugJac516_idl(ClassLoader cl) throws Exception
   {
       Class helperClazz = cl.loadClass("bugJac516.MyFixedHelper");

       verifyWriteMethod(helperClazz);

       verifyReadMethod(helperClazz);

       try
       {
           cl.loadClass("bugJac516.Helper");
           fail();
       }
       catch(ClassNotFoundException e)
       {
       }
   }

   private void verifyReadMethod(Class helperClazz) throws Exception
   {
       final org.omg.CORBA.portable.InputStream in;
       final BigDecimal result = new BigDecimal("432.1");

       if (arguments.contains("deprecated"))
       {
           in = new CDRInputStream(new byte[0])
           {
               public BigDecimal read_fixed()
               {
                   return result;
               }
           };
       }
       else
       {
           in = new CDRInputStream(new byte[0])
           {
               public BigDecimal read_fixed(short digits, short scale)
               {
                   assertEquals(4, digits);
                   assertEquals(1, scale);

                   return result;
               }
           };
       }

       Method readMethod = helperClazz.getMethod("read", new Class[] {org.omg.CORBA.portable.InputStream.class});

       if (arguments.contains("deprecated"))
       {
           // due to the extra modification of the point position in
           // the deprecated version of the stubs we need to expect another
           // result here.
           assertEquals(new BigDecimal("43.21"), readMethod.invoke(null, new Object[] {in}));
       }
       else
       {
           assertEquals(result, readMethod.invoke(null, new Object[] {in}));
       }
   }

   private void verifyWriteMethod(Class helperClazz) throws Exception
   {
       final org.omg.CORBA.portable.OutputStream out;
       final boolean[] success = new boolean[1];

       if (arguments.contains("deprecated"))
       {
           out = new CDROutputStream()
           {
               public void write_fixed(BigDecimal value, short digits, short scale)
               {
                   fail("generated code should invoke deprecated write_fixed method");
               }

               public void write_fixed(BigDecimal b)
               {
                   success[0] = true;
               }
           };
       }
       else
       {
           out = new CDROutputStream()
           {
               public void write_fixed(BigDecimal value, short digits, short scale)
               {
                   success[0] = true;
               }

               public void write_fixed(BigDecimal b)
               {
                   fail("generated code should invoke non deprecated write_fixed method");
               }
           };
       }

       Method writeMethod = helperClazz.getMethod("write", new Class[] {org.omg.CORBA.portable.OutputStream.class, BigDecimal.class});
       writeMethod.invoke(null, new Object[] {out, new BigDecimal("321.2")});

       assertTrue("write_fixed method wasn't invoked", success[0]);
   }

    public void verify_bugRTJ519_idl(ClassLoader cl) throws Exception
    {
        Class clazz = cl.loadClass("test.TestInterface");
        Method method = clazz.getMethod("test_method", new Class[] {Servant.class, POA.class});
        assertNotNull(method);
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite();

        suite.addTest(new ValidIDLWithExtraSetupTest("-DBLUB", "defined.idl"));
        suite.addTest(new ValidIDLWithExtraSetupTest("-I" + TEST_HOME + "/../../idl/omg", "Interoperability.idl"));
        suite.addTest(new ValidIDLWithExtraSetupTest("-I" + TEST_HOME + "/../../idl/omg", "rt1051.idl"));
        suite.addTest(new ValidIDLWithExtraSetupTest("-DDefB", "Ping1.idl"));
        suite.addTest(new ValidIDLWithExtraSetupTest("-ami_callback", "ami.idl"));
        suite.addTest(new ValidIDLWithExtraSetupTest("-ami_callback", "rt1180.idl"));
        suite.addTest(new ValidIDLWithExtraSetupTest("-I" + TEST_HOME + "/../../idl/omg", "bugJac307.idl"));
        suite.addTest(new ValidIDLWithExtraSetupTest(new String[] {"-ir", "-i2jpackage", "AlarmIRPSystem:org._3gpp.AlarmIRPSystem"}, "bugJac101.idl"));
        suite.addTest(new ValidIDLWithExtraSetupTest("-genEnhanced", "bugJac149.idl"));
        suite.addTest(new ValidIDLWithExtraSetupTest(new String[] {"-ami_callback", "-diistub"}, "ami.idl"));
        suite.addTest(new ValidIDLWithExtraSetupTest("-sloppy_names", TEST_HOME + "/idl/compiler/fail/sloppy.idl"));
        suite.addTest(new ValidIDLWithExtraSetupTest(new String[] {"-ir", "-i2jpackage", "test:de.siemens.hyades.test"}, "bug514.idl"));
        suite.addTest(new ValidIDLWithExtraSetupTest(new String[] {"-i2jpackage", ":myTestPackage"}, TEST_HOME + "/idl/compiler/succeed/scoping10.idl"));
        suite.addTest(new ValidIDLWithExtraSetupTest(new String[] {"-i2jpackage", ":apmInterface"}, "bugJac44.idl"));
        suite.addTest(new ValidIDLWithExtraSetupTest(new String[] {"-generate_helper", "deprecated"} , "bugJac516.idl"));

        try
        {
            org.omg.CORBA.portable.InputStream.class.getMethod("read_fixed", new Class[] {short.class, short.class});
            suite.addTest(new ValidIDLWithExtraSetupTest(new String[] {"-generate_helper", "portable"}, "bugJac516.idl"));
        }
        catch(Exception e)
        {
            System.out.println("1 test ignored because wrong org.omg.CORBA (not the ones provided by JacORB) classes are on the bootclasspath");
        }
        suite.addTest(new ValidIDLWithExtraSetupTest(new String[] {"-generate_helper", "jacorb"}, "bugJac516.idl"));

        suite.addTest(new ValidIDLWithExtraSetupTest(new String[] {"-cacheplugin", BugJac144ObjectCachePlugin.class.getName()}, "bugJac144.idl"));
        suite.addTest(new ValidIDLWithExtraSetupTest(new String[] {}, "bugJac144.idl"));
        suite.addTest(new ValidIDLWithExtraSetupTest("-sloppy_identifiers", TEST_HOME + "/idl/compiler/fail/collision.idl"));
        suite.addTest(new ValidIDLWithExtraSetupTest(new String[] {"-all", "-I" + TEST_HOME + IDL_DIR} , "895_1.idl"));
        suite.addTest(new ValidIDLWithExtraSetupTest(new String[] {"-I" + TEST_HOME + "/../../idl/omg"}, "bugRTJ519.idl"));
        return suite;
    }
}
