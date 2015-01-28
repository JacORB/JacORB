package org.jacorb.demo.grid;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

public class TieServer
{
    public static void main( String[] args ) throws Exception
    {
        org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args, null);
        org.omg.PortableServer.POA poa =
            org.omg.PortableServer.POAHelper.narrow(orb.resolve_initial_references("RootPOA"));

        GridOperationsImpl grid = new GridOperationsImpl();

        org.omg.CORBA.Object o =
            poa.servant_to_reference( new MyServerPOATie(grid) );

        poa.the_POAManager().activate();

        PrintWriter ps = new PrintWriter(new FileOutputStream(new File( args[0] )));
        ps.println( orb.object_to_string( o ) );
        ps.close();


        while ( args.length == 2 || ! grid.getShutdown ())
        {
            Thread.sleep(1000);
        }
        orb.shutdown(true);
    }
}
