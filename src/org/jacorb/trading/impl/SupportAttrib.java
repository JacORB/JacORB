
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

package org.jacorb.trading.impl;

import org.omg.CORBA.*;


/**
 * Manages the trader's support attributes
 */
public class SupportAttrib
{
  private boolean m_modifiableProperties;
  private boolean m_dynamicProperties;
  private boolean m_proxyOffers;
  private org.omg.CORBA.Object m_typeRepos;


  public synchronized boolean getModifiableProperties()
  {
    return m_modifiableProperties;
  }


  public synchronized boolean setModifiableProperties(boolean value)
  {
    boolean result = m_modifiableProperties;
    m_modifiableProperties = value;
    return result;
  }


  public synchronized boolean getDynamicProperties()
  {
    return m_dynamicProperties;
  }


  public synchronized boolean setDynamicProperties(boolean value)
  {
    boolean result = m_dynamicProperties;
    m_dynamicProperties = value;
    return result;
  }


  public synchronized boolean getProxyOffers()
  {
    return m_proxyOffers;
  }


  public synchronized boolean setProxyOffers(boolean value)
  {
    boolean result = m_proxyOffers;
    m_proxyOffers = value;
    return result;
  }


  public synchronized org.omg.CORBA.Object getTypeRepos()
  {
    return m_typeRepos;
  }


  public synchronized org.omg.CORBA.Object setTypeRepos(
    org.omg.CORBA.Object value)
  {
    org.omg.CORBA.Object result = m_typeRepos;
    m_typeRepos = value;
    return result;
  }
}










