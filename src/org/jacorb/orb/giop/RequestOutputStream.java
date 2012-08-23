package org.jacorb.orb.giop;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.jacorb.orb.CDROutputStream;
import org.jacorb.orb.miop.MIOPProfile;
import org.jacorb.orb.miop.MulticastUtil;
import org.jacorb.util.Time;
import org.omg.CONV_FRAME.CodeSetContext;
import org.omg.CONV_FRAME.CodeSetContextHelper;
import org.omg.CORBA.MARSHAL;
import org.omg.CORBA.PrincipalHelper;
import org.omg.GIOP.MsgType_1_1;
import org.omg.GIOP.TargetAddress;
import org.omg.GIOP.TargetAddressHelper;
import org.omg.IOP.INVOCATION_POLICIES;
import org.omg.IOP.ServiceContext;
import org.omg.IOP.ServiceContextListHelper;
import org.omg.IOP.TAG_CODE_SETS;
import org.omg.IOP.TAG_UIPMC;
import org.omg.IOP.TaggedProfile;
import org.omg.MIOP.UIPMC_ProfileBodyHelper;
import org.omg.Messaging.PolicyValue;
import org.omg.Messaging.PolicyValueSeqHelper;
import org.omg.Messaging.REPLY_END_TIME_POLICY_TYPE;
import org.omg.Messaging.REQUEST_END_TIME_POLICY_TYPE;
import org.omg.Messaging.REQUEST_START_TIME_POLICY_TYPE;
import org.omg.Messaging.SYNC_NONE;
import org.omg.Messaging.SYNC_WITH_SERVER;
import org.omg.Messaging.SYNC_WITH_TARGET;
import org.omg.Messaging.SYNC_WITH_TRANSPORT;
import org.omg.TimeBase.UtcT;

/**
 * @author Gerald Brose, FU Berlin 1999
 */
public class RequestOutputStream
    extends ServiceContextTransportingOutputStream
{
    private final static byte[] principal = new byte[ 0 ];
    private final static byte[] reserved = new byte[ 3 ];

    private final int request_id;
    private final boolean response_expected;
    private final short syncScope;
    private final String operation;

    /**
     * Absolute time after which this request may be delivered to its target.
     * (CORBA 3.0, 22.2.4.1)
     */
    private final UtcT requestStartTime;

    /**
     * Absolute time after which this request may no longer be delivered
     * to its target. (CORBA 3.0, 22.2.4.2/5)
     */
    private final UtcT requestEndTime;

    /**
     * Absolute time after which a reply may no longer be obtained
     * or returned to the client. (CORBA 3.0, 22.2.4.4/6)
     */
    private final UtcT replyEndTime;

    private org.jacorb.orb.dii.Request request = null;

    private final ClientConnection connection;

    private final byte[] object_key;

    /**
     * Copy constructor for <code>RequestOutputStream</code> used for SYNC_SCOPE NONE.
     *
     * @param other a <code>RequestOutputStream</code> value
     */
    public RequestOutputStream( RequestOutputStream other)
    {
        super(other.orb);

        this.object_key = other.object_key;
        this.request_id = other.request_id;
        this.response_expected = other.response_expected;
        this.syncScope = other.syncScope;
        this.operation = other.operation;
        this.connection = other.connection;
        this.requestStartTime = other.requestStartTime;
        this.requestEndTime   = other.requestEndTime;
        this.replyEndTime     = other.replyEndTime;
        this.contexts = other.contexts;
        this.header_end = other.header_end;
        this.header_padding = other.header_padding;
        this.giop_minor = other.giop_minor;

        byte[] copy = other.getBufferCopy();
        write_octet_array (copy, 0, copy.length);
    }

    public RequestOutputStream( org.jacorb.orb.ORB orb,
                                ClientConnection connection,
                                int request_id,
                                String operation,
                                boolean response_expected,
                                short syncScope,
                                UtcT requestStartTime,
                                UtcT requestEndTime,
                                UtcT replyEndTime,
                                byte[] object_key, int giop_minor )
    {
        super(orb);

        setGIOPMinor( giop_minor );

        this.object_key = object_key;
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

        boolean isMIOP = (connection != null && connection.getRegisteredProfile () instanceof MIOPProfile);

        switch( giop_minor )
        {
            case 0 :
            case 1 :
            {
                //GIOP 1.0/1.1 inlined
                ServiceContextListHelper.write( this , Messages.service_context );
                write_ulong( request_id);
                write_boolean( response_expected );

                if (giop_minor == 1)
                {
                    write_octet_array( reserved,0,3 );
                }

                if (isMIOP)
                {
                    write_char_array (MulticastUtil.MAGIC, 0, MulticastUtil.MAGIC.length);
                    write_long (TAG_UIPMC.value);
                    this.beginEncapsulation ();
                    UIPMC_ProfileBodyHelper.write (this, ((MIOPProfile)connection.getRegisteredProfile ()).getUIPMCProfile ());
                    this.endEncapsulation ();
                }
                else
                {
                    write_long( object_key.length );
                    write_octet_array( object_key, 0, object_key.length);
                }

                write_string( operation);
                PrincipalHelper.write( this, principal);

                break;
            }
            case 2 :
            {
                //GIOP 1.2
                TargetAddress addr = new TargetAddress();

                if (isMIOP)
                {
                    TaggedProfile uipmc = new TaggedProfile (org.omg.IOP.TAG_UIPMC.value, MulticastUtil.getEncapsulatedUIPMCProfile (orb, connection));
                    addr.profile (uipmc);
                }
                else
                {
                    addr.object_key( object_key );
                }

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
                        case SYNC_NONE.value:
                            // fallthrough
                        case SYNC_WITH_TRANSPORT.value:
                        {
                            write_octet ((byte)0x00);
                            break;
                        }
                        case SYNC_WITH_SERVER.value:
                        {
                            write_octet ((byte)0x01);
                            break;
                        }
                        case SYNC_WITH_TARGET.value:
                        {
                            write_octet ((byte)0x03);
                            break;
                        }
                        default:
                        {
                            throw new MARSHAL ("Invalid SYNC_SCOPE: " + syncScope);
                        }
                    }
                }

                write_octet_array( reserved,0,3 );
                TargetAddressHelper.write( this, addr );
                write_string( operation );
                ServiceContextListHelper.write( this, Messages.service_context );

                markHeaderEnd(); //use padding if GIOP minor == 2

                break;
            }
            default :
            {
                throw new MARSHAL( "Unknown GIOP minor: " + giop_minor );
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
     * Overridden to add a codeset service context if this
     * is the first request on the connection.
     */
    public void write_to(GIOPConnection conn) throws IOException
    {
        if (!conn.isTCSNegotiated())
        {
            // encapsulate context
            addServiceContext (createCodesetContext ( conn.getTCS(), conn.getTCSW()));
            conn.markTCSNegotiated();
        }
        super.write_to(conn);
    }

    private ServiceContext createCodesetContext( CodeSet tcs, CodeSet tcsw )
    {
        // encapsulate context
        final CDROutputStream os = new CDROutputStream( orb );
        try
        {
            os.beginEncapsulatedArray();
            CodeSetContextHelper.write( os, new CodeSetContext( tcs.getId(), tcsw.getId() ));

            return new ServiceContext( TAG_CODE_SETS.value, os.getBufferCopy() );
        }
        finally
        {
            os.close();
        }
    }

    /**
     * Returns the timing policies for this request as an array
     * of PolicyValues that can be propagated in a ServiceContext.
     */
    private PolicyValue[] getTimingPolicyValues()
    {
        List list = new ArrayList();
        if (requestStartTime != null)
        {
            list.add (new PolicyValue (REQUEST_START_TIME_POLICY_TYPE.value,
                                    Time.toCDR (requestStartTime)));
        }
        if (requestEndTime != null)
        {
            list.add (new PolicyValue (REQUEST_END_TIME_POLICY_TYPE.value,
                                    Time.toCDR (requestEndTime)));
        }
        if (replyEndTime != null)
        {
            list.add (new PolicyValue (REPLY_END_TIME_POLICY_TYPE.value,
                                    Time.toCDR (replyEndTime)));
        }
        return (PolicyValue[])list.toArray (new PolicyValue[list.size()]);
    }

    private ServiceContext createInvocationPolicies()
    {
        final CDROutputStream out = new CDROutputStream( orb );

        try
        {
            out.beginEncapsulatedArray();
            PolicyValueSeqHelper.write(out, getTimingPolicyValues());
            return new ServiceContext (INVOCATION_POLICIES.value,
                    out.getBufferCopy());
        }
        finally
        {
            out.close();
        }
    }
}
