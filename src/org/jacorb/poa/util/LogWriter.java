package org.jacorb.poa.util;

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

import org.jacorb.util.Environment;
import org.jacorb.orb.dsi.ServerRequest;
import org.jacorb.util.*;

import org.omg.PortableServer.POAManagerPackage.State;

import java.io.*;

/**
 * Implements the LogTrace interface. You can register another LogTrace object.
 * For the own log prints this class uses the org.jacorb.util.Debug.output interface and
 * after that the logs will passed to the second LogTrace object.
 *
 * @author Reimo Tiedemann, FU Berlin
 * @version $Id$
 */

public class LogWriter
    implements LogTrace
{
    private LogTrace delegate;
    private String prefix;
    private boolean isLogFileOut;

    private LogWriter() {
    }

    public LogWriter(String _prefix)
    {
        prefix = _prefix+" - ";
        if ( Environment.logFileOut() != null)
            isLogFileOut = true;
    }

    public boolean test(int logLevel) {
        return Environment.verbosityLevel() >= ( logLevel);
    }

    public void printLog(byte[] oid, String message)
    {
    	printLog_("oid: "+POAUtil.convert(oid)+" - "+message);
    }

    public void printLog(ServerRequest request, String message)
    {
    	printLog_("rid: "+request.requestId()+
                  " oid: "+POAUtil.convert(request.objectId())+
                  " opname: "+request.operation()+" - "+message);
    }

    public void printLog(ServerRequest request, State state, String message)
    {
        printLog_("rid: "+request.requestId()+
                  " oid: "+POAUtil.convert(request.objectId())+
                  " opname: "+request.operation()+" - "+message+
                  " (in state "+POAUtil.convert(state)+")");
    }

    public void printLog(String message)
    {
        printLog_(message);
    }

    public void printLog(Throwable e)
    {
        printLog_(e);
    }

    private void printLog_(String message)
    {
        if ( isLogFileOut || delegate == null)
        {
            org.jacorb.util.Debug.output( 0, prefix+message);
        }
        if (delegate != null)
        {
            delegate.printLog(message);
        }
    }

    private void printLog_(Throwable e)
    {
        if ( isLogFileOut || delegate == null)
        {
            org.jacorb.util.Debug.output( 0, e);
        }
        if (delegate != null)
        {
            delegate.printLog(e);
        }
    }

    public void setLogTrace(LogTrace _delegate) {
        delegate = _delegate;
    }
}
