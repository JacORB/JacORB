package org.jacorb.util;
/**
 * This class is intended to run applications in a single
 * VM with a maximum amount of separation between each other.
 * Every application gets its own class loader, therefore static 
 * members don't conflict. Every application also has its own 
 * set of system properties. <br>
 *
 * WARNING: This class replaces the system properties by the
 * class <code>org.jacorb.util.ThreadSystemProperties</code>,
 * which behaves slightly different from the standard properties
 * class.
 *
 * @author Nicolas Noffke
 * $Id$
 */
import java.util.*;
import java.net.*;
import java.lang.reflect.*;

public class CapsuleThread 
    extends Thread 
{
    private static URL[] class_path = null;

    private String class_name = null;
    private String[] args = null;
    private Properties props = null;

    private ClassLoader capsule_cl = null;

    static
    {
        createClassPathURLs();
        System.setProperties(new ThreadSystemProperties(System.getProperties()));
    }

    public CapsuleThread( String class_name,
                          String[] args,
                          Properties props ) 
        throws ClassNotFoundException,
        NoSuchMethodException        
    {
        this.class_name = class_name;
        this.args = args;
        this.props = props;

        capsule_cl = new URLClassLoader( class_path, null );
        
        //just a test run to check if args are ok
        Class app_class = Class.forName( class_name, true, capsule_cl );
        app_class.getDeclaredMethod( "main", new Class[]{args.getClass()} ); 

        setContextClassLoader( capsule_cl );
    }
    
    private static void createClassPathURLs()
    {
        if( class_path == null )
        {
            //set both orb properties, might have already
            //been done by the jaco script
            System.setProperty( "org.omg.CORBA.ORBClass",
                                "jacorb.orb.ORB" );
            System.setProperty( "org.omg.CORBA.ORBSingletonClass",
                                "jacorb.orb.ORBSingleton" );

            StringTokenizer tok = 
                new StringTokenizer( System.getProperty( "java.class.path" ),
                                     System.getProperty( "path.separator" ));

            Vector v = new Vector();

            while( tok.hasMoreTokens() )
            {
                v.add( tok.nextToken() );
            }
            
            class_path = new URL[ v.size() ];

            //bring raw classpathes into url format
            for( int i = 0; i < v.size(); i++ )
            {
                String s = (String) v.get( i );
                //dos to unix
                s = s.replace( '\\', '/' );

                if( s.startsWith( "/" ))
                {
                    //unix path
                    s = "file:" + s;
                }
                else
                {
                    //dos path
                    s = "file:/" + s;
                }
                
                if(! (s.endsWith( ".zip" ) ||
                      s.endsWith( ".jar" )))
                {
                    s = s + "/";
                }

                try
                {                
                    class_path[i] = new URL( s );                    
                }
                catch( MalformedURLException e )
                {
                    //this is not supposed to happen
                    e.printStackTrace();
                }
            }
        }
    }


    public void run()
    {
        try
        {
            if( props != null )
            {
                for( Enumeration e = props.propertyNames();
                     e.hasMoreElements(); )
                {
                    String key = (String) e.nextElement();
                    System.setProperty( key, props.getProperty( key ));
                }                    
            }

            Class app_class = Class.forName( class_name, true, capsule_cl );
            Method main = 
                app_class.getDeclaredMethod( "main", 
                                             new Class[]{ args.getClass()} );

            //run app
            main.invoke( null, new Object[]{args} );           
        }
        catch( Throwable th )
        {
            th.printStackTrace();
        }
    }
} // CapsuleThread






