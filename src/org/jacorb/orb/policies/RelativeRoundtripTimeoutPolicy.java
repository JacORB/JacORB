package org.jacorb.orb.policies;

import org.omg.CORBA.*;
import org.omg.Messaging.*;

public class RelativeRoundtripTimeoutPolicy
    extends _RelativeRoundtripTimeoutPolicyLocalBase
{
    private long relative_expiry;
    
    public RelativeRoundtripTimeoutPolicy (long relative_expiry)
    {
        this.relative_expiry = relative_expiry;
    }

    public RelativeRoundtripTimeoutPolicy (org.omg.CORBA.Any value)
    {
        this.relative_expiry = value.extract_ulonglong();
    }

    public long relative_expiry()
    {
        return relative_expiry;
    }

    public int policy_type()
    {
        return RELATIVE_RT_TIMEOUT_POLICY_TYPE.value;
    }

    public Policy copy()
    {
        return new RelativeRoundtripTimeoutPolicy (relative_expiry);
    }

    public void destroy()
    {
    }

}
