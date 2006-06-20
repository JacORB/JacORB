/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jacorb.test.common.TestUtils;
import org.omg.CORBA.Any;

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
 * @version $Id$
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
        super("testMiscParseGood", new File(TEST_HOME + IDL_DIR + file));
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
        runJacIDL(false);
        ClassLoader cl = compileGeneratedSources(false);

        invokeVerifyMethod(cl);
        deleteRecursively(dirGeneration);
        deleteRecursively(dirCompilation);
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

        return suite;
    }
}
