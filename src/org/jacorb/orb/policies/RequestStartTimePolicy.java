package org.jacorb.orb.policies;

import org.omg.CORBA.*;
import org.omg.Messaging.*;
import org.omg.TimeBase.*;

public class RequestStartTimePolicy extends _RequestStartTimePolicyLocalBase
{
    private UtcT start_time;
    
    public RequestStartTimePolicy (UtcT start_time)
    {
        this.start_time = start_time;
    }
    
    public RequestStartTimePolicy (org.omg.CORBA.Any value)
    {
        this.start_time = UtcTHelper.extract (value);
    }

    public UtcT start_time()
    {
        return start_time;
    }

    public int policy_type()
    {
        return REQUEST_START_TIME_POLICY_TYPE.value;
    }

    public Policy copy()
    {
        UtcT copy_time = new UtcT (start_time.time, 
                                   start_time.inacclo, 
                                   start_time.inacchi,
                                   start_time.tdf);

        return new RequestStartTimePolicy (copy_time);
    }

    public void destroy()
    {
    }

}
