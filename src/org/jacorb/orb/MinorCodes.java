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

package org.jacorb.orb;


/** Minor codes for CORBA system-exceptions. These codes are marshalled
 *  on the wire and allow the client to know the exact cause of the exception.
 */

public final class MinorCodes {

    // BAD_CONTEXT exception minor codes
    // BAD_INV_ORDER exception minor codes
    // BAD_OPERATION exception minor codes

    // BAD_PARAM exception minor codes
    public static final int NO_SUCH_PROFILE = 29;
    
    // BAD_TYPECODE exception minor codes
    // BOUNDS exception minor codes
    // COMM_FAILURE exception minor codes
    // DATA_CONVERSION exception minor codes
    // FREE_MEM exception minor codes
    // IMP_LIMIT exception minor codes
    // INITIALIZE exception minor codes
    // INTF_REPOS exception minor codes
    // INTERNAL exception minor codes
    // INV_FLAG exception minor codes
    // INV_IDENT exception minor codes
    // INV_OBJREF exception minor codes

    // INV_POLICY exception minor codes
    public static final int NO_SUCH_POLICY = 3;    
    
    // MARSHAL exception minor codes
    // NO_MEMORY exception minor codes
    // NO_IMPLEMENT exception minor codes

    // NO_PERMISSION exception minor codes
    public static final int SAS_CSS_FAILURE     = ORBConstants.VMCID + 1;
    public static final int SAS_ATLAS_FAILURE   = ORBConstants.VMCID + 2;
    public static final int SAS_TSS_FAILURE     = ORBConstants.VMCID + 3;

    // NO_RESOURCES exception minor codes
    // NO_RESPONSE exception minor codes
    // OBJ_ADAPTER exception minor codes
    // OBJECT_NOT_EXIST exception minor codes
    // PERSIST_STORE exception minor codes
    // TRANSIENT exception minor codes
    // UNKNOWN exception minor codes
} ;
