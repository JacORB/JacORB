package demo.benchmark;

import java.io.*;
import org.omg.CosNaming.*;

/**
 * A simple benchmark for remote invocations. It takes the system clock right
 * before and after method invocations. Originally adopted from code by
 * Christophe Warland
 */

public class Client
{

    public static void main (String args[]) throws Exception
    {
        org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init (args, null);
        bench server = null;

        if (args.length > 0)
        {
            try
            {
                File f = new File (args[0]);
                BufferedReader br = new BufferedReader (new FileReader (f));
                String ior = br.readLine ();
                br.close ();
                server = benchHelper.narrow (orb.string_to_object (ior));
            }
            catch (Exception e)
            {
                e.printStackTrace ();
                System.exit (1);
            }
        }
        else
        {
            NamingContextExt nc = NamingContextExtHelper.narrow (orb.resolve_initial_references ("NameService"));
            server = benchHelper.narrow (nc.resolve (nc.to_name ("benchmark")));
        }

        System.out.print ("     Ping                       [1]\n"
                + "     Transfer array of int      [2]\n"
                + "     Transfer array of byte     [3]\n"
                + "     Transfer array of struct   [4]\n"
                + "     Transfer array of string   [5]\n"
                + "                                   \n"
                + "     Auto mode     - Long! -    [auto]\n"
                + "     EXIT                       [0]\n"
                + "     --------------------------------\n"
                + "         Your choice :          ");

        DataInput d = new DataInputStream (System.in);
        String line;

        while ((line = d.readLine ()) != null)
        {
            if (line.equals ("1"))
            {
                System.out.print ("     Number of loops : ");
                int loop = new Integer (d.readLine ()).intValue ();
                int nb = loop;
                long startTime = 0;
                long stopTime = 0;

                startTime = System.currentTimeMillis ();
                while (nb-- > 0)
                {
                    server.ping ();
                }

                stopTime = System.currentTimeMillis ();

                System.out.println (">>> Elapsed time = "
                        + (stopTime - startTime) / 1000
                        + " secs      avg time = ("
                        + ((stopTime - startTime) / (float) loop) + ") msecs");

            }
            else if (line.equals ("2"))
            {
                System.out.print ("     Number of loops : ");
                int loop = new Integer (d.readLine ()).intValue ();
                int nb = loop;
                System.out.print ("     Size of array : ");
                int size = new Integer (d.readLine ()).intValue ();
                int myInt[] = new int[size];
                for (int si = 0; si < size; si++)
                {
                    myInt[si] = si;
                }

                int ret_vals[] = null;

                long startTime = System.currentTimeMillis ();
                while (nb-- > 0)
                {
                    ret_vals = server.intTransfer (myInt);
                }

                // for(int i = 0; i < size; System.out.print(" " +
                // ret_vals[i++]));

                long stopTime = System.currentTimeMillis ();
                System.out.println (">>> Elapsed time = "
                        + (stopTime - startTime) / 1000
                        + " secs      Average time = "
                        + ((stopTime - startTime) / (float) loop) + " msecs");

            }
            else if (line.equals ("3"))
            {
                // byte arrays

                System.out.print ("     Number of loops : ");
                int loop = new Integer (d.readLine ()).intValue ();
                int nb = loop;
                System.out.print ("     Size of array : ");
                int size = new Integer (d.readLine ()).intValue ();
                byte mybytes[] = new byte[size];

                long startTime = System.currentTimeMillis ();
                while (nb-- > 0)
                {
                    server.octetTransfer (mybytes);
                }

                long stopTime = System.currentTimeMillis ();
                System.out.println (">>> Elapsed time = "
                        + (stopTime - startTime) / 1000
                        + " secs      Average time = "
                        + ((stopTime - startTime) / (float) loop) + " msecs");

            }
            else if (line.equals ("4"))
            {
                // struct arrays

                System.out.print ("     Number of loops : ");
                int loop = new Integer (d.readLine ()).intValue ();
                int nb = loop;
                System.out.print ("     Array Size : ");
                int size = new Integer (d.readLine ()).intValue ();
                Struct myStruct[] = new Struct[size];

                for (int si = 0; si < size; si++)
                    myStruct[si] = new Struct ();

                long startTime = System.currentTimeMillis ();
                while (nb-- > 0)
                {
                    server.structTransfer (myStruct);
                }

                long stopTime = System.currentTimeMillis ();
                System.out.println (">>> Elapsed time = "
                        + (stopTime - startTime) / 1000
                        + " secs      Average time = "
                        + ((stopTime - startTime) / (float) loop) + " msecs");

            }
            else if (line.equals ("5"))
            {
                // string arrays

                System.out.print ("     Number of loops : ");
                int loop = new Integer (d.readLine ()).intValue ();
                int nb = loop;
                System.out.print ("     Array size: ");
                int size = new Integer (d.readLine ()).intValue ();
                String myString[] = new String[size];

                for (int si = 0; si < size; si++)
                    myString[si] = "testString";

                long startTime = System.currentTimeMillis ();
                while (nb-- > 0)
                {
                    server.stringTransfer (myString);
                }

                // System.exit(0);
                long stopTime = System.currentTimeMillis ();
                System.out.println (">>> Elapsed time = "
                        + (stopTime - startTime) / 1000
                        + " secs      Average time = "
                        + ((stopTime - startTime) / (float) loop) + " msecs");

            }
            else if (line.equals ("auto"))
            {
                System.out.println ("#### Entering auto-mode ####");
                System.out.print ("     Number of loops : ");
                int loop = new Integer (d.readLine ()).intValue ();
                int size = 1;
                System.out.println ("\n Results are average times in msecs for "
                        + loop + " round trips\n");
                System.out.println ("  Array size     Ping    int[]  byte[]  struct[]   string[]");
                System.out.println (" ============= ======== ====== ======= ========== =========");

                for (int i = 0; i < 6; i++)
                {
                    System.out.print ("\t" + size);
                    int myInt[] = new int[size];
                    byte myByte[] = new byte[size];
                    Struct myStruct[] = new Struct[size];
                    String myString[] = new String[size];
                    for (int si = 0; si < size; si++)
                    {
                        myStruct[si] = new Struct ();
                        myInt[si] = si;
                        myString[si] = "testString";
                    }

                    long startTime = System.currentTimeMillis ();

                    int nb = loop;
                    while (nb-- > 0)
                        server.ping ();

                    long stopTime = System.currentTimeMillis ();
                    System.out.print ("\t"
                            + ((stopTime - startTime) / (float) loop));
                    startTime = System.currentTimeMillis ();

                    nb = loop;
                    while (nb-- > 0)
                        server.intTransfer (myInt);

                    stopTime = System.currentTimeMillis ();
                    System.out.print ("\t"
                            + ((stopTime - startTime) / (float) loop));

                    startTime = System.currentTimeMillis ();
                    nb = loop;
                    while (nb-- > 0)
                        server.octetTransfer (myByte);
                    stopTime = System.currentTimeMillis ();
                    System.out.print ("\t"
                            + ((stopTime - startTime) / (float) loop));

                    startTime = System.currentTimeMillis ();
                    nb = loop;
                    while (nb-- > 0)
                        server.structTransfer (myStruct);
                    stopTime = System.currentTimeMillis ();
                    System.out.print ("\t"
                            + ((stopTime - startTime) / (float) loop));

                    startTime = System.currentTimeMillis ();
                    nb = loop;
                    while (nb-- > 0)
                        server.stringTransfer (myString);
                    stopTime = System.currentTimeMillis ();
                    System.out.print ("\t"
                            + ((stopTime - startTime) / (float) loop));

                    System.out.println ();
                    size = size * 10;
                }
                System.out.println ("\n#### Exiting auto-mode ####\n");
            }
            else if (line.equals ("0"))
            {
                System.out.println ("\nExiting ...");
                orb.shutdown (true);
                return;
                // System.exit(0);
            }
            System.out.print ("     Ping [1]   Array of int [2]  Array of byte [3] "
                    + "Array of struct [4] : ");

        } // while
        orb.shutdown (true);
    }
}
