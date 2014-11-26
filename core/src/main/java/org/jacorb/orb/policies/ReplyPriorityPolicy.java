/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2014 Gerald Brose / The JacORB Team.
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
import org.omg.Messaging.PriorityRange;
import org.omg.Messaging.PriorityRangeHelper;
import org.omg.Messaging.REPLY_PRIORITY_POLICY_TYPE;
import org.omg.Messaging._ReplyPriorityPolicyLocalBase;

public class ReplyPriorityPolicy extends _ReplyPriorityPolicyLocalBase
{
    private final PriorityRange priority_range;

    private ReplyPriorityPolicy (PriorityRange priority_range)
    {
        super();

        this.priority_range = priority_range;
    }

    public ReplyPriorityPolicy (org.omg.CORBA.Any value)
    {
        this(PriorityRangeHelper.extract (value));
    }

    public PriorityRange priority_range()
    {
        return priority_range;
    }

    public int policy_type()
    {
        return REPLY_PRIORITY_POLICY_TYPE.value;
    }

    public Policy copy()
    {
        PriorityRange copy_range = new PriorityRange (priority_range.min,
                                                      priority_range.max);

        return new ReplyPriorityPolicy (copy_range);
    }

    public void destroy()
    {
    }
}
