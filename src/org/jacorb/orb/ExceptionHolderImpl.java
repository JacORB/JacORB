package org.jacorb.orb;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2004 Gerald Brose.
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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.logger.Logger;
import org.jacorb.ir.RepositoryID;
import org.jacorb.orb.giop.ReplyInputStream;
import org.jacorb.util.ObjectUtil;
import org.omg.CORBA.ExceptionList;
import org.omg.CORBA.UserException;
import org.omg.GIOP.ReplyStatusType_1_2;

/**
 * JacORB-specific implementation of
 * <code>org.omg.Messaging.ExceptionHolder</code>.  An instance of this
 * type is used to pass an exception to a reply handler.
 *
 * @author Andre Spiegel <spiegel@gnu.org>
 * @version $Id$
 */
public class ExceptionHolderImpl
    extends org.omg.Messaging.ExceptionHolder
    implements Configurable
{
    private Logger logger = null;

    /**
     * Constructs an ExceptionHolderImpl object from an input stream.
     * It is assumed that the reply status of this input stream is
     * either USER_EXCEPTION or SYSTEM_EXCEPTION.  If it has another
     * status, a RuntimeException is thrown.
     */
    public ExceptionHolderImpl( ReplyInputStream is )
    {
        int status = is.getStatus().value();
        if ( status == ReplyStatusType_1_2._USER_EXCEPTION )
        {
            is_system_exception = false;
        }
        else if ( status == ReplyStatusType_1_2._SYSTEM_EXCEPTION )
        {
            is_system_exception = true;
        }
        else
        {
            throw new RuntimeException( "attempt to create ExceptionHolder " +
                                        "for non-exception reply" );
        }
        byte_order          = is.littleEndian;
        marshaled_exception = is.getBody();
    }

    public ExceptionHolderImpl(org.omg.CORBA.SystemException ex)
    {
        is_system_exception = true;
        byte_order          = false;

        final CDROutputStream output = new CDROutputStream();

        try
        {
            SystemExceptionHelper.write(output, ex);
            marshaled_exception = output.getBufferCopy();
        }
        finally
        {
            output.close();
        }
    }

    public void configure(org.apache.avalon.framework.configuration.Configuration configuration)
        throws org.apache.avalon.framework.configuration.ConfigurationException
    {
        logger =
            ((org.jacorb.config.Configuration)configuration).getNamedLogger("jacorb.orb.exc_holder");
    }


    public void raise_exception()
        throws UserException
    {
        final CDRInputStream input =
            new CDRInputStream (marshaled_exception, byte_order);

        try
        {
            if ( is_system_exception )
            {
                throw SystemExceptionHelper.read( input );
            }

            input.mark( 0 );
            String id = input.read_string();

            try
            {
                input.reset();
            }
            catch( IOException e )
            {
                logger.warn( "Unexpected IOException: ", e);
            }

            org.omg.CORBA.UserException result = null;
            try
            {
                result = exceptionFromHelper( id, input );
            }
            catch( Exception e )
            {
                throw new org.omg.CORBA.UnknownUserException();
            }
            throw result;
        }
        finally
        {
            input.close();
        }
    }

    public void raise_exception_with_list( ExceptionList exc_list )
        throws UserException
    {
        throw new org.omg.CORBA.NO_IMPLEMENT(
           "raise_exception_with_list not yet implemented" );
    }

    /**
     * For testing.
     */
    public String toString()
    {
        StringBuffer result = new StringBuffer();
        for (int i=0; i<marshaled_exception.length; i++)
        {
            result.append (marshaled_exception[i] +
                           "(" + (char)marshaled_exception[i] + ")  ");
        }
        return result.toString();
    }

    /**
     * Given a repository id, tries to find a helper for the corresponding
     * class and uses it to unmarshal an instance of this class from
     * the given InputStream.
     */
    public org.omg.CORBA.UserException exceptionFromHelper
                                ( String id,
                                  org.omg.CORBA.portable.InputStream input )
        throws ClassNotFoundException,
               NoSuchMethodException,
               IllegalAccessException,
               InvocationTargetException
    {
        String name = RepositoryID.className(id, "Helper", null);

        // if class doesn't exist, let exception propagate
        Class  helperClazz = ObjectUtil.classForName (name);

        // helper must not be null from here on

        // get read method from helper and invoke it,
        // i.e. read the object from the stream
        Method readMethod =
            helperClazz.getMethod( "read",
                               new Class[]{
                                   ObjectUtil.classForName("org.omg.CORBA.portable.InputStream")
                               } );
        java.lang.Object result =
            readMethod.invoke( null,
                               new java.lang.Object[]{ input }
                             );
        return ( org.omg.CORBA.UserException ) result;
    }

    /**
     * Marshals this object into a new buffer and returns that buffer.
     */
    public byte[] marshal()
    {
         final CDROutputStream output = new CDROutputStream();
         try
         {
             output.write_value( this, "IDL:omg.org/Messaging/ExceptionHolder:1.0" );
             return output.getBufferCopy();
         }
         finally
         {
             output.close();
         }
    }
}
