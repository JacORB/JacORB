/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2003 Gerald Brose
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

import org.jacorb.sasPolicy.SASPolicy;
import org.jacorb.sasPolicy.SASPolicyValues;
import org.jacorb.sasPolicy.SASPolicyValuesHelper;
import org.jacorb.sasPolicy.SAS_POLICY_TYPE;
import org.omg.CORBA.Policy;

public class SASPolicyImpl extends org.omg.CORBA.LocalObject implements SASPolicy

{
    private SASPolicyValues value;

    public SASPolicyImpl (SASPolicyValues value)
    {
        this.value = value;
    }

    public SASPolicyImpl (org.omg.CORBA.Any value)
    {
        this.value = SASPolicyValuesHelper.extract (value);
    }

    public SASPolicyValues value()
    {
        return value;
    }

    public int policy_type()
    {
        return SAS_POLICY_TYPE.value;
    }

    public Policy copy()
    {
        SASPolicyValues copy_values =
        new SASPolicyValues (value.targetRequires,
                             value.targetSupports,
                             value.stateful);
        return new SASPolicyImpl (copy_values);
    }

    public void destroy()
    {
    }

}
