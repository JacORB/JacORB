package org.jacorb.util;

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

import org.apache.avalon.framework.logger.*;

import java.util.*;
import java.io.*;

/**
 * Central anchor class to retrieve loggers, or to log messages
 * directly. This class acts as a facde and shields clients from the
 * actual log mechanisms. In its current state of evolution, it
 * returns Apache Avalong loggers, which permit still other log
 * backends, such as Apache logkit, which is the current default. The
 * actual creation of logger instances based on configuration
 * parameters is done in Environment, however.
 *
 * @author Gerald Brose
 * @version $Id$
 */

public final class Debug
{
    /* private variables */
    //for byte -> hexchar
    private static final char[] lookup =
    new char[]{ '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    /** the root logger instance */
    private static Logger logger;

    static
    {
        initialize();
    }

    public static final void initialize()
    {
        logger = Environment.getLogger();
    }

    /**
     * <code>isDebugEnabled</code> allows fast efficient checking of whether
     * debug is enabled. This ensures any inefficient String concatenations
     * can be done inside an if statement. @see output(int,String) output(int,String)
     *
     * @return a <code>boolean</code> value
     */

    public static boolean isDebugEnabled()
    {
        return logger.isDebugEnabled();
    }


    /**
     * <code>output</code> the following message. Useful in conjunction with canOutput or
     * isDebugEnabled.
     *
     * @param message a <code>String</code> value
     */

    public static void output (String msg)
    {
        if (logger == null)
        {
            System.out.println(msg);
        }
        else
        {
            logger.debug(msg);
        }
    }


    /**
     * <code>output</code> a message.
     *
     * @param msg_level an <code>int</code> value
     * @param msg a <code>String</code> value
     * @deprecated As this method can be inefficient for debug statements
     *             use {@link #isDebugEnabled() isDebugEnabled} and
     *             {@link #output(String) output(String)} methods in the form:
     *             if (Debug.isDebugEnabled ())
     *             {
     *                Debug.output ("<text>" + value);
     *             }
     */

    public static final void output (int msg_level, String msg)
    {
        if ( logger == null)
        {
            System.out.println (msg);
        }
        else
        {
            logger.debug(msg);
        }
    }


    /**
     * Output a buffer in hex format. Note that output synchronizes
     * the calling threads in order to avoid garbled debug output.
     * @deprecated As this method can be inefficient for debug statements
     *             use {@link #isDebugEnabled() isDebugEnabled} and
     *             {@link #output(String,byte[]) output(String,byte[])} methods
     *             in the form:
     *             if (Debug.isDebugEnabled ())
     *             {
     *                Debug.output ("<text>", value);
     *             }
     */
    public static synchronized void output(int msg_level,String name,byte bs[])
    {
        output(msg_level, name, bs, 0, bs.length);
    }


   /**
     * <code>output</code> a buffer in hex format. Useful in conjunction with
     * canOutput or isDebugEnabled. Note that output synchronizes the calling
     * threads in order to avoid garbled debug output
     *
     * @param message a <code>String</code> value
     * @param bs a <code>byte[]</code> value
     */

    public static synchronized void output(String name,byte bs[])
    {
        output(3, name, bs, 0, bs.length);
    }


    /**
     * Output a buffer in hex format to System.out. Note that output
     * synchronizes the calling threads in order to avoid garbled
     * debug output.
     */

    public static synchronized void output(int msg_level,
                                           String name,
                                           byte bs[],
                                           int len)
    {
        output( msg_level,name,bs,0,len );
    }


    /**
     * Output a buffer in hex format to System.out. Note that output
     * synchronizes the calling threads in order to avoid garbled
     * debug output.
     */

    public static synchronized void output( int msg_level,
                                            String name,
                                            byte bs[],
                                            int start,
                                            int len)
    {
        if (logger != null && logger.isDebugEnabled() )
        {

            System.out.print("\nHexdump ["+name+"] len="+len+","+bs.length);
            StringBuffer chars = new StringBuffer();

            for( int i = start; i < (start + len); i++ )
            {
                if((i % 16 ) == 0)
                {
                    System.out.println( chars );
                    chars = new StringBuffer();
                }

                chars.append( toAscii( bs[i] ));

                System.out.print( toHex( bs[i] ));

                if( (i % 4) == 3 )
                {
                    chars.append( ' ' );
                    System.out.print( ' ' );
                }
            }

            if( len % 16 != 0 )
            {
                int pad = 0;
                int delta_bytes = 16 - (len % 16);

                //rest of line (no of bytes)
                //each byte takes two chars plus one ws
                pad = delta_bytes * 3;

                //additional whitespaces after four bytes
                pad += (delta_bytes / 4);

                for( int i = 0; i < pad; i++ )
                {
                    chars.insert( 0, ' ' );
                }
            }

            System.out.println( chars );
        }
    }


    /**
     * <code>toHex</code> converts a byte into a readable string.
     *
     * @param b a <code>byte</code> value
     * @return a <code>String</code> value
     */

    public static final String toHex( byte b )
    {
        StringBuffer sb = new StringBuffer();

        int upper = (b >> 4) & 0x0F;
        sb.append( lookup[upper] );

        int lower = b & 0x0F;
        sb.append( lookup[lower] );

        sb.append( ' ' );

        return sb.toString();
    }

    public static final char toAscii(byte b)
    {
        if( b > (byte) 31 &&
            b < (byte) 127)
        {
            return (char) b;
        }
        else
        {
            return '.';
        }
    }

    /**
     * convenience method to output stack traces
     */

    public static final void output(int msg_level, Throwable e)
    {
        if (logger == null || msg_level == 0)
        {
            System.out.println("############################ StackTrace ############################");
            e.printStackTrace(System.out);
            System.out.println("####################################################################");
        }
        if (logger != null)
        {
            if (logger.isErrorEnabled())
            {
                try
                {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    PrintStream pos = new PrintStream( bos);
                    e.printStackTrace( pos );
                    bos.close();
                    pos.close();
                    logger.error( bos.toString() );
                }
                catch (IOException io )
                {
                }
            }
        }
    }

    /**
     * Convenience method.
     * Factory for logger instances, delegates to the actual factory
     * set up in org.jacorb.util.Environment.
     */

    public static Logger getNamedLogger(String name)
    {
        return Environment.getLoggerFactory().getNamedLogger(name);
    }

    /**
     * Convenience method.
     * Factory for logger instances, delegates to the actual factory
     * set up in org.jacorb.util.Environment.
     */

    public static Logger getNamedRootLogger(String name)
    {
        return Environment.getLoggerFactory().getNamedRootLogger(name);
    }

}
