package demo.mtclient;

/**
 * Test JacORB's multi-threading and call-back support:
 *
 * use any number of ClientThreads on the same server 
 * object.
 */

import org.omg.CosNaming.*;
import org.omg.PortableServer.*;

public class AppletClient extends java.applet.Applet
{

    static org.omg.CORBA.ORB orb=null;
    static String[] args={"I am an applet"};

    public void init()
    {
	    java.util.Properties props = new java.util.Properties();
	    props.put("org.omg.CORBA.ORBClass",
                      "org.jacorb.orb.ORB");
	    props.put("org.omg.CORBA.ORBSingletonClass",
                      "org.jacorb.orb.ORBSingleton");

	    org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(this,props);

            //	orb = org.omg.CORBA.ORB.init((java.applet.Applet)this,null);
	MyServer s = null;
	int clientNum = 2;
	String msg = "<test_msg>";

	if( args.length != 1 )
	{
	    System.out.println("Usage: remoteClient <message>");
	    System.exit(1);
	}
	try
	{

	    // get hold of the naming service
	    NamingContextExt nc = 
		NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));

	    s = MyServerHelper.narrow(nc.resolve(nc.to_name("Thread.example")));

	    POA poa = 
		POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
	
	    poa.the_POAManager().activate();
	
	    /* create thread objects */
	    ClientThread [] clientThread = new ClientThread [clientNum] ;
	    for( int i = 0; i < clientNum; i++)
	    {
		clientThread[i] = new ClientThread(s, msg, i); 
	    }

	    /* create CORBA references for each client thread */
	    Observer [] observers = new  Observer [clientNum];
	    for( int i = 0; i < clientNum; i++)
	    { 
		observers[i] = 
		    ObserverHelper.narrow(poa.servant_to_reference( new ObserverPOATie( clientThread[i] )));
		clientThread[i].setMe( observers[i]);
	    }

	    /* start threads */

	    for( int i = 0; i < clientNum; i++)
	    { 
		clientThread[i].start();   
	    }
	  
	    int which = 0;
	    while( which < clientNum )
	    {
		while( clientThread[which].isAlive() )
		    Thread.currentThread().sleep(500);
		which++;
	    }

	    System.out.println("Going down...");

	    orb.shutdown(true);
	} 
	catch (Exception e)
	{
	    e.printStackTrace();
	}

    }

}



