package org.jacorb.orb.policies;

import org.omg.CORBA.*;
import org.omg.Messaging.*;
import org.omg.TimeBase.*;

public class ReplyEndTimePolicy extends _ReplyEndTimePolicyLocalBase
{
    private UtcT end_time;
    
    public ReplyEndTimePolicy (UtcT end_time)
    {
        this.end_time = end_time;
    }
    
    public ReplyEndTimePolicy (org.omg.CORBA.Any value)
    {
        this.end_time = UtcTHelper.extract (value);
    }

    public UtcT end_time()
    {
        return end_time;
    }

    public int policy_type()
    {
        return REPLY_END_TIME_POLICY_TYPE.value;
    }

    public Policy copy()
    {
        UtcT copy_time = new UtcT (end_time.time, 
                                   end_time.inacclo, 
                                   end_time.inacchi,
                                   end_time.tdf);

        return new ReplyEndTimePolicy (copy_time);
    }

    public void destroy()
    {
    }

}
