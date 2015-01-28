package demo.benchmark;

import java.io.*;
import org.omg.CosNaming.*;

public class ColocatedServer
{
    public static void main( String[] args )
    {
	try
	{
	    org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args, null);
	    NamingContextExt nc = 
		NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));

	    org.omg.PortableServer.POA poa = 
		org.omg.PortableServer.POAHelper.narrow(orb.resolve_initial_references("RootPOA"));		

	    poa.the_POAManager().activate();

	    org.omg.CORBA.Object o = poa.servant_to_reference(new benchImpl());	        

	    NameComponent [] name = new NameComponent[1];
	    name[0] = new NameComponent("benchServer", "service");
	    nc.bind(name, o);
		
	    bench server = benchHelper.narrow(nc.resolve(name));

	    System.out.print("     Ping                       [1]\n"
			     +"     Transfer array of int      [2]\n"
			     +"     Transfer array of struct   [3]\n"
			     +"                                   \n"
			     +"     Auto mode     - Long! -    [auto]\n"
			     +"     EXIT                       [0]\n"
			     +"     --------------------------------\n"
			     +"         Your choice :          ");

	    DataInput d = new DataInputStream(System.in);
	    String line;
	
	    while ((line = d.readLine()) != null) 
	    {
		if ( line.equals("1") )
		{
		    System.out.print("     Number of loops : ");
		    int loop = new Integer(d.readLine()).intValue();
		    int nb = loop;
		    long startTime = 0;
		    long stopTime = 0;

		    startTime = System.currentTimeMillis();
		    while (nb-- > 0)
			server.ping();

		    stopTime = System.currentTimeMillis();

		    System.out.println(">>> Elapsed time = " 
				       + (stopTime - startTime)/1000 
				       + " secs      avg time = (" 
				       + ( (stopTime - startTime)/ (float)loop) 
				       + ") msecs");
		    
		} 
		else if ( line.equals("2") ) 
		{
		    System.out.print("     Number of loops : ");
		    int loop = new Integer(d.readLine()).intValue(); 
		    int nb = loop;
		    System.out.print("     Size of array : ");
		    int size = new Integer(d.readLine()).intValue(); 
		    int myInt[] = new int[size];
		    for( int si = 0; si < size; si++)
		    {
			myInt[si]=si;
		    }

		    int ret_vals[] = null;
					
		    long startTime = System.currentTimeMillis();
		    while (nb-- > 0)
			ret_vals = server.intTransfer(myInt);

		    // for(int i = 0; i < size; System.out.print(" " + ret_vals[i++]));

		    long stopTime = System.currentTimeMillis();
		    System.out.println(">>> Elapsed time = " 
				       + (stopTime - startTime)/1000 
				       + " secs      Average time = "
				       +  ((stopTime - startTime) / (float)loop) 
				       + " msecs");
					
		} 
		else if ( line.equals("3") ) 
		{
		    System.out.print("     Number of loops : ");
		    int loop = new Integer(d.readLine()).intValue();
		    int nb = loop;
		    System.out.print("     Size of structure : ");
		    int size = new Integer(d.readLine()).intValue(); 
		    Struct myStruct[] = new Struct[size];
		    for( int si = 0; si < size; si++)
			myStruct[si]= new Struct();
		
		    long startTime = System.currentTimeMillis();
		    while (nb-- > 0)
			server.structTransfer(myStruct);
		    long stopTime = System.currentTimeMillis();
		    System.out.println(">>> Elapsed time = " 
				       + (stopTime - startTime)/1000 
				       + " secs      Average time = "
				       +  ((stopTime - startTime) / (float)loop) 
				       + " msecs");
		    
		} 
		else if ( line.equals("auto")) 
		{
		    System.out.println("#### Entering auto-mode ####");
		    System.out.print("     Number of loops : ");
		    int loop = new Integer(d.readLine()).intValue();
		    int size = 1;
		    System.out.println("\n Results are average times in msecs for "
				       + loop+ " round trips\n");
		    System.out.println(" Size of array   Ping   Array of int Array of struct");
		    System.out.println(" ============= ======== ============ ===============");
		    for (int i=0;i<6;i++) 
		    {
			System.out.print("\t"+size);
			int myInt[] = new int[size];
			Struct myStruct[] = new Struct[size];
			for( int si = 0; si < size; si++)
			{
			    myStruct[si]= new Struct();
			    myInt[si]=si;
			}
			long startTime = System.currentTimeMillis();
			int nb = loop;
			while (nb-- > 0)
			    server.ping();
			long stopTime = System.currentTimeMillis();
			System.out.print("\t"+((stopTime - startTime) / (float)loop));
			startTime = System.currentTimeMillis();
			nb = loop;
			while (nb-- > 0)
			    server.intTransfer(myInt);
			stopTime = System.currentTimeMillis();
			System.out.print("\t"+((stopTime - startTime) / (float)loop));
			startTime = System.currentTimeMillis();
			nb = loop;
			while (nb-- > 0)
			    server.structTransfer(myStruct);
			stopTime = System.currentTimeMillis();
			System.out.println("\t\t"+((stopTime - startTime) / (float)loop));
			size = size*10;
		    }
		    System.out.println("\n#### Exiting auto-mode ####\n");
		} 
		else if ( line.equals("0") ) 
		{
		    System.out.println("\nExiting ...");
		    orb.shutdown( true );
		    return;
		    //System.exit(0);
		}
		System.out.print("     Ping [1]   Array of int [2]   "
				 +"Array of struct [3] : ");
			 
	    } // while
	    orb.shutdown( true );

	} 
	catch (Exception e) 
	{
	    System.out.println("### Exception !!! ### \n");
	    e.printStackTrace();
	}


    }
}




