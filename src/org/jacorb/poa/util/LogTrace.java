package org.jacorb.poa.util;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2002  Gerald Brose.
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
 * Defines an interface of an log writer that can print out logs to a file
 * or stdout. The specialized printLog routines helps the devoloper to print 
 * out the logs in a unified way.
 *
 * @author Reimo Tiedemann, FU Berlin
 * @version 1.00, 06/11/99, RT
 */
public interface LogTrace 
{
    public boolean test(int logLevel);
    public void printLog(byte[] objectId, String message);
    public void printLog(org.jacorb.orb.dsi.ServerRequest request, String message);
    public void printLog(org.jacorb.orb.dsi.ServerRequest request, org.omg.PortableServer.POAManagerPackage.State state, String message);
    public void printLog(String message);
    void printLog(Throwable e);
    void setLogTrace(LogTrace _delegate);

}

