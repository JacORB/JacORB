
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

package org.jacorb.trading.db.pse.offers;


import java.util.*;
import COM.odi.*;
import COM.odi.util.*;
import org.omg.CosTrading.RegisterPackage.*;
import org.omg.CosTrading.Property;
import jtport.ORBLayer;


public class Offer
{
  private String m_id;
  private String m_object;
  private OSVector m_props;
  private transient OfferInfo m_description;


  private Offer()
  {
  }


  public Offer(String id, org.omg.CORBA.Object obj, Property[] props)
  {
    m_id = id;
    m_object = ORBLayer.instance().getORB().object_to_string(obj);
    setProperties(props);
    m_description = null;
  }


  public OfferInfo describe()
  {
    OfferInfo result = null;

    if (m_description == null) {
      result = new OfferInfo();
      result.reference =
        ORBLayer.instance().getORB().string_to_object(m_object);
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


  /** ObjectStore PSE hook method to initialize transient fields */
  public void postInitializeContents()
  {
    m_description = null;
  }


  /** ObjectStore PSE hook method to clear transient fields */
  public void preClearContents()
  {
    m_description = null;
  }


  protected void setProperties(Property[] props)
  {
    m_props = new OSVector();
    for (int i = 0; i < props.length; i++) {
      OfferProperty prop = new OfferProperty(props[i]);
      m_props.addElement(prop);
    }
  }
}










