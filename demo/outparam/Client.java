package demo.outparam;

/**
 * An example for using out paramters
 */ 

import jacorb.orb.*;
import org.omg.CORBA.*;
import org.omg.CosNaming.*;

public class Client
{
    public static void main( String[] args )
    {
	try
	{
	    org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args,null);

	    // get hold of the naming service
	    NamingContextExt nc = NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));
	    NameComponent [] name = new NameComponent[]{ new NameComponent("ParamServer", "service")};

	    // resolve name to get a reference to our server
	    MyServer s = MyServerHelper.narrow(nc.resolve(name));

	    DoubleHolder doh = new DoubleHolder();
	    s.addNums( (double)5, (double)6, doh);
	    System.out.println("addNums 5 and 6 gives: " + doh.value);

	    String a[];
	    stringSeqHolder sh = new stringSeqHolder( );
	    s.op1( "hi_there" , sh );
	    a = sh.value;
	    System.out.println( "String array contains: ");
	    for( int i = 0; i < a.length; i++ )
		System.out.println( "\t" + i + ": " + a[i] );

	    MyServerHolder h = new MyServerHolder();
	    s.op2(h);
	    MyServer s2 = h.value;
	    s2.print("Who am I talking to?");

	    my_structHolder moh = new my_structHolder();
	    s.op3(moh);
	    my_struct m = moh.value;
	    System.out.println( "Struct contains: " + m.s + " " + m.l);

	    stringArrayHolder sah = new stringArrayHolder();
	    s.op4(sah);
	    String my_array[] = sah.value;
	    System.out.println("Array size: " + my_array.length );

	    // an example for a sequence of sequences of sequences of string
	    // 
	    // set up a 3-dimensional string array

	    String [][][] string_cube = new String[1][2][3];
	    for( int i=0; i<string_cube.length;i++)
		for( int j = 0; j < string_cube[i].length;j++)
		    for( int k = 0; k < string_cube[i][j].length;k++)
			string_cube[i][j][k] = "("+i+","+ k + "," +j+")";
 
	    // put it into the appropriate holder for inout semantics

	    stringCubeHolder sf = new stringCubeHolder(string_cube);

	    // invoke the operation

	    s.stringCubeInOut(sf);

	    // get the returned string cube

	    string_cube = sf.value;

	    System.out.println( "string_cube after operation: ");

	    for( int i=0; i<string_cube.length;i++)
		for( int j = 0; j < string_cube[i].length;j++)
		    for( int k = 0; k < string_cube[i][j].length;k++)
			System.out.println(string_cube[i][j][k]);

	    System.out.println("---Everything went alright, closing down now---");

	} 
	catch ( Exception e)
	{
	    e.printStackTrace();
	}
    }
}




