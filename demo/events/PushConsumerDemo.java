package demo.events;

/**
 * @authors Joerg v. Frantzius, Rainer Lischetzki, Gerald Brose 1997
 *
 * A simple demo for using the event channel as a push consumer
 * of events. This consumer unregisters and quits after receiving
 * 5 events.
 *
 */

import org.omg.CosEventChannelAdmin.*;
import org.omg.CosEventComm.*;
import org.omg.CosNaming.*;

public class PushConsumerDemo implements PushConsumerOperations
{
  private short count = 0;
  private ProxyPushSupplier myPps = null;
  private int limit = 25;

  static org.omg.CORBA.ORB orb = null;

  public PushConsumerDemo( ProxyPushSupplier _pps )
  {
    myPps = _pps;
  }

  public void disconnect_push_consumer()
  {
    System.out.println("Consumer disconnected.");
  }

  static public void main  (String[] args)
  {
    EventChannel ecs = null;
    ConsumerAdmin ca = null;
    PushConsumer pushConsumer = null;
    ProxyPushSupplier pps = null;

    try
    {
      orb = org.omg.CORBA.ORB.init(args, null);
      NamingContextExt nc =
          NamingContextExtHelper.narrow(
              orb.resolve_initial_references("NameService"));

      ecs = EventChannelHelper.narrow(nc.resolve(
          nc.to_name("eventchannel.example")));
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }

    ca  = ecs.for_consumers();
    pps = ca.obtain_push_supplier();

    try
    {
      org.omg.PortableServer.POA poa =
          org.omg.PortableServer.POAHelper.narrow(
              orb.resolve_initial_references("RootPOA"));

      poa.the_POAManager().activate();

      PushConsumerPOATie pt = new PushConsumerPOATie( new PushConsumerDemo( pps ));
      pt._this_object(orb);
      pushConsumer = PushConsumerHelper.narrow(poa.servant_to_reference(pt) );
      pps.connect_push_consumer( pushConsumer );
      System.out.println("PushConsumerImpl registered.");
      orb.run();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    System.out.println("Quit.");
  }

  public synchronized void push(org.omg.CORBA.Any data)
      throws org.omg.CosEventComm.Disconnected
  {
    count++;
    System.out.println("event " + count +
                       " : " + data.extract_string());
    if( count >= limit )
    {
      System.out.println("unregister");
      myPps.disconnect_push_supplier();
      // System.exit(0);
      orb.shutdown(false);
    }
  }
}