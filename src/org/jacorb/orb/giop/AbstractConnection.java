/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2000  Gerald Brose.
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

    /** write lock */
	
    /**
     * Connection OSF character formats, if zero for client side connection
     * then preferred tcs was not computed yet; use TS for zero testing.
     * Note that zero values are invalid when tcsNegotiated = true.
     * It's protected because it's directly read by CDR classes defined
     * in this package.
     *
     * @author devik
     */

    public int TCS = 0, TCSW = 0;	

    /**
     * Client's information whether the codeSet has been already sent to server.
     * It's important because there is possibility that we add the context
     * to new request but the request will never be sent because of exception
     * during marshaling in stub. TCS will be considered sent at point when
     * at least one request with tcs context was sent successfully. Also
     * note there is possibility that another requests with tcs context
     * can be pending and also sent AFTER tcsNegotiated was set to true. But it's
     * not problem because server side will ignore all tcs contexts recieved
     * after first one (but OMG doc is not clear here).<br>
     * When set for server side connection (is_server=true) it means that
     * at least one request with tcs context was recieved and tcs information
     * is valid.
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
        Debug.output(4,"TCS marked as negotiated");
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
		
    protected int selectCodeSet(org.omg.CONV_FRAME.CodeSetComponent scs,
				int ourNative)
    {
	// check if we support server's native sets
	if( scs.native_code_set == ourNative ) 
	    return ourNative;
		
	// is our native CS supported at server ?
	for( int i=0; i < scs.conversion_code_sets.length; i++)
	{
	    if( scs.conversion_code_sets[i] == ourNative ) 
		return ourNative;
	}
		
	// can't find supported set ..
	return 0;
    }

    /**
     * This code selects the appropriate codeset for connection from information
     * contained in some IOR. Code is called by client side connection.
     * Returns true if common codeset was found and so that it should be
     * sent in context.
     */
    protected boolean selectCodeSet(ParsedIOR pior)
    {
	for(int i = 0; i < pior.taggedComponents.length; i++)
	{
	    if( pior.taggedComponents[i].tag != org.omg.IOP.TAG_CODE_SETS.value ) 
		continue;

	    Debug.output(4,"TAG_CODE_SETS found");			

	    // get server cs from IOR 
	    CDRInputStream is =
		new CDRInputStream( orb, pior.taggedComponents[i].component_data);

	    is.setLittleEndian(is.read_boolean());

	    org.omg.CONV_FRAME.CodeSetComponentInfo inf = 
		org.omg.CONV_FRAME.CodeSetComponentInfoHelper.read(is);
		
	    // char data, try UTF8 and ISO8859_1
	    TCS = selectCodeSet(inf.ForCharData,CodeSet.UTF8);
	    if( TCS == 0 ) 
		TCS = CodeSet.ISO8859_1;
			
	    // wchar data, UTF8 or UTF16 can be used
	    TCSW = selectCodeSet(inf.ForWcharData,CodeSet.UTF8);
	    if( TCSW == 0) 
		TCSW = selectCodeSet(inf.ForWcharData, CodeSet.UTF16);

	    Debug.output(4,"TCS selected: "+CodeSet.csName(TCS)+","+
                                     CodeSet.csName(TCSW));
	    return true;
	}

	// if we are here then it means that no tagged component was in IOR, 
	// probably some old server so set default parameters

	// TODO(devik): we ends up here also for MICO server which seems to have some
	// strange IOR - have to investigate

	TCS = CodeSet.ISO8859_1;
	TCSW = CodeSet.UTF16; // not as in OMG spec!
		
	// mark as negotiated, why if it's not true ? because we don't
	// want to try negotiate on each IOR until IOR with codeset is found.
	// TODO(devik): Or should we ???
	markTcsNegotiated();
		
	Debug.output(4,"default TCS selected: "+CodeSet.csName(TCS)+"," +
                                 CodeSet.csName(TCSW));
	return false;
    }
	
    /**
     * Called on server side connection to evaluate requext contexts and
     * set TCS if one found. Returns true if TCS was correctly set.
     * Currently it's called from BasicAdapter.
     */

    public boolean setServerCodeSet(org.omg.IOP.ServiceContext [] ctx)
    {
        if( !Environment.charsetScanCtx() ) 
	    return false;

	// search all contexts until TAG_CODE_SETS found
	for( int i = 0; i < ctx.length; i++ )
	{
	    if( ctx[i].context_id != org.omg.IOP.TAG_CODE_SETS.value ) 
		continue;
			
	    // TAG_CODE_SETS found, demarshall
	    CDRInputStream is = new CDRInputStream( orb, ctx[i].context_data);
	    is.setLittleEndian(is.read_boolean());
	    org.omg.CONV_FRAME.CodeSetContext csx =
		org.omg.CONV_FRAME.CodeSetContextHelper.read(is);
	    TCSW = csx.wchar_data;
	    TCS = csx.char_data;
	    markTcsNegotiated();
	    Debug.output(4,"TCS set: "+CodeSet.csName(TCS)+","+
                                     CodeSet.csName(TCSW));
	    return true;
	}
	return false; // no TAG_CODE_SETS here
    }
	
    /**
     * Adds code set service context to another contexts if needed.
     */

    public org.omg.IOP.ServiceContext [] addCodeSetContext(org.omg.IOP.ServiceContext [] ctx,
							   ParsedIOR pior)
    {		
	// if already negotiated, don't send any further cs contexts
	// we should test it also directly before calling this method
	// as performance optimization

	if(tcsNegotiated || !Environment.charsetSendCtx()) 
	    return ctx;
		
	// not negotiated but also TCS is not selected, so select one
	// if it can't be selected (ior doesn't contain codesets) don't change ctx
	if( TCS==0 ) 
	{
	    if(!selectCodeSet(pior)) 
		return ctx;
	}

	// encapsulate context

	CDROutputStream os = new CDROutputStream( orb );
	os.write_boolean(false);
	org.omg.CONV_FRAME.CodeSetContextHelper.write(os,
						      new org.omg.CONV_FRAME.CodeSetContext(TCS,TCSW));
		
	org.omg.IOP.ServiceContext [] ncx =
            new org.omg.IOP.ServiceContext[ctx.length+1];

	System.arraycopy(ctx,0,ncx,0,ctx.length);
	ncx[ctx.length] = 
            new org.omg.IOP.ServiceContext(org.omg.IOP.TAG_CODE_SETS.value,
                                           os.getBufferCopy());

	// Debug.output(4,"TCS ctx added: "+CodeSet.csName(TCS)+","+CodeSet.csName(TCSW));
	return ncx;
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



