package org.jacorb.orb.policies;

import org.omg.CORBA.*;
import org.omg.Messaging.*;

public class RelativeRequestTimeoutPolicy
    extends _RelativeRequestTimeoutPolicyLocalBase
{
    private long relative_expiry;
    
    public RelativeRequestTimeoutPolicy (long relative_expiry)
    {
        this.relative_expiry = relative_expiry;
    }

    public RelativeRequestTimeoutPolicy (org.omg.CORBA.Any value)
    {
        this.relative_expiry = value.extract_long();
    }

    public long relative_expiry()
    {
        return relative_expiry;
    }

    public int policy_type()
    {
        return RELATIVE_REQ_TIMEOUT_POLICY_TYPE.value;
    }

    public Policy copy()
    {
        return new RelativeRequestTimeoutPolicy (relative_expiry);
    }

    public void destroy()
    {
    }

}
