/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2001  Gerald Brose.
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

package org.jacorb.orb.connection;

import org.omg.CORBA.Policy;
import org.omg.CORBA.Any;
import org.omg.CORBA.PolicyError;

import org.omg.PortableInterceptor.PolicyFactory;

import org.omg.BiDirPolicy.*;

import org.omg.CORBA.LocalObject;
import org.jacorb.util.Debug;

/**
 * BiDirPolicyFactory.java
 *
 *
 * Created: Mon Sep  3 18:32:16 2001
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public class BiDirPolicyFactory 
    extends org.omg.CORBA.LocalObject 
    implements PolicyFactory
{
    public BiDirPolicyFactory ()
    {
    }

    public Policy create_policy( int type, Any any ) 
        throws PolicyError
    {
        if( type != BIDIRECTIONAL_POLICY_TYPE.value )
        {
            Debug.output( 1, "ERROR: Invalid policy type of " + type );

            throw new PolicyError();
        }
        
        short value = BidirectionalPolicyValueHelper.extract( any );
        
        if( value != NORMAL.value &&
            value != BOTH.value )
        {
            Debug.output( 1, "ERROR: Invalid value for BiDir policy of " + 
                          value );
            throw new PolicyError();
        }
        
        return new BiDirPolicy( value );
    }
}






