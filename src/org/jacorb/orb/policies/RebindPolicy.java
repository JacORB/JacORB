package org.jacorb.orb.policies;

import org.omg.CORBA.Policy;
import org.omg.Messaging.REBIND_POLICY_TYPE;
import org.omg.Messaging._RebindPolicyLocalBase;

public class RebindPolicy extends _RebindPolicyLocalBase
{
    private short rebind_mode;
    
    public RebindPolicy (short rebind_mode)
    {
        this.rebind_mode = rebind_mode;
    }

    public RebindPolicy (org.omg.CORBA.Any value)
    {
        this.rebind_mode = value.extract_short();
    }

    public short rebind_mode()
    {
        return rebind_mode;
    }

    public int policy_type()
    {
        return REBIND_POLICY_TYPE.value;
    }

    public Policy copy()
    {
        return new RebindPolicy (rebind_mode);
    }

    public void destroy()
    {
    }

}
