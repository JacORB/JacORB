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

import org.jacorb.ssl.SSLPolicy;
import org.jacorb.ssl.SSLPolicyValue;
import org.jacorb.ssl.SSL_POLICY_TYPE;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.Policy;

public class SSLPolicyImpl 
        extends LocalObject 
        implements SSLPolicy
                                                                    
{
    private SSLPolicyValue value;

    public SSLPolicyImpl(SSLPolicyValue value)
    {
        this.value = value;
    }

    public SSLPolicyValue value()
    {
        return value;
    }

    public int policy_type()
    {
        return SSL_POLICY_TYPE.value;
    }

    public Policy copy()
    {
        return new SSLPolicyImpl(value);
    }

    public void destroy()
    {
    }

    public String toString()
    {
        return "SSLPolicy[" + 
            ((value == SSLPolicyValue.SSL_NOT_REQUIRED) ? "SSL_NOT_REQUIRED" 
                                                        : "SSL_REQUIRED") + "]";
    }

}
