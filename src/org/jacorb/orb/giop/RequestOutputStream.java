package org.jacorb.orb.giop;

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

import java.util.*;

import org.omg.GIOP.*;
import org.omg.IOP.*;
import org.omg.Messaging.*;
import org.omg.TimeBase.*;

import org.jacorb.orb.*;
import org.jacorb.util.*;

/**
 * @author Gerald Brose, FU Berlin 1999
 * @version $Id$
 *
 */

public class RequestOutputStream
    extends ServiceContextTransportingOutputStream
{
    private static byte[] principal = new byte[ 0 ];
    private static byte[] reserved = new byte[ 3 ];

    private int request_id = -1;
    private boolean response_expected = true;
    private short syncScope = org.omg.Messaging.SYNC_WITH_SERVER.value;
    private String operation = null;

    /**
     * Absolute time after which this request may be delivered to its target.
     * (CORBA 3.0, 22.2.4.1)
     */
    private UtcT requestStartTime = null;

    /**
     * Absolute time after which this request may no longer be delivered
     * to its target. (CORBA 3.0, 22.2.4.2/5)
     */
    private UtcT requestEndTime   = null;

    /**
     * Absolute time after which a reply may no longer be obtained
     * or returned to the client. (CORBA 3.0, 22.2.4.4/6)
     */
    private UtcT replyEndTime     = null;

    private org.jacorb.orb.dii.Request request = null;

    private ClientConnection connection = null;

    public RequestOutputStream( ClientConnection connection,
                                int request_id,
                                String operation,
                                boolean response_expected,
                                short syncScope,
                                UtcT requestStartTime,
                                UtcT requestEndTime,
                                UtcT replyEndTime,
                                byte[] object_key,
                                int giop_minor )
    {
        super();

        setGIOPMinor( giop_minor );

        this.request_id = request_id;
        this.response_expected = response_expected;
        this.syncScope = syncScope;
        this.operation = operation;
        this.connection = connection;

        this.requestStartTime = requestStartTime;
        this.requestEndTime   = requestEndTime;
        this.replyEndTime     = replyEndTime;

        if (requestStartTime != null ||
            requestEndTime != null ||
            replyEndTime != null)
        {
            addServiceContext (createInvocationPolicies());
        }

        writeGIOPMsgHeader( MsgType_1_1._Request,
                            giop_minor );

        switch( giop_minor )
        {
            case 0 :
            {
                // GIOP 1.0 inlining
		org.omg.IOP.ServiceContextListHelper.write( this , service_context );
		write_ulong( request_id);
		write_boolean( response_expected );
		write_long( object_key.length );
		write_octet_array( object_key, 0, object_key.length);
		write_string( operation);
		org.omg.CORBA.PrincipalHelper.write( this,
                                                     principal);

                break;
            }
            case 1 :
            {
                //GIOP 1.1
		org.omg.IOP.ServiceContextListHelper.write( this , service_context );
		write_ulong( request_id);
		write_boolean( response_expected );
		write_long( object_key.length );
		write_octet_array( object_key, 0, object_key.length);
		write_string( operation);
		org.omg.CORBA.PrincipalHelper.write( this,
                                                     principal);

                break;
            }
            case 2 :
            {
                //GIOP 1.2
                TargetAddress addr = new TargetAddress();
                addr.object_key( object_key );

                // inlined RequestHeader_1_2Helper.write method

                write_ulong( request_id);
                if (response_expected)
                {
                	write_octet ((byte)0x03);
                }
                else
                {
                	switch (syncScope)
                	{
                		case org.omg.Messaging.SYNC_NONE.value:
                		case org.omg.Messaging.SYNC_WITH_TRANSPORT.value:
                			write_octet ((byte)0x00);
                			break;
                		case org.omg.Messaging.SYNC_WITH_SERVER.value:
                			write_octet ((byte)0x01);
                			break;
                		case org.omg.Messaging.SYNC_WITH_TARGET.value:
                			write_octet ((byte)0x03);
                			break;
                		default:
                			throw new org.omg.CORBA.MARSHAL ("Invalid SYNC_SCOPE: " + syncScope);
                	}
                }

		write_octet_array( reserved,0,3 );
		org.omg.GIOP.TargetAddressHelper.write( this, addr );
		write_string( operation );
		org.omg.IOP.ServiceContextListHelper.write( this, service_context );

                markHeaderEnd(); //use padding if GIOP minor == 2

                break;
            }
            default :
            {
                throw new Error( "Unknown GIOP minor: " + giop_minor );
            }
        }
    }

    public int requestId()
    {
        return request_id;
    }

    public boolean response_expected()
    {
        return response_expected;
    }

	public short syncScope()
	{
		return syncScope;
	}

    public String operation()
    {
        return operation;
    }

    public UtcT getReplyEndTime()
    {
        return replyEndTime;
    }

    public void setRequest(org.jacorb.orb.dii.Request request)
    {
        this.request = request;
    }

    public org.jacorb.orb.dii.Request getRequest()
    {
        return request;
    }

    public ClientConnection getConnection()
    {
        return connection;
    }

    /**
     * Returns the timing policies for this request as an array
     * of PolicyValues that can be propagated in a ServiceContext.
     */
    private org.omg.Messaging.PolicyValue[] getTimingPolicyValues()
    {
        List l = new ArrayList();
        if (requestStartTime != null)
            l.add (new PolicyValue (REQUEST_START_TIME_POLICY_TYPE.value,
                                    Time.toCDR (requestStartTime)));
        if (requestEndTime != null)
            l.add (new PolicyValue (REQUEST_END_TIME_POLICY_TYPE.value,
                                    Time.toCDR (requestEndTime)));
        if (replyEndTime != null)
            l.add (new PolicyValue (REPLY_END_TIME_POLICY_TYPE.value,
                                    Time.toCDR (replyEndTime)));
        return (PolicyValue[])l.toArray (new PolicyValue[0]);
    }

    private ServiceContext createInvocationPolicies()
    {
        CDROutputStream out = new CDROutputStream();
        out.beginEncapsulatedArray();
        PolicyValueSeqHelper.write(out, getTimingPolicyValues());
        return new ServiceContext (org.omg.IOP.INVOCATION_POLICIES.value,
                                   out.getBufferCopy());
    }


}
