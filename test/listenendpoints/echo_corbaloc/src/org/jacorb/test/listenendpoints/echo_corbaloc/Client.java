package org.jacorb.test.listenendpoints.echo_corbaloc;


public class Client
{
    public static void main( String args[] )
    {
        CmdArgs cmdArgs = null;
        try
        {
            cmdArgs = new CmdArgs("Client", args);
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

        if (cmdArgs.getIORString() == null &&
                cmdArgs.getCorbalocString() == null)
        {
            System.err.println("Client can't connect to server, an IOR/CORBALOC string is needed!");
            System.exit(1);
        }

        try
        {
            if (cmdArgs.getIORString() != null)
            {
                for (int i = 1; i <= cmdArgs.getnThreads(); i++) {
                    ConnectIOR connectIOR = new ConnectIOR (cmdArgs, i);
                    connectIOR.start();
                    System.out.println ("Client thread " + i +
                            " started: hailing server using IOR string <" +
                            cmdArgs.getIORString() + ">");

                    // bring them up slowly
                    Thread.sleep(1000);
                }
            }
            if (cmdArgs.getCorbalocString() != null)
            {
                for (int i = 1; i <= cmdArgs.getnThreads(); i++) {
                    ConnectCorbaloc corbaloc = new ConnectCorbaloc (cmdArgs, i);
                    corbaloc.start();
                    System.out.println ("Client thread " + i +
                            " started: hailing server using corbaloc <" +
                            cmdArgs.getCorbalocString() + ">");

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
