package org.jacorb.orb.policies;

import org.omg.CORBA.*;
import org.omg.Messaging.*;
import org.omg.TimeBase.*;

public class ReplyStartTimePolicy extends _ReplyStartTimePolicyLocalBase
{
    private UtcT start_time;
    
    public ReplyStartTimePolicy (UtcT start_time)
    {
        this.start_time = start_time;
    }
    
    public ReplyStartTimePolicy (org.omg.CORBA.Any value)
    {
        this.start_time = UtcTHelper.extract (value);
    }

    public UtcT start_time()
    {
        return start_time;
    }

    public int policy_type()
    {
        return REPLY_START_TIME_POLICY_TYPE.value;
    }

    public Policy copy()
    {
        UtcT copy_time = new UtcT (start_time.time, 
                                   start_time.inacclo, 
                                   start_time.inacchi,
                                   start_time.tdf);

        return new ReplyStartTimePolicy (copy_time);
    }

    public void destroy()
    {
    }

}
