package demo.events;

/**
 * @authors Joerg v. Frantzius, Rainer Lischetzki, Gerald Brose 1997
 *
 * A simple demo for using the event channel as a pull consumer 
 * of events
 *
 */

import org.omg.CosNaming.*;
import org.omg.CosEventChannelAdmin.*;
import org.omg.CosEventComm.*;
import org.omg.CORBA.Any;

public class PullConsumerDemo
        extends PullConsumerPOA
{
  public PullConsumerDemo(org.omg.CORBA.ORB orb)
  {
    _this_object( orb );
  }

  public void disconnect_pull_consumer()
  {
    System.out.println ("Consumer disconnected");
  }

  static public void main  (String argv[])
  {
    EventChannel    ecs = null;
    ConsumerAdmin   ca;
    ProxyPullSupplier pps;
    PullConsumer  pullConsumer;
    Any     received = null;
    org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(argv, null);

    // binding the event channel reference
    try
    {
      org.omg.PortableServer.POA poa =
        org.omg.PortableServer.POAHelper.narrow
          (orb.resolve_initial_references ("RootPOA"));
 
      poa.the_POAManager().activate();
      NamingContextExt nc = 
              NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService")); 
      ecs = EventChannelHelper.narrow(nc.resolve(nc.to_name("eventchannel.example")));
    }
    catch( Exception e )
    {
      e.printStackTrace();
    }

    // registering ourselves

    pullConsumer = (org.omg.CosEventComm.PullConsumer)(new PullConsumerDemo(orb))._this();
    ca  = ecs.for_consumers();
    pps = ca.obtain_pull_supplier();
    try
    {
      pps.connect_pull_consumer( (PullConsumer) pullConsumer );
    }
    catch( Exception e )
    {
      e.printStackTrace();
    }

    System.out.println("TestPullConsumer registered.");

    // pulling events
    int i=0;
    while( i<10 )
    {
      System.out.println("pulling event " + i);
      org.omg.CORBA.BooleanHolder bh = new org.omg.CORBA.BooleanHolder();
      try
      {
        received = pps.try_pull(bh);
        //		received = pps.pull();
        if( bh.value )
        {
          System.out.println("received " + (i++) + " : " + 
                             received.extract_string() ); 
        }
        else
        {
          // we did not get any real any, so we continue
          // polling after a short nap
          Thread.currentThread().sleep(2000);
        }
      }
      catch( Exception e )
      {
        e.printStackTrace();
      }
    }
    pps.disconnect_pull_supplier();
  }
}


