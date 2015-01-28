package org.jacorb.demo.hello;

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

        // create a GoodDay object
        GoodDayImpl goodDayImpl = new GoodDayImpl( "Somewhere" );

        // create the object reference
        org.omg.CORBA.Object o = poa.servant_to_reference(goodDayImpl);

        PrintWriter ps = new PrintWriter(new FileOutputStream(new File( args[0] )));
        ps.println( orb.object_to_string( o ) );
        ps.close();

        while ( args.length == 2 || ! goodDayImpl.getShutdown ())
        {
            Thread.sleep(1000);
        }
        orb.shutdown(true);
    }
}
