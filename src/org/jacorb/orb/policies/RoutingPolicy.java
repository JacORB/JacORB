package org.jacorb.orb.policies;

import org.omg.CORBA.*;
import org.omg.Messaging.*;

public class RoutingPolicy extends _RoutingPolicyLocalBase
{
    private RoutingTypeRange routing_range;
    
    public RoutingPolicy (RoutingTypeRange routing_range)
    {
        this.routing_range = routing_range;
    }
    
    public RoutingPolicy (org.omg.CORBA.Any value)
    {
        this.routing_range = RoutingTypeRangeHelper.extract (value);
    }

    public RoutingTypeRange routing_range()
    {
        return routing_range;
    }

    public int policy_type()
    {
        return ROUTING_POLICY_TYPE.value;
    }

    public Policy copy()
    {
        RoutingTypeRange copy_range = 
                                new RoutingTypeRange (routing_range.min,
                                                      routing_range.max);
        return new RoutingPolicy (copy_range);
    }

    public void destroy()
    {
    }

}
