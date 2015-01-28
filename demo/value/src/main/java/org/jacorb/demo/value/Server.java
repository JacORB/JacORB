package org.jacorb.demo.value;

import java.io.*;

import org.omg.CORBA.*;
import org.omg.PortableServer.*;


public class Server
{
    public static void main(String[] args) throws Exception
    {
        //init ORB
        ORB orb = ORB.init( args, null );

        //init POA
        POA poa =
            POAHelper.narrow( orb.resolve_initial_references( "RootPOA" ));

        poa.the_POAManager().activate();

        ValueServerImpl s = new ValueServerImpl();

        // create the object reference
        org.omg.CORBA.Object obj = poa.servant_to_reference( s );

        PrintWriter ps = new PrintWriter(new FileOutputStream(new File( args[0] )));
        ps.println( orb.object_to_string( obj ) );
        ps.close();

        while ( args.length == 2 || ! s.getShutdown ())
        {
            Thread.sleep(1000);
        }
        orb.shutdown(true);
    }
}
