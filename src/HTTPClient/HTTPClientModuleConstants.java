/*
 * @(#)HTTPClientModuleConstants.java			0.3-2 18/06/1999
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


/**
 * This interface defines the return codes that the handlers in modules
 * may return.
 *
 * @see HTTPClientModule
 * @version	0.3-2  18/06/1999
 * @author	Ronald Tschalär
 * @since	V0.3
 */

public interface HTTPClientModuleConstants
{
    // valid return codes for request handlers

    /** continue processing the request */
    int  REQ_CONTINUE   = 0;

    /** restart request processing with first module */
    int  REQ_RESTART    = 1;

    /** stop processing and send the request */
    int  REQ_SHORTCIRC  = 2;

    /** response generated; go to phase 2 */
    int  REQ_RESPONSE   = 3;

    /** response generated; return response immediately (no processing) */
    int  REQ_RETURN     = 4;

    /** using a new HTTPConnection, restart request processing */
    int  REQ_NEWCON_RST = 5;

    /** using a new HTTPConnection, send request immediately */
    int  REQ_NEWCON_SND = 6;


    // valid return codes for the phase 2 response handlers

    /** continue processing response */
    int  RSP_CONTINUE   = 10;

    /** restart response processing with first module */
    int  RSP_RESTART    = 11;

    /** stop processing and return response */
    int  RSP_SHORTCIRC  = 12;

    /** new request generated; go to phase 1 */
    int  RSP_REQUEST    = 13;

    /** new request generated; send request immediately (no processing) */
    int  RSP_SEND       = 14;

    /** go to phase 1 using a new HTTPConnection */
    int  RSP_NEWCON_REQ = 15;

    /** send request using a new HTTPConnection */
    int  RSP_NEWCON_SND = 16;
}

