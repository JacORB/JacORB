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

class PushSupplierDemo extends PushSupplierPOA
{

  public PushSupplierDemo( String[] args )
  {
    org.omg.CosEventChannelAdmin.EventChannel e = null;
    org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args, null);

    try
    {
      org.omg.PortableServer.POA poa =
         org.omg.PortableServer.POAHelper.narrow
            (orb.resolve_initial_references ("RootPOA"));
      poa.the_POAManager().activate();
 
      NamingContextExt nc =
          NamingContextExtHelper.narrow(
              orb.resolve_initial_references("NameService"));

      e = EventChannelHelper.narrow(nc.resolve(
          nc.to_name("eventchannel.example")));
    }
    catch(Exception ex)
    {
      ex.printStackTrace();
    }

    SupplierAdmin supplierAdmin = e.for_suppliers();
    ProxyPushConsumer proxyPushConsumer = supplierAdmin.obtain_push_consumer();

    try
    {
      proxyPushConsumer.connect_push_supplier( _this(orb) );
    }
    catch (org.omg.CosEventChannelAdmin.AlreadyConnected ex)
    {
      ex.printStackTrace();
    }

    for(int i=0; i < 30; i++)
    {
      try
      {
        Any any = orb.create_any();
        any.insert_string("Test the channel!" + i);
        System.out.println("Pushing event # " + (i) );
        proxyPushConsumer.push( any );
      }
      catch(Disconnected d)
      {
        d.printStackTrace();
      }
    }
    proxyPushConsumer.disconnect_push_consumer();
  }

  public void disconnect_push_supplier ()
  {
    System.out.println ("Supplier disconnected");
  }

  public static void main(String[] args)
  {
    PushSupplierDemo demo = new PushSupplierDemo( args );
  }
}
