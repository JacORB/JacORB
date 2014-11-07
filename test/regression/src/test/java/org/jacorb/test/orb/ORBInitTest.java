package org.jacorb.test.orb;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import org.jacorb.test.harness.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.INITIALIZE;
import org.omg.CORBA.ORB;


/**
 * <code>ORBInitTest</code> tests ORBInit parsing
 *
 * @author Nick Cross
 */
public class ORBInitTest
{
    private final List<ORB> orbs = new ArrayList<ORB>();

    @Before
    public void setUp() throws Exception
    {
        PreInitFail.reset();
        PostInitFail.reset();
    }

    @After
    public void tearDown() throws Exception
    {
        for (Iterator<ORB> iter = orbs.iterator(); iter.hasNext();)
        {
            ORB orb = iter.next();
            orb.shutdown(true);
        }
        orbs.clear();
    }

    /**
     * <code>testParse1</code>
     */
    @Test (expected=BAD_PARAM.class)
    public void testParse1()
    {
        String args[] = new String[2];
        args[0] = "-ORBInitRef.NameService";
        args[1] = "NameService";

        initORB(args, null);
    }

    private ORB initORB(String[] args, Properties props)
    {
        if (props == null)
        {
            props = new Properties();
        }
        props.setProperty("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
        props.setProperty("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");
        if (TestUtils.verbose)
        {
            props.setProperty("jacorb.log.default.verbosity", "4");
        }
        else
        {
            props.setProperty("jacorb.log.default.verbosity", "0");
        }
        ORB orb = org.omg.CORBA.ORB.init( args, props );
        orbs.add(orb);
        return orb;
    }

    /**
     * <code>testParse2</code>
     */
    @Test
    public void testParse2 ()
    {
        String args[] = new String[2];
        args[0] = "-ORBInitRef";
        args[1] = "NameService=foo.ior";

        initORB( args, null );
    }

    @Test (expected=BAD_PARAM.class)
    public void testParse3 ()
    {
        String args[] = new String[1];
        args[0] = "-ORBInitRef";

        initORB( args, null );
    }

    @Test (expected=INITIALIZE.class)
    public void testORBInitializerFailClassException()
    {
        Properties props = new Properties();
        props.put("org.omg.PortableInterceptor.ORBInitializerClass.non.existent.class", "");
        props.put("jacorb.orb_initializer.fail_on_error", "on");

        initORB((String[]) null, props);
    }

    @Test
    public void testORBInitializerFailClassNoException()
    {
        Properties props = new Properties();
        props.put("org.omg.PortableInterceptor.ORBInitializerClass.none.existen.class", "");
        props.put("jacorb.orb_initializer.fail_on_error", "off");

        initORB((String[]) null, props);
    }

    @Test (expected=INITIALIZE.class)
    public void testORBInitializerFailConstructorException()
    {
        Properties props = new Properties();
        props.put("org.omg.PortableInterceptor.ORBInitializerClass.xyinit",
                  "org.jacorb.test.orb.ConstructorFail");
        props.put("jacorb.orb_initializer.fail_on_error", "on");

        initORB((String[]) null, props);
    }

    @Test
    public void testORBInitializerFailConstructorNoException()
    {
        Properties props = new Properties();
        props.put("org.omg.PortableInterceptor.ORBInitializerClass.xyinit",
                  "org.jacorb.test.orb.ConstructorFail");
        props.put("jacorb.orb_initializer.fail_on_error", "off");

        initORB((String[]) null, props);
    }

    @Test (expected=INITIALIZE.class)
    public void testORBInitializerFailPreInitException()
    {
        Properties props = new Properties();
        props.put("org.omg.PortableInterceptor.ORBInitializerClass.xyinit",
                  "org.jacorb.test.orb.PreInitFail");
        props.put("jacorb.orb_initializer.fail_on_error", "on");

        initORB((String[]) null, props);
    }

    @Test
    public void testORBInitializerFailPreInitNoException()
    {
        Properties props = new Properties();
        props.put("org.omg.PortableInterceptor.ORBInitializerClass.xyinit",
                  "org.jacorb.test.orb.PreInitFail");
        props.put("jacorb.orb_initializer.fail_on_error", "off");

        initORB((String[]) null, props);
     }

    @Test (expected=INITIALIZE.class)
    public void testORBInitializerFailPostInitException()
    {
        Properties props = new Properties();
        props.put("org.omg.PortableInterceptor.ORBInitializerClass.xyinit",
                  "org.jacorb.test.orb.PostInitFail");
        props.put("jacorb.orb_initializer.fail_on_error", "on");

        initORB((String[]) null, props);
    }

    @Test
    public void testORBInitializerFailPostInitNoException()
    {
        Properties props = new Properties();
        props.put("org.omg.PortableInterceptor.ORBInitializerClass.xyinit",
                  "org.jacorb.test.orb.PostInitFail");
        props.put("jacorb.orb_initializer.fail_on_error", "off");

        initORB((String[]) null, props);
    }

    @Test
    public void testDontInvokePostInitIfPreInitFailed()
    {
        Properties props = new Properties();
        props.put("org.omg.PortableInterceptor.ORBInitializerClass.xyinit",
                  "org.jacorb.test.orb.PreInitFail");
        props.put("jacorb.orb_initializer.fail_on_error", "off");

        initORB((String[]) null, props);

        assertEquals(1, PreInitFail.getPreCount());
        assertEquals(0, PreInitFail.getPstCount());
    }

    @Test
    public void testORBInitializerWrongClass1()
    {
        Properties props = new Properties();
        props.put("org.omg.PortableInterceptor.ORBInitializerClass.xyinit",
                  "java.lang.String");
        props.put("jacorb.orb_initializer.fail_on_error", "off");

        initORB((String[]) null, props);
    }

    @Test (expected=INITIALIZE.class)
    public void testORBInitializerWrongClass2()
    {
        Properties props = new Properties();
        props.put("org.omg.PortableInterceptor.ORBInitializerClass.xyinit",
                  "java.lang.String");
        props.put("jacorb.orb_initializer.fail_on_error", "on");

        initORB((String[]) null, props);
    }

    /**
     * <code>testSetORBId_1</code>
     */
    @Test
    public void testSetORBId_1 ()
    {
        String args[] = new String[4];
        args[0] = "-ORBInitRef";
        args[1] = "NameService=foo.ior";
        args[2] = "-ORBID";
        args[3] = "jacorb: someOrbId_1";

        // set ORBID by using commandline arguments
        ORB orb = initORB(args, null);
        assertTrue(orb != null);
        assertEquals(args[3],orb.id());
    }

     /**
     * <code>testSetORBId_3</code>
     */
    @Test (expected=INITIALIZE.class)
    public void testSetORBId_3 ()
    {
        String args[] = new String[3];
        args[0] = "-ORBInitRef";
        args[1] = "NameService=foo.ior";
        args[2] = "-ORBID";
        // args[3] = "jacorb: someOrbId_1";

        // test for -ORBID missing value
        ORB orb = initORB(args, null);
        assertTrue(orb == null);
    }

    /**
     * <code>testSetORBId_4</code>
     */
    @Test
    public void testSetORBId_4 ()
    {
        String args[] = new String[2];
        args[0] = "-ORBInitRef";
        args[1] = "NameService=foo.ior";

        // set ORBID to default ORBID by setting third argument to null
        // get default ORBID
        ORB orb = initORB(args, null);
        assertTrue (orb != null);
        String def_id = new String(orb.id());

        // run test
        ORB orb2 = initORB(args, null);
        assertTrue (orb2 != null);
        assertEquals(def_id, orb2.id());
    }

    /**
     * <code>testSetORBId_5</code>
     */
    @Test
    public void testSetORBId_5 ()
    {
        String args[] = new String[2];
        args[0] = "-ORBInitRef";
        args[1] = "NameService=foo.ior";

        // set ORBID to an empty string
        ORB orb = initORB(args, null);
        assertTrue (orb != null);
        String _id = new String(orb.id());
        assertEquals("", _id);
    }

    /**
     * <code>testSetORBId_6</code>
     */
    @Test
    public void testSetORBId_6 ()
    {
        String args[] = new String[4];
        args[0] = "-ORBInitRef";
        args[1] = "NameService=foo.ior";
        args[2] = "-ORBID";
        args[3] = "";

        // set -ORBID to an empty string in an argument
        ORB orb = initORB(args, null);
        assertTrue(orb != null);
        assertEquals("", orb.id());
    }

    /**
     * <code>testSetORBId_7</code>
     */
    @Test
    public void testSetORBId_7() throws Exception
    {
        // ORBid is not set.  ORB.id is set to an empty string.
        // So, ORBid should be "jacorb"
        try
        {
            createPropertiesFile("target/test-classes/jacorb.properties",
                                 "jacorb.connection.client.connect_timeout=33099");

            Properties props = new Properties();
            props.put("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
            props.put("org.omg.CORBA.ORBSingletonClass",
                    "org.jacorb.orb.ORBSingleton");

            // System.setProperty("ORBid", "");

            ORB orb = initORB(new String[] {}, props);
            assertTrue(orb != null);
            assertEquals("", orb.id());

            int timeout = ((org.jacorb.orb.ORB) orb).getConfiguration()
                    .getAttributeAsInteger(
                            "jacorb.connection.client.connect_timeout", 0);
            assertEquals(33099, timeout);
        }
        finally
        {
            deletePropertiesFile ("target/test-classes/jacorb.properties");
        }
    }

    private void createPropertiesFile (String name, String content) throws IOException
    {
        File file = new File(TestUtils.testHome(), name);
        File parent = file.getParentFile();

        parent.mkdirs();
        PrintWriter out = new PrintWriter (new FileWriter (file));
        out.println (content);
        out.close();
    }

    private void deletePropertiesFile (String name)
    {
        File f = new File(TestUtils.testHome(), name);
        f.delete();
    }
}
