
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
import org.omg.CosTrading.LookupPackage.*;
import org.omg.CosTrading.RegisterPackage.*;
import org.omg.DynamicAny.*;
import org.omg.DynamicAny.DynSequence;
import org.jacorb.trading.constraint.*;
import org.jacorb.trading.util.*;

/**
 * Provides property values to the expression evaluators
 */

public class SourceAdapter implements PropertySource
{
    private Hashtable m_propTable;
    private Hashtable m_values;
    private Hashtable m_propValues;
    private org.omg.CORBA.Object m_object;
    private Property[] m_props;

    private org.omg.CORBA.ORB orb;


    protected SourceAdapter()
    {
    }


    public SourceAdapter(org.omg.CORBA.Object object, Property[] props)
    {
	orb = ((org.omg.CORBA.portable.ObjectImpl)object)._orb();

	m_object = object;
	m_props = props;

	m_propTable = new Hashtable();
	// cache the Value objects we have produced
	m_values = new Hashtable();
	// cache the property values we have retrieved
	m_propValues = new Hashtable();

	// load the offer's properties into a hashtable for quicker lookup
	for (int i = 0; i < props.length; i++)
	    m_propTable.put(props[i].name, props[i]);
    }


    public org.omg.CORBA.Object getObject()
    {
	return m_object;
    }


    public Property[] getProperties()
    {
	return m_props;
    }


    public Property[] getProperties(SpecifiedProps desired_props)
    {
	Property[] result = null;

	// we use the SourceAdapter object to obtain the values for the
	// desired properties

	if (desired_props.discriminator() == HowManyProps.all) {
	    Vector vec = new Vector();
	    for (int i = 0; i < m_props.length; i++) {
		Any value = getPropertyValue(m_props[i].name);
		if (value != null)
		    vec.addElement(new Property(m_props[i].name, value));
	    }
	    result = new Property[vec.size()];
	    vec.copyInto((java.lang.Object[])result);
	}
	else if (desired_props.discriminator() == HowManyProps.some) {
	    String[] names = desired_props.prop_names();
	    Vector vec = new Vector();
	    for (int i = 0; i < names.length; i++) {
		Any value = getPropertyValue(names[i]);
		if (value != null)
		    vec.addElement(new Property(names[i], value));
	    }
	    result = new Property[vec.size()];
	    vec.copyInto((java.lang.Object[])result);
	}
	else if (desired_props.discriminator() == HowManyProps.none)
	    result = new Property[0];

	return result;
    }


    public boolean exists(String property)
    {
	return m_propTable.containsKey(property);
    }


    public Value getValue(String property)
    {
	Value result = null;

	// first see if we've already cached the value
	java.lang.Object v = m_values.get(property);

	if (v == null) {
	    Property p = (Property)m_propTable.get(property);
	    if (p == null)
		return null;

	    // next try to get the property's value
	    // NOTE: this may cause dynamic property evaluation
	    Any val = getPropertyValue(property);

	    if (val == null)
		return null;

	    // cache the property value
	    m_propValues.put(property, val);

	    try {
		TypeCode tc = val.type();

		while (tc.kind() == TCKind.tk_alias)
		    tc = tc.content_type();

		TCKind kind = tc.kind();
		if (kind == TCKind.tk_sequence)
		    return null;

		switch (kind.value()) {
		case TCKind._tk_short: {
		    int s = val.extract_short();
		    result = ValueFactory.createShort(s);
		}
		break;

		case TCKind._tk_long: {
		    int l = val.extract_long();
		    result = ValueFactory.createLong(l);
		}
		break;

		case TCKind._tk_ushort: {
		    int i = val.extract_short();
		    result = ValueFactory.createUShort(i);
		}
		break;

		case TCKind._tk_ulong: {
		    long l = val.extract_ulong();
		    result = ValueFactory.createULong(l);
		}
		break;

		case TCKind._tk_float: {
		    float f = val.extract_float();
		    result = ValueFactory.createFloat(f);
		}
		break;

		case TCKind._tk_double: {
		    double d = val.extract_double();
		    result = ValueFactory.createDouble(d);
		}
		break;

		case TCKind._tk_boolean: {
		    boolean b = val.extract_boolean();
		    result = ValueFactory.createBoolean(b);
		}
		break;

		case TCKind._tk_char: {
		    char c = val.extract_char();
		    result = ValueFactory.createChar(c);
		}
		break;

		case TCKind._tk_string: {
		    String s = val.extract_string();
		    result = ValueFactory.createString(s);
		}
		break;
		}
	    }
	    catch (org.omg.CORBA.TypeCodePackage.BadKind e) {
		throw new RuntimeException();
	    }
	    catch (BAD_OPERATION e) {
		throw new RuntimeException();
	    }

	    // cache the value object
	    m_values.put(property, result);
	}
	else {
	    // make sure we've got a Value (i.e. not a Value[])
	    if (v instanceof Value)
		result = (Value)v;
	}

	return result;
    }


    public Value[] getSequenceValues(String property)
    {
	Value[] result = null;

	// first see if we've already cached the values
	java.lang.Object v = m_values.get(property);

	if (v == null) {

	    Property p = (Property)m_propTable.get(property);
	    if (p == null)
		return null;

	    // next try to get the property's value
	    // NOTE: this may cause dynamic property evaluation
	    Any val = getPropertyValue(property);

	    if (val == null)
		return null;

	    // cache the property value
	    m_propValues.put(property, val);

	    try {
		TypeCode tc = val.type();

		// skip aliases
		while (tc.kind() == TCKind.tk_alias)
		    tc = tc.content_type();

		if (tc.kind() != TCKind.tk_sequence)
		    return null;

		// get the type of the sequence data
		tc = tc.content_type();
		TCKind kind = tc.kind();

		org.omg.DynamicAny.DynAny da = DynAnyFactoryHelper.narrow(orb.resolve_initial_references("DynAnyFactory")).create_dyn_any(val);
		DynSequence ds = DynSequenceHelper.narrow(da);
		int len = ds.get_length();
		//**//
		result = new Value[len];

		switch (kind.value()) {
		case TCKind._tk_short: {
		    for (int i = 0; i < len; i++, ds.next())
			result[i] = ValueFactory.createShort(ds.get_short());
		}
		break;

		case TCKind._tk_long: {
		    for (int i = 0; i < len; i++, ds.next())
			result[i] = ValueFactory.createLong(ds.get_long());
		}
		break;

		case TCKind._tk_ushort: {
		    for (int i = 0; i < len; i++, ds.next())
			result[i] = ValueFactory.createUShort(ds.get_ushort());
		}
		break;

		case TCKind._tk_ulong: {
		    for (int i = 0; i < len; i++, ds.next())
			result[i] = ValueFactory.createULong(ds.get_ulong());
		}
		break;

		case TCKind._tk_float: {
		    for (int i = 0; i < len; i++, ds.next())
			result[i] = ValueFactory.createFloat(ds.get_float());
		}
		break;

		case TCKind._tk_double: {
		    for (int i = 0; i < len; i++, ds.next())
			result[i] = ValueFactory.createDouble(ds.get_double());
		}
		break;

		case TCKind._tk_boolean: {
		    for (int i = 0; i < len; i++, ds.next())
			result[i] = ValueFactory.createBoolean(ds.get_boolean());
		}
		break;

		case TCKind._tk_char: {
		    for (int i = 0; i < len; i++, ds.next())
			result[i] = ValueFactory.createChar(ds.get_char());
		}
		break;

		case TCKind._tk_string: {
		    for (int i = 0; i < len; i++, ds.next())
			result[i] = ValueFactory.createString(ds.get_string());
		}
		break;
		}

		da.destroy();
	    }
	    catch (org.omg.CORBA.TypeCodePackage.BadKind e) {
		throw new RuntimeException();
	    }
	    //**//
	    catch (org.omg.DynamicAny.DynAnyPackage.TypeMismatch e) {
		throw new RuntimeException();
	    }
	    catch (org.omg.DynamicAny.DynAnyPackage.InvalidValue e) {
		throw new RuntimeException();
	    }
	    catch (org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode e) {
		throw new RuntimeException();
	    }
	    catch (org.omg.CORBA.ORBPackage.InvalidName e) {
		throw new RuntimeException();
	    }
	    //**//
	    catch (BAD_OPERATION e) {
		throw new RuntimeException();
	    }

	    // add the result to the cache
	    m_values.put(property, result);
	}
	else {
	    // make sure we've got a Value[] (i.e. not a Value)
	    if (v instanceof Value[])
		result = (Value[])v;
	}

	return result;
    }


    public Any getPropertyValue(String property)
    {
	// first see if we've already cached the value
	Any result = (Any)m_propValues.get(property);

	if (result == null) {
	    Property p = (Property)m_propTable.get(property);
	    if (p == null)
		return null;

	    // NOTE: this may cause dynamic property evaluation
	    result = PropUtil.getPropertyValue(p);

	    if (result != null)
		// cache the property value
		m_propValues.put(property, result);
	}

	return result;
    }
}










