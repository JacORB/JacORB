package org.jacorb.orb.policies;

import org.omg.CORBA.*;
import org.omg.Messaging.*;

public class MaxHopsPolicy extends _MaxHopsPolicyLocalBase
{
    private short max_hops;

    public MaxHopsPolicy (short max_hops)
    {
        this.max_hops = max_hops;
    }
    
    public MaxHopsPolicy (org.omg.CORBA.Any value)
    {
        max_hops = value.extract_ushort();
    }

    public short max_hops()
    {
        return max_hops;
    }

    public int policy_type()
    {
        return MAX_HOPS_POLICY_TYPE.value;
    }

    public Policy copy()
    {
        return new MaxHopsPolicy (max_hops);
    }

    public void destroy()
    {
    }

}
