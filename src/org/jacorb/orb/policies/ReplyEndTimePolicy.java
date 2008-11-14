/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Library General Public
 *   License as published by the Free Software Foundation; either
 *   version 2 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this library; if not, write to the Free
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */
package org.jacorb.orb.policies;

import org.omg.CORBA.Policy;
import org.omg.Messaging.REPLY_END_TIME_POLICY_TYPE;
import org.omg.Messaging._ReplyEndTimePolicyLocalBase;
import org.omg.TimeBase.UtcT;
import org.omg.TimeBase.UtcTHelper;

public class ReplyEndTimePolicy extends _ReplyEndTimePolicyLocalBase
{
    private final UtcT end_time;

    private ReplyEndTimePolicy (UtcT end_time)
    {
        super();

        this.end_time = end_time;
    }

    public ReplyEndTimePolicy (org.omg.CORBA.Any value)
    {
        this(UtcTHelper.extract(value));
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
