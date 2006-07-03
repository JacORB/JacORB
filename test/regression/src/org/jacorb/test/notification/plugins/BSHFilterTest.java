package org.jacorb.test.notification.plugins;

import junit.framework.Test;

import org.jacorb.notification.filter.bsh.BSHFilter;
import org.jacorb.notification.impl.DefaultEvaluationContextFactory;
import org.jacorb.notification.impl.DefaultMessageFactory;
import org.jacorb.test.notification.common.NotificationTestCase;
import org.jacorb.test.notification.common.NotificationTestCaseSetup;
import org.omg.CORBA.Any;
import org.omg.CosNotification.EventType;
import org.omg.CosNotifyFilter.ConstraintExp;
import org.omg.CosNotifyFilter.InvalidConstraint;

/**
 * @author Alphonse Bendt
 */

public class BSHFilterTest extends NotificationTestCase
{
    private BSHFilter objectUnderTest_;

    private Any testData_;

    ////////////////////////////////////////

    public BSHFilterTest(String test, NotificationTestCaseSetup setup)
    {
        super(test, setup);
    }

    ////////////////////////////////////////

    public void setUpTest() throws Exception
    {
        objectUnderTest_ = new BSHFilter(getConfiguration(), new DefaultEvaluationContextFactory(
                getEvaluator()), new DefaultMessageFactory(getORB(), getConfiguration()), getPOA());

        testData_ = getORB().create_any();

        testData_.insert_long(10);
    }

    private void attachFilter(String filterExpr) throws InvalidConstraint
    {
        ConstraintExp[] _constraintExp = new ConstraintExp[1];
        EventType[] _eventType = new EventType[1];
        _eventType[0] = new EventType("*", "*");

        _constraintExp[0] = new ConstraintExp(_eventType, filterExpr);
        objectUnderTest_.add_constraints(_constraintExp);
    }

    /**
     * create remote filter object and invoke match operation on it
     */
    public void testReturnTrue() throws Exception
    {
        attachFilter("return true");

        assertTrue(objectUnderTest_.match(testData_));
    }

    public void testSimpleMatch() throws Exception
    {
        attachFilter("event.extract_long() == 10");

        assertTrue(objectUnderTest_.match(testData_));
    }

    public void testReturnFalse() throws Exception
    {
        attachFilter("return false");

        assertFalse(objectUnderTest_.match(testData_));
    }

    public static Test suite() throws Exception
    {
        return NotificationTestCase.suite(BSHFilterTest.class);
    }
}