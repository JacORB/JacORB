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

package org.jacorb.trading.db.simple.types;

import java.io.*;
import java.util.*;
import org.omg.CORBA.*;
import org.omg.CosTradingRepos.ServiceTypeRepositoryPackage.*;

public class Type implements Serializable
{
    private String m_name;
    private String m_interfaceName;
    private Vector m_properties;
    private String[] m_superTypes;
    private Incarnation m_incarnation;
    private boolean m_masked;
    private transient TypeStruct m_description;

    static final long serialVersionUID = -4144073280876434847L;

    public Type()
    {
    }

    public Type(
		String name,
		String interfaceName,
		PropStruct[] props,
		String[] superTypes,
		IncarnationNumber inc)
    {
	m_name = name;
	m_interfaceName = interfaceName;
	m_incarnation = new Incarnation(inc);
	m_masked = false;

	m_superTypes = new String[superTypes.length];
	for (int i = 0; i < superTypes.length; i++)
	    m_superTypes[i] = superTypes[i];

	m_properties = new Vector();
	for (int i = 0; i < props.length; i++) {
	    TypeProperty prop = new TypeProperty(props[i]);
	    m_properties.addElement(prop);
	}

	createDescription();
    }


    public TypeStruct describe()
    {
	return m_description;
    }


    public PropStruct getPropertyInfo(String name)
    {
	PropStruct result = null;

	for (int i = 0; i < m_description.props.length && result == null; i++) {
	    if (name.equals(m_description.props[i].name))
		result = m_description.props[i];
	}

	return result;
    }


    public String getName()
    {
	return m_name;
    }


    public String getInterfaceName()
    {
	return m_interfaceName;
    }


    public boolean getMasked()
    {
	return m_masked;
    }


    public String[] getSuperTypes()
    {
	return m_description.super_types;
    }


    public void mask()
    {
	m_masked = true;
	m_description.masked = true;
    }


    public void unmask()
    {
	m_masked = false;
	m_description.masked = false;
    }


    public Incarnation getIncarnation()
    {
	return m_incarnation;
    }


    public int hashCode()
    {
	return m_name.hashCode();
    }


    public boolean equals(java.lang.Object o)
    {
	Type impl = (Type)o;
	return m_name.equals(impl.getName());
    }


    private void readObject(ObjectInputStream in)
	throws IOException, ClassNotFoundException
    {
	in.defaultReadObject();
	createDescription();
    }


    private void createDescription()
    {
	m_description = new TypeStruct();
	m_description.if_name = m_interfaceName;
	m_description.masked = m_masked;
	m_description.incarnation = m_incarnation.getIncarnationNumber();

	Enumeration e;
	int count;

	m_description.props = new PropStruct[m_properties.size()];
	count = 0;
	e = m_properties.elements();
	while (e.hasMoreElements()) {
	    TypeProperty prop = (TypeProperty)e.nextElement();
	    m_description.props[count] = prop.describe();
	    count++;
	}

	m_description.super_types = new String[m_superTypes.length];
	for (int i = 0; i < m_superTypes.length; i++)
	    m_description.super_types[i] = m_superTypes[i];
    }
}




