package org.jacorb.poa.policy;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2012 Gerald Brose / The JacORB Team.
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
 * This class implements the id assignment policy.
 *
 * @author Reimo Tiedemann, FU Berlin
 * @version $Id$
 */

public class IdAssignmentPolicy 
    extends org.omg.PortableServer._IdAssignmentPolicyLocalBase
{
    private org.omg.PortableServer.IdAssignmentPolicyValue value;

    public IdAssignmentPolicy(org.omg.PortableServer.IdAssignmentPolicyValue _value) {
        value = _value;
    }

    public org.omg.CORBA.Policy copy() {
        return new IdAssignmentPolicy(value());
    }

    public void destroy() {
    }

    public int policy_type() {
        return org.omg.PortableServer.ID_ASSIGNMENT_POLICY_ID.value;
    }

    public org.omg.PortableServer.IdAssignmentPolicyValue value() {
        return value;
    }
}






