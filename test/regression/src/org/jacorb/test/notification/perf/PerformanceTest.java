package org.jacorb.test.notification.perf;

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

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.omg.CORBA.Any;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.ORB;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.Property;
import org.omg.CosNotification.StructuredEvent;
import org.omg.CosNotifyChannelAdmin.ConsumerAdmin;
import org.omg.CosNotifyChannelAdmin.EventChannel;
import org.omg.CosNotifyChannelAdmin.EventChannelFactory;
import org.omg.CosNotifyChannelAdmin.EventChannelFactoryHelper;
import org.omg.CosNotifyChannelAdmin.SupplierAdmin;
import org.omg.CosNotifyFilter.ConstraintExp;
import org.omg.CosNotifyFilter.ConstraintInfo;
import org.omg.CosNotifyFilter.Filter;
import org.omg.CosNotifyFilter.FilterFactory;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import org.jacorb.test.notification.EchoServer;
import org.jacorb.test.notification.EchoServerHelper;
import org.jacorb.test.notification.AnyGenerator;
import org.jacorb.test.notification.AnyPushReceiver;
import org.jacorb.test.notification.AnyPushSender;
import org.jacorb.test.notification.NotificationTestCase;
import org.jacorb.test.notification.NotificationTestCaseSetup;
import org.jacorb.test.notification.PerformanceListener;
import org.jacorb.test.notification.StructuredGenerator;
import org.jacorb.test.notification.StructuredPushReceiver;
import org.jacorb.test.notification.StructuredPushSender;
import org.jacorb.test.notification.TestEventGenerator;
import org.jacorb.test.notification.TestUtils;

import EDU.oswego.cs.dl.util.concurrent.CyclicBarrier;
import EDU.oswego.cs.dl.util.concurrent.Latch;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.apache.log.Priority;

/**
 *
 * Created: Mon Jan 06 14:41:48 2003
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class PerformanceTest extends NotificationTestCase {

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

    TestUtils testUtils_;

    Logger logger_ =
        Hierarchy.getDefaultHierarchy().getLoggerFor(getClass().getName());

    static long sInterval = 250L;

    static long sTimeout = 2 * sInterval;

    /**
     * Creates a new <code>PerformanceTest</code> instance.
     *
     * @param name test name
     */
    public PerformanceTest (String name, NotificationTestCaseSetup setup) {
        super(name, setup);
    }

    /**
     * setup EventChannelFactory, FilterFactory and Any with Testdata
     */
    public void setUp() throws Exception {
        orb_ = ORB.init(new String[0], null);
        poa_ = POAHelper.narrow(orb_.resolve_initial_references("RootPOA"));

        testUtils_ = new TestUtils(orb_);

        factory_ = EventChannelFactoryHelper.narrow(orb_.resolve_initial_references("NotificationService"));

        // prepare test data
        testPerson_ = testUtils_.getTestPersonAny();

        // setup a channel
        channelId_ = new IntHolder();

        channel_ = factory_.create_channel(new Property[0],
                                           new Property[0], channelId_);

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

        new Thread(
                   new Runnable() {
                       public void run() {
                           orb_.run();
                       }
                   }
                   ).start();
    }

    public void tearDown() {
        super.tearDown();

        try {
            channel_.destroy();
        } catch (Exception e) {
            // ignore
        }

        orb_.shutdown(true);
    }

    public void testCompareAny() throws Exception {
        Any _a1 = orb_.create_any(),
            _a2 = orb_.create_any();

        _a1.insert_long(10);
        _a2.insert_long(10);

        assertEquals(_a1, _a2);
    }

    public void testMeasureFilterLatency() throws Exception {
        Any _any = orb_.create_any();
        _any.insert_long(10);

        int _runs = 100;

        measureFilterLatency("$ == 10", _any, _runs);

        _any = testUtils_.getTestPersonAny();

        measureFilterLatency("$.phone_numbers[0] == '12345678'", _any, _runs);

        measureFilterLatency("exist $.phone_numbers[0] and $.phone_numbers[0] == '12345678'", _any, _runs);

        measureFilterLatency("exist $.phone_numbers[0] and exist $.phone_numbers[0]  and $.phone_numbers[0] == '12345678'", _any, _runs);

        measureFilterLatency("exist $.phone_numbers[0] and exist $.phone_numbers[0] and exist $.phone_numbers[0] and $.phone_numbers[0] == '12345678'", _any, _runs);

        measureFilterLatency("exist $.phone_numbers[0] and exist $.phone_numbers[0] and exist $.phone_numbers[0] and exist $.phone_numbers[0] and $.phone_numbers[0] == '12345678'", _any, _runs);

        measureFilterLatency("exist $.phone_numbers[0] and exist $.phone_numbers[0] and exist $.phone_numbers[0] and exist $.phone_numbers[0] and exist $.phone_numbers[0] and $.phone_numbers[0] == '12345678'", _any, _runs);


        StructuredEvent _event = testUtils_.getStructuredEvent();

        measureFilterLatency("$event_name == 'ALARM'", _event, _runs);

        measureFilterLatency("$type_name == 'TESTING'", _event, _runs);

        measureFilterLatency("$domain_name == 'TESTING'", _event, _runs);

    }


    public void measureFilterLatency(String filterString,
                                     StructuredEvent event,
                                     int runs) throws Exception {

        Filter _filter = filterFactory_.create_filter("EXTENDED_TCL");

        ConstraintExp[] _constraintExp = new ConstraintExp[1];
        EventType[] _eventType = new EventType[1];
        _eventType[0] = new EventType("*", "*");
        String _expression = filterString;
        _constraintExp[0] = new ConstraintExp(_eventType, _expression);
        ConstraintInfo[] _info = _filter.add_constraints(_constraintExp);

        long _start = System.currentTimeMillis();

        for (int x=0;x<runs; ++x) {
            boolean _r = _filter.match_structured(event);
            assertTrue(_r);
        }

        long _total = System.currentTimeMillis() - _start;

        System.out.println(runs
                           + " Filterings of '"
                           + filterString
                           + "' took "
                           + _total
                           + " in average: "
                           + (_total / runs) );

        //      _filter.destroy();
    }



    public void measureFilterLatency(String filterString, Any event, int runs) throws Exception {
        Filter _filter = filterFactory_.create_filter("EXTENDED_TCL");

        ConstraintExp[] _constraintExp = new ConstraintExp[1];
        EventType[] _eventType = new EventType[1];
        _eventType[0] = new EventType("*", "*");
        String _expression = filterString;
        _constraintExp[0] = new ConstraintExp(_eventType, _expression);
        ConstraintInfo[] _info = _filter.add_constraints(_constraintExp);

        long _start = System.currentTimeMillis();

        for (int x=0;x<runs; ++x) {
            boolean _r = _filter.match(event);
            assertTrue(_r);
        }

        long _total = System.currentTimeMillis() - _start;

        System.out.println(runs
                           + " Filterings of '"
                           + filterString
                           + "' took "
                           + _total
                           + " in average: "
                           + (_total / runs) );

        //      _filter.destroy();
    }

    public void measureLatencyPushPushMultipleFilter(int numberOfConsumers,
                                                     int events) throws Exception {
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

        StructuredPushSender _sender =
            new StructuredPushSender((TestCase)this, (PerformanceListener)_perfLogger, _generator, events, sInterval);

        Thread[] _receiverThread = new Thread[numberOfConsumers];

        for (int x=0; x<_receivers.length; x++) {
            _receivers[x] = new StructuredPushReceiver(this, _perfLogger, 1);
            _receivers[x].connect(getSetup(), channel_,false);
            _receivers[x].setTimeOut(events * sTimeout);
            _receivers[x].setBarrier(_barrier);

            Filter _filter = filterFactory_.create_filter("EXTENDED_TCL");
            ConstraintExp[] _constraintExp = new ConstraintExp[50];

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

        _sender.connect(getSetup(), channel_, false);

        for (int x=0; x<_receiverThread.length; x++) {
            _receiverThread[x].start();
        }
        _sender.run();

        // wait until done
        _done.acquire();

        for (int x=0; x<_receivers.length; x++) {
            assertTrue(_receivers[x].isEventHandled());
            //      _receivers[x].shutdown();
        }

        //      _sender.shutdown();
        System.out.println(_perfLogger);
        System.out.println("Average per Event: " + ((System.currentTimeMillis() - _start)/events ) );
    }

    public void measureLatencyPushPushFilter(int numberOfSuppliers,
                                             int numberOfConsumers,
                                             int events) throws Exception {

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

        AnyPushSender _sender =
            new AnyPushSender(this,_perfLogger, _generator, events, sInterval);

        Thread[] _receiverThread = new Thread[numberOfConsumers];

        for (int x=0; x<_receivers.length; x++) {
            _receivers[x] = new AnyPushReceiver(this,_perfLogger, 1);
            _receivers[x].connect(getSetup(), channel_,false);
            _receivers[x].setTimeOut(events * sTimeout);
            _receivers[x].setBarrier(_barrier);

            Filter _filter = filterFactory_.create_filter("EXTENDED_TCL");
            ConstraintExp[] _constraintExp = new ConstraintExp[1];
            EventType[] _eventType = new EventType[1];
            _eventType[0] = new EventType("*", "*");

            String filterString = "$.id == " + (_receivers.length - x);

            _constraintExp[0] = new ConstraintExp(_eventType, filterString);
            ConstraintInfo[] _info = _filter.add_constraints(_constraintExp);

            _receivers[x].setFilter(_filter);
        }

        for (int x=0; x<_receiverThread.length; x++) {
            _receiverThread[x] = new Thread(_receivers[x]);
        }

        logger_.debug("Receiver started");

        _sender.connect(getSetup(), channel_,false);

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

        System.out.println("Average per Event: " +
                           ((System.currentTimeMillis() - _start)/events ) );
    }

    public void measureLatencyPushPush(int numberOfSuppliers,
                                       int numberOfConsumers,
                                       int numberOfevents,
                                       boolean checkAssertions,
                                       boolean latexTable) throws Exception {

        StringBuffer _b = new StringBuffer();

        if (!latexTable) {
            _b.append("\n");
            _b.append("measure Latency\n");
            _b.append("----------\n");
            _b.append("Suppliers: " + numberOfSuppliers + "\n");
            _b.append("Consumers: " + numberOfConsumers + "\n");
        }

        long _start = System.currentTimeMillis();

        final Latch _done = new Latch();
        PerformanceLogger _perfLogger = new PerformanceLogger();
        AnyGenerator _generator = new AnyGenerator(orb_);

        CyclicBarrier _barrier = new CyclicBarrier(numberOfConsumers);

        _barrier.setBarrierCommand(new Runnable() {
                public void run() {
                    logger_.debug("Barrier reached");
                    _done.release();
                }});

        AnyPushReceiver[] _receivers = new AnyPushReceiver[numberOfConsumers];
        AnyPushSender[] _sender = new AnyPushSender[numberOfSuppliers];

        for (int x=0; x<numberOfSuppliers; ++x) {
            _sender[x] = new AnyPushSender(this,_perfLogger, _generator, numberOfevents, sInterval);
        }

        Thread[] _receiverThreads = new Thread[numberOfConsumers];

        for (int x=0; x<_receivers.length; x++) {
            _receivers[x] = new AnyPushReceiver(this,_perfLogger, numberOfevents * numberOfSuppliers);
            _receivers[x].connect(getSetup(), channel_,false);
            _receivers[x].setTimeOut(numberOfSuppliers * numberOfevents * sTimeout);
            _receivers[x].setBarrier(_barrier);
        }

        for (int x=0; x<_receiverThreads.length; x++) {
            _receiverThreads[x] = new Thread(_receivers[x]);
        }

        Thread[] _senderThreads = new Thread[numberOfSuppliers];
        for (int x=0; x<numberOfSuppliers; ++x) {
            _senderThreads[x] = new Thread(_sender[x]);
            _sender[x].connect(getSetup(), channel_, false);
        }

        logger_.debug("Receiver started");

        for (int x=0; x<_receiverThreads.length; x++) {
            _receiverThreads[x].start();
        }

        for (int x=0; x<numberOfSuppliers; ++x) {
            _senderThreads[x].start();
        }

        // wait until done
        _done.acquire();

        for (int x=0; x<_receivers.length; x++) {
            if (checkAssertions) {
                assertTrue(_receivers[x].isEventHandled());
            }
            _receivers[x].shutdown();
        }

        for (int x=0; x<numberOfSuppliers; ++x) {
            _sender[x].shutdown();
        }

        long _averagePerEvent = (System.currentTimeMillis() - _start) / numberOfevents;

        if (latexTable) {
            _b.append(numberOfSuppliers);
            _b.append(" & ");
            _b.append(numberOfConsumers);
            _b.append(" & ");
            _b.append(numberOfevents);
            _b.append(" & ");
            _b.append(_averagePerEvent);
            _b.append(" & ");
            _b.append(_perfLogger.getMinimum());
            _b.append(" & ");
            _b.append(_perfLogger.getMaximum());
            _b.append(" & ");
            _b.append(_perfLogger.getAverage());
            _b.append(" \\\\ ");
        } else {
            _b.append(_perfLogger.toString());
            _b.append("\n");
            _b.append("Average per Event: " + _averagePerEvent);
        }
        System.out.println(_b.toString());
    }


    public void testMeasureLatencyEcho() throws Exception {

        EchoServer _echoServer =
            EchoServerHelper.narrow(orb_.resolve_initial_references("EchoServer"));

        final Any _data = testUtils_.getSizedTestData(100);

        TestEventGenerator _gen = new TestEventGenerator() {
                public Any getNextEvent() {
                    return _data;
                }
            };

        int[] sizes = new int[] {1, 32, 64, 128, 256, 512, 1024};

        for (int x=0; x<sizes.length; ++x) {

            measureLatencyEcho(_gen, _echoServer, sizes[x]);

        }

    }

    public void testBurstEcho() throws Exception {
        EchoServer server =
            EchoServerHelper.narrow(orb_.resolve_initial_references("EchoServer"));

        AnyGenerator generator = new AnyGenerator(orb_);

        measureLatencyEcho(generator, server, 10);
        measureLatencyEcho(generator, server, 100);
        measureLatencyEcho(generator, server, 1000);
        measureLatencyEcho(generator, server, 5000);
        measureLatencyEcho(generator, server, 10000);
        measureLatencyEcho(generator, server, 20000);
    }

    public void measureLatencyEcho(TestEventGenerator gen,
                                   EchoServer server,
                                   int runs) throws Exception {

        long _start = System.currentTimeMillis();
        for (int x=0; x<runs;  ++x) {
            Any _data = gen.getNextEvent();
            server.acceptAny(_data);
        }
        long _stop = System.currentTimeMillis();
        long _total = _stop - _start;

        System.out.println("Total: " + _total);
        System.out.println("Avg: " +  (int)((double)runs / _total * 1000));

    }

    public void testMeasureBurstSend() throws Exception {

        int[] tests = {10, 10, 100, 1000}; //, 5000};

        for (int x=0; x<tests.length; ++x) {

            measureBurstSend(tests[x],4);

            Thread.sleep(3000);
        }
    }

    public void testMeasureThroughputWithSize() throws Exception {

        measureLatencyPushPush(1, 1, 50, false, false);

        Thread.sleep(2000);

        System.out.println();


        measureThroughputWithSize(1, 100);
        measureThroughputWithSize(32, 100);
        measureThroughputWithSize(64, 100);
        measureThroughputWithSize(128, 100);
        measureThroughputWithSize(256, 100);
        measureThroughputWithSize(512, 100);
        measureThroughputWithSize(1024, 100);

    }

    public void measureThroughputWithSize(int size, int events) throws Exception {

        SendCounter _counter = new SendCounter(events);
        final Any _data = testUtils_.getSizedTestData(size);
        AnyPushReceiver _receiver = new AnyPushReceiver(this);
        _receiver.setExpected(events);
        _receiver.setPerformanceListener(_counter);
        _receiver.connect(getSetup(), channel_, false);
        _receiver.setTimeOut(80 * events);

        TestEventGenerator _generator = new TestEventGenerator() {
                public Any getNextEvent() {
                    return _data;
                }
            };

        AnyPushSender _sender = new AnyPushSender(this, _counter, _generator, events, 0);
        _sender.connect(getSetup(), channel_, false);

        Thread _t = new Thread(_sender);
        Thread _r = new Thread(_receiver);

        Thread.sleep(100);
        _r.start();
        _t.start();

        _r.join();

        String _result = _counter.toString();

        _receiver.shutdown();
        _sender.shutdown();

        System.out.println((32 * size) + " & " + _result);
        Thread.sleep(1000);
    }

    public void measureBurstSend(int events,int numberOfConsumers) throws Exception {

        System.out.println("Send Burst: " + events + " Events to " + numberOfConsumers + " Consumers");

        SendCounter _logger = new SendCounter(events);
        AnyGenerator _generator = new AnyGenerator(orb_);
        final Latch _done = new Latch();

        AnyPushReceiver[] _consumers = new AnyPushReceiver[numberOfConsumers];

        CyclicBarrier _barrier = new CyclicBarrier(numberOfConsumers);
        _barrier.setBarrierCommand(new Runnable() {
                public void run() {
                    _done.release();
                }
            });

        for (int x=0; x<_consumers.length; ++x) {
            _consumers[x] = new AnyPushReceiver(this);
            _consumers[x].setExpected(events);
            _consumers[x].setPerformanceListener(_logger);
            _consumers[x].connect(getSetup(), channel_, false);
            _consumers[x].setTimeOut(100 * events);
            _consumers[x].setBarrier(_barrier);
        }

        AnyPushSender _sender =
            new AnyPushSender(this, _logger, _generator, events, 0);

        _sender.connect(getSetup(), channel_, false);

        Thread _t = new Thread(_sender);

        Thread.sleep(100);

        for (int x=0; x<_consumers.length; ++x) {
            new Thread(_consumers[x]).start();
        }

        _t.start();

        _done.acquire();

        String _result = _logger.toString();

        _sender.shutdown();

        long sleepTime = 1000 + (long)Math.log(events) * 1000;

        Thread.sleep(sleepTime);

        for (int x=0; x< _consumers.length; ++x) {
            _consumers[x].shutdown();
        }

        System.out.println(_result);
    }

    public void testLatencyPushPushMultipleFilter() throws Exception {
        measureLatencyPushPushMultipleFilter(1, 1);
        //        measureLatencyPushPushMultipleFilter(1, 100);
//         measureLatencyPushPushMultipleFilter(10, 100);
//         measureLatencyPushPushMultipleFilter(50, 100);
    }



    public void testLatencyPushPush() throws Exception {
        boolean latex = false;

        measureLatencyPushPush(1, 1, 100, true, latex);
        measureLatencyPushPush(1, 10, 100, true, latex);
        measureLatencyPushPush(1, 50, 100, true, latex);

//         measureLatencyPushPush(5, 1, 100, true, latex);
//         measureLatencyPushPush(5, 10, 100, true, latex);
//         measureLatencyPushPush(5, 50, 100, true, latex);

//         measureLatencyPushPush(10, 1, 100, true, latex);
//         measureLatencyPushPush(10, 10, 100, true, latex);
//         measureLatencyPushPush(10, 50, 100, true, latex);
    }

    public void testLatencyPushPushWithFilter() throws Exception {
        measureLatencyPushPushFilter(1,1, 100);
        measureLatencyPushPushFilter(1,10, 100);
        measureLatencyPushPushFilter(1,50, 100);
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
    public static Test suite() throws Exception {
        TestSuite _suite = new TestSuite();

        NotificationTestCaseSetup setup =
            new NotificationTestCaseSetup(_suite);

        //        _suite.addTest(new PerformanceTest("testBurstEcho", setup));
                        _suite.addTest(new PerformanceTest("testMeasureBurstSend", setup));
        _suite.addTest(new PerformanceTest("testLatencyPushPush", setup));
//          _suite.addTest(new PerformanceTest("testMeasureFilterLatency", setup));
                //  _suite.addTest(new PerformanceTest("testLatencyPushPushWithFilter", setup));

        //_suite.addTest(new PerformanceTest("testLatencyPushPushMultipleFilter", setup));

        return setup;
    }

    /**
     * Entry point
     */
    public static void main(String[] args) throws Exception {
        junit.textui.TestRunner.run(suite());
    }
}// PerformanceTest

class SendCounter implements PerformanceListener {

    boolean active_ = false;
    long senderSum_ = 0;
    long recvStart_;
    long receiveTime_;
    int received_;
    int expected_;

    SendCounter(int expected) {
        expected_ = expected;
    }

    public String toString() {
        return received_
            + " & "
            + (int)((double)received_ / senderSum_ * 1000)
            + " & "
            + (int)((double)received_ / receiveTime_ * 1000)
            + " \\\\";
    }

    synchronized public void eventSent(Any event, long a, long b) {
        senderSum_ += b;
    }

    synchronized public void eventReceived(StructuredEvent event, long s) {
    }

    synchronized public void eventReceived(Any event, long a) {
        if (!active_) {
            recvStart_ = System.currentTimeMillis();
            active_ = true;
        }

        ++received_;

        receiveTime_ = System.currentTimeMillis() - recvStart_;

    }

    public void eventFailed(Any event, Exception e) {

    }
}
