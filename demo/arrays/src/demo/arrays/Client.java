package demo.arrays;

/**
 * an example for using IDL arrays and sequences 
 * with JacORB
 */ 

import java.io.BufferedReader;
import java.io.FileReader;

import demo.arrays.MyServerPackage.*;

class Client 
{
    public static void main(String args[]) throws Exception
    { 
        org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args,null);

        BufferedReader br =
            new BufferedReader( new FileReader( args[0] ));

        // resolve name to get a reference to our server
        MyServer g = MyServerHelper.narrow(orb.string_to_object(br.readLine())) ;

	    // send and receive a *sequence* of integers 

	    int[] j = new int[]{1,2,3,4};
	    int[] i = g.write2("Hello World!", j);
	    System.out.println("Result: " + i[0]);

	    // send an *array* of integers as an argument

	    int[] a = new int[] {55,56,57}; 
	    i = g.write("Again...", a);
	    for( int ii = 0; ii < i.length;  ii++)
		System.out.println("   " + i[ii]);

	    // send an array of object references as an argument

	    MyServer[] svs = new MyServer[]{g,g};
	    g._notify( svs );			
	    g.notify2( svs );

	    // send a struct containing an array of shorts

	    short [][] shorts = new short[][]{{1,7,2},{2,6,2},{3,5,2},{4,4,2},{5,3,2},{6,2,2},{7,1,2}};

	    arrayContainer ac = 
		new arrayContainer( shorts );
	    g.notify3( ac );


        // test printLongArray
        long[] refs = new long[10];
        for (int ir=0; ir<refs.length; ir++ )
        {
            refs[ir] = (long)ir;
        }
        
        g.printLongArray( refs );
        
        // test printLongArray
        double[] drefs = new double[10];
        for (int ir = 0; ir < drefs.length; ir++ )
        {
            drefs[ir] = (float)ir;
        }
        g.printDoubleArray( drefs );
    }
}


