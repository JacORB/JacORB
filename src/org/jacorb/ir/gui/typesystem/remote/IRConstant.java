package org.jacorb.ir.gui.typesystem.remote;

import java.util.*;
import org.omg.CORBA.*;
import javax.swing.tree.*;
 
public class IRConstant extends IRNodeWithType 
{
    protected java.lang.Object value;

    /**
     * Default-Konstruktor: wird von TypeSystem.createNode(...) benutzt
     */
    public IRConstant ( ) {
	super();
    }

    /**
     * This method was created by a SmartGuide.
     * @param irObject org.omg.CORBA.IRObject
     */

    public IRConstant ( IRObject irObject) 
    {
	super(irObject);
	ConstantDef constantDef = ConstantDefHelper.narrow((org.omg.CORBA.Object)irObject);
	setAssociatedTypeSystemNode(RemoteTypeSystem.createTypeSystemNode(constantDef.type_def()));
	Any any = constantDef.value();
	this.value = org.jacorb.ir.gui.remoteobject.ObjectRepresentantFactory.objectFromAny(any);
    }

    /**
     * This method was created by a SmartGuide.
     * @return java.lang.String
     */

    public  String description() {
	String result = super.description();
	result = result + "\nConstant value =\t" + value;
	return result;
    }

    /**
     * This method was created by a SmartGuide.
     * @return java.lang.String
     */
    public static String nodeTypeName() {
	return "const";
    }
}





