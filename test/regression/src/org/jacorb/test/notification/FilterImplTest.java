package org.jacorb.test.notification;

import org.omg.CORBA.ORB;
import org.omg.CosNotification.EventType;
import org.omg.CosNotifyFilter.ConstraintExp;
import org.omg.CosNotifyFilter.ConstraintInfo;
import org.omg.DynamicAny.DynAnyFactory;
import org.omg.PortableServer.POAHelper;

import org.jacorb.notification.ApplicationContext;
import org.jacorb.notification.ConstraintEntry;
import org.jacorb.notification.FilterFactoryImpl;
import org.jacorb.notification.FilterImpl;
import org.jacorb.notification.filter.FilterUtils;
import org.jacorb.notification.filter.DynamicEvaluator;

import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * FilterImplTest.java
 *
 *
 * Created: Sat Nov 09 16:34:05 2002
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class FilterImplTest extends TestCase {

    /**
     * the testling
     */
    FilterImpl filter_;

    ApplicationContext appContext_;

    public FilterImplTest(String test) {
        super(test);
    }

    public void setUp() throws Exception {
        ORB _orb = ORB.init(new String[0], null);

        appContext_ =
            new ApplicationContext(_orb, POAHelper.narrow(_orb.resolve_initial_references("RootPOA")));

        filter_ = new FilterImpl(appContext_, FilterFactoryImpl.CONSTRAINT_GRAMMAR);
    }

    public void tearDown() throws Exception {
        super.tearDown();
        appContext_.dispose();
    }

    public void testIterator() throws Exception {
        ConstraintExp[] _exp = new ConstraintExp[1];

        for (int x=0; x<_exp.length; ++x) {
            _exp[x] = new ConstraintExp();
        }

        EventType[] _eventType = new EventType[2];
        _eventType[0] = new EventType("*", "*");
        _eventType[1] = new EventType("domain*", "type*");
        _exp[0] = new ConstraintExp(_eventType, "1");
        filter_.add_constraints(_exp);

        Iterator _i =
            filter_.getIterator(FilterUtils.calcConstraintKey("domain1", "type1"));

        int _count = 0;
        while (_i.hasNext()) {
            _count++;
            _i.next();
        }
        assertTrue(_count == 2);
    }

    public void testIterator2() throws Exception {
        ConstraintExp[] _exp = new ConstraintExp[1];
        for (int x=0; x<_exp.length; ++x) {
            _exp[x] = new ConstraintExp();
        }

        EventType[] _eventType = new EventType[2];
        _eventType[0] = new EventType("*", "*");
        _eventType[1] = new EventType("domain*", "type*");
        _exp[0] = new ConstraintExp(_eventType, "1");
        filter_.add_constraints(_exp);

        Iterator _i =
            filter_.getIterator(FilterUtils.calcConstraintKey("domain1", "type1"));

        int _count = 0;
        while (_i.hasNext()) {
            _count++;
            ConstraintEntry _e = (ConstraintEntry)_i.next();
            assertEquals("1", _e.getFilterConstraint().getConstraint());
        }
        assertTrue(_count == 2);

        ConstraintExp[] _exp2 = new ConstraintExp[1];
        _exp2[0] = new ConstraintExp();

        EventType[] _eventType2 = new EventType[2];
        _eventType2[0] = new EventType("*", "*");
        _eventType2[1] = new EventType("domain*", "type*");
        _exp2[0] = new ConstraintExp(_eventType2, "2");
        filter_.add_constraints(_exp2);

        _i = filter_.getIterator(FilterUtils.calcConstraintKey("domain1", "type1"));
        _count = 0;

        while (_i.hasNext()) {
            _count++;
            ConstraintEntry _e = (ConstraintEntry)_i.next();
            assertTrue(_e.getFilterConstraint().getConstraint().equals("1") ||
                       _e.getFilterConstraint().getConstraint().equals("2"));
        }
        assertTrue(_count == 4);
    }

    public void testAddRemove() throws Exception {
        ConstraintExp[] _exp = new ConstraintExp[1];
        for (int x=0; x<_exp.length; ++x) {
            _exp[x] = new ConstraintExp();
        }

        EventType[] _eventType = new EventType[2];
        _eventType[0] = new EventType("*", "*");
        _eventType[1] = new EventType("domain*", "type*");
        _exp[0] = new ConstraintExp(_eventType, "1");
        filter_.add_constraints(_exp);

        ConstraintExp[] _exp2 = new ConstraintExp[1];
        _exp2[0] = new ConstraintExp();

        EventType[] _eventType2 = new EventType[2];
        _eventType2[0] = new EventType("*", "*");
        _eventType2[1] = new EventType("domain*", "type*");
        _exp2[0] = new ConstraintExp(_eventType2, "2");

        ConstraintInfo[] _info = filter_.add_constraints(_exp2);

        Iterator _i = filter_.getIterator(FilterUtils.calcConstraintKey("domain1", "type1"));
        int _count = 0;

        while (_i.hasNext()) {
            _count++;
            ConstraintEntry _e = (ConstraintEntry)_i.next();
            assertTrue(_e.getFilterConstraint().getConstraint().equals("1") ||
                       _e.getFilterConstraint().getConstraint().equals("2"));
        }
        assertTrue(_count == 4);

        int[] _delete_ids = new int[_info.length];
        for (int x=0; x<_delete_ids.length; ++x) {
            _delete_ids[x] = _info[x].constraint_id;
        }
        filter_.modify_constraints(_delete_ids, new ConstraintInfo[0]);

        _i = filter_.getIterator(FilterUtils.calcConstraintKey("domain1", "type1"));
        _count = 0;
        while (_i.hasNext()) {
            _count++;
            ConstraintEntry _e = (ConstraintEntry)_i.next();
            assertTrue(_e.getFilterConstraint().getConstraint().equals("1"));
        }
        assertTrue(_count == 2);
    }


    public static Test suite() {
        return new TestSuite(FilterImplTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

}
