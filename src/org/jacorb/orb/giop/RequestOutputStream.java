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
import org.jacorb.orb.*;

/**
 * @author Gerald Brose, FU Berlin 1999
 * @version $Id$
 *
 */

public class RequestOutputStream
    extends org.jacorb.orb.CDROutputStream
{
    private org.omg.GIOP.RequestHeader_1_0 req_hdr;

    /* if the msg size has been precomputed, the buffer size is exactly the msg size */
    private boolean exact_size = false;

    private org.jacorb.orb.dii.Request request = null;
  
    public RequestOutputStream(org.omg.CORBA.ORB orb,
                               int request_id,
                               String operation, 
                               boolean response_expected,
                               byte[] object_key,
                               org.omg.IOP.ServiceContext[] ctx){
        this( orb, request_id, operation, response_expected, object_key, ctx, false);
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

    public RequestOutputStream( org.omg.CORBA.ORB orb,
                                int request_id,
                                String operation, 
                                boolean response_expected,
                                byte[] object_key,
                                org.omg.IOP.ServiceContext[] ctx,
                                boolean  separate_header)
    {
        org.omg.CORBA.Principal principal = 
            new org.jacorb.orb.Principal( new byte[0] );

        req_hdr = 
            new org.omg.GIOP.RequestHeader_1_0( ctx,
                                                request_id,
                                                response_expected,
                                                object_key,
                                                operation,new byte[0] );

        if (separate_header)
            header_stream = new CDROutputStream();
        else
            writeHeader(this);
    }


    private void writeHeader(CDROutputStream out)
    {
        out.writeGIOPMsgHeader( (byte)org.omg.GIOP.MsgType_1_0._Request );
        org.omg.GIOP.RequestHeader_1_0Helper.write(out, req_hdr);
    }

    public int requestId()
    {
        return req_hdr.request_id;
    }

    public boolean response_expected()
    {
        return req_hdr.response_expected;
    }


    public String operation()
    {
        return req_hdr.operation;
    }

    public org.omg.IOP.ServiceContext[] getServiceContexts()
    {
        return req_hdr.service_context;
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

        req_hdr.service_context = context;
        writeHeader(header_stream);
    
        int difference = 8 - (header_stream.size() % 8); //difference to next 8 byte border
        difference = (difference == 8)? 0 : difference;

        //jacorb.util.Debug.output(2, "difference: " + difference);

        // This is a bit inefficent, but unfortunately, the service contexts are written
        // in the middle of the stream (not at the end), so fixing the size directly
        // would involve meddling inside the buffer.
        if (difference > 0)
        {
            req_hdr.service_context[context.length -1].context_data = new byte[difference];
            header_stream.reset();
            writeHeader(header_stream);
        }

        //jacorb.util.Debug.output(2, "Header size: " + header_stream.size());
        //jacorb.util.Debug.output(2, "Data size: " + size());
    }
  
    public void setRequest(org.jacorb.orb.dii.Request request)
    {
        this.request = request;
    }

    public org.jacorb.orb.dii.Request getRequest()
    {
        return request;
    }
}








