package org.jacorb.orb.connection;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2001  Gerald Brose.
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

import java.io.*;
import org.omg.GIOP.*;
import org.omg.IOP.ServiceContext;
import org.jacorb.orb.*;

/**
 * @author Gerald Brose, FU Berlin 1999
 * @version $Id$
 *
 */

public class ReplyOutputStream
    extends org.jacorb.orb.CDROutputStream
{
    private ServiceContext[] ctx; 
    private int request_id;
    private ReplyStatusType_1_2 reply_status = null;

    private int giop_minor = -1;
    /**
     * To be called only from derived classes.
     */
    ReplyOutputStream()
    {
        super();
    }

    public ReplyOutputStream ( ServiceContext[] service_context, 
                               int request_id,
                               ReplyStatusType_1_2 reply_status,
                               int giop_minor )
    {
        this( service_context, request_id, reply_status, giop_minor, false);
    }

    /**
     * The separate_header flag indicates whether the messages header
     * should be placed in a separate stream. A separate header means,
     * that this stream will contain another (separate)  stream for 
     * the header. <br>
     * Everything that is written on this stream is assumed to be data,
     * not header. If the stream has a separate header, be aware that calling
     * getInternalBuffer() will only return the data part.
     * If the full buffer is needed, use getBufferCopy(). This will copy
     * both buffers into a new one, thus yielding the messages complete buffer.<br>
     *
     * @see ReplyOutputStream#setServiceContexts( ServiceContext[] )
     */
    public ReplyOutputStream ( ServiceContext[] service_context, 
                               int request_id,
                               ReplyStatusType_1_2 reply_status,
                               int giop_minor,
                               boolean separate_header )
    {
        this.ctx = service_context;
        this.request_id = request_id;
        this.reply_status = reply_status;
        this.giop_minor = giop_minor;

        //tell CDR stream which version to use
        super.setGIOPMinor( giop_minor );
 
        if( separate_header )
            header_stream = new CDROutputStream();
        else
            writeHeader( this );

        System.out.println(">>>>>>>>>Created reply with GIOP 1." + 
                           giop_minor);

    }

    private void writeHeader( CDROutputStream out )
    {
        out.writeGIOPMsgHeader( MsgType_1_1._Reply,
                                giop_minor );

        switch( giop_minor )
        {
            case 0 :
            { 
                // GIOP 1.0 Reply == GIOP 1.1 Reply, fall through
            }
            case 1 :
            {
                //this currently doesn't work because GIOP.idl only
                //allows either ReplyStatusType_1_0 or
                //ReplyStatusType_1_2, but not both. The only solution
                //would be to go low-level and write directly to the
                //stream

                /*
                //GIOP 1.1
                ReplyHeader_1_0 repl_hdr = 
                    new ReplyHeader_1_0( ctx,
                                         request_id,
                                         ReplyStatusType_1_0.from_int( reply_status.value() ));

                ReplyHeader_1_0Helper.write( out, repl_hdr );
               
                break;
                */
            }
            case 2 :
            {
                //GIOP 1.2
                ReplyHeader_1_2 repl_hdr = 
                    new ReplyHeader_1_2( request_id,
                                         reply_status,
                                         ctx );

                ReplyHeader_1_2Helper.write( out, repl_hdr );

                break;
            }
            default :
            {
                throw new Error( "Unknown GIOP minor: " + giop_minor );
            }
        }
        
        out.markHeaderEnd( giop_minor == 2 ); //use padding if minor 2
    }

    public int requestId()
    {
        return request_id;
    }

    /**
     * a copy of the data part of the reply body, might be empty in 
     * case of a void return type or oneway operations
     */

    public byte[] getBodyBufferCopy()
    {
        byte[] buf = getInternalBuffer();
        byte[] result = null;
        if( header_stream != null )
        {
            result = new byte[ size() ];
            System.arraycopy( buf, 0, result, 0, result.length);
        }
        else
        {
            result = new byte[ size() - getHeaderEnd() ];
            System.arraycopy( buf, getHeaderEnd(), result, 0, result.length );
        }

        return result;
    }


    public ServiceContext[] getServiceContexts()
    {
        return ctx;
    }

    /**
     * This method sets the ServiceContexts for this message. That is, 
     * the header will be written to the header stream, and the header
     * will have a size % 8 == 0, for the data part gets aligned
     * in the right way. <br>
     * Therefor the last (that is length - 1) ServiceContext in the array
     * has to be a dummy context (id Integer.MAX_VALUE). <br>
     * The message size will be set when calling write_to().
     */

    public void setServiceContexts(ServiceContext[] context)
    {
        if (context[context.length - 1].context_id != Integer.MAX_VALUE)
            throw new Error("Last ServiceContext in array must be of type Integer.MAX_VALUE!");

        ctx = context;
    
        header_stream = new CDROutputStream();
        writeHeader( header_stream );
    
        //difference to next 8 byte border
        int difference = header_stream.size() % 8; 
        difference = (difference == 8)? 0 : difference;

        // This is a bit inefficent, but unfortunately, the service
        // contexts are written in the middle of the stream (not at
        // the end), so fixing the size directly would involve
        // meddling inside the buffer.

        if (difference > 0)
        {
            ctx[context.length -1].context_data = new byte[difference];
            header_stream.reset();
            writeHeader( header_stream );
        }
    } 
}






