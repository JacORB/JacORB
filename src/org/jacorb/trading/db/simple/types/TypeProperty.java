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

public class TypeProperty implements Serializable
{
    private String m_name;
    private int m_kind;
    private boolean m_sequence;
    private int m_mode;

    static final long serialVersionUID = 3756829227846447959L;

    public TypeProperty()
    {
    }

    public TypeProperty(PropStruct ps)
    {
	m_name = ps.name;
	m_mode = ps.mode.value();

	TCKind kind = ps.value_type.kind();
	if (kind == TCKind.tk_sequence) {
	    m_sequence = true;
	    try {
		TypeCode elemTC = ps.value_type.content_type();
		kind = elemTC.kind();
		m_kind = kind.value();
	    }
	    catch (org.omg.CORBA.TypeCodePackage.BadKind e) {
		throw new RuntimeException();
	    }
	}
	else {
	    m_sequence = false;
	    m_kind = kind.value();
	}
    }


    public String getName()
    {
	return m_name;
    }


    public PropStruct describe()
    {
	PropStruct result = new PropStruct();

	ORB orb = ORB.init();

	result.name = m_name;

	if (m_sequence) {
	    TypeCode contentType = orb.get_primitive_tc(TCKind.from_int(m_kind));
	    result.value_type = orb.create_sequence_tc(0, contentType);
	}
	else
	    result.value_type = orb.get_primitive_tc(TCKind.from_int(m_kind));

	result.mode = PropertyMode.from_int(m_mode);

	return result;
    }


    public boolean equals(java.lang.Object o)
    {
	TypeProperty prop = (TypeProperty)o;
	return m_name.equals(prop.getName());
    }


    public int hashCode()
    {
	return m_name.hashCode();
    }
}










