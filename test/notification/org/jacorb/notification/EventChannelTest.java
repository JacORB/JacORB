package org.jacorb.notification;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.omg.CosEventChannelAdmin.EventChannel;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.CORBA.IntHolder;
import org.omg.CosNotification.Property;
import org.omg.CosEventChannelAdmin.EventChannelHelper;
import org.omg.CosEventChannelAdmin.SupplierAdmin;
import org.omg.CosEventChannelAdmin.ConsumerAdmin;
import org.omg.CosEventComm.PushConsumer;
import org.omg.CosEventComm.PushSupplier;
import org.omg.CosEventChannelAdmin.ProxyPushConsumer;
import org.omg.CosEventChannelAdmin.ProxyPushSupplier;
import org.omg.CORBA.Any;
import org.jacorb.notification.test.Address;
import org.jacorb.notification.test.AddressHelper;
import org.apache.log4j.BasicConfigurator;
import org.omg.CosEventChannelAdmin.ProxyPullSupplier;
import org.omg.CosEventChannelAdmin.ProxyPullConsumer;

/**
 *  Unit Test for class EventChannel.
 *  Test Backward compability. Access Notification Channel via the
 *  CosEvent Interfaces.
 *
 * Created: Fri Nov 22 18:10:06 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version
 */
public class EventChannelTest extends TestCase {
    
    EventChannel channel_;
    ORB orb_;
    POA poa_;
    Any testData_;

    public void tearDown() throws Exception {
	channel_.destroy();
    }

    public void setUp() throws Exception {
	orb_ = ORB.init(new String[0], null);
	poa_ = POAHelper.narrow(orb_.resolve_initial_references("RootPOA"));

	ApplicationContext _context = new ApplicationContext();
	_context.setOrb(orb_);
	_context.setPoa(poa_);
	EventChannelFactoryImpl _factoryServant = new EventChannelFactoryImpl(_context);
	IntHolder _channelId = new IntHolder();
	channel_ = EventChannelHelper.narrow(_factoryServant.create_channel(new Property[0], new Property[0], _channelId));
	
	Address _addr = new Address();
	_addr.street = "Takustr.";
	_addr.number = 9;
	_addr.city = "Berlin";

	testData_ = orb_.create_any();

	AddressHelper.insert(testData_, _addr);
    }

    public void testPushPush() throws Exception {
	SupplierAdmin _supplierAdmin = channel_.for_suppliers();
	ConsumerAdmin _consumerAdmin = channel_.for_consumers();

	ProxyPushConsumer _proxyPushConsumer = _supplierAdmin.obtain_push_consumer();
	ProxyPushSupplier _proxyPushSupplier = _consumerAdmin.obtain_push_supplier();

	CosEventPushReceiver _receiver = new CosEventPushReceiver();
	_proxyPushSupplier.connect_push_consumer(_receiver._this(orb_));

	_proxyPushConsumer.connect_push_supplier(new CosEventPushSender()._this(orb_));
	Thread _t = new Thread(_receiver);
	_t.start();

	_proxyPushConsumer.push(testData_);

	_t.join();
	
	assertTrue(_receiver.received());
    }

    public void testPushPull() throws Exception {
	SupplierAdmin _supplierAdmin = channel_.for_suppliers();
	ConsumerAdmin _consumerAdmin = channel_.for_consumers();

	ProxyPushConsumer _proxyPushConsumer = _supplierAdmin.obtain_push_consumer();
	ProxyPullSupplier _proxyPullSupplier = _consumerAdmin.obtain_pull_supplier();

	CosEventPullReceiver _receiver = new CosEventPullReceiver(orb_, _proxyPullSupplier);
	Thread _t = new Thread(_receiver);

	_proxyPushConsumer.connect_push_supplier(new CosEventPushSender()._this(orb_));
	_t.start();
	_proxyPushConsumer.push(testData_);
	_t.join();

	assertTrue(_receiver.received());
    }

    public void testPullPush() throws Exception {
	SupplierAdmin _supplierAdmin = channel_.for_suppliers();
	ConsumerAdmin _consumerAdmin = channel_.for_consumers();

	ProxyPushSupplier _proxyPushSupplier = _consumerAdmin.obtain_push_supplier();
	ProxyPullConsumer _proxyPullConsumer = _supplierAdmin.obtain_pull_consumer();

	CosEventPushReceiver _receiver = new CosEventPushReceiver();
	_proxyPushSupplier.connect_push_consumer(_receiver._this(orb_));

	CosEventPullSender _sender = new CosEventPullSender(orb_, _proxyPullConsumer);

	Thread _t = new Thread(_receiver);
	_t.start();

	_sender.send(testData_);

	_t.join();
	
	assertTrue(_receiver.received());
    }

    public void testPullPull() throws Exception {
	SupplierAdmin _supplierAdmin = channel_.for_suppliers();
	ConsumerAdmin _consumerAdmin = channel_.for_consumers();

	ProxyPullConsumer _proxyPullConsumer = _supplierAdmin.obtain_pull_consumer();
	ProxyPullSupplier _proxyPullSupplier = _consumerAdmin.obtain_pull_supplier();

	CosEventPullReceiver _receiver = new CosEventPullReceiver(orb_, _proxyPullSupplier);
	Thread _t = new Thread(_receiver);

	CosEventPullSender _sender = new CosEventPullSender(orb_, _proxyPullConsumer);

	_t.start();
	_sender.send(testData_);
	_t.join();

	assertTrue(_receiver.received());
    }

    /** 
     * Creates a new <code>EventChannelTest</code> instance.
     *
     * @param name test name
     */
    public EventChannelTest (String name){
	super(name);
    }

    /**
     * @return a <code>TestSuite</code>
     */
    public static TestSuite suite(){
	TestSuite suite = new TestSuite(EventChannelTest.class);

	//	suite.addTest(new EventChannelTest("testPullPush"));
	
	return suite;
    }

    static {
	BasicConfigurator.configure();
    }

    /** 
     * Entry point 
     */ 
    public static void main(String[] args) {
	junit.textui.TestRunner.run(suite());
    }
}// EventChannelTest
