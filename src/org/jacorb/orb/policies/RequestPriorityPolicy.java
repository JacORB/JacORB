package org.jacorb.orb.policies;

import org.omg.CORBA.*;
import org.omg.Messaging.*;

public class RequestPriorityPolicy extends _RequestPriorityPolicyLocalBase
{
    private PriorityRange priority_range;
    
    public RequestPriorityPolicy (PriorityRange priority_range)
    {
        this.priority_range = priority_range;
    }
    
    public RequestPriorityPolicy (org.omg.CORBA.Any value)
    {
        this.priority_range = PriorityRangeHelper.extract (value);
    }

    public PriorityRange priority_range()
    {
        return priority_range;
    }

    public int policy_type()
    {
        return REQUEST_PRIORITY_POLICY_TYPE.value;
    }

    public Policy copy()
    {
        PriorityRange copy_range = new PriorityRange (priority_range.min,
                                                      priority_range.max);
                                                      
        return new RequestPriorityPolicy (copy_range);
    }

    public void destroy()
    {
    }

}
