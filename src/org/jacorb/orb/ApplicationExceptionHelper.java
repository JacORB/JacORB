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

public class ApplicationExceptionHelper  {

  /**
   * This method tries to insert the given ApplicationException into the
   * given any by deriving the helper name from object id. <br>
   * All exceptions are propagated upward to be handled there.
   */
  public static void insert(org.omg.CORBA.Any any, ApplicationException  s)
    throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException,
	   InvocationTargetException{

    String name = s.getId();
    org.jacorb.util.Debug.output(2, "Trying to build Helper for >>" + name + "<<");

    //((name.startsWith("IDL:omg.org"))? "org.omg" : "") + 
    name =  name.substring(name.indexOf(':') + 1, name.lastIndexOf(':'));
    name = name.replace ('/', '.');

    Class _helper = Class.forName(name + "Helper");

    //get read method from helper and invoke it,
    //i.e. read the object from the stream
    Method _read = _helper.getMethod("read", new Class[]{Class.forName("org.omg.CORBA.portable.InputStream")});    
    java.lang.Object _user_ex = _read.invoke(null, new java.lang.Object[]{s.getInputStream()});
    
    //get insert method and insert exception into any
    Method _insert = _helper.getMethod("insert", new Class[]{Class.forName("org.omg.CORBA.Any"), 
							     Class.forName(name)}); 
    _insert.invoke(null, new java.lang.Object[]{any, _user_ex});
  }
} // ApplicationExceptionHelper


