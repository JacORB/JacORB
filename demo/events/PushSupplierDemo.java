package demo.events;

/**
 * @authors Joerg v. Frantzius, Rainer Lischetzki, Gerald Brose 1997
 *
 * A simple demo for using the event channel as a push supplier of events.
 *
 */

import org.omg.CosEventChannelAdmin.*;
import org.omg.CosEventComm.*;
import org.omg.CosNaming.*;
import org.omg.CORBA.Any;

class PushSupplierDemo 
    extends PushSupplierPOA
{
    public void disconnect_push_supplier() {}

    static public void main (String argv[]) 
    {
	org.omg.CosEventChannelAdmin.EventChannel e = null;
	org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(argv, null);

	try 
	{
	    NamingContextExt nc = 
		NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));	
	    e = EventChannelHelper.narrow(nc.resolve(nc.to_name("eventchannel.example")));
	} 
	catch (Exception ex) 
	{ 
	    ex.printStackTrace();
	}			

	SupplierAdmin supplierAdmin = e.for_suppliers();
	ProxyPushConsumer proxyPushConsumer = 
	    supplierAdmin.obtain_push_consumer();

	Any event = orb.create_any();
	    
	int i=0;
	while (i<10)
	{
	    try 
	    {
		event.insert_string("Test the channel!" + i);
		System.out.println("Pushing event # " + (i++) );
		proxyPushConsumer.push( event );
	    } 
	    catch (Disconnected d) 
	    {
		d.printStackTrace();
	    }            
	}
	proxyPushConsumer.disconnect_push_consumer();
    }
}


