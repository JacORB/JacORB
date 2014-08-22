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

package org.jacorb.orb.giop;

import org.omg.BiDirPolicy.BIDIRECTIONAL_POLICY_TYPE;
import org.omg.BiDirPolicy.BOTH;
import org.omg.BiDirPolicy.BidirectionalPolicy;
import org.omg.CORBA.Policy;

/**
 * @author Nicolas Noffke
 */

public class BiDirPolicy
    extends org.omg.CORBA.LocalObject
    implements BidirectionalPolicy
{
    private final short policyValue;

    public BiDirPolicy( short value )
    {
        super();
        this.policyValue = value;
    }

    public boolean useBiDirGIOP()
    {
        return policyValue == BOTH.value;
    }

    public short value()
    {
        return policyValue;
    }

    public int policy_type()
    {
        return BIDIRECTIONAL_POLICY_TYPE.value;
    }

    public Policy copy()
    {
        return new BiDirPolicy( policyValue );
    }

    public void destroy()
    {
        // nothing to do
    }
}
