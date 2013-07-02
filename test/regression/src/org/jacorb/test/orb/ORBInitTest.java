package org.jacorb.test.orb;

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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import junit.framework.TestCase;
import org.jacorb.test.common.TestUtils;
import org.omg.CORBA.INITIALIZE;
import org.omg.CORBA.ORB;


/**
 * <code>ORBInitTest</code> tests ORBInit parsing
 *
 * @author Nick Cross
 */
public class ORBInitTest extends TestCase
{
    private final List orbs = new ArrayList();

    protected void setUp() throws Exception
    {
        PreInitFail.reset();
        PostInitFail.reset();
    }

    protected void tearDown() throws Exception
    {
        for (Iterator iter = orbs.iterator(); iter.hasNext();)
        {
            ORB orb = (ORB) iter.next();
            orb.shutdown(true);
        }
        orbs.clear();
    }

    /**
     * <code>testParse1</code>
     */
    public void testParse1()
    {
        String args[] = new String[2];
        args[0] = "-ORBInitRef.NameService";
        args[1] = "NameService";

        try
        {
            initORB(args, null);
            fail();
        }
        catch (org.omg.CORBA.BAD_PARAM e )
        {
            // expected
        }
    }

    private ORB initORB(String[] args, Properties props)
    {
        ORB orb = org.omg.CORBA.ORB.init( args, props );
        orbs.add(orb);
        return orb;
    }

    /**
     * <code>testParse2</code>
     */
    public void testParse2 ()
    {
        String args[] = new String[2];
        args[0] = "-ORBInitRef";
        args[1] = "NameService=foo.ior";

        initORB( args, null );
    }

    public void testParse3 ()
    {
        String args[] = new String[1];
        args[0] = "-ORBInitRef";

        try
        {
            initORB( args, null );
            fail();
        }
        catch (org.omg.CORBA.BAD_PARAM e )
        {
            // expected
        }
    }

    public void testORBInitializerFailClassException()
    {
        Properties props = new Properties();
        props.put("org.omg.PortableInterceptor.ORBInitializerClass.non.existent.class", "");
        props.put("jacorb.orb_initializer.fail_on_error", "on");

        try
        {
            initORB((String[]) null, props);
            fail( "No exception");
        }
        catch(org.omg.CORBA.INITIALIZE e)
        {
            // Correct exception
        }
    }

    public void testORBInitializerFailClassNoException()
    {
        Properties props = new Properties();
        props.put("org.omg.PortableInterceptor.ORBInitializerClass.none.existen.class", "");
        props.put("jacorb.orb_initializer.fail_on_error", "off");

        initORB((String[]) null, props);
    }

    public void testORBInitializerFailConstructorException()
    {
        Properties props = new Properties();
        props.put("org.omg.PortableInterceptor.ORBInitializerClass.xyinit",
                  "org.jacorb.test.orb.ConstructorFail");
        props.put("jacorb.orb_initializer.fail_on_error", "on");

        try
        {
            initORB((String[]) null, props);
            fail( "No exception");
        }
        catch(org.omg.CORBA.INITIALIZE e)
        {
            // Correct exception
        }
    }

    public void testORBInitializerFailConstructorNoException()
    {
        Properties props = new Properties();
        props.put("org.omg.PortableInterceptor.ORBInitializerClass.xyinit",
                  "org.jacorb.test.orb.ConstructorFail");
        props.put("jacorb.orb_initializer.fail_on_error", "off");

        initORB((String[]) null, props);
    }

    public void testORBInitializerFailPreInitException()
    {
        Properties props = new Properties();
        props.put("org.omg.PortableInterceptor.ORBInitializerClass.xyinit",
                  "org.jacorb.test.orb.PreInitFail");
        props.put("jacorb.orb_initializer.fail_on_error", "on");

        try
        {
            initORB((String[]) null, props);
            fail( "No exception");
        }
        catch(org.omg.CORBA.INITIALIZE e)
        {
            // Correct exception
        }
     }

    public void testORBInitializerFailPreInitNoException()
    {
        Properties props = new Properties();
        props.put("org.omg.PortableInterceptor.ORBInitializerClass.xyinit",
                  "org.jacorb.test.orb.PreInitFail");
        props.put("jacorb.orb_initializer.fail_on_error", "off");

        initORB((String[]) null, props);
     }

    public void testORBInitializerFailPostInitException()
    {
        Properties props = new Properties();
        props.put("org.omg.PortableInterceptor.ORBInitializerClass.xyinit",
                  "org.jacorb.test.orb.PostInitFail");
        props.put("jacorb.orb_initializer.fail_on_error", "on");

        try
        {
            initORB((String[]) null, props);
            fail("No exception");
        }
        catch(org.omg.CORBA.INITIALIZE e)
        {
            // Correct exception
        }
    }

    public void testORBInitializerFailPostInitNoException()
    {
        Properties props = new Properties();
        props.put("org.omg.PortableInterceptor.ORBInitializerClass.xyinit",
                  "org.jacorb.test.orb.PostInitFail");
        props.put("jacorb.orb_initializer.fail_on_error", "off");

        initORB((String[]) null, props);
    }

    public void testDontInvokePostInitIfPreInitFailed()
    {
        Properties props = new Properties();
        props.put("org.omg.PortableInterceptor.ORBInitializerClass.xyinit",
                  "org.jacorb.test.orb.PreInitFail");
        props.put("jacorb.orb_initializer.fail_on_error", "off");

        org.omg.CORBA.ORB orb = initORB((String[]) null, props);

        assertEquals(1, PreInitFail.getPreCount());
        assertEquals(0, PreInitFail.getPstCount());

        orb.shutdown(true);
    }

    public void testORBInitializerWrongClass1()
    {
        Properties props = new Properties();
        props.put("org.omg.PortableInterceptor.ORBInitializerClass.xyinit",
                  "java.lang.String");
        props.put("jacorb.orb_initializer.fail_on_error", "off");

        initORB((String[]) null, props);
    }

    public void testORBInitializerWrongClass2()
    {
        Properties props = new Properties();
        props.put("org.omg.PortableInterceptor.ORBInitializerClass.xyinit",
                  "java.lang.String");
        props.put("jacorb.orb_initializer.fail_on_error", "on");

        try
        {
            initORB((String[]) null, props);
            fail();
        } catch (INITIALIZE e)
        {
            // expected
        }
    }

    /**
     * <code>testSetORBId_1</code>
     */
    public void testSetORBId_1 ()
    {
        String args[] = new String[4];
        args[0] = "-ORBInitRef";
        args[1] = "NameService=foo.ior";
        args[2] = "-ORBID";
        args[3] = "jacorb: someOrbId_1";

        // set ORBID by using commandline arguments
        try
        {
            ORB orb = initORB(args, null);
            assertTrue(orb != (ORB)null);
            assertEquals(args[3],orb.id());
        }
        catch (Exception e)
        {
                // not expected
            e.printStackTrace ();
        }
    }

     /**
     * <code>testSetORBId_3</code>
     */
    public void testSetORBId_3 ()
    {
        String args[] = new String[3];
        args[0] = "-ORBInitRef";
        args[1] = "NameService=foo.ior";
        args[2] = "-ORBID";
        // args[3] = "jacorb: someOrbId_1";

        // test for -ORBID missing value
        try
        {
            ORB orb = initORB(args, null);
           assertTrue(orb == (ORB)null);
        }
        catch (Exception e)
        {
                // expected
            e.printStackTrace ();
        }
    }

    /**
     * <code>testSetORBId_4</code>
     */
    public void testSetORBId_4 ()
    {
        String args[] = new String[2];
        args[0] = "-ORBInitRef";
        args[1] = "NameService=foo.ior";

        // set ORBID to default ORBID by setting third argument to null
        try
        {
            // get default ORBID
            ORB orb = initORB(args, null);
            assertTrue (orb != (ORB)null);
            String def_id = new String(orb.id());

            // run test
            ORB orb2 = initORB(args, null);
            assertTrue (orb2 != (ORB)null);
            assertEquals(def_id, orb2.id());
        }
        catch (Exception e)
        {
                // not expected
            e.printStackTrace ();
        }
    }

    /**
     * <code>testSetORBId_5</code>
     */
    public void testSetORBId_5 ()
    {
        String args[] = new String[2];
        args[0] = "-ORBInitRef";
        args[1] = "NameService=foo.ior";

        // set ORBID to an empty string
        try
        {
            ORB orb = initORB(args, null);
            assertTrue (orb != (ORB)null);
            String _id = new String(orb.id());
            assertEquals("", _id);
        }
        catch (Exception e)
        {
                // not expected
            e.printStackTrace ();
        }
    }

    /**
     * <code>testSetORBId_6</code>
     */
    public void testSetORBId_6 ()
    {
        String args[] = new String[4];
        args[0] = "-ORBInitRef";
        args[1] = "NameService=foo.ior";
        args[2] = "-ORBID";
        args[3] = "";

        // set -ORBID to an empty string in an argument
        try
        {
            ORB orb = initORB(args, null);
           assertTrue(orb != (ORB)null);
           assertEquals("", orb.id());
        }
        catch (Exception e)
        {
                // not expected
            e.printStackTrace ();
        }
    }

    /**
     * <code>testSetORBId_7</code>
     */
    public void testSetORBId_7() throws Exception
    {
        // ORBid is not set.  ORB.id is set to an empty string.
        // So, ORBid should be "jacorb"
        try
        {
            createPropertiesFile("classes/jacorb.properties",
                                 "jacorb.connection.client.connect_timeout=33099");

            Properties props = new Properties();
            props.put("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
            props.put("org.omg.CORBA.ORBSingletonClass",
                    "org.jacorb.orb.ORBSingleton");

            // System.setProperty("ORBid", "");

            ORB orb = initORB(new String[] {}, props);
            assertTrue(orb != (ORB)null);
            assertEquals("", orb.id());

            int timeout = ((org.jacorb.orb.ORB) orb).getConfiguration()
                    .getAttributeAsInteger(
                            "jacorb.connection.client.connect_timeout", 0);
            assertEquals(33099, timeout);
        }
        finally
        {
            deletePropertiesFile ("classes/jacorb.properties");
        }
    }

    /**
     * Convenience method for creating an os-dependent filename relative
     * to the test home directory.
     */
    private String getConfigFilename (String name)
    {
        return TestUtils.osDependentPath
        (
            TestUtils.testHome() + "/" + name
        );
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
