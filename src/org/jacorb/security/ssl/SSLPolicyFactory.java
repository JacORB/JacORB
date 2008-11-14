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
package org.jacorb.security.ssl;

import org.jacorb.ssl.SSLPolicyValueHelper;
import org.jacorb.ssl.SSL_POLICY_TYPE;
import org.omg.CORBA.Any;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.Policy;
import org.omg.CORBA.PolicyError;
import org.omg.PortableInterceptor.PolicyFactory;

public class SSLPolicyFactory 
        extends LocalObject 
        implements PolicyFactory
{
 
    public SSLPolicyFactory()
    {
    }

    public Policy create_policy(int type, Any value)
        throws PolicyError
    {
        if (type != SSL_POLICY_TYPE.value)
            throw new PolicyError();

        return new SSLPolicyImpl(SSLPolicyValueHelper.extract(value));

    }
}
