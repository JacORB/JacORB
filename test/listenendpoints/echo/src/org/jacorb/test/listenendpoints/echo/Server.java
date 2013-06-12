package org.jacorb.test.listenendpoints.echo;

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
        EchoMessageImpl echoMessageImpl = new EchoMessageImpl( "EchoMessage" );

        // create the object reference
        org.omg.CORBA.Object o = poa.servant_to_reference(new EchoMessageImpl("EchoMessage"));

        PrintWriter ps = new PrintWriter(new FileOutputStream(new File( args[0] )));
        ps.println( orb.object_to_string( o ) );
        ps.close();

        System.out.println("SERVER IOR: " + orb.object_to_string (o));
        System.out.flush();

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
