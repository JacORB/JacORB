package org.jacorb.test.bugs.bugjac767;

import org.junit.Assert;
import org.junit.Test;

public class Bugjac767Test
{
    @Test
    public void testBugjac767()
    {
        Assert.assertFalse(DataWriterListenerOperations.class.getName()+" is subinterface of "+Listener.class.getName(), Listener.class.isAssignableFrom(DataWriterListenerOperations.class));
        Assert.assertTrue(DataWriterListenerOperations.class.getName()+" is NOT subinterface of " + ListenerOperations.class.getName(), ListenerOperations.class.isAssignableFrom(DataWriterListenerOperations.class));
    }
}
