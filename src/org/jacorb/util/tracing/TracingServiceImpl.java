package org.jacorb.util.tracing;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2004 Gerald Brose.
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

import java.util.Hashtable;
import org.jacorb.util.tracing.TracingServicePackage.NoSuchRequestId;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

/**
 * @author Gerald Brose
 * @version $Id$
 */
public class TracingServiceImpl
    extends TracingServicePOA
{
    private int pointIds = 0;
    private Hashtable traces = new Hashtable();

    public synchronized int get_id()
    {
        return pointIds++;
    }

    public TraceData getTrace( Request source )
        throws NoSuchRequestId
    {
        if( source.originator >= pointIds )
        {
            System.out.println(">>>>>>>>>EXCEPTION!!! - getTrace()");

            throw new NoSuchRequestId();
        }

        System.out.println("getTrace for tracer: " + source.originator +
                           ", rid: " +  source.rid);


        Long key = new Long( source.rid );

        TraceTreeNode t = (TraceTreeNode) traces.get( key );

        if ( t == null )
        {
            return new TraceData( new TraceData[0], 0, "", 0, 0 );
        }


        TraceData result = new TraceData( new TraceData[t.subtraces.size()],
                                          t.tracer_id,
                                          t.operation,
                                          t.client_time,
                                          t.server_time );

        for( int i = 0; i < t.subtraces.size(); i++ )
        {
            Request r = (Request) t.subtraces.elementAt( i );
            result.subtrace[i] = getTrace( r );
        }

        return result;
    }

    public void logTraceAtPoint( Request origin,
                                 String operation,
                                 long client_time,
                                 long server_time)
        throws NoSuchRequestId
    {
        if( origin.originator >= pointIds )
        {
            System.out.println(">>>>>>>>>EXCEPTION!!! - logTraceAtPoint()");


            throw new NoSuchRequestId();
        }

        System.out.println("logTraceAtPoint for tracer: " +
                           origin.originator +
                           ", rid: " + origin.rid);


        Long key = new Long( origin.rid );

        TraceTreeNode t = (TraceTreeNode) traces.get( key );

        if( t == null )
        {
            t = new TraceTreeNode( origin.originator );
            traces.put( key, t );
        }

        t.operation = operation;
        t.client_time = client_time;
        t.server_time = server_time;
    }

    public void registerSubTrace(Request original, // original call
                                 Request nested) // nested call
        throws NoSuchRequestId
    {

        System.out.println("registerSubTrace for tracer: " +
                           original.originator +
                           ", rid: " + original.rid);

        if( original.originator >= pointIds ||
            nested.originator >= pointIds )
        {
            System.out.println(">>>>>>>>>EXCEPTION!!! - registerSubTrace()");
            throw new NoSuchRequestId();
        }

        Long key = new Long( original.rid );

        TraceTreeNode t = (TraceTreeNode) traces.get( key );

        if( t == null )
        {
            t = new TraceTreeNode( original.originator );
            traces.put( key, t );
        }

        t.subtraces.addElement( nested );
    }



    public static void main( String[] args )
    {
	org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args, null);
	try
	{
       	    org.omg.PortableServer.POA poa =
		org.omg.PortableServer.POAHelper.narrow(
                                                        orb.resolve_initial_references("RootPOA"));

	    poa.the_POAManager().activate();

	    org.omg.CORBA.Object o =
                poa.servant_to_reference(new TracingServiceImpl());

            NamingContextExt nc =
                NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));
            nc.bind( nc.to_name("tracing.service"), o);
	    poa.the_POAManager().activate();
	}
	catch ( Exception e )
	{
	    e.printStackTrace();
	}
	orb.run();
    }
}






