package org.jacorb.ir.gui.typesystem.remote;

/**
 * 
 */
 
import java.util.*;
import org.omg.CORBA.*;
import javax.swing.tree.*;
 
public class IRAttribute 
	extends IRNodeWithType 
{
	/**
	 * Default constructor, called from  TypeSystem.createNode(...)
	 */
	public IRAttribute ( ) {
		super();
	}

	/**
	 * @param irObject org.omg.CORBA.IRObject
	 */

	public IRAttribute( IRObject irObject) 
	{
		super(irObject);
		AttributeDef attributeDef = 
			AttributeDefHelper.narrow(irObject);
		setAssociatedTypeSystemNode(RemoteTypeSystem.createTypeSystemNode(attributeDef.type_def()));
	}

	/**
	 * @return java.lang.String
	 */

	public String getInstanceNodeTypeName ( ) 
	{
		String result = super.getInstanceNodeTypeName();
		if ( AttributeDefHelper.narrow((org.omg.CORBA.Object)irObject).mode().value() ==
			 AttributeMode._ATTR_READONLY) 
		{
			result = "readonly" + " " + result;
		}	
		return result;
	}

	/**
	 * @return java.lang.String
	 */
	public static String nodeTypeName() 
	{
		return "attribute";
	}
}











