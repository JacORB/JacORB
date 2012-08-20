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

package org.jacorb.orb.giop;

import java.io.IOException;
import org.jacorb.orb.ORB;
import org.jacorb.orb.SystemExceptionHelper;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.INV_POLICY;
import org.omg.GIOP.ReplyStatusType_1_2;
import org.slf4j.Logger;

/**
 * @author Nicolas Noffke
 */

public class NoBiDirClientRequestListener
    implements RequestListener
{
    private final Logger logger;
    private final ORB orb;

    public NoBiDirClientRequestListener(org.jacorb.orb.ORB orb, Logger logger)
    {
        this.orb = orb;
        this.logger = logger;
    }

    public void requestReceived( byte[] request,
                                 GIOPConnection connection )
    {
        logger.warn("Received a request on a non-bidir connection" );

        connection.incPendingMessages();
        replyException( request, connection );
    }

    public void locateRequestReceived( byte[] request,
                                       GIOPConnection connection )
    {
        logger.warn("Received a locate request on a non-bidir connection" );

        connection.incPendingMessages();
        replyException( request, connection );
    }


    public void cancelRequestReceived( byte[] request,
                                       GIOPConnection connection )
    {
        logger.warn("Received a cancel request on a non-bidir connection" );

        connection.incPendingMessages();
        replyException( request, connection );
    }

    private void replyException( byte[] request,
                                 GIOPConnection connection )
    {
        final int giop_minor = Messages.getGIOPMinor( request );

        final ReplyOutputStream out =
            new ReplyOutputStream( orb,
                                   Messages.getRequestId( request ),
                                   ReplyStatusType_1_2.SYSTEM_EXCEPTION,
                                   giop_minor,
                                   false,
                                   logger);//no locate reply

        try
        {
            SystemExceptionHelper.write( out,
                    new INV_POLICY( 0, CompletionStatus.COMPLETED_NO ));

            connection.sendReply( out );
        }
        catch( IOException e )
        {
            logger.error("unexpected exception during replyException", e );
        }
        finally
        {
            out.close();
        }
    }
}
