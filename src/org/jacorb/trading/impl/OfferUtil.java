
// Copyright (C) 1998-1999
// Object Oriented Concepts, Inc.

// **********************************************************************
//
// Copyright (c) 1997
// Mark Spruiell (mark@intellisoft.com)
//
// See the COPYING file for more information
//
// **********************************************************************

package org.jacorb.trading.impl;


import java.util.*;
import org.omg.CORBA.*;
import org.omg.CosTrading.*;
import org.omg.CosTradingRepos.*;
import org.omg.CosTradingRepos.ServiceTypeRepositoryPackage.*;
import org.omg.CosTradingDynamic.*;
import org.jacorb.trading.db.OfferDatabase;
import org.jacorb.trading.util.*;


/**
 * Convenience methods
 */
public class OfferUtil
{
    private OfferUtil()
    {
    }


    /** Validate the properties of a service offer against the service type */
    public static void validateProperties(
					  OfferDatabase db,
					  Property[] exportProps,
					  String typeName,
					  TypeStruct type)
	throws IllegalPropertyName,
	PropertyTypeMismatch,
	ReadonlyDynamicProperty,
	MissingMandatoryProperty,
	DuplicatePropertyName
    {
	// create a hashtable of the service type's properties
	Hashtable typeProps = new Hashtable();
	for (int i = 0; i < type.props.length; i++)
	    typeProps.put(type.props[i].name, type.props[i]);

	// also create a hashtable of the exported properties we've processed
	Hashtable checkedProps = new Hashtable();

	// iterate through each of the exported properties
	for (int i = 0; i < exportProps.length; i++) 
	{
	    // try to find the property with the same name in the service type
	    PropStruct ps = (PropStruct)typeProps.get(exportProps[i].name);
	    if (ps != null) 
	    {
		// make sure we haven't already processed this property
		if (checkedProps.containsKey(exportProps[i].name))
		    throw new DuplicatePropertyName(exportProps[i].name);

		checkProperty(typeName, exportProps[i], ps);
		checkedProps.put(exportProps[i].name, exportProps[i]);
	    }

	    // whether or not the property is defined in the service type,
	    // we need to make sure the database can store this property;
	    // the PropertyTypeMismatch exception is about the best we can do
	    //
	    // NOTE: For dynamic properties, we may not be able to store
	    //   the extra_info member of the DynamicProp struct;
	    //   we assume the database object also validates extra_info

	    if (!db.isTypeSupported(exportProps[i].value))
	    {
		org.jacorb.util.Debug.output(3, " Type not supported: " + exportProps[i].name );
		throw new PropertyTypeMismatch(typeName, exportProps[i]);
	    }
	}

	// now we need to check for mandatory properties that were not
	// included in the export list
	Enumeration e = typeProps.elements();
	while (e.hasMoreElements()) 
	{
	    PropStruct ps = (PropStruct)e.nextElement();

	    // if property is not being exported...
	    if (checkedProps.get(ps.name) == null) {
		if (isMandatory(ps.mode))
		    throw new MissingMandatoryProperty(typeName, ps.name);
	    }
	}
    }


    public static boolean isMandatory(PropertyMode mode)
    {
	boolean result;

	result = (
		  mode == PropertyMode.PROP_MANDATORY ||
		  mode == PropertyMode.PROP_MANDATORY_READONLY
		  );

	return result;
    }


    public static boolean isReadonly(PropertyMode mode)
    {
	boolean result;

	result = (
		  mode == PropertyMode.PROP_READONLY ||
		  mode == PropertyMode.PROP_MANDATORY_READONLY
		  );

	return result;
    }


    /** Check the property's type against the expected type */
    public static void checkProperty(
				     String typeName,
				     Property prop,
				     PropStruct ps)
	throws PropertyTypeMismatch,
	ReadonlyDynamicProperty
    {
	try {
	    TypeCode propType = ps.value_type;
	    while (propType.kind() == TCKind.tk_alias)
		propType = propType.content_type();


	    if (PropUtil.isDynamicProperty(prop.value.type())) 
	    {
		// cannot allow dynamic readonly properties
		if (isReadonly(ps.mode))
		    throw new ReadonlyDynamicProperty(typeName, prop.name);

		// verify the dynamic property's type code against that
		// specified by the service type
		DynamicProp dp = DynamicPropHelper.extract(prop.value);

		TypeCode tc = dp.returned_type;
		while (tc.kind() == TCKind.tk_alias)
		    tc = tc.content_type();

		if (! tc.equal(propType))
		    throw new PropertyTypeMismatch(typeName, prop);
	    }
	    else {
		// verify the property's type code against that
		// specified by the service type
		TypeCode tc = prop.value.type();
		while (tc.kind() == TCKind.tk_alias)
		    tc = tc.content_type();

		if (! tc.equal(propType))
		    throw new PropertyTypeMismatch(typeName, prop);
	    }
	}
	catch (org.omg.CORBA.TypeCodePackage.BadKind e) {
	    throw new RuntimeException();
	}
    }
}










