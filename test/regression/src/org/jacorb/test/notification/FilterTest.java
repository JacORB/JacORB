package org.jacorb.test.notification;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;

import org.jacorb.notification.IContainer;
import org.jacorb.notification.TypedEventMessage;
import org.jacorb.notification.filter.FilterFactoryImpl;
import org.jacorb.notification.filter.DefaultFilterFactoryDelegate;
import org.jacorb.test.notification.common.NotificationTestCase;
import org.jacorb.test.notification.common.NotificationTestCaseSetup;
import org.jacorb.test.notification.common.NotificationTestUtils;
import org.omg.CORBA.Any;
import org.omg.CosNotification.EventType;
import org.omg.CosNotification.Property;
import org.omg.CosNotifyFilter.ConstraintExp;
import org.omg.CosNotifyFilter.ConstraintInfo;
import org.omg.CosNotifyFilter.Filter;
import org.omg.CosNotifyFilter.FilterFactory;
import org.omg.CosNotifyFilter.FilterFactoryHelper;
import org.picocontainer.MutablePicoContainer;

/**
 * @author Alphonse Bendt
 * @author John Farrell
 */

public class FilterTest extends NotificationTestCase
{
    static final Random random_ = new Random(System.currentTimeMillis());

    ////////////////////////////////////////

    FilterFactory factory_;

    Filter filter_;

    Any testPerson_;

    NotificationTestUtils testUtils_;

    FilterFactoryImpl factoryServant_;

    ////////////////////////////////////////

    public FilterTest(String name, NotificationTestCaseSetup setup)
    {
        super(name, setup);
    }

    ////////////////////////////////////////

    public void setUpTest() throws Exception
    {
        IContainer container = new IContainer()
        {
            public MutablePicoContainer getContainer()
            {
                return getPicoContainer();
            }
            
            public void destroy()
            {
                // no operation
            }
        };

        factoryServant_ = new FilterFactoryImpl(getORB(), getPOA(), getConfiguration(), new DefaultFilterFactoryDelegate(container, getConfiguration()));

        factoryServant_.activate();

        factory_ = FilterFactoryHelper.narrow(factoryServant_.activate());

        testUtils_ = new NotificationTestUtils(getORB());

        testPerson_ = testUtils_.getTestPersonAny();

        filter_ = factory_.create_filter("EXTENDED_TCL");
    }

    public void tearDownTest() throws Exception
    {
        factoryServant_.dispose();
    }

    public void testFalseMatch() throws Exception
    {
        ConstraintExp[] _constraintExp = new ConstraintExp[1];
        EventType[] _eventType = new EventType[1];
        _eventType[0] = new EventType("*", "*");
        String _expression = "FALSE";
        _constraintExp[0] = new ConstraintExp(_eventType, _expression);
        filter_.add_constraints(_constraintExp);

        assertFalse(filter_.match(testPerson_));
    }

    /**
     * create remote filter object and invoke match operation on it
     */
    public void testMatch() throws Exception
    {
        ConstraintExp[] _constraintExp = new ConstraintExp[1];
        EventType[] _eventType = new EventType[1];
        _eventType[0] = new EventType("*", "*");

        _constraintExp[0] = new ConstraintExp(_eventType, "$.first_name == 'firstname'");
        filter_.add_constraints(_constraintExp);

        // this should match
        assertTrue(filter_.match(testPerson_));
    }

    public void testAccessNonExistingMember() throws Exception
    {
        EventType[] _eventType = new EventType[] { new EventType("*", "*") };

        ConstraintExp[] _constraintExp = new ConstraintExp[] {
                new ConstraintExp(_eventType, "$not_exist == 3"),
                new ConstraintExp(_eventType, "TRUE"), };

        filter_.add_constraints(_constraintExp);

        assertTrue(filter_.match(testPerson_));
    }

    public void testMatchEmptyFilter() throws Exception
    {
        assertTrue(!filter_.match(testPerson_));
    }

    public void testMatch_EventTypes_IsEmpty() throws Exception
    {
        ConstraintExp[] _constraintExp = new ConstraintExp[1];
        EventType[] _eventType = new EventType[0];

        _constraintExp[0] = new ConstraintExp(_eventType, "$.first_name == 'firstname'");
        filter_.add_constraints(_constraintExp);

        // this should match
        assertTrue(filter_.match(testPerson_));
    }

    public void testMatch_EventType_IsEmptyString() throws Exception
    {
        ConstraintExp[] _constraintExp = new ConstraintExp[1];
        EventType[] _eventType = new EventType[] { new EventType("", "") };

        _constraintExp[0] = new ConstraintExp(_eventType, "$.first_name == 'firstname'");
        filter_.add_constraints(_constraintExp);

        // this should match
        assertTrue(filter_.match(testPerson_));
    }

    public void testMatch_FilterString_IsEmpty() throws Exception
    {
        ConstraintExp[] _constraintExp = new ConstraintExp[1];
        EventType[] _eventType = new EventType[] { new EventType("*", "*") };

        _constraintExp[0] = new ConstraintExp(_eventType, "");
        filter_.add_constraints(_constraintExp);

        // this should match
        assertTrue(filter_.match(testPerson_));
    }

    public void testMatchModify() throws Exception
    {
        ConstraintExp[] _constraintExp = new ConstraintExp[1];
        EventType[] _eventType = new EventType[1];
        _eventType[0] = new EventType("*", "*");
        _constraintExp[0] = new ConstraintExp(_eventType, "$.first_name == 'something'");
        ConstraintInfo[] _info = filter_.add_constraints(_constraintExp);

        // oops wrong
        assertTrue(!filter_.match(testPerson_));

        // modify the filter
        _info[0].constraint_expression.constraint_expr = "$.first_name == 'firstname'";
        filter_.modify_constraints(new int[0], _info);

        // this one should match
        assertTrue(filter_.match(testPerson_));
    }

    public void testConstraintGrammar() throws Exception
    {
        assertEquals("EXTENDED_TCL", filter_.constraint_grammar());
    }

    public void testAddConstraints() throws Exception
    {
        ConstraintExp[] _constraintExp = new ConstraintExp[1];

        EventType[] _eventType = new EventType[1];
        _eventType[0] = new EventType("domain", "name");
        String _expression = "1 + 1";
        _constraintExp[0] = new ConstraintExp(_eventType, _expression);

        ConstraintInfo[] _info = filter_.add_constraints(_constraintExp);

        assertTrue(_info.length == 1);
        assertTrue(_info[0].constraint_expression.event_types.length == 1);
        assertEquals(_expression, _info[0].constraint_expression.constraint_expr);
        assertEquals(_eventType[0].domain_name,
                _info[0].constraint_expression.event_types[0].domain_name);
        assertEquals(_eventType[0].type_name,
                _info[0].constraint_expression.event_types[0].type_name);
    }

    public void testDeleteConstraints() throws Exception
    {
        ConstraintExp[] _constraintExp = new ConstraintExp[2];

        EventType[] _eventType = new EventType[1];
        _eventType[0] = new EventType("domain", "name");
        String _expression = "1 + 1";
        String _expression2 = "2 + 2";
        _constraintExp[0] = new ConstraintExp(_eventType, _expression);

        _eventType[0] = new EventType("domain2", "name");
        _constraintExp[1] = new ConstraintExp(_eventType, _expression2);

        ConstraintInfo[] _info = filter_.add_constraints(_constraintExp);

        assertTrue(_info.length == 2);
        assertTrue(_info[0].constraint_expression.event_types.length == 1);

        assertTrue(_info[1].constraint_expression.event_types.length == 1);

        int[] _delete = { _info[0].constraint_id };

        filter_.modify_constraints(_delete, new ConstraintInfo[0]);

        ConstraintInfo[] _info2 = filter_.get_all_constraints();
        assertTrue(_info2.length == 1);
        assertEquals(_info[1].constraint_id, _info2[0].constraint_id);

        assertEquals(_info[1].constraint_expression.constraint_expr,
                _info2[0].constraint_expression.constraint_expr);
    }

    /**
     * multithreaded test. Some Writers modify the Constraints of a Filter. Some Readers constantly
     * access the Filter. They should always get consistent data.  
     */
    public void testModifyConcurrent() throws Exception
    {
        FilterRead _fr1 = new FilterRead(this, filter_, 100);
        FilterRead _fr2 = new FilterRead(this, filter_, 100);
        FilterRead _fr3 = new FilterRead(this, filter_, 100);
        FilterRead _fr4 = new FilterRead(this, filter_, 100);

        FilterModify _mod1 = new FilterModify(this, filter_, "true", 50);
        FilterModify _mod2 = new FilterModify(this, filter_, "false", 50);

        _fr1.start();
        _fr2.start();
        _fr3.start();
        _fr4.start();

        _mod1.start();
        _mod2.start();

        _fr1.join();
        _fr2.join();
        _fr3.join();
        _fr4.join();

        _mod1.join();
        _mod2.join();
    }

    public void testMatchTyped() throws Exception
    {
        Property[] _props = new Property[] { new Property("operation", toAny("operationName")),
                new Property("value1", toAny(100)), new Property("value2", toAny(200)) };

        ConstraintExp[] _constraintExp = new ConstraintExp[1];

        EventType[] _eventType = new EventType[1];
        _eventType[0] = new EventType("", "%TYPED");

        String _expression = "$value1 > 50 and $value2 > 50";

        _constraintExp[0] = new ConstraintExp(_eventType, _expression);

        filter_.add_constraints(_constraintExp);

        assertTrue(filter_.match_typed(_props));
    }

    public void testFilterTypedMessageEvent() throws Exception
    {
        TypedEventMessage _mesg = new TypedEventMessage();

        String _domainName = "IDL:org.jacorb/org/jacorb/test/filter/Bla:1.0";
        String _operationName = "blaOperation";

        _mesg.setTypedEvent(_domainName, _operationName, new Property[] {
                new Property("param1", toAny("value1")), new Property("param2", toAny(100)) });

        assertFalse(_mesg.match(filter_));

        ConstraintExp[] _constraintExp = new ConstraintExp[1];

        EventType[] _eventType = new EventType[1];
        _eventType[0] = new EventType("", "%TYPED");

        String _expression = "$event_type.domain_name == '" + _domainName
                + "' and $event_type.type_name == '" + _operationName + "' and $param2 > 50";

        _constraintExp[0] = new ConstraintExp(_eventType, _expression);

        filter_.add_constraints(_constraintExp);

        assertTrue(_mesg.match(filter_));
    }

    public static Test suite() throws Exception
    {
        return NotificationTestCase.suite(FilterTest.class);
    }
}

//////////////////////////////////////////////////

class FilterRead extends Thread
{
    Filter filter_;

    int iterations_;

    boolean lengthOk_ = true;

    boolean countOk_ = true;

    TestCase testCase_;

    static int sCounter = 0;

    FilterRead()
    {
        super();
        setDaemon(true);
    }

    FilterRead(TestCase testCase, Filter filter, int iterations)
    {
        super();
        testCase_ = testCase;
        filter_ = filter;
        iterations_ = iterations;
    }

    public void run()
    {
        try
        {
            sleep(FilterTest.random_.nextInt(1000));
        } catch (InterruptedException e)
        {
            // ignored
        }

        CounterMap _counter = new CounterMap();
        for (int x = 0; x < iterations_; x++)
        {
            // constraint count should always be a multiple of 10
            ConstraintInfo[] _info = filter_.get_all_constraints();
            Assert.assertTrue(_info.length % 10 == 0);

            for (int y = 0; y < _info.length; y++)
            {
                _counter.incr(_info[y].constraint_expression.constraint_expr);
            }

            Iterator _i = _counter.allCounters();

            // constraint type count should always be a multiple of 10
            while (_i.hasNext())
            {
                Counter _c = (Counter) _i.next();
                Assert.assertTrue(_c.value() % 10 == 0);
            }

            _counter.reset();

            try
            {
                Thread.sleep(FilterTest.random_.nextInt(110));
            } catch (InterruptedException ie)
            {
                // ignored
            }
        }
    }
}

////////////////////////////////////////

class CounterMap
{
    Map counters_ = new Hashtable();

    public void incr(Object t)
    {
        Counter _c = (Counter) counters_.get(t);
        if (_c == null)
        {
            _c = new Counter();
            counters_.put(t, _c);
        }
        _c.incr();
    }

    public int value(Object t)
    {
        Counter _c = (Counter) counters_.get(t);
        if (_c == null)
        {
            return 0;
        }
        
            return _c.value();
        
    }

    public void reset()
    {
        counters_.clear();
    }

    Iterator allCounters()
    {
        return counters_.values().iterator();
    }
}

////////////////////////////////////////

class Counter
{
    int counter_ = 0;

    public void incr()
    {
        ++counter_;
    }

    public int value()
    {
        return counter_;
    }
}

class FilterModify extends Thread
{
    TestCase testCase_;

    Filter filter_;

    int iterations_ = 100;

    ConstraintExp[] constraintExp_;

    FilterModify(TestCase testCase, Filter filter, String expression, int iterations)
    {
        super();

        setDaemon(true);

        testCase_ = testCase;
        filter_ = filter;
        iterations_ = iterations;

        constraintExp_ = new ConstraintExp[10];

        EventType[] _eventType = new EventType[1];
        _eventType[0] = new EventType("domain", expression);

        for (int x = 0; x < constraintExp_.length; x++)
        {
            constraintExp_[x] = new ConstraintExp(_eventType, expression);
        }
    }

    public void run()
    {
        try
        {
            sleep(FilterTest.random_.nextInt(1000));
        } catch (InterruptedException e)
        {
            // ignored
        }

        ConstraintInfo[] _info = null;
        for (int x = 0; x < iterations_; x++)
        {
            try
            {
                if (_info != null)
                {
                    int[] _toBeDeleted = new int[_info.length];
                    for (int y = 0; y < _info.length; y++)
                    {
                        _toBeDeleted[y] = _info[y].constraint_id;
                    }
                    // delete the constraints this thread added earlier
                    filter_.modify_constraints(_toBeDeleted, new ConstraintInfo[0]);

                    try
                    {
                        Thread.sleep(FilterTest.random_.nextInt(20));
                    } catch (InterruptedException ie)
                    {
                        // ignore
                    }

                }
                // add some constraints
                _info = filter_.add_constraints(constraintExp_);

                try
                {
                    Thread.sleep(FilterTest.random_.nextInt(200));
                } catch (InterruptedException ie)
                {
                    // ignore
                }
            } catch (Exception e)
            {
                e.printStackTrace();
                Assert.fail();
            }
        }
    }
}