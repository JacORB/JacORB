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

package org.jacorb.orb.connection;

import java.io.*;
import java.util.Vector;

import org.omg.GIOP.*;
import org.omg.IOP.*;

import org.jacorb.orb.CDROutputStream;
import org.jacorb.util.Debug;

/**
 * GIOPOutputStream.java
 *
 *
 * Created: Sat Aug 18 12:12:22 2001
 *
 * @author Nicolas Noffke
 * @version $Id$ 
 */

public class GIOPOutputStream 
    extends CDROutputStream 
{
    //The purpose of this array is to align the data following this
    //array on an 8 byte boundary.  This allows for adding service
    //contexts to the header without having to remarshal everything
    //that follows. There are already 12 bytes on the stream (the GIOP
    //message header), so the next possible 8 byte boundary is
    //16. This is reached by adding an array of length 0, because that
    //will only write a ulong for the length (0), which consumes 4
    //bytes.
    //
    // This is only necessary for GIOP 1.0/1.1, because in 1.2, the
    // service_context array is the last attribute of the
    // [Request|Reply]Header, and the body is per spec aligned to an 8
    // byte boundary.
    protected static ServiceContext[] alignment_ctx = new ServiceContext[0];

    //The end of the GIOP message header. Only valid if 
    //header_padding != 0
    private int header_end = -1;
    
    //no. of bytes used for padding between header and body
    private int header_padding = 0;

    //If ServiceContexts are actually added, this will be the last
    //contexts, and the context_data is used to fill up to the next 8
    //byte boundary.
    private static ServiceContext padding_ctx = new ServiceContext( 0x4A4143FF,
                                                                    new byte[0] );

    private Vector contexts = null;


    public GIOPOutputStream()
    {
        super( (org.omg.CORBA.ORB) null );
    }

    /**
     * Writes a GIOPMessageHeader of the required type to the beginning of
     * the buffer and sets the start position and index.
     */

    public void writeGIOPMsgHeader( int message_type,
                                    int minor_version )
    {
        byte[] buffer = getInternalBuffer();

        //attribute: magic (4 bytes)
        buffer[0] = (byte) 'G';
        buffer[1] = (byte) 'I';
        buffer[2] = (byte) 'O';
        buffer[3] = (byte) 'P';

        //version
        buffer[4] = 1; //GIOP major
        buffer[5] = (byte) minor_version; //GIOP minor

        //endianess in GIOP 1.0, flags in GIOP 1.1/1.2. Always use big
        //endian. 
        //For 1.1/1.2: 2nd LSB is 1 for fragments, but this
        //isn't supported (yet?) by JacORB. 6 MSBs must stay 0
        buffer[6] = 0;
        
        buffer[7] = (byte) message_type;

        // Skip the header + leave 4 bytes for message size
        skip( Messages.MSG_HEADER_SIZE );
    }

    /**
     * GIOP 1.2 requires the message body to start on an 8 byte
     * border, while 1.0/1.1 does not. Additionally, this padding shall
     * only be performed, if the body is not empty (which we don't
     * know at this stage.  
     */
    protected void markHeaderEnd()
    {
        header_end = size();

        header_padding = 8 - (size() % 8); //difference to next 8 byte border
        header_padding = (header_padding == 8)? 0 : header_padding;
        
        skip( header_padding );
    }
    
    public int getHeaderEnd()
    {
        return header_end;
    }
    
    public int getBodyBegin()
    {
        return header_end + header_padding;
    }

    public int getHeaderPadding()
    {
        return header_padding;
    }
    
    public boolean hasBody()
    {
        return size() > getBodyBegin();
    }

    private void insertMsgSize( int size )
    {
        byte[] buffer = getInternalBuffer();
        
        //using big endian byte ordering
        buffer[8]  = (byte)((size >> 24) & 0xFF);
        buffer[9]  = (byte)((size >> 16) & 0xFF);
        buffer[10] = (byte)((size >>  8) & 0xFF);
        buffer[11] = (byte) (size        & 0xFF);
    }

    private void insertMsgSize()
    {
        if( header_padding == 0 )
        {
            insertMsgSize( size() - Messages.MSG_HEADER_SIZE );
        }
        else
        {
            if( size() >  header_end + header_padding)
            {
                //has a body, so include padding (by not removing it :-)
                insertMsgSize( size() - Messages.MSG_HEADER_SIZE );
            }
            else
            {
                //no body written, so remove padding
                insertMsgSize( size() - header_padding - Messages.MSG_HEADER_SIZE );
                
                reduceSize( header_padding );
            }
        }
    }

    public void write_to( GIOPConnection conn )
        throws IOException
    {
        if( contexts == null )
        {
            //no additional service contexts present, so buffer can be
            //sent as a whole
            insertMsgSize();

            conn.addMessageFragment( getInternalBuffer(), 0, size() );
        }
        else
        {
            switch( giop_minor )
            {
                case 0 :
                { 
                    // GIOP 1.0 (== GIOP 1.1, fall through)
                }
                case 1 :
                {
                    //GIOP 1.1

                    //First of all, we need to know the the length of
                    //the service context array

                    //For GIOP 1.1, we have to add a padding context
                    contexts.addElement( padding_ctx );

                    CDROutputStream ctx_out = createContextStream();
                    
                    //difference to next 8 byte border
                    int difference = ctx_out.size() % 8; 
                    difference = (difference == 8)? 0 : difference;
                    
                    if( difference > 0 )
                    {
                        //the last padding context has a 0 length data
                        //part. Therefore, the last data is a ulong
                        //with value 0 (the length of the array). To
                        //increase the data part, we have to increase
                        //the size and add the actual data.
                        
                        //"unwrite" the last ulong
                        ctx_out.reduceSize( 1 );
                        
                        //write new length
                        ctx_out.write_ulong( difference );
                        
                        //add "new" data (by just increasing the size
                        //of the stream and not actually writing
                        //anything).
                        ctx_out.increaseSize( difference );
                    }        
                                       
                    //Then, we have to update the message size in the GIOP
                    //message header. The new size is the size of the
                    //"original" message minus the length ulong (4 bytes) of
                    //the original empty ServiceContext array plus the length
                    //of the new service context array
                    insertMsgSize( size() - 4 + ctx_out.size() );


                    //The ServiceContexts are the first attribute in
                    //the RequestHeader struct. Therefore firstly, we
                    //have to write the GIOP message header...
                    conn.addMessageFragment( getInternalBuffer(), 
                                             0,
                                             Messages.MSG_HEADER_SIZE );
                                             
                    
                    
                    //... then add the contexts ...
                    conn.addMessageFragment( ctx_out.getInternalBuffer(),
                                             0,
                                             ctx_out.size() );

                    //... and finally the rest of the message
                    //(omitting the empty original context array).
                    conn.addMessageFragment( getInternalBuffer(), 
                                             Messages.MSG_HEADER_SIZE + 4,
                                             size() );
                    break;
                }
                case 2 :
                {
                    //GIOP 1.2

                    //First of all, we need to know the the length of
                    //the service context array

                    //For GIOP 1.2, the header is padded per spec, so
                    //no additional context is needed

                    CDROutputStream ctx_out = createContextStream();
                                      
                    //difference to next 8 byte border
                    int difference = ctx_out.size() % 8; 
                    difference = (difference == 8)? 0 : difference;
                    
                    if( difference > 0  && hasBody() )
                    {                        
                        //add padding bytes (by just increasing the
                        //size of the stream and not actually writing
                        //anything). If no body is present, no padding
                        //has to be inserted
                        ctx_out.increaseSize( difference );
                    }        
                                       
                    //Then, we have to update the message size in the
                    //GIOP message header. The new size is the size of
                    //the "original" message minus the length ulong (4
                    //bytes) of the original empty ServiceContext
                    //array minus the "original" header padding plus
                    //the length of the new service context array
                    //(containing the new padding)
                    insertMsgSize( size()
                                   - 4 
                                   - getHeaderPadding()
                                   + ctx_out.size() );
                    
                    //The GIOP message and request header (up until
                    //the ServiceContexts) stay unmanipulated. We also
                    //have to remove the length ulong of the
                    //"original" empty service context array, because
                    //the new one has its own length attribute
                    conn.addMessageFragment( getInternalBuffer(), 
                                             0,
                                             Messages.MSG_HEADER_SIZE
                                             + getHeaderEnd() 
                                             - 4);
                                                                                   
                    //... then add the contexts ...
                    conn.addMessageFragment( ctx_out.getInternalBuffer(),
                                             0,
                                             ctx_out.size() );

                    //... and finally the rest of the message
                    //(omitting the empty original context array).
                    conn.addMessageFragment( getInternalBuffer(), 
                                             getBodyBegin(),
                                             size() );
                    
                    break;
                }
                default :
                {
                    throw new Error( "Unknown GIOP minor: " + giop_minor );
                }
            }               
        }            
    }

    public void addServiceContext( ServiceContext ctx )
    {
        if( contexts == null )
        {
            contexts = new Vector();
        }
        
        contexts.add( ctx );
    }
    
    private CDROutputStream createContextStream()
    {
        CDROutputStream out = new CDROutputStream( (org.omg.CORBA.ORB) null );
        
        //write the length of the service context array. 
        out.write_ulong( contexts.size() );

        for( int i = 0; i < contexts.size(); i++ )
        {
            ServiceContextHelper.write( out, 
                                        (ServiceContext) contexts.elementAt( i ));
        }

        return out;
    }

}// GIOPOutputStream






