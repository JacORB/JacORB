package demo.benchmark;

import java.io.*;
import org.omg.CosNaming.*;

public class OctetClient 
{

    public static void main(String args[]) 
    {
	org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args, null);
	octetBench server = null;

	try 
	{            
            int start = 500;
            int stop = 51000;
            int step = 500;
            int LOOPS = 100;

            NamingContextExt nc =
                NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));

            server = 
                octetBenchHelper.narrow(nc.resolve(nc.to_name("octet_benchmark")));
	
            System.out.println("** Octet IN tests **");

            long startTime = 0;
            long stopTime = 0;
            int nb = LOOPS;

            for( int i = start; i < stop; i += step )
            {
                nb = LOOPS;
                byte test[] = new byte[i];

                startTime = System.currentTimeMillis();
                while (nb-- > 0)
                {
                    server.opOctetSeqIn( test );
                }

                stopTime = System.currentTimeMillis();
                System.out.println( i + "\t" + ( (stopTime - startTime)/ (float)LOOPS));
            }

            System.out.println("** Octet INOut tests **");
            demo.benchmark.OctetSeqHolder holder = new demo.benchmark.OctetSeqHolder();

            for( int i = start; i < stop; i += step )
            {
                nb = LOOPS;
                holder.value  = new byte[i];

                startTime = System.currentTimeMillis();
                while (nb-- > 0)
                {
                    server.opOctetSeqInOut( holder );
                }

                stopTime = System.currentTimeMillis();
                System.out.println( i + "\t" + ( (stopTime - startTime)/ (float)LOOPS));
            }



	} 
	catch (Exception e) 
	{
	    System.out.println("### Exception !!! ### \n");
	    e.printStackTrace();
	}
	orb.shutdown( true );
	// System.exit(0);
    }
}


