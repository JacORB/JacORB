
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

package org.jacorb.trading.db.simple.offers;

import java.io.*;
import java.util.*;
import org.omg.CosTrading.*;
import org.omg.CosTrading.ProxyPackage.*;
import org.omg.CosTrading.RegisterPackage.*;

public class ProxyOffer implements Serializable
{
  private String m_id;
  private String m_target;
  private Vector m_props;
  private boolean m_ifMatchAll;
  private String m_recipe;
  private Vector m_policies;
  private transient ProxyInfo m_description;

  static final long serialVersionUID = -2760477527985159980L;

  private ProxyOffer()
  {
  }

  public ProxyOffer(
    String id,
    Lookup target,
    Property[] props,
    boolean ifMatchAll,
    String recipe,
    Policy[] policies)
  {
    m_id = id;
    m_target = org.jacorb.trading.TradingService.getORB().object_to_string(target);
    setProperties(props);
    m_ifMatchAll = ifMatchAll;
    m_recipe = recipe;
    setPolicies(policies);
    m_description = null;
  }


  public ProxyInfo describe()
  {
    ProxyInfo result = null;

    if (m_description == null) 
    {
      result = new ProxyInfo();
      org.omg.CORBA.Object obj =jacorb.trading.TradingService.getORB().string_to_object(m_target);
      result.target = LookupHelper.narrow(obj);

      result.properties = new Property[m_props.size()];
      int count = 0;
      Enumeration e = m_props.elements();
      while (e.hasMoreElements()) {
        OfferProperty prop = (OfferProperty)e.nextElement();
        result.properties[count] = prop.describe();
        count++;
      }

      result.if_match_all = m_ifMatchAll;
      result.recipe = m_recipe;

      result.policies_to_pass_on = new Policy[m_policies.size()];
      count = 0;
      e = m_policies.elements();
      while (e.hasMoreElements()) {
        ProxyPolicy policy = (ProxyPolicy)e.nextElement();
        result.policies_to_pass_on[count] = policy.describe();
        count++;
      }

      m_description = result;
    }
    else
      result = m_description;

    return result;
  }


  public int hashCode()
  {
    return m_id.hashCode();
  }


  public boolean equals(java.lang.Object o)
  {
    ProxyOffer proxy = (ProxyOffer)o;
    return m_id.equals(proxy.m_id);
  }


  protected void setProperties(Property[] props)
  {
    m_props = new Vector();
    for (int i = 0; i < props.length; i++) 
    {
      OfferProperty prop = new OfferProperty( props[i]);
      m_props.addElement(prop);
    }
  }


  protected void setPolicies(Policy[] policies)
  {
    m_policies = new Vector();
    for (int i = 0; i < policies.length; i++) 
    {
      ProxyPolicy policy = new ProxyPolicy( policies[i]);
      m_policies.addElement(policy);
    }
  }


  private void readObject(ObjectInputStream in)
    throws IOException, ClassNotFoundException
  {
    in.defaultReadObject();
    m_description = null;
  }
}










