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

import java.util.*;
import java.io.*;

/**
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

    private static StringBuffer sb = new StringBuffer();

    private static int _verbosity;
    private static int _category;
    private static PrintWriter _log_file_out = Environment.logFileOut();
    // the length of a String is given by the number of 16 bit unicode
    // characters it contains. The maxLogSize is given in kBytes.
    // This conversion gives us the max number of characters in a
    // single log.
    private static long maxLogSize;
    private static long currentLogSize;
    private static boolean timestamp = Environment.isPropertyOn ("jacorb.log.timestamp");

    /* Debug priorities */

    public static final int QUIET = 0;
    public static final int IMPORTANT = 1;
    public static final int INFORMATION = 2;
    public static final int DEBUG1 = 3;

    /* debug categories, disjoint */

    public static final int ORB_CONNECT = 0x0100;
    public static final int ORB_MISC = 0x0200;
    public static final int POA = 0x0400;
    public static final int IMR = 0x0800;
    public static final int DSI = 0x1000;
    public static final int DII = 0x2000;
    public static final int INTERCEPTOR = 0x4000;

    public static final int DOMAIN = 0x8000;
    public static final int PROXY = 0x010000;
    public static final int COMPILER = 0x020000;
    public static final int TOOLS = 0x040000;

    /* unused */
    /*

       !! Please update names in CAD.java as well,
       if you make updates here !!

       public static final int UNUSED = 0x080000;
       ...
       public static final int ORB_CONNECT = 0x400000;
    */

    public static final int NAMING = 0x01000000;
    public static final int TRADING = 0x02000000;
    public static final int EVENTS = 0x04000000;
    public static final int TRANSACTION = 0x08000000;
    public static final int SECURITY = 0x10000000;

    /* unused */
    /*
      !! Please update names in CAD.java as well,
      if you make updates here !!

      public static final int ORB_CONNECT = 0x20000000;
      public static final int ORB_CONNECT = 0x30000000;
      public static final int RESERVED = 0x40000000;
    */

    static
    {
        initialize();
    }

    public static final void initialize()
    {
        _verbosity = Environment.verbosityLevel() & 0xff;
        _category = Environment.verbosityLevel() & 0xffffff00;
        if( _category == 0 )
            _category = 0xffffff00;
        _log_file_out = Environment.logFileOut();
         maxLogSize = (Environment.maxLogSize ()) * 1024;
         currentLogSize = Environment.currentLogSize ()/2;
    }

    public static boolean canOutput( int msg_level )
    {
        int category = msg_level & 0xffffff00;
        int _msg_level = msg_level & 0xff;

        if( category == 0 )
        {
            category = 0xffffff00;
        }

        return ( (category & _category) != 0 ) &&
            (_msg_level <= _verbosity);
    }

    public static final void output (int msg_level, String msg)
    {
        if (canOutput (msg_level))
        {
            if (timestamp)
            {
               msg = "[ " + Environment.date () + ':' +
                     Environment.time() + "> " + msg + " ]";
            }
            else
            {
               msg = "[ " + msg + " ]";
            }

            if (_log_file_out == null)
            {
                System.out.println (msg);
            }
            else
            {
                if (maxLogSize > 0)
                {
                    if (exceedsMaxLogSize (msg.length ()))
                    {
                        _log_file_out = Environment.logFileOut ();
                    }
                }

                _log_file_out.println (msg);
                _log_file_out.flush ();
            }
        }
    }

    /**
     * Output a buffer in hex format. Note that output synchronizes
     * the calling threads in order to avoid garbled debug output.
     */

    public static synchronized void output(int msg_level,String name,byte bs[])
    {
        output(msg_level,name,bs, 0, bs.length);
    }

    /**
     * Output a buffer in hex format. Note that output synchronizes
     * the calling threads in order to avoid garbled debug output.
     */

    public static synchronized void output(int msg_level,
                                           String name,
                                           byte bs[],
                                           int len)
    {
        output( msg_level,name,bs,0,len );
    }

    public static synchronized void output(int msg_level,
                                           String name,
                                           byte bs[],
                                           int start,
                                           int len)
    {
        if( canOutput( msg_level ) )
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

    public static final void output(int msg_level, Throwable e)
    {
        if( canOutput( msg_level ) )
        {
            if (_log_file_out == null || msg_level == 0)
            {
                System.out.println("############################ StackTrace ############################");
                e.printStackTrace(System.out);
                System.out.println("####################################################################");
            }
            if (_log_file_out != null)
            {
                //Assumption made that will want events prior to exception in
                //same log, no call to exceedsMaxLogSize
                _log_file_out.println("############################ StackTrace ############################");
                e.printStackTrace(_log_file_out);
                _log_file_out.println("####################################################################");
                _log_file_out.flush();
            }
        }
    }


    public static void printTrace(int msg_level)
    {
        if( canOutput( msg_level ) )
        {
            try
            {
                throw new RuntimeException();
            }
            catch( Exception e )
            {
                e.printStackTrace();
            }
        }
    }

    /** throws an myAssertion violation exception if the boolean expression
     *  is not satisfied.
     *  @param expression the expression to be checked
     *  @param msg_level the message level of the myAssertion
     *  @param msg the message to be printed
     *  @exception AssertionViolation
     */

    public static void myAssert(int msg_level, boolean expression, String msg)
    {
        if( canOutput( msg_level ))
        {
            if (! expression)
                throw new AssertionViolation(msg);
        }
    }

    public static void myAssert(boolean expression, String msg)
    {
        myAssert( 1, expression, msg );
    }


    private static boolean exceedsMaxLogSize (int length)
    {
        if ((length + currentLogSize) >= maxLogSize)
        {

        // if to big, call onto Environment.rollLog () - which will copy
        // existing log to a new (backup) file, with date/time appended to name
            Environment.rollLog ();
            //will write this msg to a new log, so reset the currentLogSize
            currentLogSize = length;
            return true;
        }
        else
        {
            //will append (or otherwise) to existing log.
            currentLogSize += length;
            return false;
        }
    }

    /**
     * utility method,
     */
    /*
      public static void dumpBA(byte bs[])
      {
      int len = bs.length;
      for (int i = 0; i < len; i++)
      {
      if (0 == i%20)
      {
      java.lang.System.out.println();
      }
      dumpHex(bs[i]);
      }
      java.lang.System.out.println();
      }
    */
    /**
     * utility method,
     */
    /*
      public static void dumpHex(byte b)
      {
      int n1 = (b & 0xff) / 16;
      int n2 = (b & 0xff) % 16;
      char c1 = (char)(n1>9 ? ('A'+(n1-10)) : ('0'+n1));
      char c2 = (char)(n2>9 ? ('A'+(n2-10)) : ('0'+n2));
      char c3;
      if (b>(byte)31 && b<(byte)127)
      c3 = (char)b;
      else
      c3 = ' ';
      java.lang.System.out.print(c1+(c2+" ")+c3+"  ");
      }
    */
}
