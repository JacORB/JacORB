package org.jacorb.orb.policies;

import org.omg.CORBA.*;
import org.omg.Messaging.*;

public class QueueOrderPolicy extends _QueueOrderPolicyLocalBase
{
    private short allowed_orders;
    
    public QueueOrderPolicy (short allowed_orders)
    {
        this.allowed_orders = allowed_orders;
    }

    public QueueOrderPolicy (org.omg.CORBA.Any value)
    {
        this.allowed_orders = value.extract_short();
    }

    public short allowed_orders()
    {
        return allowed_orders;
    }

    public int policy_type()
    {
        return QUEUE_ORDER_POLICY_TYPE.value;
    }

    public Policy copy()
    {
        return new QueueOrderPolicy (allowed_orders);
    }

    public void destroy()
    {
    }

}
