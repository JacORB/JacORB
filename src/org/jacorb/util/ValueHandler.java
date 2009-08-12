package org.jacorb.util;

/**
 * A static wrapper around classes in javax.rmi.
 */
public class ValueHandler
{

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
        javax.rmi.CORBA.Util.createValueHandler().writeValue(out, value);
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

}
