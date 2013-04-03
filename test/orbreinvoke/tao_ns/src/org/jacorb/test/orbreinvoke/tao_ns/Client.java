package org.jacorb.test.orbreinvoke.tao_ns;

import java.util.List;
import java.util.ArrayList;
import java.io.*;
import org.omg.CORBA.*;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.jacorb.test.listenendpoints.echo_corbaloc.*;



public class Client
{
    public static void main( String args[] )
    {
        MyCmdArgs cmdArgs = null;
        try
        {
            cmdArgs = new MyCmdArgs("Client", args);
            if (!cmdArgs.processArgs())
            {
                System.exit(1);
            }
            cmdArgs.show();
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        if (!cmdArgs.useNameService())
        {
            System.err.println("Client can't connect to server, a NameService's IOR/CORBANAME string is needed!");
            System.exit(1);
        }

        try
        {
            if (cmdArgs.useNameService())
            {
                for (int i = 1; i <= cmdArgs.getnThreads(); i++) {
                    ConnectNameService connectNameService = new ConnectNameService (cmdArgs, i);
                    connectNameService.start();
                    System.out.println ("Client thread " + i +
                            " started: hailing server using IOR string <" +
                            cmdArgs.getNsCorbaName() + ">");

                    // bring them up slowly
                    Thread.sleep(1000);
                }
            }
        }
        catch( Exception ex )
        {
            ex.printStackTrace();
            System.exit(1);
        }
    }
}

