package demo.mtclient;

//
// Server for multi-threaded client
//

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import org.omg.CosNaming.*;
import org.omg.PortableServer.*;

public class Server
{
    public static void main( String[] args ) throws Exception
    {
        org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args, null);
        
	    POA poa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
	
	    poa.the_POAManager().activate();

	    org.omg.CORBA.Object o = poa.servant_to_reference(new serverImpl());

        PrintWriter ps = new PrintWriter(new FileOutputStream(new File(args[0])));
        ps.println( orb.object_to_string( o ) );
        ps.close();

        if (args.length == 2)
        {
            File killFile = new File(args[1]);
            while(!killFile.exists())
            {
                Thread.sleep(1000);
            }
            orb.shutdown(true);
        }
        else
        {
            orb.run();
        }
    }
}


