/*
 * @(#)HTTPClientModule.java				0.3-2 18/06/1999
 *
 *  This file is part of the HTTPClient package
 *  Copyright (C) 1996-1999  Ronald Tschalär
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free
 *  Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *  MA 02111-1307, USA
 *
 *  For questions, suggestions, bug-reports, enhancement-requests etc.
 *  I may be contacted at:
 *
 *  ronald@innovation.ch
 *
 */

package HTTPClient;

import java.io.IOException;

/**
 * This is the interface that a module must implement. There are two parts
 * during a request: the construction of the request, and the handling of
 * the response. A request may cycle through these parts multiple times
 * when a module generates additional subrequests (such as a redirection
 * status handling module might do).
 *
 * <P>In the first step the request handler is invoked; here the headers,
 * the request-uri, etc. can be modified, or a complete response can be
 * generated. Then, if no response was generated, the request is sent over
 * the wire. In the second step the response handlers are invoked. These
 * may modify the response or, in phase 2, may generate a new request; the
 * returned status from the phase 2 handler specifies how the processing of
 * the request or response should further proceed.
 *
 * <P>The response handling is split into three phases. In the first phase
 * the response handling cannot be modified; this is so that all modules
 * get a chance to see the returned response. Modules will typically make
 * notes of responses and do certain header processing here (for example the
 * cookie module does it's work in this phase). In the second phase modules
 * may generate new subrequests or otherwise control the further handling of
 * the response. This is typically used for response status handling (such
 * as for redirections and authentication). Finally, if no new subrequest
 * was generated, the phase 3 response handlers are invoked so that modules
 * can perform any necessary cleanups and final processing (no additional
 * subrequests can be made anymore). It is recommended that any response
 * processing which needn't be done if the request is not returned to the
 * user is deferred until this phase. For example, the Content-MD5,
 * Content-Encoding and Transfer-Encoding modules do their work in this
 * phase as the body is usually discarded if a new subrequest is generated.
 *
 * <P>When the user invokes any request method (such as <code>Get(...)</code>)
 * a list of of modules to be used is built. Then, for each module in the
 * list, an instance is created using the <code>Class.newInstance()</code>
 * method. This means that each module must have a constructor which takes
 * no arguments. This instance is then used to handle the request, its
 * response, and any additional subrequests and their responses. In this way
 * a module can easily keep state between related subrequests. For example, a
 * redirection module might want to keep track of the number of redirections
 * made to detect redirect loops; it could do this by defining an instance
 * variable and incrementing it each time the request handler is invoked.
 *
 * @version	0.3-2  18/06/1999
 * @author	Ronald Tschalär
 * @since	V0.3
 */

public interface HTTPClientModule extends HTTPClientModuleConstants
{
    /**
     * This is invoked before the request is sent. A module will typically
     * use this to make a note of headers, to modify headers and/or data,
     * or even generate and return a response (e.g. for a cache module).
     * If a response is generated the module must return the appropriate
     * return code (<var>REQ_RESPONSE</var> or <var>REQ_RETURN</var>).
     *
     * <P>Return codes for phase 1 (defined in HTTPClientModuleConstants.java)
     * <DL>
     * <DT>REQ_CONTINUE	  <DI>continue processing
     * <DT>REQ_RESTART    <DI>restart processing with first module
     * <DT>REQ_SHORTCIRC  <DI>stop processing and send
     * <DT>REQ_RESPONSE   <DI>go to phase 2
     * <DT>REQ_RETURN     <DI>return response immediately (no processing)
     * <DT>REQ_NEWCON_RST <DI>use a new HTTPConnection, restart processing
     * <DT>REQ_NEWCON_SND <DI>use a new HTTPConnection, send immediately
     * </DL>
     *
     * @param request  the request - may be modified as needed
     * @param response the response if the status is REQ_RESPONSE or REQ_RETURN
     * @return status code REQ_XXX specifying further action
     * @exception IOException if an IOException occurs on the socket
     * @exception ModuleException if an exception occurs during the handling
     *                            of the request
     */
    public int requestHandler(Request request, Response[] response)
	    throws IOException, ModuleException;


    /**
     * The phase 1 response handler. This will be invoked for every response.
     * Modules will typically make notes of the response and do any header
     * processing which must always be performed.
     *
     * @param response the response - may be modified
     * @param request  the original request
     * @exception IOException if an IOException occurs on the socket
     * @exception ModuleException if an exception occurs during the handling
     *                            of the response
     */
    public void responsePhase1Handler(Response response, RoRequest request)
	    throws IOException, ModuleException;

    /**
     * The phase 2 response handler. A module may modify the response or
     * generate a new request (e.g. for redirection). This handler will
     * only be invoked for a given module if all previous modules returned
     * <var>RSP_CONTINUE</var>. If the request is modified the handler must
     * return an appropriate return code (<var>RSP_REQUEST</var>,
     * <var>RSP_SEND</var>, <var>RSP_NEWCON_REQ</var> or
     * <var>RSP_NEWCON_SND</var>). If any other code is return the request
     * must not be modified.
     *
     * <P>Return codes for phase 2 (defined in HTTPClientModuleConstants.java)
     * <DL>
     * <DT>RSP_CONTINUE   <DI>continue processing
     * <DT>RSP_RESTART    <DI>restart processing with first module (phase 1)
     * <DT>RSP_SHORTCIRC  <DI>stop processing and return
     * <DT>RSP_REQUEST    <DI>go to phase 1
     * <DT>RSP_SEND       <DI>send request immediately (no processing)
     * <DT>RSP_NEWCON_REQ <DI>go to phase 1 using a new HTTPConnection
     * <DT>RSP_NEWCON_SND <DI>send request using a new HTTPConnection
     * </DL>
     *
     * @param response the response - may be modified
     * @param request  the request; if the status is RSP_REQUEST then this
     *                 must contain the new request; however do not modify
     *                 this if you don't return a RSP_REQUEST status.
     * @return status code RSP_XXX specifying further action
     * @exception IOException if an IOException occurs on the socket
     * @exception ModuleException if an exception occurs during the handling
     *                            of the response
     */
    public int  responsePhase2Handler(Response response, Request request)
	    throws IOException, ModuleException;


    /**
     * The phase 3 response handler. This will only be invoked if no new
     * subrequest was generated in phase 2. Modules should defer any repsonse
     * handling which need only be done if the response is returned to the
     * user to this phase.
     * 
     * @param response the response - may be modified
     * @param request  the original request
     * @exception IOException if an IOException occurs on the socket
     * @exception ModuleException if an exception occurs during the handling
     *                            of the response
     */
    public void responsePhase3Handler(Response response, RoRequest request)
	    throws IOException, ModuleException;


    /**
     * The chunked transfer-encoding (and in future maybe others) can contain
     * trailer fields at the end of the body. Since the
     * <code>responsePhaseXHandler()</code>'s are invoked before the body is
     * read and therefore do not have access to the trailers (unless they
     * force the complete body to be read) this method will be invoked when
     * the trailers have been read and parsed (sort of a post-response
     * handling).
     *
     * <P>Note: This method <strong>must not</strong> modify any part of the
     * response other than the trailers.
     *
     * @param response the response
     * @param request  the request
     * @exception IOException if an IOException occurs on the socket
     * @exception ModuleException if an exception occurs during the handling
     *                            of the trailers
     */
    public void trailerHandler(Response response, RoRequest request)
	    throws IOException, ModuleException;
}

