package org.jacorb.test.bugs.bugjac767;

import junit.framework.Assert;
import junit.framework.TestCase;

public class Bugjac767Test extends TestCase
{
    public void testBugjac767()
    {
        Assert.assertFalse(DataWriterListenerOperations.class.getName()+" is subinterface of "+Listener.class.getName(), Listener.class.isAssignableFrom(DataWriterListenerOperations.class));
        Assert.assertTrue(DataWriterListenerOperations.class.getName()+" is NOT subinterface of " + ListenerOperations.class.getName(), ListenerOperations.class.isAssignableFrom(DataWriterListenerOperations.class));
    }
}
