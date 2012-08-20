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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.jacorb.orb.BasicAdapter;
import org.jacorb.orb.CDROutputStream;
import org.jacorb.orb.ORB;
import org.jacorb.orb.etf.ProfileBase;
import org.jacorb.orb.portableInterceptor.ClientRequestInfoImpl;
import org.jacorb.orb.portableInterceptor.DefaultClientInterceptor;
import org.omg.ETF.Profile;
import org.omg.IIOP.BiDirIIOPServiceContext;
import org.omg.IIOP.BiDirIIOPServiceContextHelper;
import org.omg.IIOP.ListenPoint;
import org.omg.IOP.BI_DIR_IIOP;
import org.omg.IOP.ServiceContext;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ForwardRequest;

/**
 * @author Nicolas Noffke
 */
public class BiDirConnectionClientInterceptor
    extends DefaultClientInterceptor
{
    private static final String name = "BiDirConnectionClientInterceptor";

    private final ORB orb;

    private ServiceContext bidir_ctx = null;

    public BiDirConnectionClientInterceptor( ORB orb )
    {
        this.orb = orb;
    }

    public String name()
    {
        return name;
    }

    public void destroy()
    {
        // nothing to do
    }

    synchronized private void init_bidir_ctx ()
    {
        if (bidir_ctx != null)
            return;

        BasicAdapter ba = orb.getBasicAdapter();
                       
        List endpoints = ba.getEndpointProfiles();
                       
        Iterator i = endpoints.iterator();
        final List listenPoints = new ArrayList();
        while(i.hasNext())
        {
            Profile profile = (Profile) i.next();
                       
            if (profile instanceof ProfileBase)
            {
                listenPoints.addAll(((ProfileBase)profile).asListenPoints());
            }
            else
            {
                listenPoints.addAll(getListenPoints(profile));
            }
        }

        ListenPoint[] listenPointsArray = 
            (ListenPoint[]) listenPoints.toArray(new ListenPoint[listenPoints.size()]);
                       
        BiDirIIOPServiceContext context =
            new BiDirIIOPServiceContext( listenPointsArray );
        org.omg.CORBA.Any any = orb.create_any();
        BiDirIIOPServiceContextHelper.insert( any, context );

        final CDROutputStream cdr_out = new CDROutputStream(orb);

        try
        {
            cdr_out.beginEncapsulatedArray();
            BiDirIIOPServiceContextHelper.write( cdr_out, context );
            
            bidir_ctx = new ServiceContext( BI_DIR_IIOP.value,
                                            cdr_out.getBufferCopy() );
        }
        finally
        {
            cdr_out.close();
        }
    }

    public void send_request( ClientRequestInfo ri )
        throws ForwardRequest
    {
        //only send a BiDir service context if our orb allows it, and
        //the connection was initiated in this process

        if( !orb.useBiDirGIOP() ||
            !((ClientRequestInfoImpl) ri).connection.isClientInitiated() )
            return;

        if( bidir_ctx == null )
        {
            init_bidir_ctx ();
        }
       
        if ( !((ClientRequestInfoImpl) ri).connection.isListenPointListSent() )
        {
            ri.add_request_service_context( bidir_ctx, true );
        }

        //if this connection isn't "bidir'ed" yet, do so now
        GIOPConnection conn =
            ((ClientRequestInfoImpl) ri).connection.getGIOPConnection();
        if(conn.getRequestListener() instanceof
           NoBiDirClientRequestListener)
        {
            conn.setRequestListener(orb.getBasicAdapter().getRequestListener());
        }
    }

    protected Collection getListenPoints(Profile profile)
    {
        return Collections.EMPTY_LIST;
    }
}
