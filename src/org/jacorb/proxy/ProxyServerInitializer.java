package org.jacorb.proxy;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2002  Gerald Brose.
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

import org.omg.PortableInterceptor.*;
import org.omg.CosNaming.*;
import org.jacorb.orb.*;
import org.jacorb.util.Debug;
import org.omg.IOP.*;

/**
 * This class registers the ClientForwardInterceptor
 * with the ORB.
 *
 * @author Nicolas Noffke, Sebastian Müller, Steve Osselton
 */

public class ProxyServerInitializer
    extends org.omg.CORBA.LocalObject
    implements ORBInitializer
{
    public ProxyServerInitializer () 
    {
    }

    /**
     * This method registers the server proxy interceptor.
     */

    public void post_init (ORBInitInfo info)
    {
        Codec codec;
        Encoding encoding;
        int slot = info.allocate_slot_id ();

        try
        {
            encoding = new Encoding (ENCODING_CDR_ENCAPS.value, (byte) 1, (byte) 0);
            codec = info.codec_factory().create_codec (encoding);

            info.add_server_request_interceptor
                (new ProxyServerForwardInterceptor (info, codec, slot));
        }
        catch (org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName ex)
        {
            Debug.output (1, "Duplicate server interceptor name");
        }
        catch (Exception ex)
        {
            Debug.output (1, ex);
        }
    }

    public void pre_init (ORBInitInfo info)
    {
    }
}
