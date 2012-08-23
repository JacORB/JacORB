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

import org.jacorb.orb.ORB;
import org.jacorb.orb.miop.MulticastUtil;
import org.jacorb.orb.miop.ServerMIOPConnection;
import org.omg.CORBA.MARSHAL;
import org.omg.GIOP.LocateRequestHeader_1_0;
import org.omg.GIOP.LocateRequestHeader_1_0Helper;
import org.omg.GIOP.LocateRequestHeader_1_2;
import org.omg.GIOP.LocateRequestHeader_1_2Helper;
import org.omg.GIOP.MsgType_1_1;
import org.omg.GIOP.RequestHeader_1_2;
import org.omg.GIOP.RequestHeader_1_2Helper;
import org.omg.GIOP.TargetAddress;
import org.omg.IOP.ServiceContext;
import org.omg.IOP.TAG_UIPMC;
import org.omg.IOP.TaggedProfile;


/**
 * @author Gerald Brose, FU Berlin
 */
public class RequestInputStream
    extends ServiceContextTransportingInputStream
{
    private static final byte[] reserved = new byte[3];

    private final boolean is_locate_request;

    public final RequestHeader_1_2 req_hdr;

    public RequestInputStream( org.omg.CORBA.ORB orb, GIOPConnection connection, byte[] buf )
    {
        super( orb,  buf );

        boolean isMIOP = (connection != null && connection.getTransport () instanceof ServerMIOPConnection);

        if( Messages.getMsgType( buffer ) == MsgType_1_1._Request )
        {
            switch( giop_minor )
            {
                case 0:
                case 1:
                {
                   TargetAddress addr = new TargetAddress();
                   req_hdr = new org.omg.GIOP.RequestHeader_1_2();
                   req_hdr.service_context = org.omg.IOP.ServiceContextListHelper.read(this);
                   req_hdr.request_id=read_ulong();
                   req_hdr.response_flags=Messages.responseFlags(read_boolean());

                   if (giop_minor == 1)
                   {
                      req_hdr.reserved = new byte[3];
                      read_octet_array(req_hdr.reserved,0,3);
                   }

                   if (isMIOP)
                   {
                      // Manually read the MIOP magic bytes and the UIPMC Profile.
                      char marker[] = new char[4];
                      read_char_array (marker, 0, 4);
                      if ( ! MulticastUtil.matchMIOPMagic (marker))
                      {
                         throw new MARSHAL("MIOP magic marker does not match");
                      }

                      // Check the tag is correct.
                      int tag = read_ulong ();
                      if (tag != TAG_UIPMC.value)
                      {
                         throw new MARSHAL ("TAG_UIPMC marker does not match (" + tag + ')');
                      }

                      openEncapsulation ();
                      org.omg.MIOP.UIPMC_ProfileBody upb = org.omg.MIOP.UIPMC_ProfileBodyHelper.read (this);
                      closeEncapsulation();

                      TaggedProfile uipmc = new TaggedProfile
                         (org.omg.IOP.TAG_UIPMC.value, MulticastUtil.getEncapsulatedUIPMCProfile ((ORB)orb, upb));
                      addr.profile (uipmc);
                   }
                   else
                   {
                      int l = read_long();
                      int x = available();
                      if ( x > 0 && l > x )
                      {
                         throw new MARSHAL("Sequence length too large. Only " + x + " available and trying to assign " + l);
                      }

                      byte object_key[] = new byte[l];
                      read_octet_array (object_key,0,l);
                      addr.object_key (object_key);
                   }
                   req_hdr.operation=read_string();
                   org.omg.CORBA.PrincipalHelper.read(this);
                   req_hdr.target = addr;

                   break;
                }
                case 2 :
                {
                    //GIOP 1.2
                    req_hdr = RequestHeader_1_2Helper.read( this );

                    skipHeaderPadding();

                    break;
                }
                default : {
                    throw new MARSHAL( "Unknown GIOP minor version: " + giop_minor );
                }
            }

            is_locate_request = false;
        }
        else if( Messages.getMsgType( buffer ) == MsgType_1_1._LocateRequest )
        {
            switch( giop_minor )
            {
                case 0 :
                {
                    //GIOP 1.0 = GIOP 1.1, fall through
                }
                case 1 :
                {
                    //GIOP 1.1
                    LocateRequestHeader_1_0 locate_req_hdr =
                        LocateRequestHeader_1_0Helper.read( this );

                    TargetAddress addr = new TargetAddress();
                    addr.object_key( locate_req_hdr.object_key );

                    req_hdr =
                        new RequestHeader_1_2( locate_req_hdr.request_id,
                                               (byte) 0x03,//response_expected
                                               reserved,
                                               addr,
                                               "_non_existent",
                                               Messages.service_context );
                    break;
                }
                case 2 :
                {
                    //GIOP 1.2
                    LocateRequestHeader_1_2 locate_req_hdr =
                        LocateRequestHeader_1_2Helper.read( this );

                    req_hdr =
                        new RequestHeader_1_2( locate_req_hdr.request_id,
                                               (byte) 0x03,//response_expected
                                               reserved,
                                               locate_req_hdr.target,
                                               "_non_existent",
                                               Messages.service_context );
                    break;
                }
                default :
                {
                    throw new MARSHAL( "Unknown GIOP minor version: " + giop_minor );
                }
            }

            is_locate_request = true;
        }
        else
        {
            throw new MARSHAL( "Error: not a request!" );
        }
    }

    public ServiceContext getServiceContext( int id )
    {
        for( int i = 0; i < req_hdr.service_context.length; i++ )
        {
            if( req_hdr.service_context[i].context_id == id )
            {
                return req_hdr.service_context[i];
            }
        }

        return null;
    }

    public boolean isLocateRequest()
    {
        return is_locate_request;
    }
}
