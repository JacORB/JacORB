/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose.
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

import org.jacorb.orb.*;
import org.jacorb.orb.portableInterceptor.*;

import org.omg.PortableInterceptor.*;
import org.omg.IOP.Codec;
import org.omg.IOP.*;
import org.omg.IIOP.*;

/**
 * @author Nicolas Noffke
 * @version $Id$
 */

public class BiDirConnectionClientInterceptor
    extends DefaultClientInterceptor
{
    private String name = "BiDirConnectionClientInterceptor";

    private Codec codec = null;    
    private ORB orb = null;

    private ServiceContext bidir_ctx = null;

    public BiDirConnectionClientInterceptor( ORB orb, Codec codec ) 
    {
        this.orb = orb;
        this.codec = codec;
    }

    public String name() 
    {
        return name;
    }

    public void destroy()
    {
    }

    public void send_request( ClientRequestInfo ri ) 
        throws ForwardRequest
    {
        //only send a BiDir service context if our orb allows it, and
        //the connection was initiated in this process
        if( orb.useBiDirGIOP() && 
            ((ClientRequestInfoImpl) ri).connection.isClientInitiated() )
        {
            if( bidir_ctx == null )
            {
                BasicAdapter ba = orb.getBasicAdapter();
                
                ListenPoint lp = new ListenPoint( ba.getAddress(),
                                                  (short) ba.getPort() );

                ListenPoint[] points = null;
                if( ba.hasSSLListener() )
                {   
                    ListenPoint ssl_lp = 
                        new ListenPoint( ba.getAddress(),
                                         (short) ba.getSSLPort() );

                    points = new ListenPoint[]{ lp, ssl_lp };
                }
                else
                {
                    points = new ListenPoint[]{ lp };
                }

                BiDirIIOPServiceContext b = 
                    new BiDirIIOPServiceContext( points );
                org.omg.CORBA.Any any = orb.create_any();
                BiDirIIOPServiceContextHelper.insert( any, b );
                
                CDROutputStream cdr_out = new CDROutputStream();

                cdr_out.beginEncapsulatedArray();
                BiDirIIOPServiceContextHelper.write( cdr_out, b );

                bidir_ctx = new ServiceContext( BI_DIR_IIOP.value,
                                                cdr_out.getBufferCopy() );
            }
            
            ri.add_request_service_context( bidir_ctx, true );
        }
    }
} // BiDirConnectionClientInterceptor

