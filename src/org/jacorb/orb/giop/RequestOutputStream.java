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

public class RequestOutputStream
    extends CDROutputStream
{
    private static byte[] principal = new byte[ 0 ];
    private static byte[] reserved = new byte[ 3 ];

    private int giop_minor = -1; //determines the class of the req_hdr

    private int request_id = -1;
    private String operation = null;
    private boolean response_expected = true;
    private byte[] object_key = null;
    private org.omg.IOP.ServiceContext[] ctx = null;

    /* if the msg size has been precomputed, the buffer size is exactly the msg size */
    //private boolean exact_size = false;

    private org.jacorb.orb.dii.Request request = null;
  
    public RequestOutputStream(org.omg.CORBA.ORB orb,
                               int request_id,
                               String operation, 
                               boolean response_expected,
                               byte[] object_key,
                               ServiceContext[] ctx,
                               int giop_minor )
    {
        this( orb, 
              request_id, 
              operation, 
              response_expected, 
              object_key, 
              ctx, 
              giop_minor, 
              false); //no separate header
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
     * @see RequestOutputStream#setServiceContexts(ServiceContext[])
     */

    public RequestOutputStream( org.omg.CORBA.ORB orb,
                                int request_id,
                                String operation, 
                                boolean response_expected,
                                byte[] object_key,
                                ServiceContext[] ctx,
                                int giop_minor,
                                boolean  separate_header)
    {
        super( orb );

        this.giop_minor = giop_minor;

        //tell CDR stream which version to use
        super.setGIOPMinor( giop_minor );

        this.request_id = request_id;
        this.operation = operation;
        this.response_expected = response_expected;        
        this.object_key = object_key;
        this.ctx = ctx;

        if( separate_header )
        {
            header_stream = new CDROutputStream();
        }
        else
        {
            writeHeader( this );
        }
        
        System.out.println(">>>>>>>>>Created request for op " + 
                           operation + 
                           " with GIOP 1." + 
                           giop_minor);
        
    }


    private void writeHeader( CDROutputStream out )
    {
        out.writeGIOPMsgHeader( MsgType_1_1._Request,
                                giop_minor );

        switch( giop_minor )
        {
            case 0 :
            { 
                // GIOP 1.0
                RequestHeader_1_0 req_hdr = 
                    new RequestHeader_1_0( ctx,
                                           request_id,
                                           response_expected,
                                           object_key,
                                           operation,
                                           principal );

                RequestHeader_1_0Helper.write( out, req_hdr );
                break;
            }
            case 1 :
            {
                //GIOP 1.1
                RequestHeader_1_1 req_hdr = 
                    new RequestHeader_1_1( ctx,
                                           request_id,
                                           response_expected,
                                           reserved,
                                           object_key,
                                           operation,
                                           principal );

                RequestHeader_1_1Helper.write( out, req_hdr );
               
                break;
            }
            case 2 :
            {
                //GIOP 1.2
                TargetAddress addr = new TargetAddress();
                addr.object_key( object_key );

                RequestHeader_1_2 req_hdr = 
                    new RequestHeader_1_2( request_id,
                                           (byte) ((response_expected)? 0x03 : 0x00),
                                           reserved,
                                           addr,
                                           operation,
                                           ctx );

                RequestHeader_1_2Helper.write( out, req_hdr );

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

    public boolean response_expected()
    {
        return response_expected;
    }


    public String operation()
    {
        return operation;
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
        //TODO: optimize for GIOP1.2
        if (context[context.length - 1].context_id != Integer.MAX_VALUE)
            throw new Error("Last ServiceContext in array must be of type Integer.MAX_VALUE!");

        ctx = context;
        writeHeader( header_stream );
    
        //difference to next 8 byte border
        int difference = 8 - (header_stream.size() % 8); 
        difference = (difference == 8)? 0 : difference;

        //jacorb.util.Debug.output(2, "difference: " + difference);

        // This is a bit inefficent, but unfortunately, the service
        // contexts are written in the middle of the stream (not at
        // the end), so fixing the size directly would involve
        // meddling inside the buffer.
        if (difference > 0)
        {
            ctx[context.length -1].context_data = new byte[difference];
            header_stream.reset();
            writeHeader(header_stream);
        }

        //org.jacorb.util.Debug.output(2, "Header size: " + header_stream.size());
        //org.jacorb.util.Debug.output(2, "Data size: " + size());
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






