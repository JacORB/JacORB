package org.jacorb.security.level2;

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

import org.omg.Security.*;
import org.omg.SecurityLevel2.*;

/**
 *  JacORB implementation of security Credentials
 *
 * @author Gerald Brose
 * @version  $Id$
 *
 */

public class ReceivedCredentialsImpl
    extends CredentialsImpl
    implements ReceivedCredentials
{

    public ReceivedCredentialsImpl( SecAttribute[] attributes,
                                    AuthenticationStatus authStatus )
    {
        super( attributes, 
               authStatus, 
               InvocationCredentialsType.SecReceivedCredentials );
    }

    public ReceivedCredentialsImpl( SecAttribute[] attributes )
    {
        super( attributes, 
               AuthenticationStatus.SecAuthSuccess , 
               InvocationCredentialsType.SecReceivedCredentials );
    }
  
    public Credentials accepting_credentials()
    {
        return null;
    }

    public short association_options_used()
    {
        return -1;
    }

    public DelegationState delegation_state()
    {
        return DelegationState.SecInitiator;
    }

    public org.omg.Security.DelegationMode delegation_mode()
    {
        return org.omg.Security.DelegationMode.SecDelModeNoDelegation;
    }
}











