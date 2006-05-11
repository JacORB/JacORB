package org.jacorb.naming;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2004 Gerald Brose.
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.Logger;
import org.jacorb.imr.util.ImRManager;
import org.jacorb.util.ObjectUtil;
import org.omg.PortableServer.ForwardRequest;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.RequestProcessingPolicyValue;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer._ServantActivatorLocalBase;

/**
 *  The name server application
 *
 *  @author Gerald Brose, FU Berlin
 *  @version $Id$
 */


public class NameServer
{
    private static org.omg.CORBA.ORB orb = null;
    private static org.jacorb.config.Configuration configuration = null;

    /** the specific logger for this component */
    private static Logger logger = null;

    /** the file name int which the IOR will be stored */
    private static String fileName = null;

    private static String filePrefix = "_nsdb";
    private static String commandSuffix = "";

    /** if this value is != 0, the name server will automatically shut
        down after the given time */
    private static int time_out = 0;

    static String name_delimiter = "/";


    public static void configure(Configuration myConfiguration)
        throws ConfigurationException
    {
        configuration = (org.jacorb.config.Configuration)myConfiguration;
        logger = 
            configuration.getNamedLogger("jacorb.naming");

        time_out = 
            configuration.getAttributeAsInteger("jacorb.naming.time_out",0);

        fileName = 
            configuration.getAttribute("jacorb.naming.ior_filename", "");

        /* which directory to store/load in? */
        String directory = 
            configuration.getAttribute("jacorb.naming.db_dir", "");
        
        if( !directory.equals("") )
            filePrefix = directory + File.separatorChar + filePrefix;

        if ( configuration.getAttribute("jacorb.use_imr","off").equals("on") )
        {

            // don't supply "imr_register", so a ns started by an imr_ssd
            // won't try to register himself again.

            String command = 
                configuration.getAttribute("jacorb.java_exec", "") + commandSuffix;
            
            ImRManager.autoRegisterServer( orb, 
                                           "StandardNS", 
                                           command,
                                           ImRManager.getLocalHostName(),
                                           true); //edit existing
        }       
    }


    /**
     * The servant manager (servant activator) for the name server POA
     */

    static class NameServantActivatorImpl
        extends _ServantActivatorLocalBase
    {
        private org.omg.CORBA.ORB orb = null;
        private org.jacorb.config.Configuration configuration = null;
        private Logger logger = null;

        public NameServantActivatorImpl(org.omg.CORBA.ORB orb)
        {
            this.orb = orb;
        }

        public void configure(Configuration myConfiguration)
            throws ConfigurationException
        {
            this.configuration = (org.jacorb.config.Configuration)myConfiguration;
            this.logger = configuration.getNamedLogger("jacorb.naming.activator");
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
                if (logger.isErrorEnabled())
                {
                    logger.error("Could not read object from file, class not found!");
                }
                throw new RuntimeException ("Could not read object from file, class not found!");
            }

            if( n == null )
            {
                n = new NamingContextImpl();               
            }

            n.init(adapter);
            try
            {
                n.configure(configuration);
            }
            catch( ConfigurationException ce )
            {
                if (logger.isErrorEnabled())
                    logger.error("ConfigurationException: " + ce.getMessage());
            }
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
        System.err.println("Usage: java org.jacorb.naming.NameServer [-Djacorb.naming.ior_filename=fname] [-Djacorb.naming.time_out=x][-Djacorb.use_imr=on/off][-Djacorb.naming.purge=on/off ]");
        System.exit(1);
    }

    /** Main */

    public static void main( String args[] )
    {
        try
        {
            // TODO: is this correct? needs testing
            commandSuffix = " org.jacorb.naming.NameServer";

            // translate any properties set on the commandline but after the 
            // class name to a properties
            java.util.Properties argProps = ObjectUtil.argsToProps( args );

            java.util.Properties props = new java.util.Properties();
            props.put("jacorb.implname", "StandardNS");

            /*
             * by setting the following property, the ORB will
             * accept client requests targeted at the object with
             * key "NameService", so more readablee corbaloc URLs
             * can be used
             */

            props.put("jacorb.orb.objectKeyMap.NameService",
                      "StandardNS/NameServer-POA/_root");

            /* any command line properties set _after_ the class name will also 
               be considered */
            props.putAll( argProps );

            /* intialize the ORB and Root POA */
            orb = org.omg.CORBA.ORB.init(args, props);

            Configuration config = 
                ((org.jacorb.orb.ORB)orb).getConfiguration();

            /* configure the name service using the ORB configuration */
            configure(config);

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
            servantActivator.configure(config);

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

                if( fileName != null && fileName.length() > 0 )
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

            // This shutdown hook fixes the fact that servants aren't being
            // etherealized because orb.shutdown is not getting called
            // when ns is killed.
            Thread shutdownHook = new Thread()
            {
               public void run()
               {
                  logger.info( "shutdownHook invoked" );
                  orb.shutdown( true );
               }
            };
            Runtime.getRuntime().addShutdownHook( shutdownHook );


            /* either block indefinitely or time out */

            if( time_out == 0 )
                orb.run();
            else
                Thread.sleep(time_out);


            /* shutdown. This will etherealize all servants, thus
               saving their state */
            orb.shutdown( true );

            try
            {
               Runtime.getRuntime().removeShutdownHook( shutdownHook );
            }
            catch( Exception removeShutdownHookException )
            {
               // removeShutdownHook will throw ignorable illegal state
               // exception if got here via signal.  Ignore exception.
            }


            //      System.exit(0);
        }
        catch( ConfigurationException e )
        {
            e.printStackTrace();
            usage();
        }
        catch( Exception e )
        {
            e.printStackTrace();
            System.exit(1);
        }
    }



}
