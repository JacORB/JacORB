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

package org.jacorb.orb.connection;

import org.omg.PortableInterceptor.*;
import org.omg.IOP.Codec;
import org.omg.IIOP.*;
import org.omg.IOP.*;

import org.jacorb.orb.*;
import org.jacorb.util.Debug;
import org.jacorb.orb.portableInterceptor.*;


/**
 * BiDirConnectionServerInterceptor.java
 *
 *
 * Created: Sun Sep  2 18:16:27 2002
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public class BiDirConnectionServerInterceptor 
    extends DefaultServerInterceptor 
{
    private String name = "BiDirConnectionServerInterceptor";

    private ORB orb = null;
    private Codec codec = null;

    private ConnectionManager conn_mg = null;

    public BiDirConnectionServerInterceptor( ORB orb,
                                             Codec codec )
    {
        this.orb = orb;
        this.codec = codec;

        conn_mg = orb.getConnectionManager();
    }

    public String name()
    {
        return name;
    }

    public void destroy()
    {
    }    

    public void receive_request_service_contexts( ServerRequestInfo ri ) 
        throws ForwardRequest
    {
        if( orb.useBiDirGIOP() )
        {
            ServiceContext ctx = null;
            
            try
            {
                ctx = ri.get_request_service_context( BI_DIR_IIOP.value );
            }
            catch( org.omg.CORBA.BAD_PARAM bp )
            {
                //ignore
            }

            if( ctx == null )
            {
                return;//no bidir context present
            }

            BiDirIIOPServiceContext bidir_ctx = null;

            CDRInputStream cdr_in = 
                new CDRInputStream( orb, ctx.context_data );

            cdr_in.openEncapsulatedArray();
            
            bidir_ctx = 
                BiDirIIOPServiceContextHelper.read( cdr_in );            

            GIOPConnection connection = 
                ((ServerRequestInfoImpl) ri).request.getConnection();
            
            for( int i = 0; i < bidir_ctx.listen_points.length; i++ )
            {
                ListenPoint p = bidir_ctx.listen_points[i];
                
                String info = p.host + ':' + 
                    ((p.port < 0)? ((int) p.port) + 65536 : (int) p.port);

                Debug.output( 2, "BiDirServerInterceptor: Added client conn to target " + info );
                
                conn_mg.addConnection( connection, info );
            }            
        }
    }    
}// BiDirConnectionServerInterceptor



