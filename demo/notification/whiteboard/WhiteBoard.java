package demo.notification.whiteboard;

import java.util.Hashtable;
import java.util.Map;

import org.omg.CORBA.IntHolder;
import org.omg.CORBA.ORB;
import org.omg.CosEventChannelAdmin.AlreadyConnected;
import org.omg.CosEventComm.Disconnected;
import org.omg.CosNotification.EventHeader;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.FixedEventHeader;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyChannelAdmin.AdminLimitExceeded;
import org.omg.CosNotifyChannelAdmin.AdminNotFound;
import org.omg.CosNotifyChannelAdmin.ClientType;
import org.omg.CosNotifyChannelAdmin.ConsumerAdmin;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyChannelAdmin.InterFilterGroupOperator;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPushConsumer;
import org.omg.CosNotifyChannelAdmin.StructuredProxyPushConsumerHelper;
import org.omg.CosNotifyChannelAdmin.SupplierAdmin;
import org.omg.CosNotifyComm.InvalidEventType;
import org.omg.CosNotifyComm.StructuredPushSupplierOperations;
import org.omg.CosNotifyComm.StructuredPushSupplierPOATie;
import org.omg.CosNotifyFilter.ConstraintExp;
import org.omg.CosNotifyFilter.Filter;
import org.omg.CosNotifyFilter.FilterFactory;
import org.omg.CosNotifyFilter.InvalidConstraint;
import org.omg.CosNotifyFilter.InvalidGrammar;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public class WhiteBoard
    extends IWhiteBoardPOA
    implements IWhiteBoardOperations, WhiteboardVars {

    // zur Erzeugung einer eindeutigen Id
    static int COUNT = 0;

    EventChannel channel_;
    FilterFactory filterFactory_;

    // alle angemeldeten Workgroups
    protected Map workgroups_;

    Map registrationInfo_ = new Hashtable();

    // bisher gesammelte Updates
    //    Queue updates_;

    // Dispatcher Thread
    Dispatcher disp;

    // prima"rkopie
    BrushSizePixelImage image;

    public WhiteBoard(ORB orb, EventChannel channel) throws AdminLimitExceeded {
	this(orb, channel, 400, 400);
    }

    public WhiteBoard(ORB orb,
		      EventChannel channel,
		      int xsize,
		      int ysize) throws AdminLimitExceeded {

	System.out.println("WhiteBoard.init()");

	_this(orb);
	workgroups_ = new Hashtable();
	clear();
	image = new ExtendedPixelImage(xsize,ysize);
	disp = new Dispatcher(orb, this, channel.default_supplier_admin());
	//	disp.start();
	System.out.println("done");
	filterFactory_ = channel.default_filter_factory();
	channel_ = channel;
    }

    public int[] getCurrentImage() {
	return image.getPixelBuffer();
    }

    // Workgroup an Whiteboard anmelden
    // Id zur Kommunikation mit dem Whiteboard zuru"ck
    public IRegistrationInfo join(IWorkgroup group) {
	System.out.println("Workgroup joins the Whiteboard ");

	Integer _id = new Integer(COUNT++);
	System.out.println("workgroups: " + workgroups_ + ".put(" + _id + ", " + group);

	workgroups_.put(_id, group);

	IRegistrationInfo _registrationInfo = new IRegistrationInfo();
	_registrationInfo.workgroup_identifier = _id.intValue();

	IntHolder
	    _supplierAdminId = new IntHolder(),
	    _consumerAdminId = new IntHolder();

	Filter _filter = null;
	try {
	    _filter = filterFactory_.create_filter("EXTENDED_TCL");

	    ConstraintExp[] _constraints = new ConstraintExp[1];
	    _constraints[0] = new ConstraintExp();
	    _constraints[0].event_types = new EventType[] {new EventType(EVENT_DOMAIN, "*")};
	    _constraints[0].constraint_expr = "$.header.variable_header(" + WORKGROUP_ID + ") != " + _id.intValue();


	    _filter.add_constraints(_constraints);

	    SupplierAdmin _supplierAdmin =
		channel_.new_for_suppliers(InterFilterGroupOperator.AND_OP, _supplierAdminId);

	    ConsumerAdmin _consumerAdmin =
		channel_.new_for_consumers(InterFilterGroupOperator.AND_OP, _consumerAdminId);
	    _consumerAdmin.add_filter(_filter);

	    _registrationInfo.supplier_admin = _supplierAdmin;
	    _registrationInfo.consumer_admin = _consumerAdmin;
	    _registrationInfo.filter_factory = filterFactory_;

	    LocalRegistrationInfo _localInfo = new LocalRegistrationInfo();
	    _localInfo.consumerAdmin_ = _consumerAdminId.value;
	    _localInfo.supplierAdmin_ = _supplierAdminId.value;

	    registrationInfo_.put(_id, _localInfo);

	    return _registrationInfo;
	} catch (InvalidGrammar ig) {
	    ig.printStackTrace();
	} catch (InvalidConstraint ic) {
	    ic.printStackTrace();
        }
	throw new RuntimeException();
    }

    public boolean leave(int workgroup) {
	System.out.println("Bye");

	Integer _id = new Integer(workgroup);

	LocalRegistrationInfo _info =
	    (LocalRegistrationInfo)registrationInfo_.get(_id);

	try {
	    channel_.get_consumeradmin(_info.consumerAdmin_).destroy();
	    channel_.get_supplieradmin(_info.supplierAdmin_).destroy();
	} catch (AdminNotFound anf) {
	}

	return (workgroups_.remove(_id) != null);
    }

    // Whiteboard lo"schen
    public void clear() {
	//	updates_ = new Queue();
    }

} // WhiteBoard

class Dispatcher
    extends Thread
    implements WhiteboardVars, StructuredPushSupplierOperations {

    boolean connected_ = false;
    boolean active_ = true;
    WhiteBoard master_;
    StructuredProxyPushConsumer myConsumer_;
    ORB orb_;
    IntHolder myConsumerId_ = new IntHolder();
    static long SLEEP = 10000L;

    public Dispatcher(ORB orb,
		      WhiteBoard master,
		      SupplierAdmin supplierAdmin) throws AdminLimitExceeded {

	master_ = master;
	orb_ = orb;
	myConsumer_ =
	    StructuredProxyPushConsumerHelper.narrow(supplierAdmin.obtain_notification_push_consumer(ClientType.STRUCTURED_EVENT, myConsumerId_));
	connected_ = tryConnect();
    }

    boolean tryConnect() {
	long BACKOFF = 100L;

	for (int x=0; x<3; ++x) {
	    try {
		myConsumer_.connect_structured_push_supplier(new StructuredPushSupplierPOATie(this)._this(orb_));
		connected_ = true;
		return true;
	    } catch (AlreadyConnected ac) {
	    }

	    try {
		Thread.sleep(BACKOFF^(x+1));
	    } catch (InterruptedException ie) {
	    }
	}
	return false;
    }

    public void shutdown() {
	active_ = false;
	interrupt();
    }

    public void run() {
	StructuredEvent _event = new StructuredEvent();
	_event.header = new EventHeader();
	_event.header.fixed_header = new FixedEventHeader();
	_event.header.fixed_header.event_type = new EventType();
	_event.header.fixed_header.event_type.domain_name = EVENT_DOMAIN;
	_event.header.fixed_header.event_type.type_name = "UPDATE";
	_event.header.fixed_header.event_name = "";
	_event.header.variable_header = new Property[0];

	_event.filterable_data = new Property[0];

	while (active_) {
	    try {
		Thread.sleep(SLEEP);
	    } catch (InterruptedException ie) {
		if (!active_) {
		    return;
		}
	    }

	    if (!connected_) {
		connected_ = tryConnect();
		if (!connected_) {
		    System.out.println("Giving Up");
		    return;
		}
	    }

	    try {
		WhiteboardUpdate _update = new WhiteboardUpdate();
		_update.image(master_.getCurrentImage());
		_event.remainder_of_body = orb_.create_any();
		WhiteboardUpdateHelper.insert(_event.remainder_of_body, _update);
		myConsumer_.push_structured_event(_event);
	    } catch (Disconnected d) {
		connected_ = false;
	    }
	}
    }

    public void disconnect_structured_push_supplier() {
	connected_ = false;
    }

    public void subscription_change(EventType[] t1, EventType[] t2) throws InvalidEventType {
    }
}

class LocalRegistrationInfo {
    int consumerAdmin_;
    int supplierAdmin_;
}

