package test.interop.bug360;

import java.io.*;
import org.omg.CORBA.Any;
import org.omg.PortableServer.*;
import org.omg.CosNaming.*;
import org.omg.CORBA.OctetSeqHelper;
import org.omg.CORBA.StringSeqHelper;

public class Client 
{
    public static onewayPushConsumer consumer = null;
    private static int counter;


    public static void main( String[] args )
    {
	try
	{	
	    org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args,null);

	    // get hold of the naming service
	    NamingContextExt nc = 
                NamingContextExtHelper.narrow( orb.resolve_initial_references("NameService"));

	     consumer =  
              onewayPushConsumerHelper.narrow( nc.resolve(nc.to_name("bug360.service")));

	    // create a new any
	    Any a = org.omg.CORBA.ORB.init().create_any();	    

            Struct3 s3 = 
                new Struct3( Enum5xxxxxxxx.E5Exxx , 
                             (float)0.0, (float)1.1, 
                             Enum6xxxxxxxxxxxxxxxxx.E6Cxxxxxxxxxxxxx
                             );
            Struct2 s2 = 
                new Struct2( Enum1.E1B, 
                             1, 
                             (float)1.1, 
                             Enum2xxxxxxxxxxxx.E2Bxxxxxxxx, 
                             (float)2.2, 
                             (float)2.2, 
                             Enum2xxxxxxxxxxxx.E2Bxxxxxxxx, 
                             Enum3xxxxxxxxxxxxxxxx.E3Cxxxxxxxxxxx,
                             (float)3.3,
                             Enum4xxxxxxxxxxxxx.E4Dxxxxxxxx ,
                             s3
                             );

            Struct1Helper.insert( a, new Struct1( 1, s2 ) );

	    consumer.synchronousPush( a ) ;
	
	} 
	catch ( Exception e)
	{
	    e.printStackTrace();
	}
    }
}


