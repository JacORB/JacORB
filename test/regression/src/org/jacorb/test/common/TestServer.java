package org.jacorb.test.common;

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

import java.util.Properties;
import org.jacorb.config.Configurable;
import org.jacorb.config.Configuration;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Policy;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.Servant;
import org.slf4j.Logger;

/**
 * A server program that can set up an arbitrary CORBA servant.
 * The program takes the name of the servant class to use from
 * the command line.  It then creates an instance of this class,
 * using its no-arg constructor (which must exist).  It registers
 * the instance with the POA, and prints the resulting IOR to
 * standard output.  If anything goes wrong, a message starting
 * with the string "ERROR" is instead written to standard output.
 * <p>
 * This program is intended to be used with a
 * {@link ClientServerSetup ClientServerSetup}.  To read and process
 * the <code>TestServer</code>'s output from another program such as
 * the above, JacORB's normal diagnostic messages should be completely
 * silenced.  This must be done using the normal configuration settings,
 * e.g. from the command line (see
 * {@link ClientServerSetup#setUp ClientServerSetup.setUp()} for an
 * example).
 * <p>
 * @author Andre Spiegel <spiegel@gnu.org>
 */
public class TestServer
{
    private static Boolean useCorbaloc = null;

    /**
     * Returns true if this TestServer should make its object available
     * via a corbaloc URL.
     */
    public static boolean useCorbaloc()
    {
        if (useCorbaloc == null)
        {
            String prop = System.getProperty ("jacorb.test.corbaloc.enable",
                                              "false");
            useCorbaloc = Boolean.valueOf(prop);
        }
        return useCorbaloc.booleanValue();
    }

    /**
     * Creates and returns the POA with which the test server object should be
     * registered.  In the normal case, this is just the RootPOA.  When
     * Corbaloc is used, a child POA with the appropriate settings is created
     * and returned.
     */
    public static POA createPOA (ORB orb) throws Exception
    {
        POA rootPOA = POAHelper.narrow
        (
            orb.resolve_initial_references ("RootPOA")
        );
        rootPOA.the_POAManager().activate();
        if (useCorbaloc())
        {
            Policy[] p = new Policy[2];
            p[0] = rootPOA.create_lifespan_policy (LifespanPolicyValue.PERSISTENT);
            p[1] = rootPOA.create_id_assignment_policy (IdAssignmentPolicyValue.USER_ID);

            POA poa = rootPOA.create_POA
            (
                System.getProperty("jacorb.test.corbaloc.poaname"),
                rootPOA.the_POAManager(), p
            );
            return poa;
        }
        else
        {
            return rootPOA;
        }
    }

    /**
     * Returns the Corbaloc URL under which the test server object
     * can be accessed.  This only works if jacorb.test.corbaloc.enable is
     * true, and the additional parameters are set via properties, see below.
     */
    public static String getCorbaloc()
    {
        StringBuffer result = new StringBuffer();
        result.append ("corbaloc::localhost:");
        result.append (System.getProperty ("jacorb.test.corbaloc.port"));
        result.append ("/");
        result.append (System.getProperty ("jacorb.test.corbaloc.implname"));
        result.append ("/");
        result.append (System.getProperty ("jacorb.test.corbaloc.poaname"));
        result.append ("/");
        result.append (System.getProperty ("jacorb.test.corbaloc.objectid"));
        return result.toString();
    }

    public static void main (String[] args)
    {
        Thread.currentThread().setContextClassLoader(TestServer.class.getClassLoader());

        Logger logger = null;
        try
        {
            Properties props = new Properties();
            if (useCorbaloc())
            {
                props.put ("OAPort",
                           System.getProperty("jacorb.test.corbaloc.port", "0"));
                props.put ("OASSLPort",
                        System.getProperty("jacorb.test.corbaloc.sslport", "0"));
                props.put ("jacorb.implname",
                           System.getProperty("jacorb.test.corbaloc.implname"));
            }

            //init ORB
            ORB orb = ORB.init (args, props);

            log("init ORB");

            try
            {
                Configuration config = ((org.jacorb.orb.ORB)orb).getConfiguration();
                logger = ((org.jacorb.config.Configuration)config).getLogger("TestServer");
            }
            catch (ClassCastException e)
            {
                // ignore. not a JacORB ORB
            }

            //init POA
            POA poa = createPOA (orb);

            log("init POA");

            final String servantName = args[0];

            log("use ServantClass: " + servantName);

            Class servantClass = TestUtils.classForName(servantName);
            Servant servant = ( Servant ) servantClass.newInstance();

            log("using Servant: " + servant);

            if (servant instanceof Configurable && orb instanceof org.jacorb.orb.ORB)
            {
                ((Configurable)servant).configure (((org.jacorb.orb.ORB)orb).getConfiguration());
                log("configured Servant");
            }

            final String ior;
            if (useCorbaloc())
            {
                String oid = System.getProperty ("jacorb.test.corbaloc.objectid");
                poa.activate_object_with_id (oid.getBytes(), servant);
                String shortcut = System.getProperty ("jacorb.test.corbaloc.shortcut");
                if (shortcut != null)
                    ((org.jacorb.orb.ORB)orb).addObjectKey(shortcut,
                            System.getProperty ("jacorb.test.corbaloc.implname")
                    + "/" + System.getProperty ("jacorb.test.corbaloc.poaname")
                    + "/" + System.getProperty ("jacorb.test.corbaloc.objectid"));

                ior = "SERVER IOR: " + getCorbaloc();
            }
            else
            {
                // create the object reference
                org.omg.CORBA.Object obj = poa.servant_to_reference( servant );
                ior = "SERVER IOR: " + orb.object_to_string(obj);
            }

            /**
             * NOTE: output SERVER IOR for client to pick it up.
             * Don't delete or the test will fail.
             */
            System.out.println(ior);

            TestUtils.log("using IOR: " + ior);
            System.out.flush();

            if (logger != null)
            {
                logger.debug("Entering ORB event loop" );
            }
            // wait for requests
            orb.run();
        }
        catch( Exception e )
        {
            if (logger != null)
            {
                logger.error ("TestServer error ", e);
            }
            else
            {
                System.err.println ("TestServer error " + e);
                e.printStackTrace();
            }
        }
    }

    private static void log(String msg)
    {
        TestUtils.log("TestServer: " + msg);
    }
}
