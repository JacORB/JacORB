package org.jacorb.poa.except;

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
 
/**
 * This error will be thrown in the case of an incorrect application of the
 * POA class (JacORB specific application error).
 * 
 * @author Reimo Tiedemann, FU Berlin
 * @version $Id$
 * @see	  jacorb.poa.POA#createPOA(String, org.omg.PortableServer.POAManager, org.omg.CORBA.Policy[])
 */
public final class ApplicationError 
    extends java.lang.Error 
{
    public ApplicationError(String error) {
        super(error);
    }
}






