package org.jacorb.test.notification;

import java.util.Iterator;

import junit.framework.Test;

import org.jacorb.notification.AbstractMessage;
import org.jacorb.notification.filter.AbstractFilter;
import org.jacorb.notification.filter.ConstraintEntry;
import org.jacorb.notification.filter.etcl.ETCLFilter;
import org.jacorb.notification.impl.DefaultEvaluationContextFactory;
import org.jacorb.notification.impl.DefaultMessageFactory;
import org.omg.CosNotification.EventType;
import org.omg.CosNotifyFilter.ConstraintExp;
import org.omg.CosNotifyFilter.ConstraintInfo;

/**
 * @author Alphonse Bendt
 */

public class FilterImplTest extends NotificationTestCase {

    private AbstractFilter objectUnderTest_;
    
    ////////////////////////////////////////

    public FilterImplTest(String test, NotificationTestCaseSetup setup) {
        super(test, setup);
    }

    ////////////////////////////////////////

    public void setUpTest() throws Exception {
        objectUnderTest_ = new ETCLFilter(getConfiguration(), new DefaultEvaluationContextFactory(getEvaluator()), new DefaultMessageFactory(getConfiguration()), getORB(), getPOA());
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
        objectUnderTest_.add_constraints(_exp);

        Iterator _i =
            objectUnderTest_.getIterator(AbstractMessage.calcConstraintKey("domain1", "type1"));

        int _count = 0;
        while (_i.hasNext()) {
            _count++;
            _i.next();
        }
        assertEquals(2, _count);
    }


    /**
     * test to reveal a bug reported by
     * John Farrell
     * (news://news.gmane.org:119/200402191446.17527.Farrell_John_W@cat.com)
     * When the event types in the event don't match the event types
     * in the filter, the ConstraintIterator may be required to
     * iterate over nothing at all.
     */
    public void testIteratorBug() throws Exception {
        ConstraintExp[] _exp = new ConstraintExp[1];

        for (int x=0; x<_exp.length; ++x) {
            _exp[x] = new ConstraintExp();
        }

        EventType[] _eventType = new EventType[2];
        _eventType[0] = new EventType("domain1", "type1");
        _eventType[1] = new EventType("domain2", "type2");
        _exp[0] = new ConstraintExp(_eventType, "1");
        objectUnderTest_.add_constraints(_exp);

        Iterator _i =
            objectUnderTest_.getIterator(AbstractMessage.calcConstraintKey("domain3", "type3"));

        while (_i.hasNext()) {
            _i.next();
        }
    }


    public void testEmptyIteratorThrowsException() throws Exception {
        ConstraintExp[] _exp = new ConstraintExp[1];

        for (int x=0; x<_exp.length; ++x) {
            _exp[x] = new ConstraintExp();
        }

        EventType[] _eventType = new EventType[2];
        _eventType[0] = new EventType("domain1", "type1");
        _eventType[1] = new EventType("domain2", "type2");
        _exp[0] = new ConstraintExp(_eventType, "1");
        objectUnderTest_.add_constraints(_exp);

        Iterator _i =
            objectUnderTest_.getIterator(AbstractMessage.calcConstraintKey("domain3", "type3"));

        try {
            _i.next();
            fail("Calling Iterator.next() on an empty Iterator should fail!");
        } catch (Exception e) {
        }
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
        objectUnderTest_.add_constraints(_exp);

        Iterator _i =
            objectUnderTest_.getIterator(AbstractMessage.calcConstraintKey("domain1", "type1"));

        int _count = 0;
        while (_i.hasNext()) {
            _count++;
            ConstraintEntry _e = (ConstraintEntry)_i.next();
            assertEquals("1", _e.getConstraintInfo().constraint_expression.constraint_expr);
        }
        assertTrue(_count == 2);

        ConstraintExp[] _exp2 = new ConstraintExp[1];
        _exp2[0] = new ConstraintExp();

        EventType[] _eventType2 = new EventType[2];
        _eventType2[0] = new EventType("*", "*");
        _eventType2[1] = new EventType("domain*", "type*");
        _exp2[0] = new ConstraintExp(_eventType2, "2");
        objectUnderTest_.add_constraints(_exp2);

        _i = objectUnderTest_.getIterator(AbstractMessage.calcConstraintKey("domain1", "type1"));
        _count = 0;

        while (_i.hasNext()) {
            _count++;
            ConstraintEntry _e = (ConstraintEntry)_i.next();
            assertTrue(_e.getConstraintExpression().equals("1") ||
                       _e.getConstraintExpression().equals("2"));
        }
        assertEquals(4, _count);
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
        objectUnderTest_.add_constraints(_exp);

        ConstraintExp[] _exp2 = new ConstraintExp[1];
        _exp2[0] = new ConstraintExp();

        EventType[] _eventType2 = new EventType[2];
        _eventType2[0] = new EventType("*", "*");
        _eventType2[1] = new EventType("domain*", "type*");
        _exp2[0] = new ConstraintExp(_eventType2, "2");

        ConstraintInfo[] _info = objectUnderTest_.add_constraints(_exp2);

        Iterator _i = objectUnderTest_.getIterator(AbstractMessage.calcConstraintKey("domain1", "type1"));
        int _count = 0;

        while (_i.hasNext()) {
            _count++;
            ConstraintEntry _e = (ConstraintEntry)_i.next();
            assertTrue(_e.getConstraintExpression().equals("1") ||
                       _e.getConstraintExpression().equals("2"));
        }
        assertEquals(4, _count);

        int[] _delete_ids = new int[_info.length];
        for (int x=0; x<_delete_ids.length; ++x) {
            _delete_ids[x] = _info[x].constraint_id;
        }
        objectUnderTest_.modify_constraints(_delete_ids, new ConstraintInfo[0]);

        _i = objectUnderTest_.getIterator(AbstractMessage.calcConstraintKey("domain1", "type1"));
        _count = 0;
        while (_i.hasNext()) {
            _count++;
            ConstraintEntry _e = (ConstraintEntry)_i.next();
            assertTrue(_e.getConstraintExpression().equals("1"));
        }
        assertEquals(2, _count);
    }


    public static Test suite() throws Exception {
        return NotificationTestCase.suite(FilterImplTest.class);
    }
}
