package org.jacorb.orb;

import org.omg.CORBA.*;
import org.omg.CORBA.portable.*;
import java.lang.reflect.*;

/**
 * This class provides a method for inserting an arbirtary
 * application exception into an any.
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public class ApplicationExceptionHelper  
{

    /**
     * This method tries to insert the given ApplicationException into the
     * given any by deriving the helper name from object id. <br>
     * All exceptions are propagated upward to be handled there.
     */

    public static void insert(org.omg.CORBA.Any any, ApplicationException  s)
        throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException,
               InvocationTargetException
    {

        String name = s.getId();
        org.jacorb.util.Debug.output(2, "Trying to build Helper for >>" + name + 
                                     "<< (" + s.getClass().getName() + ")");

        //((name.startsWith("IDL:omg.org"))? "org.omg" : "") + 
        name = name.substring( name.indexOf(':') + 1, name.lastIndexOf(':'));
        name = name.replace ('/', '.');

        java.util.StringTokenizer strtok = 
            new java.util.StringTokenizer( name, "." );

        int count = strtok.countTokens();
        String[] scopes = new String[ count ];

        for( int i = 0; strtok.hasMoreTokens(); i++ )
        {
            scopes[i] = strtok.nextToken();
        }

        Class _helper = null;
        int idx = count-2;

        while ( _helper == null && idx >= 0 )
        {
            StringBuffer nameBuf = new StringBuffer();
            for( int j = 0; j < scopes.length-1; j++ )
                nameBuf.append( scopes[j] + "." );

            nameBuf.append( scopes[ scopes.length-1 ] );
            nameBuf.append( "Helper" );

            try
            {
                _helper = Class.forName( nameBuf.toString());
                name = nameBuf.toString();
                name = name.substring( 0, name.indexOf("Helper"));
                break;
            }
            catch( ClassNotFoundException cnf )
            {
                scopes[ idx-- ] +=  "Package";               
            }
            
        }

        if( _helper == null )
            throw new ClassNotFoundException();

        //get read method from helper and invoke it,
        //i.e. read the object from the stream
        Method _read = 
            _helper.getMethod( "read", 
                               new Class[]{ 
                                   Class.forName("org.omg.CORBA.portable.InputStream")
                               }
                               );    
        java.lang.Object _user_ex = 
            _read.invoke(null, new java.lang.Object[]{ s.getInputStream() } );
    
        //get insert method and insert exception into any
        Method _insert = 
            _helper.getMethod("insert", 
                              new Class[]{ Class.forName("org.omg.CORBA.Any"), 
                                           Class.forName( name ) }
                              ); 
        _insert.invoke( null, new java.lang.Object[]{any, _user_ex} );
    }
} // ApplicationExceptionHelper








