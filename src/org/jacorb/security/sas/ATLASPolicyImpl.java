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
package org.jacorb.security.sas;

import org.jacorb.sasPolicy.ATLASPolicy;
import org.jacorb.sasPolicy.ATLASPolicyValues;
import org.jacorb.sasPolicy.ATLASPolicyValuesHelper;
import org.jacorb.sasPolicy.ATLAS_POLICY_TYPE;
import org.omg.CORBA.Policy;

public class ATLASPolicyImpl extends org.omg.CORBA.LocalObject implements ATLASPolicy

{
    private ATLASPolicyValues value;

    public ATLASPolicyImpl (ATLASPolicyValues value)
    {
        this.value = value;
    }

    public ATLASPolicyImpl (org.omg.CORBA.Any value)
    {
        this.value = ATLASPolicyValuesHelper.extract (value);
    }

    public ATLASPolicyValues value()
    {
        return value;
    }

    public int policy_type()
    {
        return ATLAS_POLICY_TYPE.value;
    }

    public Policy copy()
    {
        ATLASPolicyValues copy_values =
        new ATLASPolicyValues (value.atlasURL,
                               value.atlasCache);
        return new ATLASPolicyImpl (copy_values);
    }

    public void destroy()
    {
    }

}
