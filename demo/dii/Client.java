package demo.dii;

/**
 * An example for using the Dynamic Invocation Interface
 */
import org.omg.CosNaming.*;

public class Client 
{
    public static void main( String[] args )
    {
	    org.omg.CORBA.ORB orb = null;
	    try
	    {
		orb = org.omg.CORBA.ORB.init(args,null);

		NamingContextExt nc = NamingContextExtHelper.narrow(
                           orb.resolve_initial_references("NameService"));
	
		org.omg.CORBA.Object s = nc.resolve( nc.to_name("dii.example"));

		// a simple request

		org.omg.CORBA.Request r = s._request("_get_long_number");
	    
		r.set_return_type( orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_long));
		r.invoke();

		if( r.env().exception() != null )
		    throw r.env().exception();
		else
		    System.out.println("0: " + r.return_value() );  
	
		// a request with out args

		org.omg.CORBA.Request r_out = s._request("add");
	    
		r_out.add_in_arg().insert_long( 3 );
		r_out.add_in_arg().insert_long( 4 );

		org.omg.CORBA.Any out_arg = r_out.add_out_arg();
		out_arg.type( orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_long) );

		r_out.set_return_type( orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_void));

		r_out.invoke();

		if( r_out.env().exception() != null )
		    throw r_out.env().exception();
		else
		    System.out.println("add: " + out_arg.extract_long() );  

		// another simple request with a string argumente

		org.omg.CORBA.Request r1 =  s._request("notify");
		r1.add_in_arg().insert_string("hallo");
		r1.invoke();

		// a oneway request
	    
		r1.send_oneway();

		// a request with a return value

		org.omg.CORBA.Request r2 =  s._request("writeNumber");
		r2.add_in_arg().insert_long( 5 );
		r2.set_return_type( orb.get_primitive_tc( org.omg.CORBA.TCKind.tk_string ));
		r2.invoke();
		if( r2.env().exception() != null )
		    throw r2.env().exception();
		else
		    System.out.println("1: " + r2.return_value() );  
			
		// deferred asynchronous operation
	    
		// synchronize with result

		r2.send_deferred();
		r2.get_response();
		if( r2.env().exception() != null )
		    throw r2.env().exception();
		else
		    System.out.println("2: " + r2.return_value() );  

		// polling until response is there
	    
		r2.send_deferred();
	    
		while( ! r2.poll_response() )
		{
		    /* we could be doing s.th. useful here instead of sleeping...*/
		    try 
		    {
			Thread.currentThread().sleep(10);
		    } 
		    catch ( InterruptedException i){}
		    System.out.print("."); 
		}

		if( r2.env().exception() != null )
		    throw r2.env().exception();
		else
		    System.out.println("3: " + r2.return_value() );  


		org.omg.CORBA.Request r3 =  s._request("writeNumberWithEx");
		r3.add_in_arg().insert_long( 5 );
		r3.set_return_type( orb.get_primitive_tc( org.omg.CORBA.TCKind.tk_string ));
		r3.invoke();
		if( r3.env().exception() != null )
		    throw r3.env().exception();
		else
		    System.out.println("4: " + r3.return_value() );  

	    } 
	    catch ( Exception e)
	    {
		e.printStackTrace();
	    }
	    orb.shutdown( false );
    }
}



