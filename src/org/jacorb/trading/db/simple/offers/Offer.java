
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

package org.jacorb.trading.db.simple.offers;

import java.io.*;
import java.util.*;
import org.omg.CosTrading.RegisterPackage.*;
import org.omg.CosTrading.Property;

public class Offer implements Serializable
{
    private String m_id;
    private String m_object;
    private Vector m_props;
    private transient OfferInfo m_description;

    static final long serialVersionUID = 4241426996695613295L;

    private Offer()
    {
    }

    public Offer(String id, org.omg.CORBA.Object obj, Property[] props)
    {
	m_id = id;
	m_object = org.jacorb.trading.TradingService.getORB().object_to_string(obj);
	setProperties(props);
	m_description = null;
    }

    public OfferInfo describe()
    {
	OfferInfo result = null;

	if (m_description == null) {
	    result = new OfferInfo();
	    result.reference = org.jacorb.trading.TradingService.getORB().string_to_object(m_object);
	    result.properties = new Property[m_props.size()];
	    int count = 0;
	    Enumeration e = m_props.elements();
	    while (e.hasMoreElements()) {
		OfferProperty prop = (OfferProperty)e.nextElement();
		result.properties[count] = prop.describe();
		count++;
	    }

	    m_description = result;
	}
	else
	    result = m_description;

	return result;
    }


    public void modify(Property[] props)
    {
	setProperties(props);
	m_description = null;
    }


    public int hashCode()
    {
	return m_id.hashCode();
    }


    public boolean equals(java.lang.Object o)
    {
	Offer offer = (Offer)o;
	return m_id.equals(offer.m_id);
    }


    protected void setProperties(Property[] props)
    {
	m_props = new Vector();
	for (int i = 0; i < props.length; i++) 
	{
	    OfferProperty prop = new OfferProperty(props[i]);
	    m_props.addElement(prop);
	}
    }


    private void readObject(ObjectInputStream in)
	throws IOException, ClassNotFoundException
    {
	in.defaultReadObject();
	m_description = null;
    }
}




