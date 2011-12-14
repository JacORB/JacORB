package org.jacorb.util;

import javax.rmi.CORBA.ValueHandlerMultiFormat;
import org.omg.CORBA.MARSHAL;

/**
 * A static wrapper around classes in javax.rmi.
 */
public class ValueHandler
{
   public static final byte STREAM_FORMAT_VERSION_1 = (byte)1;
   public static final byte STREAM_FORMAT_VERSION_2 = (byte)2;


    public static String getRMIRepositoryID(Class clz)
    {
        return javax.rmi.CORBA.Util.createValueHandler()
                .getRMIRepositoryID(clz);
    }

    public static org.omg.SendingContext.RunTime getRunTimeCodeBase()
    {
        return javax.rmi.CORBA.Util.createValueHandler().getRunTimeCodeBase();
    }

    public static java.io.Serializable readValue(
            org.omg.CORBA.portable.InputStream in, int offset, Class clz,
            String repositoryID, org.omg.SendingContext.RunTime sender)
    {
        return javax.rmi.CORBA.Util.createValueHandler().readValue(in, offset,
                clz, repositoryID, sender);
    }

    public static void writeValue(org.omg.CORBA.portable.OutputStream out,
            java.io.Serializable value)
    {
       byte version = ValueHandler.getMaximumStreamFormatVersion (out);
       javax.rmi.CORBA.ValueHandler vh = javax.rmi.CORBA.Util.createValueHandler();

       if (version == ValueHandler.STREAM_FORMAT_VERSION_1)
       {
          vh.writeValue (out, value);
       }
       else if (version == ValueHandler.STREAM_FORMAT_VERSION_2)
       {
          ((ValueHandlerMultiFormat)vh).writeValue (out, value, ValueHandler.STREAM_FORMAT_VERSION_2);
       }
       else
       {
          throw new MARSHAL ("Unsupported stream format version.");
       }
    }

    public static boolean isCustomMarshaled(Class clz)
    {
        return javax.rmi.CORBA.Util.createValueHandler().isCustomMarshaled(clz);
    }

    public static java.io.Serializable writeReplace(java.io.Serializable value)
    {
        return javax.rmi.CORBA.Util.createValueHandler().writeReplace(value);
    }

    /**
     * From javax.rmi.CORBA.Util.
     */
    public static Class loadClass(String className, String remoteCodebase,
            ClassLoader loader) throws ClassNotFoundException
    {
        return javax.rmi.CORBA.Util
                .loadClass(className, remoteCodebase, loader);
    }

    /**
     * From javax.rmi.CORBA.Util.
     */
    public static String getCodebase(Class clz)
    {
        return javax.rmi.CORBA.Util.getCodebase(clz);
    }

    /**
     * From javax.rmi.PortableRemoteObject.
     */
    public static Object portableRemoteObject_narrow(Object narrowFrom,
            Class narrowTo) throws ClassCastException
    {
        return javax.rmi.PortableRemoteObject.narrow(narrowFrom, narrowTo);
    }


    public static byte getMaximumStreamFormatVersion (org.omg.CORBA.portable.OutputStream out)
    {
       javax.rmi.CORBA.ValueHandler vh = javax.rmi.CORBA.Util.createValueHandler();
       byte streamFormatVersion = STREAM_FORMAT_VERSION_1;
       
       // Maximum stream format version calculation rules:
       // 1. Assume version 1 by default (GIOP v1.2).
       // 2. If ValueHandler supports higher version take it as preliminary value.
       // 3. If CDROutputStream returns version lower that ValueHandler supports 
       //    then use maximum version that is supported by output stream.

       if (vh instanceof ValueHandlerMultiFormat)
       {
          streamFormatVersion = ((ValueHandlerMultiFormat)vh).getMaximumStreamFormatVersion ();
       }
       
       if (out instanceof org.jacorb.orb.CDROutputStream
           && streamFormatVersion > ((org.jacorb.orb.CDROutputStream)out).getMaximumStreamFormatVersion ())
       {
           streamFormatVersion = ((org.jacorb.orb.CDROutputStream)out).getMaximumStreamFormatVersion ();
       }
       
       return streamFormatVersion;
    }
}
