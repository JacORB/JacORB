package org.jacorb.orb.policies;

import org.omg.CORBA.*;
import org.omg.Messaging.*;

public class SyncScopePolicy extends _SyncScopePolicyLocalBase
{
    private short synchronization;
    
    public SyncScopePolicy (short synchronization)
    {
        this.synchronization = synchronization;
    }
    
    public SyncScopePolicy (org.omg.CORBA.Any value)
    {
        this.synchronization = value.extract_short();        
    }


    public short synchronization()
    {
        return synchronization;
    }

    public int policy_type()
    {
        return SYNC_SCOPE_POLICY_TYPE.value;
    }

    public Policy copy()
    {
        return new SyncScopePolicy (synchronization);
    }

    public void destroy()
    {
    }

}
