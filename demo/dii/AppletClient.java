package demo.dii;

/**
 * An example for using the Dynamic Invocation Interface
 * This is the applet version.
 */

public class AppletClient 
    extends java.applet.Applet
{
    public void init()
    {
	try
	{
	    java.util.Properties props = new java.util.Properties();
	    props.put("org.omg.CORBA.ORBClass",
                      "org.jacorb.orb.ORB");
	    props.put("org.omg.CORBA.ORBSingletonClass",
                      "org.jacorb.orb.ORBSingleton");
       	     props.put
                ("org.omg.PortableInterceptor.ORBInitializerClass.ForwardInit",
                 "demo.dii.ProxyClientInitializer");
	
            
	    org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(this,props);

	    // JacORB specific localization of objects
	    
	    org.omg.CORBA.Object n = orb.resolve_initial_references("NameService");

	    if( n == null )
	    {
		System.out.println("No name server found!");
	    }

	    org.omg.CosNaming.NamingContextExt nc = 
		org.omg.CosNaming.NamingContextExtHelper.narrow( n );

	    if( nc == null )
	    {
		System.out.println("Name server has incorrect type!");
	    }

	    org.omg.CORBA.Object s = nc.resolve( nc.to_name("dii.example"));

	    // a simple request

	    org.omg.CORBA.Request r = s._request("_get_long_number");
	    
	    r.set_return_type( orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_long));
	    r.invoke();

	    if( r.env().exception() != null )
		throw r.env().exception();
	    else
		System.out.println("0: " + r.return_value() );  

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

            System.out.println("Expecting an exception now: ");  

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
    }
}


