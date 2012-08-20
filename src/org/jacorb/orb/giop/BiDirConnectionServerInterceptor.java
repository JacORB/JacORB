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

import org.jacorb.config.ConfigurationException;
import org.jacorb.orb.CDRInputStream;
import org.jacorb.orb.ORB;
import org.jacorb.orb.iiop.IIOPAddress;
import org.jacorb.orb.iiop.IIOPProfile;
import org.jacorb.orb.portableInterceptor.DefaultServerInterceptor;
import org.jacorb.orb.portableInterceptor.ServerRequestInfoImpl;
import org.omg.IIOP.BiDirIIOPServiceContext;
import org.omg.IIOP.BiDirIIOPServiceContextHelper;
import org.omg.IIOP.ListenPoint;
import org.omg.IOP.BI_DIR_IIOP;
import org.omg.IOP.ServiceContext;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.ServerRequestInfo;
import org.slf4j.Logger;

/**
 * @author Nicolas Noffke
 */
public class BiDirConnectionServerInterceptor
    extends DefaultServerInterceptor
{
    private static final String NAME = "BiDirConnectionServerInterceptor";

    private final ORB orb;
    private final Logger logger;
    private final ClientConnectionManager conn_mg;

    protected BiDirConnectionServerInterceptor( ORB orb )
    {
        super();

        this.orb = orb;
        this.logger = orb.getConfiguration().getLogger("jacorb.giop.bidir.interceptor");
        conn_mg = orb.getClientConnectionManager();
    }

    public String name()
    {
        return NAME;
    }

    public void destroy()
    {
        // nothing to do
    }

    public void receive_request_service_contexts( ServerRequestInfo requestInfo )
        throws ForwardRequest
    {
        if( orb.useBiDirGIOP() )
        {
            try
            {
                final ServiceContext context = requestInfo.get_request_service_context( BI_DIR_IIOP.value );
                addConnections(requestInfo, context);
            }
            catch( org.omg.CORBA.BAD_PARAM e )
            {
                logger.debug("no BiDir context present");
            }
        }
    }

    private void addConnections(ServerRequestInfo requestInfo, ServiceContext ctx)
    {
        final BiDirIIOPServiceContext bidir_ctx = readBiDirContext(ctx);

        GIOPConnection connection =
            ((ServerRequestInfoImpl) requestInfo).request.getConnection();

        for( int i = 0; i < bidir_ctx.listen_points.length; i++ )
        {
            ListenPoint listenPoint = bidir_ctx.listen_points[i];

            IIOPAddress addr = new IIOPAddress (listenPoint.host, listenPoint.port);
            try
            {
               addr.configure (orb.getConfiguration ());
            }
            catch( ConfigurationException ce)
            {
                logger.warn("ConfigurationException", ce );
            }

            if (logger.isDebugEnabled())
            {
                logger.debug("Client conn. added to target " + addr );
            }

            conn_mg.addConnection( connection, new IIOPProfile (addr, null, connection.profile.version().minor));
        }
    }

    private BiDirIIOPServiceContext readBiDirContext(ServiceContext ctx)
    {
        final CDRInputStream cdr_in =
            new CDRInputStream( orb, ctx.context_data );

        try
        {
            cdr_in.openEncapsulatedArray();

            return BiDirIIOPServiceContextHelper.read(cdr_in);
        }
        finally
        {
            cdr_in.close();
        }
    }
}
