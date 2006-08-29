package org.jacorb.poa.policy;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2004 Gerald Brose.
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
 * This class implements the implicit activation policy.
 *
 * @author Reimo Tiedemann, FU Berlin
 * @version $Id$
 */

public class ImplicitActivationPolicy 
    extends org.omg.PortableServer._ImplicitActivationPolicyLocalBase
{
    private org.omg.PortableServer.ImplicitActivationPolicyValue value;

    public ImplicitActivationPolicy(org.omg.PortableServer.ImplicitActivationPolicyValue _value) {
        value = _value;
    }

    public org.omg.CORBA.Policy copy() {
        return new ImplicitActivationPolicy(value());
    }

    public void destroy() {
    }

    public int policy_type() {
        return org.omg.PortableServer.IMPLICIT_ACTIVATION_POLICY_ID.value;
    }

    public org.omg.PortableServer.ImplicitActivationPolicyValue value() {
        return value;
    }
}






