package org.jacorb.poa.policy;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose.
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
 */
 
/**
 * This class implements the lifespan policy.
 *
 * @author Reimo Tiedemann, FU Berlin
 * @version $Id$
 */

public class LifespanPolicy 
    extends org.omg.PortableServer._LifespanPolicyLocalBase
{
    private org.omg.PortableServer.LifespanPolicyValue value;

    private LifespanPolicy() {
    }

    public LifespanPolicy(org.omg.PortableServer.LifespanPolicyValue _value) {
        value = _value;
    }

    public org.omg.CORBA.Policy copy() {
        return new LifespanPolicy(value());
    }

    public void destroy() {
    }

    public int policy_type() {
        return org.omg.PortableServer.LIFESPAN_POLICY_ID.value;
    }

    public org.omg.PortableServer.LifespanPolicyValue value() 
    {
        return value;
    }
}







