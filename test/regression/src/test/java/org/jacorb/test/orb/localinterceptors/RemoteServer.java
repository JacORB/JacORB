package org.jacorb.test.orb.localinterceptors;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Properties;
import org.jacorb.test.harness.TestUtils;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

public class RemoteServer
{
    private static ORB orb = null;

    public static void main (String [] args) throws Exception
    {
        Properties props = new Properties();

        props.setProperty ("jacorb.codeSet", "on");
        props.setProperty ("org.omg.PortableInterceptor.ORBInitializerClass.standard_init",
                           "org.jacorb.orb.standardInterceptors.IORInterceptorInitializer");

        // Initialize the ORB -
        orb = ORB.init (args, props);

        // Acquire Root POA
        POA rootPOA =
        POAHelper.narrow (orb.resolve_initial_references ("RootPOA"));

        // Create the servant
        RemotePIServerImpl remote = new RemotePIServerImpl (rootPOA);

        rootPOA.the_POAManager().activate();

        org.omg.CORBA.Object obj = rootPOA.servant_to_reference (remote);

        PrintWriter pw = new PrintWriter (new FileWriter ("remoteserver.ior"));

        pw.println (orb.object_to_string (obj));

        pw.flush();
        pw.close();

        File file = new File ("remoteserver.ior");
        file.deleteOnExit();

        System.out.println("SERVER IOR: " + orb.object_to_string (obj));

        orb.run ();
    }

    public static void shutdown()
    {
        try
        {
           TestUtils.getLogger().debug ("RemoteServer shutting down");
           orb.shutdown (false);
        }
        catch (Exception ex)
        {
            TestUtils.getLogger().debug ("Exception shutting server down");
        }
    }
}
