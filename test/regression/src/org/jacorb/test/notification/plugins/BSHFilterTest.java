package org.jacorb.test.notification.plugins;

import org.omg.CORBA.Any;
import org.omg.CosNotification.EventType;
import org.omg.CosNotifyFilter.ConstraintExp;
import org.omg.CosNotifyFilter.ConstraintInfo;
import org.omg.CosNotifyFilter.InvalidConstraint;

import org.jacorb.notification.ApplicationContext;
import org.jacorb.notification.filter.bsh.BSHFilter;
import org.jacorb.test.notification.NotificationTestCase;
import org.jacorb.test.notification.NotificationTestCaseSetup;

import junit.framework.Test;

/**
 * @author Alphonse Bendt
 */

public class BSHFilterTest extends NotificationTestCase {

    BSHFilter filter_;

    ApplicationContext appContext_;

    Any testData_;

    ////////////////////////////////////////

    public BSHFilterTest(String test, NotificationTestCaseSetup setup) {
        super(test, setup);
    }

    ////////////////////////////////////////

    public void setUp() throws Exception {
        appContext_ =
            new ApplicationContext(getORB(), getPOA() );

        appContext_.configure(getConfiguration());

        filter_ = new BSHFilter(appContext_);

        filter_.configure(getConfiguration());

        testData_ = getORB().create_any();

        testData_.insert_long(10);
    }


    public void tearDown() throws Exception {
        super.tearDown();

        appContext_.dispose();
    }

    private void attachFilter(String filterExpr) throws InvalidConstraint {
        ConstraintExp[] _constraintExp = new ConstraintExp[1];
        EventType[] _eventType = new EventType[1];
        _eventType[0] = new EventType("*", "*");

        _constraintExp[0] = new ConstraintExp(_eventType, filterExpr);
        ConstraintInfo[] _info = filter_.add_constraints(_constraintExp);
    }

    /**
     * create remote filter object and invoke match operation on it
     */
    public void testReturnTrue() throws Exception {
        String filterExpr = "return true";

        attachFilter(filterExpr);

        // this should match
        assertTrue(filter_.match(testData_));
    }

    public void testSimpleMatch() throws Exception {
        attachFilter("event.extract_long() == 10");

        filter_.match(testData_);
    }


    public void testReturnFalse() throws Exception {
        attachFilter("return false");

        assertFalse(filter_.match(testData_));
    }


    public static Test suite() throws Exception {
        return NotificationTestCase.suite(BSHFilterTest.class);
    }
}
