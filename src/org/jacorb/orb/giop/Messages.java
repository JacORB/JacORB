package org.jacorb.orb.connection;

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

    private static org.omg.IOP.ServiceContext[] service_context = 
	new org.omg.IOP.ServiceContext[0]; 

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
    private static int skipServiceContext(byte[] buf, 
                                          int offset, 
                                          int length,
                                          boolean little_endian ) 
    {

        int pos = offset;

        for( int i = 0; i < length; i++) 
        {
            // Skip the context ID long
            pos += 4;

            // Skip the octet sequence
            pos = skipSequence( buf, pos, 1, little_endian );
            
            //next is context id long.
            //it has to be aligned to 4 bytes            
            int diff = pos % 4;
            if( diff != 0 )
            {
                pos += ( 4 - diff );            
            }
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
    private static final int skipSequence( byte[] buf, 
                                           int offset, 
                                           int element_size,
                                           boolean little_endian ) 
    {
 
        int length = readULong( buf, offset, little_endian );

        return offset + 4 + length * element_size;
    }

    /** directly extract request ID from a buffer */

    public static int getRequestId( byte[] buf )
    {
        int msg_type = getMsgType( buf );
        int giop_minor = getGIOPMinor( buf );
        boolean little_endian = isLittleEndian( buf );

        int request_id = -1;

        if( giop_minor == 2 )
        {
            //GIOP 1.2
            
            if( msg_type == MsgType_1_1._Request || 
                msg_type == MsgType_1_1._LocateRequest ||  
                msg_type == MsgType_1_1._Reply || 
                msg_type == MsgType_1_1._LocateReply ||
                msg_type == MsgType_1_1._CancelRequest ||
                msg_type == MsgType_1_1._Fragment )
            {   
                //easy for GIOP 1.2, it's right after the message
                //header
                request_id = readULong( buf, MSG_HEADER_SIZE, little_endian );
            }
            else
            {
                throw new Error( "Messages of type " +
                                 msg_type + " don't have request ids" );
            }
        }
        else if( giop_minor == 0 || giop_minor == 1 )
        {
            if( msg_type == MsgType_1_1._Request || 
                msg_type == MsgType_1_1._Reply )
            {
                // service contexts are the first entry in the header
                
                //get the number of individual service contexts
                int service_ctx_length = readULong( buf, 
                                                    MSG_HEADER_SIZE, 
                                                    little_endian );

                if( service_ctx_length == 0 )
                {
                    //array of length 0, so request id folows the
                    //array length entry
                    
                    request_id = readULong( buf, 
                                            MSG_HEADER_SIZE + 4, 
                                            little_endian ); 
                }
                else
                {
                    //get the first index after the contexts array
                    int pos = skipServiceContext( buf, 
                                                  MSG_HEADER_SIZE + 4, // 4 bytes is ulong 
                                                  service_ctx_length, 
                                                  little_endian );

                    //the request id follows the body 
                    request_id = readULong( buf, pos, little_endian );
                }
            }
            else if( msg_type == MsgType_1_1._LocateRequest || 
                     msg_type == MsgType_1_1._LocateReply )
            {
                //easy, it's right after the message header
                request_id = readULong( buf, MSG_HEADER_SIZE, little_endian );
            }
            else
            {
                throw new Error( "Messages of type " +
                                 msg_type + " don't have request ids" );
            }
        } 
        
        return request_id;
    }

    public static final int getMsgSize( byte[] buf )
    {
        return readULong( buf, 8, isLittleEndian( buf ) );
    }   

    public static final int readULong( byte[] buf, 
                                       int pos, 
                                       boolean little_endian )
    {
	if( little_endian )
        {
	    return (( (buf[pos+3] & 0xff) << 24) +
		    ( (buf[pos+2] & 0xff) << 16) +
		    ( (buf[pos+1] & 0xff) <<  8) +
		    ( (buf[pos]   & 0xff) <<  0));
        }
	else //big endian
        {
	    return (( (buf[pos]   & 0xff) << 24) +
		    ( (buf[pos+1] & 0xff) << 16) +
		    ( (buf[pos+2] & 0xff) <<  8) +
		    ( (buf[pos+3] & 0xff) <<  0));
        }
    }    
    
    public static final boolean isLittleEndian( byte[] buf )
    {
        //this is new for GIOP 1.1/1.2
        return (0x01 & buf[6]) != 0;
    }

    public static final boolean moreFragmentsFollow( byte[] buf )
    {
        //this is new for GIOP 1.1/1.2
        return (0x02 & buf[6]) != 0;
    }
    
    public static final int getMsgType( byte[] buf )
    {
        return buf[7];
    }

    public static final int getGIOPMajor( byte[] buf )
    {
        return buf[4];
    }

    public static final int getGIOPMinor( byte[] buf )
    {
        return buf[5];
    }
    
    public static final boolean responseExpected( byte flags )
    {
        return flags == (byte)0x01 || flags == (byte)0x03;
    }
    
    public static final byte responseFlags( boolean response_expected )
    {
        return (byte) (response_expected ? 0x03 : 0x00);
    }
    
}






