package org.jacorb.config;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2009  Gerald Brose.
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
 *   Software Foundation, 51 Franklin Street, Fifth Floor, Boston,
 *   MA 02110-1301, USA.
 */

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Level;

/**
 * A formatter for JDK logs which produces a more concise log output
 * than the standard JDK setting.
 * 
 * @author Andre Spiegel <spiegel@gnu.org>
 * @version $Id$
 */
public class JacORBLogFormatter extends Formatter
{
    private static final DateFormat timeFormat
      = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss.SSS");
    
    public String format (LogRecord record)
    {
        long time = record.getMillis();
        String loggerName = record.getLoggerName();
        Level level = record.getLevel();
        String message = record.getMessage();
        Throwable t = record.getThrown();
        StringBuffer result = new StringBuffer();
        result.append (timeFormat.format (new Long(time)));
        result.append (" ");
        result.append (level);
        result.append (" ");
        result.append (message);
        result.append ("\n");
        return t == null ? result.toString() 
                         : result.toString() + getStackTrace (t);
    }
    
    private String getStackTrace (Throwable t)
    {
        StringBuffer result = new StringBuffer();
        StackTraceElement[] elements = t.getStackTrace();
        for (int i=0; i < elements.length; i++)
        {
            result.append ("    ");
            result.append (elements[i].toString());
            result.append ("\n");
        }
        return result.toString();
    }

}
