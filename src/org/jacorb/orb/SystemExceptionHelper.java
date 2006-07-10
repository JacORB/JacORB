/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
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
 *
 */

package org.jacorb.orb;

import java.lang.reflect.*;

import org.jacorb.orb.giop.ReplyInputStream;
import org.jacorb.util.ObjectUtil;
import org.omg.IOP.*;

/**
 * @author Gerald Brose
 * @version $Id$
 */

public class SystemExceptionHelper
{
    private SystemExceptionHelper()
    {
        // utility class
    }

    private static final String className( String repId )
    {
        // cut "IDL:" and version
        String id_base = repId.substring(4, repId.lastIndexOf(':'));
        return ir2scopes("org.omg",id_base.substring(7));
    }

    private static final String ir2scopes( String prefix, String s )
    {
        if( s.indexOf('/') < 0)
        {
            return s;
        }
        java.util.StringTokenizer strtok =
            new java.util.StringTokenizer( s, "/" );

        int count = strtok.countTokens();
        StringBuffer buffer = new StringBuffer();
        buffer.append(prefix);

        for( int i = 0; strtok.hasMoreTokens(); i++ )
        {
            String nextToken = strtok.nextToken();
            try
            {
                if( buffer.length() > 0 )
                {
                    ObjectUtil.classForName( buffer.toString() + "." + nextToken );
                }
                else
                {
                    ObjectUtil.classForName( nextToken );
                }

                if( i < count-1)
                {
                    buffer.append('.');
                    buffer.append(nextToken);
                    buffer.append("Package");
                }
                else
                {
                    buffer.append('.');
                    buffer.append(nextToken);
                }
            }
            catch ( ClassNotFoundException cnfe )
            {
                if( buffer.length() > 0 )
                {
                    buffer.append('.');
                    buffer.append(nextToken);
                }
                else
                {
                    buffer.append( nextToken );
                }
            }
        }

        return buffer.toString();
    }

    private static final String repId( Class clazz )
    {
        String className = clazz.getName();
        String body = className.substring(7);
        return "IDL:omg.org/" + scopesToIR(body) + ":1.0";
    }

    private static final String scopesToIR( String s )
    {
        if( s.indexOf('.') < 0)
        {
            return s;
        }
        java.util.StringTokenizer strtok = new java.util.StringTokenizer( s, "." );
        String scopes[] = new String[strtok.countTokens()];
        for( int i = 0; strtok.hasMoreTokens(); i++ )
        {
            String nextToken = strtok.nextToken();
            if( nextToken.endsWith("Package"))
            {
                scopes[i] = nextToken.substring(0,nextToken.indexOf("Package"));
            }
            else
            {
                scopes[i] = nextToken;
            }
        }
        StringBuffer buffer = new StringBuffer();
        if( scopes.length > 1 )
        {
            for( int i = 0; i < scopes.length-1; i++)
            {
                buffer.append(scopes[i]);
                buffer.append('/');
            }
        }

        buffer.append( scopes[scopes.length-1] );
        return buffer.toString();
    }

    public static void insert(org.omg.CORBA.Any any, org.omg.CORBA.SystemException exception)
    {
        any.type( type( exception ));
        write( any.create_output_stream(), exception);
    }

    public static org.omg.CORBA.TypeCode type( org.omg.CORBA.SystemException exception)
    {
        String name = exception.getClass().getName();
        name = name.substring(name.lastIndexOf('.') + 1);
        org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init();

        org.omg.CORBA.TypeCode _type =
            orb.create_struct_tc(
                    "IDL:omg.org/CORBA/" + name + ":1.0",
                    name,
                    new org.omg.CORBA.StructMember[]{
                            new org.omg.CORBA.StructMember(
                                    "minor",
                                    orb.get_primitive_tc(org.omg.CORBA.TCKind.from_int(3)),
                                    null),
                                    new org.omg.CORBA.StructMember(
                                            "completed",
                                            orb.create_enum_tc("IDL:omg.org/CORBA/CompletionStatus:1.0",
                                                    "CompletionStatus",
                                                    new String[]{"COMPLETED_YES","COMPLETED_NO","COMPLETED_MAYBE"}),
                                                    null)
                    });
        return _type;
    }

    public static org.omg.CORBA.SystemException read(org.omg.CORBA.portable.InputStream in)
    {
        final String className = className(in.read_string());
        final int minor = in.read_long();
        final org.omg.CORBA.CompletionStatus completed =
            org.omg.CORBA.CompletionStatusHelper.read(in);

        String message = null;

        if (in instanceof ReplyInputStream)
        {
            final ReplyInputStream input = (ReplyInputStream)in;

            try
            {
                final ServiceContext context = input.getServiceContext(ExceptionDetailMessage.value);
                if (context != null)
                {
                    final CDRInputStream data = new CDRInputStream(null, context.context_data);

                    try
                    {
                        data.openEncapsulatedArray();
                        message = data.read_wstring();
                    }
                    finally
                    {
                        data.close();
                    }
                }
            }
            finally
            {
                input.close();
            }
        }

        try
        {
            Class clazz = ObjectUtil.classForName( className );
            Constructor ctor =
                clazz.getConstructor(
                        new Class[]{ String.class,
                                int.class,
                                org.omg.CORBA.CompletionStatus.class});

            return (org.omg.CORBA.SystemException)ctor.newInstance(
                    new Object[]{"Server-side Exception: " + message,
                            ObjectUtil.newInteger(minor),
                            completed});
        }
        catch (Exception e )
        {
            return (org.omg.CORBA.SystemException)
            new org.omg.CORBA.UNKNOWN(className).initCause(e);
        }
    }

    public static void write(org.omg.CORBA.portable.OutputStream out,
                             org.omg.CORBA.SystemException exception)
    {
        out.write_string(repId(exception.getClass()));
        out.write_long(exception.minor);
        org.omg.CORBA.CompletionStatusHelper.write(out,exception.completed);
    }
}
