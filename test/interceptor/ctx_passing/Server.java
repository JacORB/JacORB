package test.interceptor.ctx_passing;

import java.io.*;

import org.omg.CORBA.*;
import org.omg.PortableServer.*;

class TestObjectImpl
    extends TestObjectPOA
{
    
    public void foo()
    {
        try
        {
            org.omg.PortableInterceptor.Current current = 
                (org.omg.PortableInterceptor.Current) Server.orb.resolve_initial_references( "PICurrent" );
        
            Any any = current.get_slot( ServerInitializer.slot_id );
        
            System.out.println("Server extracted from PICurrent: >>" + 
                               any.extract_string() + "<<");        
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }
}

public class Server 
{
    public static ORB orb = null;

    public static void main(String[] args) 
    {
        if( args.length != 1 ) 
	{
            System.out.println(
                "Usage: jaco test.interceptor.ctx_passing.Server <ior_file>");
            System.exit( 1 );
        }

        try 
        {             
            System.setProperty( "org.omg.PortableInterceptor.ORBInitializerClass.a",
                                "test.interceptor.ctx_passing.ServerInitializer" );
            //init ORB
	    orb = ORB.init( args, null );

	    //init POA
	    POA poa = 
                POAHelper.narrow( orb.resolve_initial_references( "RootPOA" ));

	    poa.the_POAManager().activate();
    
            // create the object reference
            org.omg.CORBA.Object obj = 
                poa.servant_to_reference( new TestObjectImpl() );

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
