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

import org.omg.CORBA.*;
import org.omg.Messaging.*;

public class QueueOrderPolicy extends _QueueOrderPolicyLocalBase
{
    private short allowed_orders;
    
    public QueueOrderPolicy (short allowed_orders)
    {
        this.allowed_orders = allowed_orders;
    }

    public QueueOrderPolicy (org.omg.CORBA.Any value)
    {
        this.allowed_orders = value.extract_short();
    }

    public short allowed_orders()
    {
        return allowed_orders;
    }

    public int policy_type()
    {
        return QUEUE_ORDER_POLICY_TYPE.value;
    }

    public Policy copy()
    {
        return new QueueOrderPolicy (allowed_orders);
    }

    public void destroy()
    {
    }

}
