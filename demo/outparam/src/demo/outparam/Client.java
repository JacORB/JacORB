package demo.outparam;

/**
 * An example for using out paramters
 */

import java.io.BufferedReader;
import java.io.FileReader;

import org.omg.CORBA.DoubleHolder;
import org.omg.CORBA.StringHolder;

public class Client
{
    public static void main( String[] args ) throws Exception
    {
        org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args,null);

        BufferedReader reader = new BufferedReader(new FileReader(args[0]));

        // resolve name to get a reference to our server
        MyServer server = MyServerHelper.narrow(orb.string_to_object(reader.readLine()));

        DoubleHolder doh = new DoubleHolder();
        server.addNums( (double)5, (double)6, doh);
        System.out.println("addNums 5 and 6 gives: " + doh.value);

        String stringSeq[];
        stringSeqHolder seqHolder = new stringSeqHolder( );
        server.op1( "hi_there" , seqHolder );
        stringSeq = seqHolder.value;
        System.out.println( "String array contains: ");
        for( int i = 0; i < stringSeq.length; i++ )
            System.out.println( "\t" + i + ": " + stringSeq[i] );

        MyServerHolder h = new MyServerHolder();
        server.op2(h);
        MyServer server2 = h.value;
        server2.print("Who am I talking to?");

        my_structHolder moh = new my_structHolder();
        server.op3(moh);
        my_struct m = moh.value;
        System.out.println( "Struct contains: " + m.s + " " + m.l);

        stringArrayHolder sah = new stringArrayHolder();
        server.op4(sah);
        String my_array[] = sah.value;
        System.out.println("Array size: " + my_array.length );

        StringHolder sh1 = new StringHolder();
        String sh2 = server.op5( sh1 );
        System.out.println( sh2 + " out: " + sh1.value );


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

        server.stringCubeInOut(sf);

        // get the returned string cube

        string_cube = sf.value;

        System.out.println( "string_cube after operation: ");

        for( int i=0; i<string_cube.length;i++)
            for( int j = 0; j < string_cube[i].length;j++)
                for( int k = 0; k < string_cube[i][j].length;k++)
                    System.out.println(string_cube[i][j][k]);

        System.out.println("---Everything went alright, closing down now---");
    }
}
