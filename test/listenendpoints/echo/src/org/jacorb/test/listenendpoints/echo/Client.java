package org.jacorb.test.listenendpoints.echo;

import java.io.*;
import java.net.InetAddress;
import org.omg.CORBA.*;

public class Client
{
    private static void help() {
        System.out.println("test.listenendpoints.echo.Client [-help/--help]");
        System.out.println("test.listenendpoints.echo.Client <-iorfile/--iorfile filename> <-msg/--msg quoted echo message>");

    }

    public static void main( String args[] )
    {
        if( args.length < 1 )
        {
            help();
            System.exit( 1 );
        }

        PrintWriter out = null;
        BufferedReader in = null;
        String echoMsg = null;
        String iorFile = null;
        String corbaloc = null;


        try
        {
            for(int i=0; i < args.length; i++) {
                if (args[i] == null) {
                    continue;
                }

                String cmd = args[i].trim();

                if("-help".equals(cmd) || "--help".equals(cmd)) {
                    help();
                    return;
                }


                if("-iorfile".equals(cmd) || "--iorfile".equals(cmd)) {
                    if ((i+1) < args.length && args[i+1] != null)
                    {
                        iorFile = args[++i].trim();
                        System.out.println("Got " + cmd + ": " + iorFile);
                        continue;
                    }
                    else
                    {
                        System.err.println("Commandline argument " + cmd + " <value> is missing the value");
                        help();
                        return;
                    }
                }

                if("-msg".equals(cmd) || "--msg".equals(cmd)) {
                    if ((i+1) < args.length && args[i+1] != null)
                    {
                        echoMsg = args[++i];
                        System.out.println("Got " + cmd + ": " + echoMsg);
                        continue;
                    }

                }
            }

        }
        catch(Exception ex)
        {
            System.err.println(ex);
            System.exit(1);
        }


        try
        {
            ORB orb = null;
            org.omg.CORBA.Object obj = null;

            if(echoMsg == null)
            {
                echoMsg = new String(InetAddress.getLocalHost().getHostAddress() +
                                        " is hailing server!");
            }

            if (iorFile != null)
            {
                File f = new File( iorFile);
                if( ! f.exists() )
                {
                    System.out.println("File " + iorFile +
                                    " does not exist.");

                    System.exit( -1 );
                }
                if( f.isDirectory() )
                {
                    System.out.println("File " + iorFile +
                            " is a directory.");

                    System.exit( -1 );
                }


                // initialize the ORB.
                orb = ORB.init( args, null );

                BufferedReader br =
                    new BufferedReader( new FileReader( f ));

                System.out.println ("Hailing server using IOR string ...");
                obj =
                    orb.string_to_object( br.readLine() );

                br.close();
            }
            else
            {
                System.err.println("IOR file is missing");
                System.exit(1);
            }

            // and narrow it
            // if this fails, a BAD_PARAM will be thrown
            EchoMessage echoMessage = EchoMessageHelper.narrow( obj );


            // invoke the operation and print the result
            System.out.println( echoMessage.echo_simple() );

            int cnt = 0;
                // Thread sleeper = new Thread();
                while ( true )
                {
                    try
                    {
                        cnt++;
                        // Calendar lCDateTime = Calendar.getInstance();
                        String outMsg = new String(Integer.toString(cnt) + " " + echoMsg);
                        long tms_out = System.currentTimeMillis();

                        // send it to the server
                        String inMsg = echoMessage.echo_wide(outMsg);

                        long tms_in = System.currentTimeMillis();
                        long tms_dif = tms_in - tms_out;
                        if (inMsg.equals(outMsg))
                        {
                           System.out.println("OK" + " " + tms_dif + "mSec <" + inMsg + ">");
                        }
                        else
                        {
                            System.out.println("ERR" + " " + tms_dif + "mSec in=<" + inMsg + "> out=<" + outMsg + ">");
                        }
                        Thread.sleep(1 * 1000);

                    }
                    catch (Exception e)
                    {
                        System.err.println("Got an exception: " + e.getMessage());
                        break;
                    }
                }
        }
        catch( Exception ex )
        {
            ex.printStackTrace();
        }
    }
}
