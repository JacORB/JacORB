package org.jacorb.test.bugs.bug1010;

/**
 * @author Alon Hessing
 */
//
// Server for multi-threaded client
//

//import org.jetbrains.annotations.Nullable;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Properties;

public class Server {

    public static void main(String[] args) throws Exception {


        System.setProperty("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
        System.setProperty("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");

        String resourceName = "jacorb_3_5.properties";

        Properties propsNew = loadProperties(resourceName);


        org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args, propsNew);

        POA poa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));

        poa.the_POAManager().activate();

        ServerImpl s = new ServerImpl();

        org.omg.CORBA.Object o = poa.servant_to_reference(s);

        PrintWriter ps = new PrintWriter(new FileOutputStream(new File(args[0])));
        ps.println(orb.object_to_string(o));
        ps.close();

        while (args.length == 2 || !s.getShutdown()) {
            Thread.sleep(1000);
        }
        orb.shutdown(true);
    }

    private static Properties loadProperties(String resourceName) {

        //ClassLoader cl = Thread.currentThread().getContextClassLoader();

        URL url = ServerImpl.class.getResource(resourceName);

        Properties propsNew = new Properties();

        try
        {
            InputStream inStream = url.openStream();

            propsNew.load(inStream);

            inStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return propsNew;
    }
}
