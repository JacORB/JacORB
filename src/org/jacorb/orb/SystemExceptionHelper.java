package org.jacorb.orb;

import java.lang.reflect.*;

public class SystemExceptionHelper
{
    private static final String className( String repId )
    {
        // cut "IDL:" and version
        String id_base = repId.substring(4, repId.lastIndexOf(':'));
	return ir2scopes("org.omg",id_base.substring(7));
    }

    private static final String ir2scopes( String prefix, String s ) 
    {
        if( s.indexOf("/") < 0)
            return s;
        java.util.StringTokenizer strtok = 
            new java.util.StringTokenizer( s, "/" );

        int count = strtok.countTokens();
        StringBuffer sb = new StringBuffer();
        sb.append(prefix);

        for( int i = 0; strtok.hasMoreTokens(); i++ )
        {
            String sc = strtok.nextToken();
            try
            {
                Class c = null;
                if( sb.toString().length() > 0 )
                    c = Class.forName( sb.toString() + "." + sc );
                else
                    c = Class.forName( sc );

                if( i < count-1)
                {
                    sb.append( "." + sc + "Package");
                }
                else
                    sb.append( "." + sc );
            }
            catch ( ClassNotFoundException cnfe )
            {
                if( sb.toString().length() > 0 )
                    sb.append( "." + sc );
                else
                    sb.append( sc );
            }
        }

        return sb.toString();
    }

    private static final String repId( Class c )
    {
        String className = c.getName();
	String body = className.substring(7);
	return "IDL:omg.org/" + scopesToIR(body) + ":1.0";        
    }

    private static final String scopesToIR( String s )
    {
        if( s.indexOf(".") < 0)
            return s;
        java.util.StringTokenizer strtok = 
            new java.util.StringTokenizer( s, "." );
        String scopes[] = new String[strtok.countTokens()];
        for( int i = 0; strtok.hasMoreTokens(); i++ ){
            String sc = strtok.nextToken();
            if( sc.endsWith("Package"))
                scopes[i] = sc.substring(0,sc.indexOf("Package"));
            else
                scopes[i] = sc;
        }
        StringBuffer sb = new StringBuffer();
        if( scopes.length > 1 )
        {
            for( int i = 0; i < scopes.length-1; i++)
                sb.append( scopes[i] + "/" );
        }

        sb.append( scopes[scopes.length-1] );
        return sb.toString();
    }


    public static void insert(org.omg.CORBA.Any any, org.omg.CORBA.SystemException  s)
    {
	any.type( type( s ));
	write( any.create_output_stream(), s);
    }


    public static org.omg.CORBA.TypeCode type( org.omg.CORBA.SystemException  s)
    {
	String name = s.getClass().getName();
	name = name.substring(name.lastIndexOf('.') + 1);
	org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init();

        org.omg.CORBA.TypeCode _type = 
            orb.create_struct_tc(
                                 "IDL:omg.org/CORBA/" + name + ":1.0",
                                 name,
                                 new org.omg.CORBA.StructMember[]{
                                     new org.omg.CORBA.StructMember(
                                         "minor",
                                         orb.get_primitive_tc(org.omg.CORBA.TCKind.from_int(3)),
                                         null),
                                     new org.omg.CORBA.StructMember(
                                         "completed",
                                         orb.create_enum_tc("IDL:omg.org/CORBA/CompletionStatus:1.0",
                                                            "CompletionStatus",
                                                            new String[]{"COMPLETED_YES","COMPLETED_NO","COMPLETED_MAYBE"}),
                                         null)
                                 });
	return _type;
    }



    public static org.omg.CORBA.SystemException read(org.omg.CORBA.portable.InputStream in)
    {
	String className = className(in.read_string());
	int minor = in.read_long();
	org.omg.CORBA.CompletionStatus completed = 
            org.omg.CORBA.CompletionStatusHelper.read(in);
	try
	{
	    Class ex = Class.forName( className );
	    Constructor constr = 
                ex.getConstructor( 
                                  new Class[]{ String.class, int.class, org.omg.CORBA.CompletionStatus.class});

	    return (org.omg.CORBA.SystemException)constr.newInstance(new Object[]{"",new Integer(minor), completed});
	}
	catch (Exception e )
	{
	    //debug:
	    //e.printStackTrace();
	    throw new org.omg.CORBA.UNKNOWN(className);
	}		    
    }

    public static void write(org.omg.CORBA.portable.OutputStream out, 
                             org.omg.CORBA.SystemException s)
    {	    
	out.write_string(repId(s.getClass()));
	out.write_long(s.minor);
	org.omg.CORBA.CompletionStatusHelper.write(out,s.completed);
    }
}








