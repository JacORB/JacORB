package demo.dynany;

import org.omg.DynamicAny.*;
import org.omg.CORBA.Any;
import org.omg.CORBA.TCKind;
import jacorb.orb.*;
import org.omg.CosNaming.*;

public class Server 
    extends AnyServerPOA
{
    public static org.omg.CORBA.ORB orb;
    DynAnyFactory factory;

    public Server()
    {
	try
	{
	    factory = DynAnyFactoryHelper.narrow( orb.resolve_initial_references("DynAnyFactory"));
	}
	catch( Exception e )
	{
	    e.printStackTrace();
	}
    }
    public java.lang.String generic(Any a) 
    {
	printAny(a);
	return "done.";
    }

    private void printAny(Any a) 
    {
	String result = "<empty>";
	try
	{ 
	    switch( a.type().kind().value() )
	    {
	    case TCKind._tk_char:
		System.out.println("char: " + a.extract_char());
		break;
	    case TCKind._tk_short:
		System.out.println("Short: " + a.extract_short());
		break;
	    case TCKind._tk_long:
		System.out.println("Long: " + a.extract_long());
		break;
	    case TCKind._tk_double:
		System.out.println("Double: " + a.extract_double());
		break;
	    case TCKind._tk_float:
		System.out.println("Float: " + a.extract_float());
		break;
	    case TCKind._tk_string:
		System.out.println("String: " + a.extract_string());
		break;
	    case TCKind._tk_enum:
		DynEnum dynEnum = (DynEnum)factory.create_dyn_any(a);
		System.out.println("** Enum **");
		System.out.println( dynEnum.get_as_string());
		break;
	    case TCKind._tk_struct:
		DynStruct dynstruct = (DynStruct)factory.create_dyn_any( a );
		org.omg.DynamicAny.NameValuePair[] members = dynstruct.get_members();
		System.out.println("** Struct **");
		for( int i = 0; i < members.length; i++ )
		{
		    System.out.println("name: " + members[i].id + " value: " );
		    printAny( members[i].value);
		}
		break;
	    case TCKind._tk_union:
		System.out.println("** Union **");
		DynUnion dynunion = (DynUnion)factory.create_dyn_any( a );
		System.out.println("member name " + dynunion.member_name());
		printAny(dynunion.member().to_any());
		break;
	    case TCKind._tk_sequence:
		DynSequence dynseq = (DynSequence)factory.create_dyn_any( a );
		Any[] contents = dynseq.get_elements();
		System.out.println("** Sequence of length " + contents.length + " **");
		for( int i = 0; i < contents.length; i++)
		    printAny( contents[i]);
		break;
	    case TCKind._tk_array:
		DynArray dynarray = (DynArray)factory.create_dyn_any( a );
		Any[] array_contents = dynarray.get_elements();
		System.out.println("** Array **");
		for( int i = 0; i < array_contents.length; i++)
		    printAny( array_contents[i]);
		break;
	    default:
		System.out.println("Unknown, kind " + a.type().kind().value());
		
	    }
	} 
	catch ( Exception e )
	{
	    e.printStackTrace();
	}
    }

    public static void main( String[] args )
    {
	orb = org.omg.CORBA.ORB.init(args, null);
	try
	{
	    org.omg.PortableServer.POA poa = 
		org.omg.PortableServer.POAHelper.narrow(orb.resolve_initial_references("RootPOA"));

	    poa.the_POAManager().activate();
	    
	    org.omg.CORBA.Object o = poa.servant_to_reference(new Server());

	    NamingContextExt nc = 
		NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));

	    nc.bind(nc.to_name("DynAnyServer.example"), o);
	} 
	catch ( Exception e )
	{
	    e.printStackTrace();
	}
	orb.run();
    }
}


