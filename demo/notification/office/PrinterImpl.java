package demo.notification.office;

/**
 *
 */

import org.omg.CosNotification.*;
import org.omg.CosNotifyComm.*;
import org.omg.CosNotifyChannelAdmin.*;

import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.*;

import java.util.Hashtable;
import demo.notification.office.PrinterPackage.*;

class PrinterImpl
    extends PrinterPOA
    implements StructuredPushSupplierOperations
{
    private EventChannel channel;
    private SupplierAdmin supplierAdmin ;
    private StructuredProxyPushConsumer pushConsumer;
    private ORB orb;
    private POA poa;

    private Hashtable queue;
    private int jobId;
    private int printIdx;
    private int eventId;
    private boolean offline;
    private boolean disconnected;
    private PrintThread printThread;

    static class JobInfo
    {
	public int jobId;
	public String userId;
	public String text;

	public JobInfo(int jobId, String userId, String text)
	{
	    this.jobId = jobId;
	    this.userId = userId;
	    this.text = text;
	}
    }

    public int getEventId()
    {
        return eventId++;
    }


    /** Inner class PrintThread ( member class)
	simulates the actual "printing" in a separate thread
    */

    class PrintThread extends Thread
    {
	public PrintThread()
	{
	    start();
	}

	/**
	 * convenience method that does the synchronization
	 */

	public synchronized void tell()
	{
	    super.notify();
	}

	public void run()
	{
	    while( true )
	    {
		// wait until there are jobs waiting
		while( printIdx >= jobId || offline )
		{
		    try
		    {
			synchronized( this )
			{
			    this.wait();
			}
		    }
		    catch( InterruptedException ie )
		    {}
		}

		// "print"
		JobInfo job = (JobInfo)queue.remove( new Integer( printIdx ));
		if( job != null && generateEvents() )
		{
		    System.out.println("--Printing Job # " + job.jobId + " --\n" + job.text + "\n--END JOB---");
		    // create a structured event
		    StructuredEvent printedEvent = new StructuredEvent();

		    // set the event type and name
		    EventType type = new EventType("Office", "Printed");
		    FixedEventHeader fixed = new FixedEventHeader(type, "" + getEventId() );

		    // complete header date
		    Property variable[] = new Property[0];

		    printedEvent.header = new EventHeader(fixed, variable);

		    // set filterable event body data
		    printedEvent.filterable_data = new Property[3];

		    Any jobAny = orb.create_any();
		    jobAny.insert_long( job.jobId );
		    printedEvent.filterable_data[0] = new Property("job_id", jobAny );

		    Any userAny = orb.create_any();
		    userAny.insert_string( job.userId );
		    printedEvent.filterable_data[1] = new Property("user_id", userAny );

                    Any urgentAny = orb.create_any();
                    urgentAny.insert_boolean( false );
                    printedEvent.filterable_data[2] = new Property( "urgent", urgentAny );

		    // no further even data
		    printedEvent.remainder_of_body = orb.create_any();

		    try
		    {
                boolean exist = false;
                try
                {
                    exist = ! pushConsumer._non_existent();
                }
                catch( org.omg.CORBA.SystemException e )
                {
                    // exist remains false
                }

                if( exist )
                    pushConsumer.push_structured_event(printedEvent);
                else
                    System.err.println("Object " + pushConsumer + " not existent");
		    }
		    catch( org.omg.CosEventComm.Disconnected d )
		    {
                        // ignore
		    }
		}
		// update internal printing position
		printIdx++;
		try
		{
		    Thread.sleep(5000);
		}
		catch( Exception e )
		{
		    // ignore
		}
	    }
	}
    }


    public PrinterImpl(EventChannel e, ORB orb, POA poa)
    {
	// set the ORb and event channel
	this.orb = orb;
	this.poa = poa;
	channel = e;
    }

    public void connect()
    {
	StructuredPushSupplierPOATie thisTie = new StructuredPushSupplierPOATie( this );

	// get admin interface and proxy consumer
	supplierAdmin = channel.default_supplier_admin();

        ClientType ctype = ClientType.STRUCTURED_EVENT;
        org.omg.CORBA.IntHolder proxyIdHolder = new org.omg.CORBA.IntHolder();

        try
        {
            pushConsumer =
		StructuredProxyPushConsumerHelper.narrow(
                        supplierAdmin.obtain_notification_push_consumer(ctype, proxyIdHolder));
        }
        catch (AdminLimitExceeded ex)
        {
            System.err.println("Could not get consumer proxy, maximum number of proxies exceeded!");
            System.exit(1);
        }


	// connect the push supplier
	try
	{
	    pushConsumer.connect_structured_push_supplier( StructuredPushSupplierHelper.narrow( poa.servant_to_reference( thisTie )));
	}
	catch( Exception e )
	{
	    e.printStackTrace();
	}
	// initialize "queue" and start printer thread
	queue = new Hashtable();
	printThread = new PrintThread();
    }

    /**
     * Enter a job in the printer queue
     */

    public synchronized int print( String text, String uid)
	throws OffLine
    {
	if( offline )
	    throw new OffLine();

	queue.put( new Integer(jobId), new JobInfo( jobId, uid, text ));
	printThread.tell();
	return jobId++;
    }

    /**
     * Remove a job in the printer queue
     */

    public void cancel(int id, String uid )
	throws UnknownJobID, AlreadyPrinted
    {

	if( id > jobId || id < 0)
	    throw new UnknownJobID();

	if( id < printIdx )
	    throw new AlreadyPrinted();

	JobInfo job = (JobInfo)queue.get( new Integer( id ));
	if( job != null )
	{
	    if( !job.userId.equals( uid ))
		throw new org.omg.CORBA.NO_PERMISSION();

	    queue.remove( new Integer( id ));

	    System.out.println("--CANCELLED JOB #" + id  + "--");

	    if( generateEvents() )
	    {
		// create a structured event
		StructuredEvent cancelEvent = new StructuredEvent();

		// set the event type and name
		EventType type = new EventType("Office", "Canceled");
		FixedEventHeader fixed = new FixedEventHeader(type, "" + getEventId() );

		// complete header date
		Property variable[] = new Property[0];
		cancelEvent.header = new EventHeader(fixed, variable);

		// set filterable event body data
		cancelEvent.filterable_data = new Property[3];

		Any jobAny = orb.create_any();
		jobAny.insert_long( job.jobId );
		cancelEvent.filterable_data[0] = new Property("job_id ", jobAny );

		Any userAny = orb.create_any();
		userAny.insert_string( job.userId );
		cancelEvent.filterable_data[1] = new Property("user_id ", userAny );

		Any urgentAny = orb.create_any();
		urgentAny.insert_boolean( true );
		cancelEvent.filterable_data[2] = new Property( "urgent", urgentAny );

		cancelEvent.remainder_of_body = orb.create_any();

		try
		{
		    pushConsumer.push_structured_event( cancelEvent );
		}
		catch( org.omg.CosEventComm.Disconnected d )
		{
		    // ignore
		}
	    }
	}
    }

    /**
     * Sets the printer online/offline
     */

    public void setOffLine(boolean flag)
    {
	offline = flag;
	if( !offline )
	    printThread.tell();

	if( generateEvents() )
	{
	    // create a structured event
	    StructuredEvent lineEvent = new StructuredEvent();

            String typeSuffix = ( offline ? "offline" : "online" );

	    // set the event type and name
	    EventType type = new EventType("Office", "Printer" + typeSuffix);
            FixedEventHeader fixed = new FixedEventHeader(type, "" + getEventId() );

            // complete header date
            //		Any priorityAny = orb.create_any();
            //		priorityAny.insert_short( (short)4 );

            Property variable[] = new Property[0];
	    lineEvent.header = new EventHeader(fixed, variable);

	    // set filterable event body data
	    lineEvent.filterable_data = new Property[1];

            Any urgentAny = orb.create_any();
            urgentAny.insert_boolean( false );
            lineEvent.filterable_data[0] = new Property( "urgent", urgentAny );

	    lineEvent.remainder_of_body = orb.create_any();
	    try
	    {
		pushConsumer.push_structured_event( lineEvent );
	    }
	    catch( org.omg.CosEventComm.Disconnected d )
	    {
                // ignore
	    }
	}
    }


    boolean generateEvents()
    {
	return !disconnected;
    }

    /**
     * Potentially release resources,
     * from CosNotifyComm.NotifySubscribe
     */

    public void disconnect_structured_push_supplier()
    {
	disconnected = true;
        System.out.println("Disconnected!");
    }

    /**
     * from CosNotifyComm.NotifySubscribe
     */

    public void subscription_change(EventType added[], EventType removed[])
    {
	// react somehow;
    }



}
