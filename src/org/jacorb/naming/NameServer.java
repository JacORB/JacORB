package org.jacorb.naming;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose.
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

import java.net.*;
import java.io.*;

import org.omg.PortableServer.*;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;

import org.jacorb.orb.*;
import org.jacorb.util.*;

import org.apache.avalon.framework.logger.Logger;


//#ifjdk 1.2
import org.jacorb.imr.util.ImRManager;
//#endif

/**
 *  The name server application
 *
 *  @author Gerald Brose, FU Berlin
 *  @version $Id$
 */


public class NameServer
{
    private static org.omg.CORBA.ORB orb = null;
    public static String name_delimiter = "/";
    private static String filePrefix = "_nsdb";

    /** the specific logger for this component */
    private static Logger logger = Debug.getNamedLogger("jacorb.naming");


    /**
     * The servant manager (servant activator) for the name server POA
     */


    static class NameServantActivatorImpl
        extends _ServantActivatorLocalBase
    {
        private org.omg.CORBA.ORB orb = null;

        public NameServantActivatorImpl(org.omg.CORBA.ORB orb)
        {
            this.orb = orb;
        }

        /**
         * @return - a servant initialized from a file
         */

        public Servant incarnate( byte[] oid, POA adapter )
            throws ForwardRequest
        {
            String oidStr = new String(oid);

            NamingContextImpl n = null;
            try
            {
                File f = new File( filePrefix + oidStr );
                if( f.exists() )
                {
                    if( logger.isDebugEnabled())
                        logger.debug("Reading in context state from file");

                    FileInputStream f_in = new FileInputStream(f);

                    if( f_in.available() > 0 )
                    {
                        ObjectInputStream in = new ObjectInputStream(f_in);
                        n = (NamingContextImpl)in.readObject();
                        in.close();
                    }
                    f_in.close();
                }
                else
                {
                    if( logger.isDebugEnabled())
                        logger.debug("No naming context state, starting empty");
                }

            }
            catch( IOException io )
            {
                if( logger.isDebugEnabled())
                    logger.debug("File seems corrupt, starting empty");
            }
            catch( java.lang.ClassNotFoundException c )
            {
                logger.error("Could not read object from file, class not found!");
                System.exit(1);
            }
            if( n == null )
            {
                n = new NamingContextImpl();
            }

            n.init(adapter);
            return n;
        }

        /**
         * Saves the servant's  state in a file
         */

        public void etherealize(byte[] oid, POA adapter,
                                Servant servant,
                                boolean cleanup_in_progress, boolean remaining_activations)
        {
            String oidStr = new String(oid);

            try
            {
                File f = new File(filePrefix + oidStr);
                FileOutputStream fout = new FileOutputStream(f);

                ObjectOutputStream out =
                new ObjectOutputStream(fout);

                /* save state */
                out.writeObject((NamingContextImpl)servant);
                if (logger.isDebugEnabled())
                {
                    logger.debug("Saved state for servant " + oidStr);
                }
            }
            catch( IOException io )
            {
                io.printStackTrace();
                logger.error("Error opening output file " + filePrefix + oidStr );
                //  System.exit(1);
            }
        }
    }


    private static void usage()
    {
        System.err.println("Usage: java org.jacorb.naming.NameServer [<ior_filename>] [-p <ns_port>] [-t <time_out> [imr_register] ]");
        System.exit(1);
    }

    /** Main */

    public static void main( String args[] )
    {
        String port = null;
        boolean imr_register = false;
        String fileName = null;

        try
        {
            /* get time out value if any */
            int time_out = 0;

            if( args.length > 6 )
            {
                usage();
            }

            int idx = 0;

            if( args.length > 0 )
            {
                if( !args[0].startsWith("-p"))
                {
                    fileName = args[0];
                    idx++;
                }

                if( idx < args.length  && args[idx].startsWith("-p"))
                {
                    if( idx+1 < args.length )
                    {
                        port = args[ idx+1 ];
                        idx++;
                    }
                    else
                        usage();
                }


                if( idx < args.length  && args[ idx ].startsWith("-t"))
                {
                    if( idx+1 < args.length )
                    {
                        try
                        {
                            time_out = Integer.parseInt( args[ idx+1] );
                            idx++;
                        }
                        catch( NumberFormatException nf )
                        {
                        }
                        if( idx +1 < args.length && args[idx +1].equals("imr_register") )
                            //#ifjdk 1.2
                            imr_register = true;
                        //#else
                        //# throw new RuntimeException ("Sorry, imr_register not supported in this release");
                        //#endif
                    }
                    else
                        usage();
                }
            }

            java.util.Properties props = new java.util.Properties();
            props.put("jacorb.implname","StandardNS");

            /*
             * by setting the following property, the ORB will
             * accept client requests targeted at the object with
             * key "NameService", so more readablee corbaloc URLs
             * can be used
             */

            props.put("jacorb.orb.objectKeyMap.NameService",
                      "StandardNS/NameServer-POA/_root");

            /*
             * set a connection time out : after 10 secs. idle time,
             * the adapter will close connections
             */
            if( Environment.getIntPropertyWithDefault( "jacorb.connection.server.timeout", -1 ) < 0 )
            {
                logger.debug( "Default server.timeout to 10000" );
                props.put( "jacorb.connection.server.timeout", "10000" );
            }

            // If port not set on command line see if configured

            if (port == null)
            {
                port = Environment.getProperty ("jacorb.naming.port");
                if (port != null)
                {
                    try
                    {
                        Integer.parseInt (port);
                    }
                    catch (NumberFormatException ex)
                    {
                        port = null;
                    }
                }
            }

            if (port != null)
            {
                props.put ("OAPort", port);
            }

            /* which directory to store/load in? */

            String directory =
            org.jacorb.util.Environment.getProperty("jacorb.naming.db_dir");

            if( directory != null )
                filePrefix = directory + File.separatorChar + filePrefix;

            /* intialize the ORB and Root POA */

            orb = org.omg.CORBA.ORB.init(args, props);

            if ( org.jacorb.util.Environment.useImR() && imr_register)
            {
                //#ifjdk 1.2
                // don't supply "imr_register", so a ns started by an imr_ssd
                // won't try to register himself again.
                String command = Environment.getProperty("jacorb.java_exec") +
                " org.jacorb.naming.NameServer " + args[0] + " " + args[1];

                ImRManager.autoRegisterServer(orb, "StandardNS", command,
                                              ImRManager.getLocalHostName(),
                                              true); //edit existing
                //#endif
            }

            org.omg.PortableServer.POA rootPOA =
            org.omg.PortableServer.POAHelper.narrow(orb.resolve_initial_references("RootPOA"));

            /* create a user defined poa for the naming contexts */

            org.omg.CORBA.Policy [] policies = new org.omg.CORBA.Policy[3];

            policies[0] =
            rootPOA.create_id_assignment_policy(IdAssignmentPolicyValue.USER_ID);
            policies[1] =
            rootPOA.create_lifespan_policy(LifespanPolicyValue.PERSISTENT);

            policies[2] =
            rootPOA.create_request_processing_policy(
                RequestProcessingPolicyValue.USE_SERVANT_MANAGER);

            POA nsPOA = rootPOA.create_POA("NameServer-POA",
                                           rootPOA.the_POAManager(),
                                           policies);

            NamingContextImpl.init(orb, rootPOA);
            NameServer.NameServantActivatorImpl servantActivator =
                new NameServer.NameServantActivatorImpl( orb );

            nsPOA.set_servant_manager( servantActivator );
            nsPOA.the_POAManager().activate();

            for (int i = 0; i < policies.length; i++)
                policies[i].destroy();


            /* export the root context's reference to a file */

            byte[] oid = ( new String("_root").getBytes() );
            try
            {
                org.omg.CORBA.Object obj =
                nsPOA.create_reference_with_id( oid, "IDL:omg.org/CosNaming/NamingContextExt:1.0");

                if( fileName != null )
                {
                    PrintWriter out =
                    new PrintWriter( new FileOutputStream( fileName ), true );

                    out.println( orb.object_to_string(obj) );
                    out.close();
                }
            }
            catch ( Exception e )
            {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }

            if (logger.isInfoEnabled())
            {
                logger.info("NS up");
            }

            /* either block indefinitely or time out */

            if( time_out == 0 )
                orb.run();
            else
                Thread.sleep(time_out);


            /* shutdown. This will etherealize all servants, thus
               saving their state */
            orb.shutdown( true );

            //      System.exit(0);
        }
        catch( Exception e )
        {
            e.printStackTrace();
            System.exit(1);
        }
    }



}
