package org.jacorb.notification;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2003 Gerald Brose
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Library General Public
 *   License as published by the Free Software Foundation; either
 *   version 2 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this library; if not, write to the Free
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.CosNotifyChannelAdmin.EventChannelFactory;
import org.omg.CosNotifyFilter.FilterFactory;
import org.omg.CORBA.Any;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CORBA.IntHolder;
import org.omg.CosNotifyChannelAdmin.SupplierAdmin;
import org.omg.CosNotifyChannelAdmin.ConsumerAdmin;
import org.omg.CosNotifyFilter.Filter;
import org.apache.log4j.Logger;
import org.omg.CosNotification.Property;
import org.omg.CosNotifyFilter.ConstraintExp;
import org.omg.CosNotification.EventType;
import EDU.oswego.cs.dl.util.concurrent.CyclicBarrier;
import EDU.oswego.cs.dl.util.concurrent.Latch;
import org.omg.PortableServer.POAHelper;
import org.omg.CosNotifyFilter.ConstraintInfo;
import junit.extensions.RepeatedTest;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import java.util.Map;
import java.util.Hashtable;
import java.util.Iterator;
import org.jacorb.notification.test.TimingTest;
import org.jacorb.notification.test.TimingTestHelper;
import java.util.Vector;
import org.omg.CosNotifyChannelAdmin.EventChannelFactoryHelper;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotification.FixedEventHeader;
import org.omg.CosNotification.EventHeader;

/**
 *  Unit Test for class Performance
 *
 *
 * Created: Mon Jan 06 14:41:48 2003
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public class PerformanceTest extends TestCase {
    ORB orb_;
    POA poa_;

    EventChannelFactory factory_;
    FilterFactory filterFactory_;
    Any testPerson_;
    EventChannel channel_;
    IntHolder channelId_;
    SupplierAdmin supplierAdmin_;
    ConsumerAdmin consumerAdmin_;
    Filter trueFilter_;

    Logger logger_ = Logger.getLogger("PerformanceTest");

    /** 
     * Creates a new <code>PerformanceTest</code> instance.
     *
     * @param name test name
     */
    public PerformanceTest (String name){
	super(name);
    }
    
    /**
     * setup EventChannelFactory, FilterFactory and Any with Testdata
     */
    public void setUp() throws Exception {
	orb_ = ORB.init(new String[0], null);
	poa_ = POAHelper.narrow(orb_.resolve_initial_references("RootPOA"));

	ApplicationContext _context = new ApplicationContext(orb_, poa_);

	EventChannelFactoryImpl _factoryServant = 
	    new EventChannelFactoryImpl(_context);

	factory_ = _factoryServant._this(orb_);
	//factory_ = EventChannelFactoryHelper.narrow(orb_.resolve_initial_references("OpenorbNotification"));
	//factory_ = EventChannelFactoryHelper.narrow(orb_.resolve_initial_references("NotificationService"));

	// prepare test data
	testPerson_ = TestUtils.getTestPersonAny(orb_);

	// setup a channel
	channelId_ = new IntHolder();
	channel_ = factory_.create_channel(new Property[0], new Property[0], channelId_);

	filterFactory_ = channel_.default_filter_factory();

	supplierAdmin_ = channel_.default_supplier_admin();
	consumerAdmin_ = channel_.default_consumer_admin();

	trueFilter_ = filterFactory_.create_filter("EXTENDED_TCL");
	ConstraintExp[] _constraintExp = new ConstraintExp[1];
	EventType[] _eventType = new EventType[1];
	_eventType[0] = new EventType("*", "*");
	String _expression = "TRUE";
	_constraintExp[0] = new ConstraintExp(_eventType, _expression);
	ConstraintInfo[] _info = trueFilter_.add_constraints(_constraintExp);

	poa_.the_POAManager().activate();
    }

    public void testCompareAny() throws Exception {
	Any _a1 = orb_.create_any(), 
	    _a2 = orb_.create_any();

	_a1.insert_long(10);
	_a2.insert_long(10);

	assertEquals(_a1, _a2);
    }

    public void measureLatencyPushPushMultipleFilter(int numberOfConsumers, int events) throws Exception {
	System.out.println("\n");
	
	long _start = System.currentTimeMillis();
	final Latch _done = new Latch();
	PerformanceLogger _perfLogger = new PerformanceLogger();
	StructuredGenerator _generator = new StructuredGenerator(orb_);

	CyclicBarrier _barrier = new CyclicBarrier(numberOfConsumers);
	_barrier.setBarrierCommand(new Runnable() {
		public void run() {
		    _done.release();
		}});

	StructuredPushReceiver[] _receivers = new StructuredPushReceiver[numberOfConsumers];
	StructuredPushSender _sender = new StructuredPushSender(_perfLogger, _generator, events, 250L);
	Thread[] _receiverThread = new Thread[numberOfConsumers];

	for (int x=0; x<_receivers.length; x++) {
	    _receivers[x] = new StructuredPushReceiver(_perfLogger, 1);
	    _receivers[x].connect(orb_, poa_, channel_);
	    _receivers[x].setTimeOut(10000);
	    _receivers[x].setBarrier(_barrier);

	    Filter _filter = filterFactory_.create_filter("EXTENDED_TCL");
	    ConstraintExp[] _constraintExp = new ConstraintExp[1000];
	    
	    EventType[] _eventType;
	    for (int y=0; y<_constraintExp.length - 1; ++y) {
		_eventType = new EventType[1];
		_eventType[0] = new EventType("SOME", "THING" + y);
		_constraintExp[y] = new ConstraintExp(_eventType, "$.value == 'does not exist'");
	    }

	    _eventType = new EventType[1];
	    _eventType[0] = new EventType("TIMING", "TIMING");

	    String filterString = "$.remainder_of_body.id == " + (_receivers.length - x);
	    
	    _constraintExp[_constraintExp.length-1] = new ConstraintExp(_eventType, filterString);

	    ConstraintInfo[] _info = _filter.add_constraints(_constraintExp);

	    _receivers[x].setFilter(_filter);
	}

	for (int x=0; x<_receiverThread.length; x++) {
	    _receiverThread[x] = new Thread(_receivers[x]);
	}
	
	logger_.debug("Receiver started");

	_sender.connect(orb_, poa_, channel_);	
	
	for (int x=0; x<_receiverThread.length; x++) {
	    _receiverThread[x].start();
	}
	_sender.run();

	// wait until done
	_done.acquire();

	for (int x=0; x<_receivers.length; x++) {
	    assertTrue(_receivers[x].isEventHandled());
	    _receivers[x].shutdown();
	}

	_sender.shutdown();
	System.out.println(_perfLogger);
	System.out.println("Average per Event: " + ((System.currentTimeMillis() - _start)/events ) );
    }

    public void measureLatencyPushPushFilter(int numberOfConsumers, int events) throws Exception {	
	System.out.println("\n");
	System.out.println("measure Latency with 1 Filter/ProxySupplier");
	System.out.println("----------");
	System.out.println("Consumers: " + numberOfConsumers);

	long _start = System.currentTimeMillis();

	final Latch _done = new Latch();
	PerformanceLogger _perfLogger = new PerformanceLogger();
	AnyGenerator _generator = new AnyGenerator(orb_);

	CyclicBarrier _barrier = new CyclicBarrier(numberOfConsumers);
	_barrier.setBarrierCommand(new Runnable() {
		public void run() {
		    _done.release();
		}});

	AnyPushReceiver[] _receivers = new AnyPushReceiver[numberOfConsumers];
	AnyPushSender _sender = new AnyPushSender(_perfLogger, _generator, events, 250L);
	Thread[] _receiverThread = new Thread[numberOfConsumers];

	for (int x=0; x<_receivers.length; x++) {
	    _receivers[x] = new AnyPushReceiver(_perfLogger, 1);
	    _receivers[x].connect(orb_, poa_, channel_);
	    _receivers[x].setTimeOut(10000);
	    _receivers[x].setBarrier(_barrier);

	    Filter _filter = filterFactory_.create_filter("EXTENDED_TCL");
	    ConstraintExp[] _constraintExp = new ConstraintExp[1];
	    EventType[] _eventType = new EventType[1];
	    _eventType[0] = new EventType("*", "*");

	    String filterString = "$.id == " + (_receivers.length - x);
	    //System.out.println(filterString);
	    
	    _constraintExp[0] = new ConstraintExp(_eventType, filterString);
	    ConstraintInfo[] _info = _filter.add_constraints(_constraintExp);

	    _receivers[x].setFilter(_filter);
	}

	for (int x=0; x<_receiverThread.length; x++) {
	    _receiverThread[x] = new Thread(_receivers[x]);
	}
	
	logger_.debug("Receiver started");

	_sender.connect(orb_, poa_, channel_);	
	
	for (int x=0; x<_receiverThread.length; x++) {
	    _receiverThread[x].start();
	}
	_sender.run();

	// wait until done
	_done.acquire();

	for (int x=0; x<_receivers.length; x++) {
	    assertTrue(_receivers[x].isEventHandled());
	    _receivers[x].shutdown();
	}
	_sender.shutdown();
	System.out.println(_perfLogger);
	System.out.println("Average per Event: " + ((System.currentTimeMillis() - _start)/events ) );		   
    }

    public void measureLatencyPushPush(int numberOfConsumers, int events) throws Exception {	
	System.out.println("\n");
	System.out.println("measure Latency");
	System.out.println("----------");
	System.out.println("Consumers: " + numberOfConsumers);

	long _start = System.currentTimeMillis();

	final Latch _done = new Latch();
	PerformanceLogger _perfLogger = new PerformanceLogger();
	AnyGenerator _generator = new AnyGenerator(orb_);

	CyclicBarrier _barrier = new CyclicBarrier(numberOfConsumers);
	_barrier.setBarrierCommand(new Runnable() {
		public void run() {
		    _done.release();
		}});

	AnyPushReceiver[] _receivers = new AnyPushReceiver[numberOfConsumers];
	AnyPushSender _sender = new AnyPushSender(_perfLogger, _generator, events, 250L);
	Thread[] _receiverThread = new Thread[numberOfConsumers];

	for (int x=0; x<_receivers.length; x++) {
	    _receivers[x] = new AnyPushReceiver(_perfLogger, events);
	    _receivers[x].connect(orb_, poa_, channel_);
	    _receivers[x].setTimeOut(0);
	    _receivers[x].setBarrier(_barrier);
	}

	for (int x=0; x<_receiverThread.length; x++) {
	    _receiverThread[x] = new Thread(_receivers[x]);
	}
	
	logger_.debug("Receiver started");

	_sender.connect(orb_, poa_, channel_);	
	
	for (int x=0; x<_receiverThread.length; x++) {
	    _receiverThread[x].start();
	}
	_sender.run();

	// wait until done
	_done.acquire();

	for (int x=0; x<_receivers.length; x++) {
	    assertTrue(_receivers[x].isEventHandled());
	    _receivers[x].shutdown();
	}
	_sender.shutdown();
	System.out.println(_perfLogger);
	System.out.println("Average per Event: " + ((System.currentTimeMillis() - _start)/events ));
    }

    public void testLatencyPushPushMultipleFilter() throws Exception {
	measureLatencyPushPushMultipleFilter(1, 100);
 	measureLatencyPushPushMultipleFilter(10, 100);
	// 	measureLatencyPushPushMultipleFilter(50, 100);
    }

    public void testLatencyPushPush() throws Exception {
	measureLatencyPushPush(1, 100);
 	measureLatencyPushPush(10, 100);
	measureLatencyPushPush(50, 100);
    }
    
    public void testLatencyPushPushWithFilter() throws Exception {
	measureLatencyPushPushFilter(1, 100);
	measureLatencyPushPushFilter(10, 100);
 	measureLatencyPushPushFilter(50, 100);
    }

    public void testAccessTimes() throws Exception {
	Vector _v = new Vector();
	Hashtable _h = new Hashtable();

	for (int x=0; x<100000; x++) {
	    Integer _i = new Integer(x);
	    String _s = _i.toString();
	    _v.add(_s);
	    _h.put(_i, _s);
	}

	long _start = System.currentTimeMillis();
	Iterator _i = _h.values().iterator();
	while (_i.hasNext()) {
	    doIt(_i.next());
	}
	System.out.println("Iterate Hashtable: " + (System.currentTimeMillis() - _start));

	_start = System.currentTimeMillis();
	Iterator _j = _v.iterator();
	while (_i.hasNext()) {
	    doIt(_i.next());
	}
	System.out.println("Iterate Vector: " + (System.currentTimeMillis() - _start));	
    }

    private void doIt(Object s) {
	s.getClass();
    }

    /**
     * @return a <code>TestSuite</code>
     */
    public static TestSuite suite(){
	TestSuite _suite = new TestSuite();
	//		_suite = new TestSuite(PerformanceTest.class);
	_suite.addTest(new PerformanceTest("testLatencyPushPushMultipleFilter"));
	return _suite;
    }

    /** 
     * Entry point 
     */ 
    public static void main(String[] args) {
	BasicConfigurator.configure();
	Logger.getRootLogger().setLevel(Level.OFF);
	//	Logger.getRootLogger().getLogger("TIME").setLevel(Level.DEBUG);

	junit.textui.TestRunner.run(suite());
    }
}// PerformanceTest

class StructuredGenerator {
    AnyGenerator anyGenerator_;

    StructuredGenerator(ORB orb) {
	anyGenerator_ = new AnyGenerator(orb);
    }

    public StructuredEvent getNextEvent() {
	FixedEventHeader _fixedHeader = new FixedEventHeader();

	_fixedHeader.event_name = "TIMING";
	_fixedHeader.event_type = new EventType("TIMING", "TIMING");
	EventHeader _header = new EventHeader(_fixedHeader, new Property[0]);
	StructuredEvent _event = new StructuredEvent(_header, new Property[0], anyGenerator_.getNextEvent());
	
	return _event;
    }
}

class AnyGenerator implements TestEventGenerator {
    ORB orb_;
    int counter_ = 0;

    AnyGenerator(ORB orb) {
	orb_ = orb;
    }

    public Any getNextEvent() {
	TimingTest _t = new TimingTest();
	_t.id = counter_++;
	_t.currentTime = (int)System.currentTimeMillis();

	Any _event = orb_.create_any();
	TimingTestHelper.insert(_event, _t);

	return _event;
    }
}

class PerformanceLogger implements PerformanceListener {

    class LogEntry {
	long receiveTime;
	long sendTime;
	long getTotalTime() {
	    return receiveTime - sendTime;
	}
    }

    Map allEntries_ = new Hashtable();

    public String toString() {
	StringBuffer _b = new StringBuffer();
	Iterator _i = allEntries_.values().iterator();
	_b.append("Number of Events: " + allEntries_.size());
	_b.append("\n");
	
	int size = allEntries_.size();
	long minimum = Long.MAX_VALUE;
	long maximum = 0;
	long average = 0;

	while(_i.hasNext()) {
	    LogEntry _e = (LogEntry)_i.next();
	    long _v = _e.getTotalTime();
	    if (_v > maximum) {
		maximum = _v;
	    }
	    if (_v < minimum) {
		minimum = _v;
	    }
	    average += _v;
	}
	_b.append("Min: " + minimum);
	_b.append("\n");
	_b.append("Max: " + maximum);
	_b.append("\n");
	_b.append("Avg: " + (average / size));
	return _b.toString();
    } 

    public void eventSent(Any event, long currentTime, long took) {
// 	TimingTest _t = TimingTestHelper.extract(event);
// 	synchronized(allEntries_) {
// 	    LogEntry _entry = (LogEntry)allEntries_.get(event);
// 	    if (_entry == null) {
// 		_entry = new LogEntry();
// 	    }
// 	    _entry.sendTime = currentTime;
// 	    allEntries_.put(event, _entry);
// 	}
    }

    public void eventReceived(StructuredEvent event, long currentTime) {
	eventReceived(event.remainder_of_body, currentTime);
    }

    public void eventReceived(Any event, long currentTime) {
	TimingTest _t = TimingTestHelper.extract(event);
	Integer _key = new Integer(_t.id);	

	synchronized(allEntries_) {
	    LogEntry _entry = (LogEntry)allEntries_.get(_key);
	    if (_entry == null) {
		_entry = new LogEntry();
	    }
	    _entry.sendTime = _t.currentTime;
	    _entry.receiveTime = (int)currentTime;
	    allEntries_.put(_key, _entry);
	}
    }

    public void eventFailed(Any event, Exception e) {}
}
