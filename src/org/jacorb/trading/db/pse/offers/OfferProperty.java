
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


import org.omg.CosTrading.Property;
import org.jacorb.trading.util.*;


public class OfferProperty
{
  private String m_name;
  private Object m_value;


  private OfferProperty()
  {
  }


  public OfferProperty(Property prop)
  {
    m_name = prop.name;
    if (PropUtil.isDynamicProperty(prop.value.type()))
      m_value = new DynPropValue(prop.value);
    else
      m_value = new AnyValue(prop.value);
  }


  public Property describe()
  {
    Property result = new Property();

    result.name = m_name;
    if (m_value instanceof AnyValue)
      result.value = ((AnyValue)m_value).getValue();
    else if (m_value instanceof DynPropValue)
      result.value = ((DynPropValue)m_value).getValue();

    return result;
  }
}










