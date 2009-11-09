package demo.notification.office;

/**
 * A simple notification demo client for the Printer service.
 * It will send a few commands to the printer and wait for
 * structured events to see what happens. Serves to show that
 * the Notification Service is set up correctly and works.
 *
 * @author Gerald Brose, 08/01/2003
 */

import org.omg.CosNotification.*;
import org.omg.CosNotifyComm.*;
import org.omg.CosNotifyFilter.*;
import org.omg.CosNotifyChannelAdmin.*;

import org.omg.CosNaming.*;
import org.omg.CORBA.Any;

import demo.notification.office.PrinterPackage.*;

import org.omg.PortableServer.*;
import org.omg.CORBA.NO_IMPLEMENT;

public class PrintClient
    extends StructuredPullConsumerPOA
{
    /**
     * releases any resources, none in this case
     */
    public void disconnect_structured_pull_consumer()
    {
        System.out.println("Disconnected!");
    }

    public void offer_change( EventType added[], EventType removed[] )
    {
    }

    /**
     *    main
     */

    static public void main( String argv[] )
    {
        EventChannel channel = null;
        FilterFactory filterFactory = null;
        Filter filter = null;
        ConsumerAdmin consumerAdmin;
        StructuredProxyPullSupplier  proxyPullSupplier = null;
        StructuredPullConsumer structuredPullConsumer;
        Printer printer = null;
        String userid = "MeMyselfAndI";

        if( argv.length > 0 )
            userid = argv[0];

        // initialize ORB

        org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init( argv, null);
        POA poa = null;

        try
        {
            // inititialize POA
            poa =
                POAHelper.narrow(orb.resolve_initial_references("RootPOA"));

            // get naming service reference
            NamingContextExt nc =
                NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));

            // find the event channel reference and the Printer
            channel =
                EventChannelHelper.narrow(nc.resolve(nc.to_name("office_event.channel")));

            printer = PrinterHelper.narrow( nc.resolve(nc.to_name("Printer")));

            poa.the_POAManager().activate();

            // create and implicitly activate the client
            structuredPullConsumer =
                (StructuredPullConsumer)new PrintClient()._this(orb);

            // get the admin interface and the supplier proxy
            consumerAdmin  = channel.default_consumer_admin();

            proxyPullSupplier =
                StructuredProxyPullSupplierHelper.narrow(
                    consumerAdmin.obtain_notification_pull_supplier(
                                 ClientType.STRUCTURED_EVENT,
                                 new org.omg.CORBA.IntHolder() ) );

            // connect ourselves to the event channel
            proxyPullSupplier.connect_structured_pull_consumer( structuredPullConsumer );

            // get the default filter factory
            filterFactory = channel.default_filter_factory();
            if( filterFactory == null )
            {
                System.err.println("No default filter Factory!");
            }
            else
            {
                filter = filterFactory.create_filter("EXTENDED_TCL");
                EventType [] eventTypes =
                    new EventType[] { new EventType("Office", "Printed"),
                                      new EventType("Office", "Canceled") };

                ConstraintExp constraint =
                    new ConstraintExp ( eventTypes, "TRUE" );

                filter.add_constraints( new ConstraintExp[]{ constraint } );
                proxyPullSupplier.add_filter(filter);
            }

            Property[] qos = new Property[1];
            org.omg.CORBA.Any data = org.omg.CORBA.ORB.init().create_any();
            data.insert_short( PriorityOrder.value );
            qos[0] = new Property( OrderPolicy.value, data);

            try
            {
                consumerAdmin.set_qos(qos);
            }
            catch (UnsupportedQoS ex)
            {
                System.err.println("Unsupported QoS");
            }
            catch (NO_IMPLEMENT e) {
                // this method is not supported yet
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }

        // print a couple of jobs

        for( int i = 0; i < 5; i++ )
        {
            try
            {
                System.out.println("Sending job, ID #" +
                                   printer.print("A test job", userid));
            }
            catch( OffLine ol )
            {
                System.err.println("Printer found off line when printing job.");
            }
        }

        // wait a sec...

        try
        {
            System.out.println("Sleep...");
            Thread.sleep(5000);
        }
        catch( Exception e)
        {}

        // try to cancel the last job

        int job = 4;
        try
        {
            System.out.println("Cancelling job ID #" + job );
            printer.cancel( job, userid );
        }
        catch( UnknownJobID ol )
        {
            System.err.println("Unknown job ID #" + job );
        }
        catch( AlreadyPrinted ap)
        {
            System.err.println("Could not cancel, job #" + job + " already printed");
        }
        catch( org.omg.CORBA.NO_PERMISSION np)
        {
            System.err.println("Could not cancel, job #" + job + ", no permission");
        }

        int eventsReceived = 0;

        for( int i = 0; i < 5; i++ )
        {
            org.omg.CORBA.BooleanHolder bh =
                new org.omg.CORBA.BooleanHolder();

            try
            {
                System.out.println("Looking for structured events....");
                // try to pull an event
                StructuredEvent event =
                    proxyPullSupplier.try_pull_structured_event(bh);

                if( bh.value )
                {
                    System.out.println("got structured event.");
                    FixedEventHeader fixed_header = event.header.fixed_header;
                    System.out.println("\t" + fixed_header.event_type.domain_name + "." +
                                       fixed_header.event_type.type_name + "#" +
                                       fixed_header.event_name  );

                    Property properties [] = event.filterable_data;
                    System.out.println("\t" + properties[0].name +
                                       " : " + properties[0].value.extract_long() );
                    System.out.println("\t" + properties[1].name +
                                       " : " + properties[1].value.extract_string() );
                }
                Thread.currentThread().sleep(2000);
            }
            catch( Exception e )
            {
                e.printStackTrace();
            }
        }
        // disconnect and shutdown
        proxyPullSupplier.disconnect_structured_pull_supplier();
        orb.shutdown(true);
    }
}





