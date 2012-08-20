package org.jacorb.orb;

/*
 *        JacORB - a free Java ORB
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
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.jacorb.config.Configurable;
import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;
import org.jacorb.ir.RepositoryID;
import org.jacorb.orb.giop.ReplyInputStream;
import org.jacorb.util.ObjectUtil;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.ExceptionList;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.UserException;
import org.omg.GIOP.ReplyStatusType_1_2;
import org.slf4j.Logger;

/**
 * JacORB-specific implementation of
 * <code>org.omg.Messaging.ExceptionHolder</code>.  An instance of this
 * type is used to pass an exception to a reply handler.
 *
 * @author Andre Spiegel <spiegel@gnu.org>
 */
public class ExceptionHolderImpl
    extends org.omg.Messaging.ExceptionHolder
    implements Configurable
{
    private Logger logger = null;
    private final ORB orb;

    /**
     * No-arg constructor for demarshaling.
     */
    public ExceptionHolderImpl(ORB orb)
    {
        super();

        this.orb = orb;

        try
        {
           configure (orb.getConfiguration ());
        }
        catch (ConfigurationException ex)
        {
           throw new INTERNAL ("Caught configuration exception." + ex);
        }
    }

    /**
     * Constructs an ExceptionHolderImpl object from an input stream.
     * It is assumed that the reply status of this input stream is
     * either USER_EXCEPTION or SYSTEM_EXCEPTION.  If it has another
     * status, a RuntimeException is thrown.
     */
    public ExceptionHolderImpl(ORB orb, ReplyInputStream inputStream )
    {
        this(orb);

        int status = inputStream.getStatus().value();
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
            throw new BAD_PARAM( "attempt to create ExceptionHolder " +
                                        "for non-exception reply" );
        }
        byte_order          = inputStream.getLittleEndian();
        marshaled_exception = inputStream.getBody();
    }

    public ExceptionHolderImpl(ORB orb, org.omg.CORBA.SystemException exception)
    {
        this(orb);

        is_system_exception = true;
        byte_order          = false;

        final CDROutputStream out = new CDROutputStream(orb);

        try
        {
            SystemExceptionHelper.write(out, exception);
            marshaled_exception = out.getBufferCopy();
        }
        finally
        {
            out.close();
        }
    }

    public void configure(Configuration configuration)
        throws ConfigurationException
    {
        logger =
            ((org.jacorb.config.Configuration)configuration).getLogger("jacorb.orb.exc_holder");
    }


    public void raise_exception_with_list (TypeCode[] exc_list) throws UserException
    {
        throw new NO_IMPLEMENT ("NYI");
    }

    public void raise_exception()
        throws UserException
    {
        final CDRInputStream input =
            new CDRInputStream (orb, marshaled_exception, byte_order);

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
                logger.error( "Unexpected IOException: ", e);

                throw new INTERNAL("Unexpected IOException: " + e);
            }

            final UserException result;
            try
            {
                result = exceptionFromHelper( id, input );
            }
            catch( Exception e )
            {
                logger.error("error reading exception", e);

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
            result.append (marshaled_exception[i]);
            result.append ('(');
            result.append ((char)marshaled_exception[i]);
            result.append (")  ");
        }
        return result.toString();
    }

    /**
     * Given a repository id, tries to find a helper for the corresponding
     * class and uses it to unmarshal an instance of this class from
     * the given InputStream.
     */
    public org.omg.CORBA.UserException exceptionFromHelper
                                ( String repositoryID,
                                  org.omg.CORBA.portable.InputStream input )
        throws ClassNotFoundException,
               NoSuchMethodException,
               IllegalAccessException,
               InvocationTargetException
    {
        final String helperClassName = RepositoryID.className(repositoryID, "Helper", null);

        try
        {
            final Class<?> helperClazz = ObjectUtil.classForName (helperClassName);

            return exceptionFromHelper(input, helperClazz);
        }
        catch (ClassNotFoundException e)
        {
            final String repositoryIDWithoutPragmaPrefix = stripPragmaPrefix(repositoryID);

            if ( ! repositoryIDWithoutPragmaPrefix.equals (repositoryID))
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("unable to locate class " + helperClassName + " for repository ID " + repositoryID + ". Retrying without pragma prefix: " + repositoryIDWithoutPragmaPrefix);
                }
                return exceptionFromHelper(repositoryIDWithoutPragmaPrefix, input);
            }
            else
            {
                // if class doesn't exist, let exception propagate
                throw e;
            }
        }
    }


    /**
     * try to strip off pragma prefix information.
     * returns the unmodified string if not possible
     */
    private String stripPragmaPrefix(String original)
    {
        final int index = original.indexOf('.', 4);
        final int versionIndex = original.lastIndexOf(':');

        if (index > 0 && index < versionIndex)
        {
            return "IDL:" + original.substring(original.indexOf('/') + 1);
        }
        return original;
    }


    private org.omg.CORBA.UserException exceptionFromHelper(org.omg.CORBA.portable.InputStream input, Class<?> helperClazz) throws NoSuchMethodException, ClassNotFoundException, IllegalAccessException, InvocationTargetException
    {
        // helper must not be null from here on

        // get read method from helper and invoke it,
        // i.e. read the object from the stream
        final Method readMethod =
        helperClazz.getMethod( "read", new Class[]{ org.omg.CORBA.portable.InputStream.class });

        return ( org.omg.CORBA.UserException ) readMethod.invoke( null, new java.lang.Object[]{ input } );
    }

    /**
     * Marshals this object into a new buffer and returns that buffer.
     */
    public byte[] marshal()
    {
         final CDROutputStream out = new CDROutputStream( orb );
         try
         {
             out.write_value( this, "IDL:omg.org/Messaging/ExceptionHolder:1.0" );
             return out.getBufferCopy();
         }
         finally
         {
             out.close();
         }
    }
}
