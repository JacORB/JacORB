package jacorb.orb.connection;

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

import jacorb.orb.*;

/**
 * @author Gerald Brose, FU Berlin 1999
 * @version $Id$
 *
 */

public class ReplyOutputStream
    extends jacorb.orb.CDROutputStream
{
    private org.omg.GIOP.ReplyHeader_1_0 rep_hdr;

    /**
     * To be called only from derived classes.
     */
    ReplyOutputStream()
    {
    }

    public ReplyOutputStream ( org.omg.IOP.ServiceContext[] service_context, 
                               int request_id,
                               org.omg.GIOP.ReplyStatusType_1_0 reply_status )
    {
        this( service_context, request_id, reply_status, false);
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
     * @see setServiceContexts()
     */
    public ReplyOutputStream ( org.omg.IOP.ServiceContext[] service_context, 
                               int request_id,
                               org.omg.GIOP.ReplyStatusType_1_0 reply_status,
                               boolean separate_header )
    {
        rep_hdr = 
            new org.omg.GIOP.ReplyHeader_1_0(service_context,  request_id, reply_status);

        if (separate_header)
            header_stream = new CDROutputStream();
        else
            writeHeader(this);
    }

    private void writeHeader(CDROutputStream out)
    {
        out.writeGIOPMsgHeader( (byte)org.omg.GIOP.MsgType_1_0._Reply );
        org.omg.GIOP.ReplyHeader_1_0Helper.write(out, rep_hdr);
    }

    public int requestId()
    {
        return rep_hdr.request_id;
    }

    public org.omg.IOP.ServiceContext[] getServiceContexts()
    {
        return rep_hdr.service_context;
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

    public void setServiceContexts(org.omg.IOP.ServiceContext[] context)
    {
        if (context[context.length - 1].context_id != Integer.MAX_VALUE)
            throw new Error("Last ServiceContext in array must be of type Integer.MAX_VALUE!");

        rep_hdr.service_context = context;
    
        header_stream = new CDROutputStream();
        writeHeader(header_stream);
    
        int difference = header_stream.size() % 8; //difference to next 8 byte border
        difference = (difference == 8)? 0 : difference;

        // This is a bit inefficent, but unfortunately, the service contexts are written
        // in the middle of the stream (not at the end), so fixing the size directly
        // would involve meddling inside the buffer.    

        if (difference > 0)
        {
            rep_hdr.service_context[context.length -1].context_data = new byte[difference];
            header_stream.reset();
            writeHeader(header_stream);
        }
    } 
}






