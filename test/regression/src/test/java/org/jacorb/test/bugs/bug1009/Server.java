package org.jacorb.test.bugs.bug1009;

import java.io.*;

import org.omg.CORBA.*;
import org.omg.PortableServer.*;


public class Server extends iPOA
{
    public static void main(String[] args) throws Exception
    {
        //init ORB
        ORB orb = ORB.init( args, null );

        //init POA
        POA poa =
            POAHelper.narrow( orb.resolve_initial_references( "RootPOA" ));

        poa.the_POAManager().activate();

        // create the object reference
        org.omg.CORBA.Object o = poa.servant_to_reference(new Server());

        PrintWriter ps = new PrintWriter(new FileOutputStream(new File( args[0] )));
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

    public void f()
    {
        throw new RuntimeException ();
    }
}
