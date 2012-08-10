package org.jacorb.config;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2012 Gerald Brose / The JacORB Team.
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

/**
 * A formatter for JDK logs which produces a more concise log output
 * than the standard JDK setting.
 *
 * @author Andre Spiegel <spiegel@gnu.org>
 */
public class JacORBLogFormatter extends Formatter
{
    private final DateFormat timeFormat = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss.SSS");

    private boolean showThread = false;

    public JacORBLogFormatter (boolean show_thread)
    {
        showThread = show_thread;
    }

    public String format (LogRecord record)
    {
        String result;
        if (showThread) {
            result = String.format
                ( "%s %s [%d] %s\n",
                  timeFormat.format (record.getMillis()),
                  record.getLevel(),
                  record.getThreadID(),
                  record.getMessage() );
        }
        else {
            result = String.format
                ( "%s %s %s\n",
                  timeFormat.format (record.getMillis()),
                  record.getLevel(),
                  record.getMessage() );
        }

        Throwable t = record.getThrown();
        return t == null ? result : result + getStackTrace (t);
    }

    private String getStackTrace (Throwable t)
    {
        StringBuffer result = new StringBuffer();
        for (StackTraceElement ste : t.getStackTrace()) {
            result.append ("    ");
            result.append (ste.toString());
            result.append ("\n");
        }
        return result.toString();
    }

}
