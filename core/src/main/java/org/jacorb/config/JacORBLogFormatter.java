package org.jacorb.config;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2014 Gerald Brose / The JacORB Team.
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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * A formatter for JDK logs which produces a more concise log output
 * than the standard JDK setting.
 *
 * @author Andre Spiegel {@literal <spiegel@gnu.org>}
 * @author Nick Cross
 */
public class JacORBLogFormatter extends Formatter
{
    public static enum ClockFormat
    {
        TIME,
        DATE_TIME,
        NONE;

        public static ClockFormat getClockFormat (String cf) throws ConfigurationException
        {
            try
            {
                return valueOf (cf.toUpperCase());
            }
            catch (IllegalArgumentException e)
            {
                throw new ConfigurationException ("Invalid type for ClockFormat", e);
            }
        }
    };

    private final DateFormat timeFormat;

    private boolean showThread;
    private boolean showSrcInfo;
    private ClockFormat clockFormat;

    public JacORBLogFormatter (boolean show_thread, boolean srcinfo, ClockFormat cf)
    {
        showThread = show_thread;
        showSrcInfo = srcinfo;
        clockFormat = cf;

        if (clockFormat == ClockFormat.DATE_TIME)
        {
            timeFormat = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss.SSS");
        }
        else
        {
            timeFormat = null;
        }
    }

    @Override
    public String format (LogRecord record)
    {
        String result;

        result = String.format
        (
            (clockFormat != ClockFormat.NONE ? "%s %s " : "%s%s ") +
            (showSrcInfo ? "%s::%s" : "%s%s") +
            (showThread ? " [%d] " : "%s" ) +
            "%s\n",
            (clockFormat != ClockFormat.NONE ? getClockFormat(record.getMillis()) : ""),
            record.getLevel(),
            (showSrcInfo ? record.getSourceClassName() : ""),
            (showSrcInfo ? record.getSourceMethodName() : ""),
            (showThread ? record.getThreadID() : ""),
            record.getMessage()
        );

        Throwable t = record.getThrown();
        return t == null ? result : result + getStackTrace (t);
    }

    private String getStackTrace (Throwable t)
    {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter (sw));
        return sw.toString();
    }

    private String getClockFormat (long millis)
    {
        switch (clockFormat)
        {
            case NONE:
            {
                return "";
            }
            case TIME:
            {
                return String.format("%02d:%02d:%02d",
                        (millis / (1000 * 60 * 60)) % 24,
                        (millis / (1000 * 60)) % 60,
                        (millis / 1000) % 60 );
            }
            case DATE_TIME:
            {
                return timeFormat.format (millis);
            }
            default:
                throw new RuntimeException ("Invalid clock format type");
        }
    }
}
