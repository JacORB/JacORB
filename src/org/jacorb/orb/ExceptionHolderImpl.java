package org.jacorb.orb;

import java.io.*;
import java.lang.reflect.*;

import org.omg.GIOP.*;
import org.omg.CORBA.ExceptionList;
import org.omg.CORBA.UnknownUserException;
import org.omg.CORBA.UserException;

import org._orgModule;
import org.jacorb.ir.*;
import org.jacorb.util.*;
import org.jacorb.orb.connection.*;

public class ExceptionHolderImpl extends org.omg.Messaging.ExceptionHolder
{
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

    /**
     * No-arg constructor for demarshaling.
     */
    public ExceptionHolderImpl()
    {
        super();
    }

    public void raise_exception() throws UserException
    {
        CDRInputStream input = 
            new CDRInputStream (null, marshaled_exception, byte_order);
        if ( is_system_exception )
        {
            throw SystemExceptionHelper.read( input );
        }
        else
        {
            input.mark( 0 );
            String id = input.read_string();
            try
            {
                input.reset();
            }
            catch( IOException ioe )
            {
                Debug.output( Debug.IMPORTANT, 
                              "Unexpected IOException: " + ioe );
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
    }

    public void raise_exception_with_list( ExceptionList exc_list )
        throws UserException
    {
        throw new org.omg.CORBA.NO_IMPLEMENT(
           "raise_exception_with_list not yet implemented" );
    }

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
        String name = RepositoryID.className( id );
        Class  helper = null;

        //first, try with unmodified name
        try
        {
            helper = Class.forName( name + "Helper" );
        }
        catch( ClassNotFoundException cnf )
        {
        }
        
        //not found, try with "Package" inserted
        if( helper == null )
        {
            StringBuffer buf = new StringBuffer( name );
            buf.insert( name.lastIndexOf( '.' ),
                        "Package" );
            
            name = buf.toString();
            
            //don't try-catch here, so the exception will make this
            //method return
            helper = Class.forName( name + "Helper" );
        }

        //_helper must not be null from here on
        
        //get read method from helper and invoke it,
        //i.e. read the object from the stream
        Method readMethod = 
            helper.getMethod( "read", 
                               new Class[]{ 
                                   Class.forName("org.omg.CORBA.portable.InputStream")
                               } );    
        java.lang.Object result = 
            readMethod.invoke( null, 
                               new java.lang.Object[]{ input }
                             );
        return ( org.omg.CORBA.UserException ) result;           
    }
    
    public byte[] marshal()
    {
         byte[] buffer = 
             BufferManager.getInstance()
                          .getBuffer( marshaled_exception.length + 128 );
         CDROutputStream output = new CDROutputStream( buffer );
         output.write_value( this, "IDL:omg.org/Messaging/ExceptionHolder:1.0" );
         return buffer;
    }  
}
