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

public class Messages 
{
    /** GIOP message header size constant */
    public static int MSG_HEADER_SIZE = 12;

    private static org.omg.IOP.ServiceContext service_context[] = 
	new org.omg.IOP.ServiceContext[0]; 

    /** 
     * @returns a buffer containing a locate reply message ready for writing
     * called through Connection by servers only.
     */

    public static byte [] locateReplyMessage( int request_id, 
                                              int status, 
                                              org.omg.CORBA.Object arg, 
                                              ServerConnection conn) 
	throws org.omg.CORBA.COMM_FAILURE
    {
	try 
	{	
	    LocateReplyOutputStream lr_out = 
                new LocateReplyOutputStream(conn,request_id,status,arg);
	    lr_out.close();
	    return lr_out.getBufferCopy();
	} 
	catch ( Exception e )
	{
	    org.jacorb.util.Debug.output(2,e);
	    throw new org.omg.CORBA.COMM_FAILURE("Error marshalling GIOP reply");
	}
    }

    public static byte [] closeConnectionMessage() 
    {
	byte [] buffer = new byte[MSG_HEADER_SIZE];
	buffer[0] = (byte)'G';
	buffer[1] = (byte)'I';
	buffer[2] = (byte)'O';
	buffer[3] = (byte)'P';
	buffer[4] = (byte)((char)1);
	buffer[5] = (byte)((char)0);
	buffer[6] = 0;
	buffer[7] = (byte)org.omg.GIOP.MsgType_1_0._CloseConnection;
	return buffer;
    }

    /**
     * Skips over a number of service contexts in a GIOP reply message.
     *
     * @param buf the array of octets containing the service context
     * @param offset the index into the buffer at which the service context
     * starts. This should be after the length of the service context.
     * @param length the number of service contexts
     * @param endianness 0 for big-endian, 1 for little-endian
     *
     * @return the index of the octet following the service context
     */
    private static int skipServiceContext(byte[] buf, int offset, int length,
                                          byte endianness) {

        int pos = offset;
        for(int i = 0; i < length; ++i) {
            // Skip the context ID long
            pos += 4;
            // Skip the octet sequence
            pos = skipSequence(buf, pos, 1, endianness);
            
            //next is context id long.
            //it has to be aligned to 4 bytes
            
            int diff = pos % 4;
            if ( diff != 0 )
                pos += ( 4 - diff );            
        }

        return pos;
    }

    /**
     * Skips over a sequence in a GIOP message.
     *
     * @param buf the array of octets containing the sequence
     * @param offset the index into the buffer at which the sequence starts.
     * This should be where the length information begins.
     * @param elementSize the number of octets per element in the sequence
     * @param endianness 0 for big-endian, 1 for little-endian
     *
     * @return the index of the octet following the sequence
     */     
    private static int skipSequence(byte[] buf, int offset, int elementSize,
                                    byte endianness) {
 
        int length;

        if(endianness == 0) 
        { 
            // Big-endian
            length = ((buf[offset]& 0xff) << 24) + 
                ((buf[offset+1]& 0xff) << 16) + 
                ((buf[offset+2]& 0xff)<< 8) + 
                ((buf[offset+3]& 0xff));
        } 
        else 
        { 
            // Little-endian
            length = ((buf[offset+3] & 0xff) << 24) + 
                ((buf[offset+2]& 0xff) << 16) + 
                ((buf[offset+1]& 0xff)<< 8) + 
                ((buf[offset]& 0xff));
        }

//  	jacorb.util.Debug.output(3, "Skipping a sequence of " + 
//                                   length + " elements of size " + 
//                                   elementSize);

        return offset + 4 + length*elementSize;
    }

    /** directly extract request ID from a reply or locate reply buffer */

    public static int getRequestId( byte[] buf, int msg_type )
    {
	if( msg_type == org.omg.GIOP.MsgType_1_0._Reply )
	{
	    if( buf[6] == 0 ) //big endian
	    {
		int service_ctx_length = ((buf[12]& 0xff) << 24) + (buf[13] << 16) + (buf[14]<< 8) + (buf[15]);
                int pos = 16;
		if( service_ctx_length != 0 ) 
                {
//                      try {
//                          Diagnosis.dumpBytes(buf, 16, 32);
//                      } catch(Exception e) {}
		    //                    org.jacorb.util.Debug.output(3, "Skipping " + service_ctx_length + " octet sequences of service context...");
                    pos = skipServiceContext(buf, 16, service_ctx_length, buf[6]);
                }
                return ((buf[pos]& 0xff) << 24) + 
                    ((buf[pos+1]& 0xff) << 16) + 
                    ((buf[pos+2]& 0xff)<< 8) + 
                    ((buf[pos+3]& 0xff));
	    }
	    else
	    {
                int service_ctx_length = 
                    ((buf[15]& 0xff) << 24) + 
                    (buf[14] << 16) + 
                    (buf[13]<< 8) + 
                    (buf[12]);

                int pos = 16;
                if( service_ctx_length != 0 ) 
                {
                    pos = skipServiceContext( buf, 16, service_ctx_length, buf[6] );
                }
                return ((buf[pos+3] & 0xff) << 24) + 
		    ((buf[pos+2]& 0xff) << 16) + 
		    ((buf[pos+1]& 0xff)<< 8) + 
		    ((buf[pos]& 0xff));
	    }
	}
	else if( msg_type == org.omg.GIOP.MsgType_1_0._LocateReply )
	{
	    if( buf[6] == 0 ) //big endian
	    {
		return ((buf[15]& 0xff) << 24) + 
		    ((buf[14]& 0xff) << 16) + 
		    ((buf[13]& 0xff)<< 8) + 
		    ((buf[12]& 0xff));
	    }
	    else
	    {
		return ((buf[15] & 0xff) << 24) + 
		    ((buf[14]& 0xff) << 16) + 
		    ((buf[13]& 0xff)<< 8) + 
		    ((buf[12]& 0xff));		
	    }	    
	}
	else
	    throw new RuntimeException("Cannot deal with reply message type " + msg_type + " !");
    }
}

