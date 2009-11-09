package demo.events;

/**
 * @authors Joerg v. Frantzius, Rainer Lischetzki, Gerald Brose 1997
 *
 * A simple demo for using the event channel as a push supplier of events.
 *
 */

import org.omg.CosNaming.*;
import org.omg.CosEventChannelAdmin.*;
import org.omg.CosEventComm.*;
import org.omg.CORBA.Any;


class PullSupplierDemo extends Thread implements PullSupplierOperations
{
  Any event = null;

  public PullSupplierDemo()
  {
    start();
  }

  public void disconnect_pull_supplier()
  {
    System.out.println("Bye.");
  }

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
    catch( Exception ex )
    {
      ex.printStackTrace();
    }

    SupplierAdmin supplierAdmin = e.for_suppliers();
    ProxyPullConsumer proxyPullConsumer = 
            supplierAdmin.obtain_pull_consumer();

    try
    {
      org.omg.PortableServer.POA poa = 
              org.omg.PortableServer.POAHelper.narrow(orb.resolve_initial_references("RootPOA"));

      PullSupplierPOATie pt = new PullSupplierPOATie( new PullSupplierDemo());
      pt._this_object(orb);
      org.omg.CORBA.Object o = poa.servant_to_reference( pt );

      poa.the_POAManager().activate();

      proxyPullConsumer.connect_pull_supplier( PullSupplierHelper.narrow(o) );
    }
    catch( Exception ex )
    {
      ex.printStackTrace();
    }
  }

  public Any pull() throws Disconnected
  {
    System.out.println("I m being pulled.");
    event = org.omg.CORBA.ORB.init().create_any();
    event.insert_string("Pull.");
    return event;
  }

  public void run()
  {
    // do something
    while( true )
    {
      try
      {
        synchronized( this )
        {
          wait(); 
        }
      }
      catch( Exception e )
      {
        disconnect_pull_supplier();
      }
    }
  }


  public Any try_pull( org.omg.CORBA.BooleanHolder has_event) 
      throws Disconnected 
  {
    System.out.println("I m being try_pulled.");
    event = org.omg.CORBA.ORB.init().create_any();
    event.insert_string("TryPull.");
    has_event.value = true;
    return event;
  }
}


