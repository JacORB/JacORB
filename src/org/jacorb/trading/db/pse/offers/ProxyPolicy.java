
// Copyright (C) 1998-2001
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
import org.omg.CosTrading.Policy;
import org.jacorb.trading.util.*;


public class ProxyPolicy
{
  private String m_name;
  private AnyValue m_value;


  private ProxyPolicy()
  {
  }


  public ProxyPolicy(Policy prop)
  {
    m_name = prop.name;
    m_value = new AnyValue(prop.value);
  }


  public Policy describe()
  {
    Policy result = new Policy();

    result.name = m_name;
    result.value = m_value.getValue();

    return result;
  }
}










