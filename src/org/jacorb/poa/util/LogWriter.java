package org.jacorb.poa.util;

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

import org.jacorb.util.Environment;
import org.jacorb.orb.dsi.ServerRequest;

import org.omg.PortableServer.POAManagerPackage.State;

import java.io.*;

/**
 * Implements the LogTrace interface. You can register another LogTrace object.
 * For the own log prints this class uses the org.jacorb.util.Debug.output interface and
 * after that the logs will passed to the second LogTrace object.
 *
 * @author Reimo Tiedemann, FU Berlin
 * @version 1.00, 06/11/99, RT
 */

public class LogWriter 
    implements LogTrace 
{
    private LogTrace delegate;
    private String prefix;
    private boolean isSystemId;
    private boolean isLogFileOut;

    private LogWriter() {
    }

    public LogWriter(String _prefix, boolean is_system_id) 
    {
        prefix = _prefix+" - ";
        isSystemId = is_system_id;
        if ( Environment.logFileOut() != null) 
            isLogFileOut = true;
    }

    public void printLog(int mode, byte[] oid, String message) 
    {
        if (Environment.verbosityLevel() >= mode) 
        {
            printLog_(mode, "oid: "+POAUtil.convert(oid, isSystemId)+" - "+message);
        }
    }

    public void printLog(int mode, ServerRequest request, String message) 
    {
        if (Environment.verbosityLevel() >= mode) 
        {
            printLog_(mode, "rid: "+request.requestId()+
                      " oid: "+POAUtil.convert(request.objectId(), isSystemId)+
                      " opname: "+request.operation()+" - "+message);
        }
    }
    public void printLog(int mode, ServerRequest request, State state, String message) 
    {
        if (Environment.verbosityLevel() >= mode) 
        {
            printLog_(mode, "rid: "+request.requestId()+
                      " oid: "+POAUtil.convert(request.objectId(), isSystemId)+
                      " opname: "+request.operation()+" - "+message+
                      " (in state "+POAUtil.convert(state)+")");
        }
    }

    public void printLog(int mode, String message) 
    {
        if (Environment.verbosityLevel() >= mode) {
            printLog_(mode, message);
        }
    }

    public void printLog(int mode, Throwable e) 
    {
        if ( Environment.verbosityLevel() >= mode ) 
        {
            printLog_(mode, e);
        }
    }

    private void printLog_( int mode, String message) 
    {		
        if ( isLogFileOut || delegate == null) 
        {
            org.jacorb.util.Debug.output(mode, prefix+message);			
        }
        if (delegate != null) 
        {
            delegate.printLog(mode, message);
        }		
    }

    private void printLog_( int mode, Throwable e) 
    {		
        if ( isLogFileOut || delegate == null) 
        {
            org.jacorb.util.Debug.output( mode, e);			
        }
        if (delegate != null) 
        {
            delegate.printLog(mode, e);
        }		
    }

    public void setLogTrace(LogTrace _delegate) {
        delegate = _delegate;
    }
}






