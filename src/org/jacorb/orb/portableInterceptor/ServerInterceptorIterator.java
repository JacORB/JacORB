package org.jacorb.orb.portableInterceptor;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose, Nicolas Noffke
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

import org.omg.PortableInterceptor.*;
import org.omg.CORBA.UserException;

import org.jacorb.orb.SystemExceptionHelper;

/**
 * This class iterates over an array of
 * ServerRequestInterceptors.
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public class ServerInterceptorIterator
    extends RequestInterceptorIterator
{
    public static final short RECEIVE_REQUEST_SERVICE_CONTEXTS = 0;
    public static final short RECEIVE_REQUEST = 1;
    public static final short SEND_REPLY = 2;
    public static final short SEND_EXCEPTION = 3;
    public static final short SEND_OTHER = 4;

    private ServerRequestInfoImpl info = null;

    public ServerInterceptorIterator(Interceptor[] interceptors) 
    {
	super(interceptors);
    }

    /**
     * Iterates over the enumeration, i.e. calls "op" on
     * nextElement() until !hasMoreElements().
     */
    public void iterate( ServerRequestInfoImpl info, short op )
	throws UserException
    {
      
	this.info = info;
	this.op = op;

	//set sending_exception right
	info.update();
	info.caller_op = op;

	// ok, op <= RECEIVE_REQUEST is more efficient but 
	// less understandable
	setDirection( (op == RECEIVE_REQUEST_SERVICE_CONTEXTS) || 
		      (op == RECEIVE_REQUEST));

	iterate();

	//propagate last exception upwards
	if ( interceptor_ex != null )
	    if (interceptor_ex instanceof ForwardRequest)
		throw (ForwardRequest) interceptor_ex;
	    else
		throw (org.omg.CORBA.SystemException) interceptor_ex;
    }

    protected void invoke(Interceptor interceptor)
	throws UserException
    {

	try
        {
	    switch (op) 
            {
	    case RECEIVE_REQUEST_SERVICE_CONTEXTS :
		((ServerRequestInterceptor) interceptor).
		    receive_request_service_contexts(info);
		break;
	    case RECEIVE_REQUEST :
		((ServerRequestInterceptor) interceptor).receive_request(info);
		break;
	    case SEND_REPLY :
		((ServerRequestInterceptor) interceptor).send_reply(info);
		break;
	    case SEND_EXCEPTION :
		((ServerRequestInterceptor) interceptor).send_exception(info);
		break;
	    case SEND_OTHER :
		((ServerRequestInterceptor) interceptor).send_other(info);
		break;
	    }
	}
        catch (ForwardRequest _fwd)
        {
	    reverseDirection();
	    op = SEND_OTHER;
	
            info.reply_status = LOCATION_FORWARD.value;

	    info.forward_reference = _fwd.forward;

	    interceptor_ex = _fwd;
	}
        catch (org.omg.CORBA.SystemException _sysex)
        {
	    reverseDirection();
	    op = SEND_EXCEPTION;
	    interceptor_ex = _sysex;

	    SystemExceptionHelper.insert(info.sending_exception, _sysex);
	}
        catch (Throwable th)
        {
            th.printStackTrace();
	}

	info.caller_op = op;
    }
} // ServerInterceptorIterator



