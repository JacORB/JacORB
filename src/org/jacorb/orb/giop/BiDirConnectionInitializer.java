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

import org.jacorb.orb.*;

import org.omg.PortableInterceptor.*;
import org.omg.IOP_N.*;
import org.omg.BiDirPolicy.BIDIRECTIONAL_POLICY_TYPE;

/**
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public class BiDirConnectionInitializer 
  extends LocalityConstrainedObject 
  implements ORBInitializer
{
    public void post_init(ORBInitInfo info) 
    {
        try
        {
            ORB orb = ((org.jacorb.orb.portableInterceptor.ORBInitInfoImpl) info).getORB();
            Encoding encoding = new Encoding(ENCODING_CDR_ENCAPS.value, 
                                             (byte) 1, (byte) 0);
            Codec codec = info.codec_factory().create_codec(encoding);

            info.add_client_request_interceptor( new BiDirConnectionClientInterceptor( orb, codec ));
            info.add_server_request_interceptor( new BiDirConnectionServerInterceptor( orb, codec ));
            
            info.register_policy_factory( BIDIRECTIONAL_POLICY_TYPE.value,
                                          new BiDirPolicyFactory() );
        }
        catch (Exception e)
        {
            org.jacorb.util.Debug.output(2, e);
        }
    }

    public void pre_init(ORBInitInfo info) 
    {    
    }
} // BiDirConnectionInitializer
