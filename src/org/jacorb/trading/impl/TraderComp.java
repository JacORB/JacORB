
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

import org.omg.CORBA.*;
import org.omg.CosTrading.*;


/**
 * Manages the trader components
 */
public class TraderComp
{
  private Lookup m_lookup;
  private Register m_register;
  private Link m_link;
  private Proxy m_proxy;
  private Admin m_admin;


  public Lookup getLookupInterface()
  {
    return m_lookup;
  }


  public void setLookupInterface(Lookup value)
  {
    m_lookup = value;
  }


  public Register getRegisterInterface()
  {
    return m_register;
  }


  public void setRegisterInterface(Register value)
  {
    m_register = value;
  }


  public Link getLinkInterface()
  {
    return m_link;
  }


  public void setLinkInterface(Link value)
  {
    m_link = value;
  }


  public Proxy getProxyInterface()
  {
    return m_proxy;
  }


  public void setProxyInterface(Proxy value)
  {
    m_proxy = value;
  }


  public Admin getAdminInterface()
  {
    return m_admin;
  }


  public void setAdminInterface(Admin value)
  {
    m_admin = value;
  }
}










