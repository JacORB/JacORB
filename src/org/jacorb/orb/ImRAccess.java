/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2003 Gerald Brose
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
 *
 */
package org.jacorb.orb;

/**
 * ImRAccess.java
 *
 *
 * Created: Thu Jan 31 20:55:32 2002
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public interface ImRAccess
{
    public String getImRHost();
    public int getImRPort();
    public void registerPOA( String name,
                             String server,
                             String host,
                             int port)
        throws org.omg.CORBA.INTERNAL;
    public void setServerDown( String name )
}// ImRAccess
