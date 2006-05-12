package org.jacorb.orb.giop;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2004 Gerald Brose.
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
import java.util.Vector;
import org.jacorb.orb.CDROutputStream;
import org.jacorb.orb.ORB;
import org.jacorb.orb.ORBConstants;
import org.omg.CORBA.MARSHAL;
import org.omg.IOP.ServiceContext;
import org.omg.IOP.ServiceContextHelper;

/**
 * ServiceContextTransportingOutputStream.java
 *
 *
 * Created: Sat Aug 18 12:12:22 2002
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public class ServiceContextTransportingOutputStream
    extends MessageOutputStream
{
    /**
     * <code>header_end</code> represents the end of the GIOP message header.
     * Only valid if header_padding != 0
     */
    private int header_end = -1;

    /**
     * <code>header_padding</code> represents the number of bytes used for padding
     * between header and body
     */
    private int header_padding = 0;

    /**
     * <code>padding_ctx</code> is used if ServiceContexts are actually added.
     * This will be the last context, and the context_data is used to fill up
     * to the next 8 byte boundary
     */
    private static ServiceContext padding_ctx =
        new ServiceContext (ORBConstants.SERVICE_PADDING_CONTEXT, new byte[0]);

    private Vector contexts;

    public ServiceContextTransportingOutputStream()
    {
        super();
    }

    public ServiceContextTransportingOutputStream(ORB orb)
    {
        super(orb);
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

    private int getHeaderEnd()
    {
        return header_end;
    }

    private int getBodyBegin()
    {
        return header_end + header_padding;
    }


    private int getHeaderPadding()
    {
        return header_padding;
    }

    private boolean hasBody()
    {
        return size() > getBodyBegin();
    }


    public void insertMsgSize()
    {
        if( header_padding == 0 )
        {
            insertMsgSize( size() - Messages.MSG_HEADER_SIZE );
        }
        else
        {
            if( size() >  header_end + header_padding)
            {
                //has a body, so include padding by not removing it :-)
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
        CDROutputStream ctx_out = null;

        if( contexts == null || contexts.size() == 0 )
        {
            //no additional service contexts present, so buffer can be
            //sent as a whole
            insertMsgSize();
            write( conn, 0, size() );
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

                    ctx_out = createContextStream();

                    //difference to next 8 byte border

                    // Need to calculate whether the service context needs any
                    // padding. This is the difference. To calculate this need
                    // to add the new CDRStream and the header size - this
                    // should end on an 8 byte boundary.
                    int difference =
                        (8 - ((Messages.MSG_HEADER_SIZE + ctx_out.size ()) % 8));
                    difference = (difference == 8)? 0 : difference;

                    if( difference > 0 )
                    {
                        //the last padding context has a 0 length data
                        //part. Therefore, the last data is a ulong
                        //with value 0 (the length of the array). To
                        //increase the data part, we have to increase
                        //the size and add the actual data.

                        //"unwrite" the last ulong
                        ctx_out.reduceSize( 4 );

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
                    insertMsgSize( size()
                                   - Messages.MSG_HEADER_SIZE
                                   - 4
                                   + ctx_out.size() );


                    //The ServiceContexts are the first attribute in
                    //the RequestHeader struct. Therefore firstly, we
                    //have to write the GIOP message header...
                    write( conn, 0, Messages.MSG_HEADER_SIZE );

                    //... then add the contexts ...
                    ctx_out.write( conn, 0, ctx_out.size() );

                    //... and finally the rest of the message
                    //(omitting the empty original context array).

                    write( conn,
                           Messages.MSG_HEADER_SIZE + 4,
                           size() -
                           (Messages.MSG_HEADER_SIZE + 4) );
                    break;
                }
                case 2 :
                {
                    //GIOP 1.2

                    //First of all, we need to know the the length of
                    //the service context array

                    //For GIOP 1.2, the header is padded per spec, so
                    //no additional context is needed

                    ctx_out = createContextStream();

                    //the new header end is the old header end minus
                    //the length ulong of the context array plus the
                    //length of the context array (wich contains its
                    //own length ulong)
                    int new_header_end = getHeaderEnd() - 4 + ctx_out.size();

                    //difference to next 8 byte border
                    int difference =  8 - (new_header_end % 8);
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
                                   - Messages.MSG_HEADER_SIZE
                                   - 4
                                   - getHeaderPadding()
                                   + ctx_out.size() );

                    //The GIOP message and request header (up until
                    //the ServiceContexts) stay unmanipulated. We also
                    //have to remove the length ulong of the
                    //"original" empty service context array, because
                    //the new one has its own length attribute
                    write( conn,
                           0,
                           getHeaderEnd() - 4 );

                    //... then add the contexts ...

                    ctx_out.write( conn, 0, ctx_out.size());

                    //... and finally the rest of the message
                    //(omitting the empty original context array).

                    write( conn,
                           getBodyBegin(),
                           size() - getBodyBegin() );

                    break;
                }
                default :
                {
                    throw new MARSHAL( "Unknown GIOP minor: " + giop_minor );
                }
            }
        }
        close();
        if ( ctx_out != null )
        {
            ctx_out.close();
            ctx_out = null;
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


    /**
     * private hack...
     */

    public byte[] getBody()
    {
        byte [] result =
            org.jacorb.orb.BufferManager.getInstance().getBuffer( size() - getBodyBegin());

        System.arraycopy( getBufferCopy(), getBodyBegin(), result, 0, result.length );

        return result;
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

}// ServiceContextTransportingOutputStream
