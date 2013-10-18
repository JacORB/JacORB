package org.jacorb.test.notification.plugins;

import org.junit.*;
import static org.junit.Assert.*;

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

    ////////////////////////////////////////

    @Before
    public void setUp() throws Exception
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
    @Test
    public void testReturnTrue() throws Exception
    {
        attachFilter("return true");

        assertTrue(objectUnderTest_.match(testData_));
    }

    @Test
    public void testSimpleMatch() throws Exception
    {
        attachFilter("event.extract_long() == 10");

        assertTrue(objectUnderTest_.match(testData_));
    }

    @Test
    public void testReturnFalse() throws Exception
    {
        attachFilter("return false");

        assertFalse(objectUnderTest_.match(testData_));
    }
}