package org.jacorb.util;

/**
 * A static wrapper around javax.rmi.CORBA.ValueHandler.
 */
public class ValueHandler {

  public static String getRMIRepositoryID (Class clz)
  {
      return javax.rmi.CORBA.Util.createValueHandler()
          .getRMIRepositoryID (clz);
  }

  public static org.omg.SendingContext.RunTime getRunTimeCodeBase() 
  {
      return javax.rmi.CORBA.Util.createValueHandler()
          .getRunTimeCodeBase();
  }

  public static java.io.Serializable readValue 
      (org.omg.CORBA.portable.InputStream in,
       int offset, Class clz, String repositoryID, 
       org.omg.SendingContext.RunTime sender)
  {
      return javax.rmi.CORBA.Util.createValueHandler()
          .readValue (in, offset, clz, repositoryID, sender);
  }

  public static void writeValue (org.omg.CORBA.portable.OutputStream out,
                                 java.io.Serializable value)
  {
      javax.rmi.CORBA.Util.createValueHandler()
          .writeValue (out, value);
  }

}
