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

/**
 * @author Gerald Brose, FU Berlin
 * @version $Id$
 *
 */

import java.io.*;
import java.net.*;
import java.util.*;

import org.jacorb.util.Debug;
import org.jacorb.orb.*;
import org.jacorb.util.*;
import org.jacorb.orb.factory.SocketFactory;

public abstract class AbstractConnection 
{
    protected InputStream in_stream;
    BufferedOutputStream out_stream;

    public  org.jacorb.orb.ORB orb;
    protected ConnectionManager manager;
	
    /**
     * Connection OSF character formats.
     */

    public int TCS = CodeSet.getTCSDefault();
    public int TCSW = CodeSet.getTCSWDefault();	

    /**
     * Client's information whether  the codeSet has been already sent
     * to server.  It's important because there is possibility that we
     * add  the context to new  request but the request  will never be
     * sent because  of exception during marshaling in  stub. TCS will
     * be considered sent at point  when at least one request with tcs
     * context  was sent successfully. Also note  there is possibility
     * that another requests with  tcs context can be pending and also
     * sent AFTER tcsNegotiated was  set to true. But it's not problem
     * because server side will ignore all tcs contexts recieved after
     * first  one (but OMG  doc is not  clear here).
     * <br> 
     * When  set for server side connection  (is_server=true) it means
     * that at least one request with tcs context was recieved and tcs
     * information is valid.
     * @author devik 
     */

    private boolean tcsNegotiated = false;
	
    /**
     * IIOP version active on the channel.
     * @author devik
     */

    public org.omg.IIOP.Version IIOPVersion = 
	new org.omg.IIOP.Version((byte)1,(byte)0);

    /* how many clients use this connection? */
    protected int client_count = 0;

    protected Socket mysock = null;

    private byte [] header = new byte[ Messages.MSG_HEADER_SIZE ];

    /**
     * Called by Delegate or setServerCodeSet in order to mark tcs on this
     * connection as negotiated. It's public because it has to be called
     * also from dii.Request.
     */
	
    public void markTcsNegotiated()
    {
        if( tcsNegotiated ) 
            return;
        tcsNegotiated = true;
    }
	
    public boolean isTCSNegotiated()
    {
        return tcsNegotiated;
    }
	
    /** don't use, this is a temporary hack! */
	
    public Socket getSocket()
    {
        return mysock;
    }		

    public boolean connected()
    {
        return mysock != null;
    }

    public BufferedOutputStream get_out_stream()
    {
	return out_stream;
    }

}









