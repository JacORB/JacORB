package test.longlongseq;

import java.io.*;

import org.omg.CORBA.*;
import org.omg.PortableServer.*;

import org.jacorb.util.*;

public class Server 
    extends TestIfPOA
{
    public Server()
    {
    }

    private void test( long[] arg )
    {
        Debug.myAssert( arg[0] == Long.MIN_VALUE, "" );
        Debug.myAssert( arg[1] == Long.MIN_VALUE, "" );
    }

    public long[] test1( long[] argin, 
                         SeqLongLongHolder argout, 
                         SeqLongLongHolder arginout )
    {
        test( argin );
        
        test( arginout.value );

        argout.value = argin;
        arginout.value = argin;

        return argin;
    }
	
    public long[] test2( long[] argin, SeqLongLongHolder argout )
    {
        test( argin );
        
        argout.value = argin;
        
        return argin;
    }
	
    public void test3( SeqLongLongHolder arginout )
    {
        test( arginout.value );
    }
        
    public static void main(String[] args) 
    {
        if( args.length != 1 ) 
	{
            System.out.println(
                "Usage: jaco test.longlongseq.Server <ior_file>");
            System.exit( 1 );
        }

        try 
        {            
            //init ORB
	    ORB orb = ORB.init( args, null );

	    //init POA
	    POA poa = 
                POAHelper.narrow( orb.resolve_initial_references( "RootPOA" ));

	    poa.the_POAManager().activate();

            Server s = new Server();
            
            // create the object reference
            org.omg.CORBA.Object obj = 
                poa.servant_to_reference( s );

            PrintWriter pw = 
                new PrintWriter( new FileWriter( args[ 0 ] ));

            // print stringified object reference to file
            pw.println( orb.object_to_string( obj ));
            
            pw.flush();
            pw.close();
    
            // wait for requests
	    orb.run();
        }
        catch( Exception e ) 
        {
            System.out.println( e );
        }
    }
}


