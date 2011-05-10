/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2011 Gerald Brose / The JacORB Team.
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

package org.jacorb.test.common;

import org.slf4j.Logger;
import org.slf4j.Marker;

/**
 * specific NullLogger that returns
 * true for the isXXXEnabled methods.
 * this is to ensure that more parts of the sourcecode
 * are actually executed.
 *
 * @author Alphonse Bendt
 * @version $Id$
 */
public class MyNullLogger implements Logger
{
    public void debug(String message)
    {
    }

    public void debug(String message, Throwable throwable)
    {
    }

    public void error(String message)
    {
    }

    public void error(String message, Throwable throwable)
    {
    }

    public void fatalError(String message)
    {
    }

    public void fatalError(String message, Throwable throwable)
    {
    }

    public Logger getChildLogger(String name)
    {
        return null;
    }

    public void info(String message)
    {
    }

    public void info(String message, Throwable throwable)
    {
    }

    public boolean isDebugEnabled()
    {
        return true;
    }

    public boolean isErrorEnabled()
    {
        return true;
    }

    public boolean isFatalErrorEnabled()
    {
        return true;
    }

    public boolean isInfoEnabled()
    {
        return true;
    }

    public boolean isWarnEnabled()
    {
        return true;
    }

    public void warn(String message)
    {
    }

    public void warn(String message, Throwable throwable)
    {
    }

    public void debug(String arg0, Object arg1)
    {
    }

    public void debug(String arg0, Object[] arg1)
    {
    }

    public void debug(Marker arg0, String arg1)
    {
    }

    public void debug(String arg0, Object arg1, Object arg2)
    {
    }

    public void debug(Marker arg0, String arg1, Object arg2)
    {
    }

    public void debug(Marker arg0, String arg1, Object[] arg2)
    {
    }

    public void debug(Marker arg0, String arg1, Throwable arg2)
    {
    }

    public void debug(Marker arg0, String arg1, Object arg2, Object arg3)
    {
    }

    public void error(String arg0, Object arg1)
    {
    }

    public void error(String arg0, Object[] arg1)
    {
    }

    public void error(Marker arg0, String arg1)
    {
    }

    public void error(String arg0, Object arg1, Object arg2)
    {
    }

    public void error(Marker arg0, String arg1, Object arg2)
    {
    }

    public void error(Marker arg0, String arg1, Object[] arg2)
    {
    }

    public void error(Marker arg0, String arg1, Throwable arg2)
    {
    }

    public void error(Marker arg0, String arg1, Object arg2, Object arg3)
    {
    }

    public String getName()
    {
        return "MyNullLogger";
    }

    public void info(String arg0, Object arg1)
    {
    }

    public void info(String arg0, Object[] arg1)
    {
    }

    public void info(Marker arg0, String arg1)
    {
    }

    public void info(String arg0, Object arg1, Object arg2)
    {
    }

    public void info(Marker arg0, String arg1, Object arg2)
    {
    }

    public void info(Marker arg0, String arg1, Object[] arg2)
    {
    }

    public void info(Marker arg0, String arg1, Throwable arg2)
    {
    }

    public void info(Marker arg0, String arg1, Object arg2, Object arg3)
    {
    }

    public boolean isDebugEnabled(Marker arg0)
    {
        return true;
    }

    public boolean isErrorEnabled(Marker arg0)
    {
        return true;
    }

    public boolean isInfoEnabled(Marker arg0)
    {
        return true;
    }

    public boolean isTraceEnabled()
    {
        return false;
    }

    public boolean isTraceEnabled(Marker arg0)
    {
        return false;
    }

    public boolean isWarnEnabled(Marker arg0)
    {
        return true;
    }

    public void trace(String arg0)
    {
    }

    public void trace(String arg0, Object arg1)
    {
    }

    public void trace(String arg0, Object[] arg1)
    {
    }

    public void trace(String arg0, Throwable arg1)
    {
    }

    public void trace(Marker arg0, String arg1)
    {
    }

    public void trace(String arg0, Object arg1, Object arg2)
    {
    }

    public void trace(Marker arg0, String arg1, Object arg2)
    {
    }

    public void trace(Marker arg0, String arg1, Object[] arg2)
    {
    }

    public void trace(Marker arg0, String arg1, Throwable arg2)
    {
    }

    public void trace(Marker arg0, String arg1, Object arg2, Object arg3)
    {
    }

    public void warn(String arg0, Object arg1)
    {
    }

    public void warn(String arg0, Object[] arg1)
    {
    }

    public void warn(Marker arg0, String arg1)
    {
    }

    public void warn(String arg0, Object arg1, Object arg2)
    {
    }

    public void warn(Marker arg0, String arg1, Object arg2)
    {
    }

    public void warn(Marker arg0, String arg1, Object[] arg2)
    {
    }

    public void warn(Marker arg0, String arg1, Throwable arg2)
    {
    }

    public void warn(Marker arg0, String arg1, Object arg2, Object arg3)
    {
    }
}