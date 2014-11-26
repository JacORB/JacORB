package org.jacorb.orb.giop;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2014 Gerald Brose / The JacORB Team.
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
import org.jacorb.orb.SystemExceptionHelper;
import org.omg.CORBA.MARSHAL;
import org.omg.GIOP.LocateStatusType_1_2;
import org.omg.GIOP.MsgType_1_1;
import org.omg.GIOP.ReplyStatusType_1_0;
import org.omg.GIOP.ReplyStatusType_1_2;
import org.slf4j.Logger;

/**
 * @author Gerald Brose
 *
 */
public class ReplyOutputStream
    extends ServiceContextTransportingOutputStream
{
    private final boolean is_locate_reply;
    private final Logger logger;

    public ReplyOutputStream ( org.omg.CORBA.ORB orb,
                               int request_id,
                               ReplyStatusType_1_2 reply_status,
                               int giop_minor,
                               boolean is_locate_reply,
                               Logger logger)
    {
        super(orb);

        this.logger = logger;
        this.is_locate_reply = is_locate_reply;

        setGIOPMinor( giop_minor );

        writeGIOPMsgHeader( MsgType_1_1._Reply,
                            giop_minor );

        switch( giop_minor )
        {
            case 0 :
            {
                // GIOP 1.0 Reply == GIOP 1.1 Reply, fall through
            }
            case 1 :
            {
                //Technically, GIOP.idl only allows either
                //ReplyStatusType_1_0 or ReplyStatusType_1_2, but not
                //both. We go around this by compiling GIOP.idl two
                //times

                //GIOP 1.1
//                  ReplyHeader_1_0 repl_hdr =
//                      new ReplyHeader_1_0( alignment_ctx,
//                                           request_id,
//                                           ReplyStatusType_1_0.from_int( reply_status.value() ));
//                  ReplyHeader_1_0Helper.write( this, repl_hdr );

                // inlining for performance

                org.omg.IOP.ServiceContextListHelper.write(this , Messages.service_context );
                write_ulong( request_id );
                org.omg.GIOP.ReplyStatusType_1_0Helper.write( this,
                                                              ReplyStatusType_1_0.from_int( reply_status.value() ));

                break;
            }
            case 2 :
            {
                //GIOP 1.2
//                  ReplyHeader_1_2 repl_hdr =
//                      new ReplyHeader_1_2( request_id,
//                                           reply_status,
//                                           alignment_ctx );

//                  ReplyHeader_1_2Helper.write( this, repl_hdr );

                // more inlining

                write_ulong( request_id );
                org.omg.GIOP.ReplyStatusType_1_2Helper.write(this,
                                                             reply_status);


                org.omg.IOP.ServiceContextListHelper.write( this, Messages.service_context );

                markHeaderEnd(); //use padding if minor 2

                break;
            }
            default :
            {
                throw new MARSHAL( "Unknown GIOP minor: " + giop_minor );
            }
        }
    }

    public void write_to( GIOPConnection conn )
        throws IOException
    {
        if( is_locate_reply )
        {
            final ReplyInputStream in = new ReplyInputStream( orb, getBufferCopy() );
            try
            {
                final LocateReplyOutputStream out = getLocateReplyOutputStream(in);
                try
                {
                    out.write_to( conn );
                }
                finally
                {
                    out.close();
                }
            }
            finally
            {
                in.close();
            }
        }
        else
        {
            super.write_to( conn );
        }
    }

    private LocateReplyOutputStream getLocateReplyOutputStream(ReplyInputStream replyInputStream)
    {
        final LocateReplyOutputStream result;

        if( replyInputStream.getGIOPMinor() < 2 )
        {
            //GIOP 1.0 or 1.1
            switch( replyInputStream.rep_hdr.reply_status.value() )
            {
                case ReplyStatusType_1_2._NO_EXCEPTION :
                {
                    int status;

                    //_non_existent?
                    if (replyInputStream.read_boolean())
                    {
                        //_non_existent == true
                        status = LocateStatusType_1_2._UNKNOWN_OBJECT;
                    }
                    else
                    {
                        //_non_existent == false
                        status = LocateStatusType_1_2._OBJECT_HERE;
                    }

                    result = new LocateReplyOutputStream( orb,
                                                          replyInputStream.rep_hdr.request_id,
                                                          status,
                                                          replyInputStream.getGIOPMinor() );

                    break;
                }
                case ReplyStatusType_1_2._USER_EXCEPTION :
                {
                    //fall through
                }
                case ReplyStatusType_1_2._SYSTEM_EXCEPTION :
                {
                    //uh oh, can't reply with exception
                    if (logger.isErrorEnabled())
                    {
                        logger.error("Received an exception when processing a LocateRequest" );
                    }

                    // GIOP prior to 1.2 doesn't have the status
                    // LOC_SYSTEM_EXCEPTION, so we have to return
                    // OBJECT_UNKNOWN (even if it may not be unknown)
                    result =
                        new LocateReplyOutputStream( orb,
                                                     replyInputStream.rep_hdr.request_id,
                                                     LocateStatusType_1_2._UNKNOWN_OBJECT,
                                                     replyInputStream.getGIOPMinor() );
                    break;
                }
                case ReplyStatusType_1_2._LOCATION_FORWARD :
                {

                    result =
                        new LocateReplyOutputStream( orb,
                                                     replyInputStream.rep_hdr.request_id,
                                                     LocateStatusType_1_2._OBJECT_FORWARD,
                                                     replyInputStream.getGIOPMinor() );


                    //FIXME: it would be more efficient to copy
                    //the body part of this buffer to the new
                    //buffer
                    result.write_IOR( org.omg.IOP.IORHelper.read( replyInputStream ));

                    break;
                }
                default:
                    throw new IllegalArgumentException("ReplyStatus is invalid");
            }
        }
        else
        {
            //GIOP 1.2
            switch( replyInputStream.rep_hdr.reply_status.value() )
            {
                case ReplyStatusType_1_2._NO_EXCEPTION :
                {
                    int status;

                    //_non_existent?
                    if( replyInputStream.read_boolean() )
                    {
                        //_non_existent == true
                        status = LocateStatusType_1_2._UNKNOWN_OBJECT;
                    }
                    else
                    {
                        //_non_existent == false
                        status = LocateStatusType_1_2._OBJECT_HERE;
                    }

                    result = new LocateReplyOutputStream( orb,
                                                          replyInputStream.rep_hdr.request_id,
                                                          status,
                                                          replyInputStream.getGIOPMinor() );

                    break;
                }
                case ReplyStatusType_1_2._USER_EXCEPTION :
                {
                    //uh oh, can't reply with user exception
                    if (logger.isErrorEnabled())
                    {
                        logger.error("Received an exception when processing a LocateRequest - mapping to UNKNOWN system exception" );
                    }

                    result =
                    new LocateReplyOutputStream( orb,
                                                 replyInputStream.rep_hdr.request_id,
                                                 LocateStatusType_1_2._LOC_SYSTEM_EXCEPTION,
                                                 replyInputStream.getGIOPMinor() );

                    SystemExceptionHelper.write( result,
                                                 new org.omg.CORBA.UNKNOWN() );
                    break;
                }
                case ReplyStatusType_1_2._SYSTEM_EXCEPTION :
                {


                    result =
                    new LocateReplyOutputStream( orb,
                                                 replyInputStream.rep_hdr.request_id,
                                                 LocateStatusType_1_2._LOC_SYSTEM_EXCEPTION,
                                                 replyInputStream.getGIOPMinor() );

                    //FIXME: inefficient, use copying
                    SystemExceptionHelper.write( result,
                                                 SystemExceptionHelper.read( replyInputStream ));

                    break;
                }
                case ReplyStatusType_1_2._LOCATION_FORWARD :
                {

                    result =
                    new LocateReplyOutputStream( orb,
                                                 replyInputStream.rep_hdr.request_id,
                                                 LocateStatusType_1_2._OBJECT_FORWARD,
                                                 replyInputStream.getGIOPMinor() );


                    //FIXME: it would be more efficient to copy
                    //the body part of this buffer to the new
                    //buffer
                    result.write_IOR( org.omg.IOP.IORHelper.read( replyInputStream ));

                    break;
                }
                default:
                    throw new IllegalArgumentException("ReplyStatus is invalid");
            }
        }
        return result;
    }
}
