package demo.any;

import org.omg.CORBA.Any;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.StringSeqHelper;
import org.omg.CORBA.OctetSeqHelper;

public class AnyServerImpl 
    extends AnyServerPOA
{
    org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init();

    public AnyServerImpl(){}

    public java.lang.String generic(Any a) 
    {
	String result = "<empty>";
	try
	{ 		
            int kind =  a.type().kind().value();

	    switch( kind )
	    {
	    case TCKind._tk_char:
		result = "char: " + a.extract_char();
		break;
	    case TCKind._tk_longlong:
		result = "longlong: " + a.extract_longlong();
		break;
	    case TCKind._tk_short:
		result ="Short: " + a.extract_short();
		break;
	    case TCKind._tk_double:
		result = "Double: " + a.extract_double();
		break;
	    case TCKind._tk_float:
		result = "Float: " + a.extract_float();
		break;
	    case TCKind._tk_string:
		result = "String: " + a.extract_string();
		break;
	    case TCKind._tk_wstring:
		result = "WString: " + a.extract_wstring();
		break;
	    case TCKind._tk_struct:
		if( NodeHelper.type().equal( a.type()))
		{		
		    StringBuffer sb = new StringBuffer();
		    Node t = NodeHelper.extract( a );
		    sb.append( " " + t.name );
		    do
		    {			
			t = t.next[0];
			sb.append( " " + t.name );
		    }
		    while( t.next.length > 0 );
		    result = sb.toString();
		}	
		break;
	    case TCKind._tk_union:
		Nums n = NumsHelper.extract( a );
		switch (n.discriminator() )
                {
                case 'l' :
                    result = "Union: l " + n.l();
                    break;
                case 'f' :
		    result = "Union: f " + n.f();
                    break;
                default :
                    result = "default: " + n.s();
                }
		break;
	    case TCKind._tk_objref:
		System.out.println("Objectref.");
		AnyServer s = AnyServerHelper.narrow(a.extract_Object());
		Any any = org.omg.CORBA.ORB.init().create_any();
		any.insert_string("hallo");
		result = s.generic( any );
		break;
	    case TCKind._tk_any:
		System.out.println("Any.");
		Any inner_any = a.extract_any();
		result = generic( inner_any );
		break;
	    case TCKind._tk_alias:
		System.out.print("Alias: ");
		Any alias_any = a;
                if( alias_any.type().equal( MyStringSeqHelper.type()))
                {
                    String[] stra = MyStringSeqHelper.extract( alias_any );
                    for( int slen = 0; slen < stra.length; slen++ )
                        result += stra[slen];
                }
                else if( alias_any.type().equal( myWStringHelper.type()))
                {
                    result = myWStringHelper.extract( alias_any );
                }
                else if( alias_any.type().equal( OctetSeqHelper.type()))
                {
                    byte[] octets = OctetSeqHelper.extract( a );
                    result = "Octet Sequence: " + octets[0];
                }
                else if( alias_any.type().equal( stringsHelper.type()))
                {
                    String[] str3 = stringsHelper.extract( a );
                    result = "Array: " + str3[1];
                }
                else
                    System.out.println("Unknown alias, type kind: " + 
                                       alias_any.type().kind().value() );

		break;
	    default:
		System.out.println("Unknown, kind " + a.type().kind().value());
		
	    }
	} 
	catch ( Exception e )
	{
	    e.printStackTrace();
	}
	System.out.println(result);
	return result;
    }
}


