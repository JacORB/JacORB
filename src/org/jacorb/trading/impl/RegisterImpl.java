
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
import org.omg.CosTrading.RegisterPackage.*;
import org.omg.CosTradingRepos.*;
import org.omg.CosTradingRepos.ServiceTypeRepositoryPackage.*;
import org.omg.CosTradingDynamic.*;
import org.jacorb.trading.constraint.*;
import org.jacorb.trading.db.OfferDatabase;
import org.jacorb.trading.util.*;

/**
 * Implementation of CosTrading::Register
 */
public class RegisterImpl extends org.omg.CosTrading.RegisterPOA
{
    private TraderComp m_traderComp;
    private SupportAttrib m_support;
    private OfferDatabase m_db;
    private ServiceTypeRepository m_repos;
    private org.omg.CORBA.Repository m_interfaceRepos;


    private RegisterImpl()
    {
    }


    public RegisterImpl(
			TraderComp traderComp,
			SupportAttrib supportAttrib,
			OfferDatabase db,
			org.omg.CORBA.Repository interfaceRepos)
    {
	m_traderComp = traderComp;
	m_support = supportAttrib;
	m_db = db;
	m_interfaceRepos = interfaceRepos;
	org.omg.CORBA.Object obj = supportAttrib.getTypeRepos();
	m_repos = ServiceTypeRepositoryHelper.narrow(obj);
    }


    // operations inherited from CosTrading::TraderComponents

    public Lookup lookup_if()
    {
	return m_traderComp.getLookupInterface();
    }


    public Register register_if()
    {
	return m_traderComp.getRegisterInterface();
    }


    public Link link_if()
    {
	return m_traderComp.getLinkInterface();
    }


    public Proxy proxy_if()
    {
	return m_traderComp.getProxyInterface();
    }


    public Admin admin_if()
    {
	return m_traderComp.getAdminInterface();
    }


    // operations inherited from CosTrading::SupportAttributes

    public boolean supports_modifiable_properties()
    {
	return m_support.getModifiableProperties();
    }


    public boolean supports_dynamic_properties()
    {
	return m_support.getDynamicProperties();
    }


    public boolean supports_proxy_offers()
    {
	return m_support.getProxyOffers();
    }


    public org.omg.CORBA.Object type_repos()
    {
	return m_support.getTypeRepos();
    }



    // operations inherited from CosTrading::Register

    public String export(
			 org.omg.CORBA.Object reference,
			 String type,
			 Property[] properties)
	throws InvalidObjectRef,
	IllegalServiceType,
	UnknownServiceType,
	org.omg.CosTrading.RegisterPackage.InterfaceTypeMismatch,
	IllegalPropertyName,
	PropertyTypeMismatch,
	ReadonlyDynamicProperty,
	MissingMandatoryProperty,
	DuplicatePropertyName
    {

	String result = null;

	if (reference == null)
	    throw new InvalidObjectRef(reference);

	// retrieve complete information about the service type from the
	// repository - may throw IllegalServiceType, UnknownServiceType
	TypeStruct ts = m_repos.fully_describe_type(type);

	// do not allow exporting for a masked service type
	if (ts.masked)
	    throw new UnknownServiceType(type);

	// validate the interface - may throw InterfaceTypeMismatch
	validateInterface(reference, type, ts);

	// validate the exported properties - may throw
	// IllegalPropertyName, PropertyTypeMismatch,
	// MissingMandatoryProperty, DuplicatePropertyName
	OfferUtil.validateProperties(m_db, properties, type, ts);

	// save the offer in the database

	m_db.begin(OfferDatabase.WRITE);

	try {
	    result = m_db.create(type, reference, properties);
	}
	finally {
	    m_db.end();
	}

	return result;
    }


    public void withdraw(String id)
	throws IllegalOfferId,
	UnknownOfferId,
	ProxyOfferId
    {
	if (! m_db.validateOfferId(id))
	    throw new IllegalOfferId(id);

	m_db.begin(OfferDatabase.WRITE);

	try {
	    if (! m_db.exists(id))
		throw new UnknownOfferId(id);

	    if (m_db.isProxy(id))
		throw new ProxyOfferId(id);

	    m_db.remove(id);
	}
	finally {
	    m_db.end();
	}
    }


    public OfferInfo describe(String id)
	throws IllegalOfferId,
	UnknownOfferId,
	ProxyOfferId
    {
	OfferInfo result;

	if (! m_db.validateOfferId(id))
	    throw new IllegalOfferId(id);


	m_db.begin(OfferDatabase.READ);

	try {
	    if (! m_db.exists(id))
		throw new UnknownOfferId(id);

	    if (m_db.isProxy(id))
		throw new ProxyOfferId(id);

	    result = m_db.describe(id);
	}
	finally {
	    m_db.end();
	}

	return result;
    }


    public void modify(
		       String id,
		       String[] del_list,
		       Property[] modify_list)
	throws NotImplemented,
	IllegalOfferId,
	UnknownOfferId,
	ProxyOfferId,
	IllegalPropertyName,
	UnknownPropertyName,
	PropertyTypeMismatch,
	ReadonlyDynamicProperty,
	MandatoryProperty,
	ReadonlyProperty,
	DuplicatePropertyName
    {
	if (! m_support.getModifiableProperties())
	    throw new NotImplemented();

	if (! m_db.validateOfferId(id))
	    throw new IllegalOfferId(id);

	try {
	    m_db.begin(OfferDatabase.WRITE);

	    if (! m_db.exists(id))
		throw new UnknownOfferId(id);

	    if (m_db.isProxy(id))
		throw new ProxyOfferId(id);

	    String type = m_db.whichService(id);

	    // retrieve complete information about the service type from the
	    // repository - may throw IllegalServiceType, UnknownServiceType
	    TypeStruct ts = m_repos.fully_describe_type(type);

	    OfferInfo info = m_db.describe(id);

	    // check the proposed deletion list - may throw several
	    // exceptions
	    checkDelete(del_list, type, ts, info);

	    // check the proposed modification list - may throw several
	    // exceptions
	    checkModify(modify_list, type, ts, info);

	    // Build a vector of the offer's new properties
	    //
	    // Algorithm:
	    //   - copy in the modify_list
	    //   - for each property in info.properties
	    //     - if not already in vector, and not in del_list, add
	    //       to vector

	    Vector props = new Vector();
	    for (int i = 0; i < modify_list.length; i++)
		props.addElement(modify_list[i]);

	    for (int i = 0; i < info.properties.length; i++) {
		boolean found = false;

		// check del_list to see if property is to be deleted
		for (int d = 0; d < del_list.length && ! found; d++)
		    if (info.properties[i].name.equals(del_list[d]))
			found = true;

		// check props to see if we already have this property
		Enumeration e = props.elements();
		while (e.hasMoreElements() && ! found) {
		    Property p = (Property)e.nextElement();
		    if (info.properties[i].name.equals(p.name))
			found = true;
		}

		// if we didn't find it in del_list, and it's not already
		// in props, then add it now
		if (! found)
		    props.addElement(info.properties[i]);
	    }

	    // create an array from the vector
	    Property[] arr = new Property[props.size()];
	    props.copyInto((java.lang.Object[])arr);

	    // make the changes
	    m_db.modify(id, arr);
	}
	catch (IllegalServiceType e) {
	    throw new UnknownOfferId(id);
	}
	catch (UnknownServiceType e) {
	    throw new UnknownOfferId(id);
	}
	finally {
	    m_db.end();
	}
    }


    public void withdraw_using_constraint(String type, String constr)
	throws IllegalServiceType,
	UnknownServiceType,
	IllegalConstraint,
	NoMatchingOffers
    {
	// retrieve complete information about the service type from the
	// repository - may throw IllegalServiceType, UnknownServiceType
	TypeStruct ts = m_repos.fully_describe_type(type);

	// instantiate the schema object required by the constraint parser
	SchemaAdapter schema = new SchemaAdapter(ts);

	Constraint c = new Constraint(schema);

	int count = 0;

	try {
	    m_db.begin(OfferDatabase.WRITE);

	    // attempt to parse the constraint expression
	    c.parse(constr);

	    // retrieve all of the offers of this type and process them;
	    // we get a Hashtable whose keys are offer IDs and whose values
	    // are OfferInfo structs
	    Hashtable offers = m_db.getOffers(type);

	    if (offers != null) {
		Enumeration e = offers.keys();
		while (e.hasMoreElements()) {
		    String id = (String)e.nextElement();
		    OfferInfo info = (OfferInfo)offers.get(id);

		    SourceAdapter source =
			new SourceAdapter(info.reference, info.properties);

		    // NOTE: dynamic properties will be evaluated

		    if (c.evaluate(source)) {
			m_db.remove(id);
			count++;
		    }
		}
	    }
	}
	catch (ParseException ex) {
	    // the exception doesn't include a reason, so we just print it
	    System.out.println("Illegal constraint '" + constr + "'");
	    System.out.println(ex.getMessage());
	    throw new IllegalConstraint(constr);
	}
	finally {
	    m_db.end();
	}

	if (count == 0)
	    throw new NoMatchingOffers(constr);
    }


    public Register resolve(String[] name)
	throws IllegalTraderName,
	UnknownTraderName,
	RegisterNotSupported
    {
	throw new RegisterNotSupported(name);
    }


    protected void validateInterface(
				     org.omg.CORBA.Object ref,
				     String typeName,
				     TypeStruct type)
	throws org.omg.CosTrading.RegisterPackage.InterfaceTypeMismatch
    {
	// Verify that the given object is compatible with the interface
	// specified by the service type

	// To validate an interface, we must check that the object reference
	// supplied is the same as the interface specified in the service
	// type, or is a subtype of the interface

	if (m_interfaceRepos != null) {
	    // retrieve the InterfaceDef object for the interface
	    try {
		org.omg.CORBA.InterfaceDef def = InterfaceDefHelper.narrow(ref._get_interface_def());
		org.omg.CORBA.Contained c = m_interfaceRepos.lookup(type.if_name);

		// we validate only if we got both the object's definition
		// and the type's interface definition
		if (def != null && c != null) {
		    String id = c.id();
		    if (! def.is_a(id))
			throw new org.omg.CosTrading.RegisterPackage.InterfaceTypeMismatch(
											   typeName, ref);
		}
	    }
	    catch (org.omg.CORBA.SystemException e) {
		// ignore
	    }
	}
    }


    protected void checkDelete(
			       String[] del_list,
			       String typeName,
			       TypeStruct type,
			       OfferInfo info)
	throws IllegalPropertyName,
	UnknownPropertyName,
	MandatoryProperty,
	ReadonlyProperty,
	DuplicatePropertyName
    {
	// create a hashtable of the offer's properties
	Hashtable offerProps = new Hashtable();
	for (int i = 0; i < info.properties.length; i++)
	    offerProps.put(info.properties[i].name, info.properties[i]);

	// create a hashtable of the service type's properties
	Hashtable typeProps = new Hashtable();
	for (int i = 0; i < type.props.length; i++)
	    typeProps.put(type.props[i].name, type.props[i]);

	// also create a hashtable of the deleted property names we've processed
	Hashtable deletedProps = new Hashtable();

	// for each property name in del_list, check to see if the offer
	// contains the property, make sure it isn't mandatory, and make
	// sure there aren't duplicate names in del_list
	for (int i = 0; i < del_list.length; i++) {
	    String propName = del_list[i];

	    // check for a duplicate
	    if (deletedProps.containsKey(propName))
		throw new DuplicatePropertyName(propName);

	    deletedProps.put(propName, propName);

	    // find the property with the matching name
	    Property prop = (Property)offerProps.get(propName);

	    if (prop != null) {
		PropStruct ps = (PropStruct)typeProps.get(propName);
		if (ps != null) {
		    if (OfferUtil.isMandatory(ps.mode))
			throw new MandatoryProperty(typeName, propName);
		}
	    }
	    else
		throw new UnknownPropertyName(propName);
	}
    }


    protected void checkModify(
			       Property[] modify_list,
			       String typeName,
			       TypeStruct type,
			       OfferInfo info)
	throws IllegalPropertyName,
	UnknownPropertyName,
	PropertyTypeMismatch,
	ReadonlyDynamicProperty,
	ReadonlyProperty,
	DuplicatePropertyName
    {
	// create a hashtable of the modified properties
	Hashtable modProps = new Hashtable();
	for (int i = 0; i < modify_list.length; i++)
	    modProps.put(modify_list[i].name, modify_list[i]);

	// create a hashtable of the offer's properties
	Hashtable offerProps = new Hashtable();
	for (int i = 0; i < info.properties.length; i++)
	    offerProps.put(info.properties[i].name, info.properties[i]);

	// create a hashtable of the service type's properties
	Hashtable typeProps = new Hashtable();
	for (int i = 0; i < type.props.length; i++)
	    typeProps.put(type.props[i].name, type.props[i]);

	// also create a hashtable of the properties we've processed
	Hashtable checkedProps = new Hashtable();


	// check the modification list for readonly properties, type
	// mismatches and duplicates

	Enumeration e = modProps.elements();
	while (e.hasMoreElements()) {
	    Property prop = (Property)e.nextElement();

	    if (checkedProps.containsKey(prop.name))
		throw new DuplicatePropertyName(prop.name);

	    checkedProps.put(prop.name, prop);

	    // lookup the property information in the service type
	    PropStruct ps = (PropStruct)typeProps.get(prop.name);
	    if (ps == null)
		throw new UnknownPropertyName(prop.name);

	    // if the property is present in the offer, and the service
	    // type says the property mode is read-only, then it's an error
	    if (offerProps.containsKey(prop.name) && OfferUtil.isReadonly(ps.mode))
		throw new ReadonlyProperty(typeName, prop.name);

	    OfferUtil.checkProperty(typeName, prop, ps);
	}
    }
}




