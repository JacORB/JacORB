package org.jacorb.idl;

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

import java.util.*;
import java.io.*;

public final class Environment
{
    private static int _verbosity = 0;

    public static final int verbosityLevel() 
    { 
        return _verbosity; 
    }

    public static final void verbosityLevel(int level) 
    { 
        _verbosity = level; 
    }

        
    public static void printTrace(int msg_level)
    {
        if( msg_level > _verbosity )
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

    public static final void output(int msg_level, String msg) 
    {
	if( msg_level > _verbosity ) 
            return;		
        
        System.out.println( "   [ " + msg + " ]" );
    }

    public static void output(int msg_level, String name, byte bs[])
    {
        output(msg_level, name, bs, bs.length);
    }
	
    public static void output(int msg_level, String name, byte bs[], int len)
    {
        if ( msg_level > _verbosity)
            return;
        
        System.out.print("\nHexdump ["+name+"] len=" + len + ","+ bs.length);
        String chars="";
        for (int i=0; i<len; i++)
        {
            if (0 == i%16)
            {
                System.out.println(chars); 
                chars="";
            }
            chars += toAscii(bs[i]);
            System.out.print(toHex(bs[i]));
            if(3 == i%4 ) 
            { 
                chars +=" "; 
                System.out.print(" "); 
            }
        }
        System.out.println(chars);
    }

    public static final String toHex(byte b)
    {
        int n1 = (b & 0xff) / 16;
        int n2 = (b & 0xff) % 16;
        char c1 = (char)(n1>9 ? ('A'+(n1-10)) : ('0'+n1));
        char c2 = (char)(n2>9 ? ('A'+(n2-10)) : ('0'+n2));
        return ""+ c1 + c2 +" ";
    }
	
    public static final char toAscii(byte b)
    {
        if (b > (byte)31 && b < (byte)127)
            return (char)b; 
        else 
            return '.';
    }
	
    public static final void output(int msg_level, Throwable e) 
    {
        if ( msg_level > _verbosity)
            return;
		
        System.out.println("############################ StackTrace ############################");
        e.printStackTrace(System.out);
        System.out.println("####################################################################");
    }


}

