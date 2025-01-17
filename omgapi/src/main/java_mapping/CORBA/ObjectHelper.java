package org.omg.CORBA;
 
abstract public class ObjectHelper
{
    private static String  repo_id = "";
 
    public static void insert (org.omg.CORBA.Any a, org.omg.CORBA.Object obj)
    {
    	a.insert_Streamable(new ObjectHolder(obj));
    }
 
    public static org.omg.CORBA.Object extract (org.omg.CORBA.Any a)
    {
        return ((ObjectHolder) a.extract_Streamable ()).value;
    }
 
    private static org.omg.CORBA.TypeCode type_code = null;
    
    synchronized public static org.omg.CORBA.TypeCode type ()
    {
        if (type_code == null)
        {
        	type_code = org.omg.CORBA.ORB.init ().get_primitive_tc (TCKind.tk_objref);
        }
        return type_code;
    }
 
    public static String id ()
    {
        return repo_id;
    }
 
    public static org.omg.CORBA.Object read (org.omg.CORBA.portable.InputStream istream)
    {
        return istream.read_Object ();
    }
 
    public static void write (org.omg.CORBA.portable.OutputStream ostream, org.omg.CORBA.Object value)
    {
        ostream.write_Object (value);
    }
}
