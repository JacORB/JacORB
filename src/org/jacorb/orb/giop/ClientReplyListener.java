package org.jacorb.orb.connection;

/**
 * ClientReplyListener.java
 *
 *
 * Created: Sat Aug 18 18:37:56 2001
 *
 * @author Nicolas Noffke
 * @version $Id$ 
 */

public class ClientReplyListener 
    implements ReplyListener
{
    public ClientReplyListener ()
    {
        
    }

    public void replyReceived( byte[] reply,
                               GIOPConnection connection )
    {
        
    }

        
    public void locateReplyReceived( byte[] reply,
                                     GIOPConnection connection )
    {
        
    }


    public void closeConnectionReceived( byte[] close_conn,
                                         GIOPConnection connection )
    {
        
    }

    
    public void fragmentReceived( byte[] fragment,
                                  GIOPConnection connection )
    {
        
    }

    public void connectionClosed()
    {

    }
}// ClientReplyListener

