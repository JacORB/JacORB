package demo.notification.office;

/**
 *
 */

import org.omg.CosNotification.*;
import org.omg.CosNotifyComm.*;
import org.omg.CosNotifyChannelAdmin.*;

import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.*;

import demo.notification.office.*;

class PrintServer
{
    /**
     * main
     */

    static public void main (String argv[])
    {
	EventChannel channel = null;
	org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(argv, null);

	try
	{
	    // initialize POA, get naming and event service references
	    POA poa = POAHelper.narrow(orb.resolve_initial_references("RootPOA") );
	    poa.the_POAManager().activate();

	    NamingContextExt nc =
		NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));

            EventChannelFactory factory =
                EventChannelFactoryHelper.narrow( orb.resolve_initial_references("NotificationService"));

            if( factory == null )
            {
                System.err.println("Could not find or narrow EventChannelFactory");
                System.exit(1);
            }

            org.omg.CORBA.IntHolder idHolder =
                new org.omg.CORBA.IntHolder();

            Property[] qos = new Property[0];
            Property[] adm = new Property[0];

            channel = factory.create_channel(qos, adm, idHolder);
            nc.rebind( nc.to_name("office_event.channel"), channel );

            System.out.println("Channel " + idHolder.value +
                               " created and bound to name office_event.channel.");

	    // create a Printer object, implicitly activate it and advertise its presence
	    PrinterImpl printer = new PrinterImpl( channel, orb, poa );
	    printer.connect();
            System.out.println("Printer created and connected");

	    org.omg.CORBA.Object printerObj = poa.servant_to_reference( printer );
	    nc.rebind( nc.to_name("Printer"), printerObj);
            System.out.println("Printer exported");

	    // wait for requests
	    orb.run();
	}
	catch (Exception ex)
	{
	    ex.printStackTrace();
	}
    }
}
