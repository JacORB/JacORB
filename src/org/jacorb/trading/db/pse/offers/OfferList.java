
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
import COM.odi.*;
import COM.odi.util.*;
import org.omg.CORBA.*;
import org.omg.CosTrading.Lookup;
import org.omg.CosTrading.RegisterPackage.*;
import org.omg.CosTrading.ProxyPackage.*;
import org.omg.CosTrading.Property;
import org.omg.CosTrading.Policy;


public class OfferList
{
  private String m_serviceType;
  private OSHashtable m_offers;
  private OSHashtable m_proxies;
  private int m_nextId;

  private static final char ID_SEP = '/';


  private OfferList()
  {
  }


  public OfferList(String serviceType)
  {
    m_serviceType = serviceType;
    m_offers = new OSHashtable();
    m_proxies = new OSHashtable();
    m_nextId = 1;
  }


  public String getServiceType()
  {
    return m_serviceType;
  }


  /** Returns true if the offerId is legal, false otherwise */
  public static boolean validateOfferId(String offerId)
  {
    boolean result = false;

    if (offerId != null) {
        // not a very complete check
      int index = offerId.indexOf(ID_SEP);
      result = (index > 0);
    }

    return result;
  }


  public boolean exists(String offerId)
  {
    return (m_offers.containsKey(offerId) || m_proxies.containsKey(offerId));
  }


  public boolean isProxy(String offerId)
  {
    return m_proxies.containsKey(offerId);
  }


  public String create(org.omg.CORBA.Object obj, Property[] props)
  {
    String result = null;

    result = m_serviceType + ID_SEP + m_nextId;
    m_nextId++;
    Offer offer = new Offer(result, obj, props);
    m_offers.put(result, offer);

    return result;
  }


  public String createProxy(
    Lookup target,
    Property[] props,
    boolean ifMatchAll,
    String recipe,
    Policy[] policies)
  {
    String result = null;

    result = m_serviceType + ID_SEP + m_nextId;
    m_nextId++;
    ProxyOffer proxy =
      new ProxyOffer(result, target, props, ifMatchAll, recipe, policies);
    m_proxies.put(result, proxy);

    return result;
  }


  public void remove(String offerId)
  {
    if (m_offers.containsKey(offerId))
      m_offers.remove(offerId);
  }


  public void removeProxy(String offerId)
  {
    if (m_proxies.containsKey(offerId))
      m_proxies.remove(offerId);
  }


  public OfferInfo describe(String offerId)
  {
    OfferInfo result = null;

    Offer offer = (Offer)m_offers.get(offerId);
    if (offer != null) {
      result = offer.describe();
      result.type = m_serviceType;
    }

    return result;
  }


  public ProxyInfo describeProxy(String offerId)
  {
    ProxyInfo result = null;

    ProxyOffer proxy = (ProxyOffer)m_proxies.get(offerId);
    if (proxy != null) {
      result = proxy.describe();
      result.type = m_serviceType;
    }

    return result;
  }


  public boolean modify(String offerId, Property[] props)
  {
    boolean result = false;

    Offer offer = (Offer)m_offers.get(offerId);
    if (offer != null) {
      offer.modify(props);
      result = true;
    }

    return result;
  }


  public Hashtable getOffers()
  {
    Hashtable result = new Hashtable();

    Enumeration e = m_offers.keys();
    while (e.hasMoreElements()) {
      String offerId = (String)e.nextElement();
      Offer offer = (Offer)m_offers.get(offerId);
      OfferInfo info = offer.describe();
      info.type = m_serviceType;
      result.put(offerId, info);
    }

    return result;
  }


  public String[] getOfferIds()
  {
    String[] result = new String[m_offers.size()];

    int count = 0;
    Enumeration e = m_offers.keys();
    while (e.hasMoreElements())
      result[count++] = (String)e.nextElement();

    return result;
  }


  public Hashtable getProxyOffers()
  {
    Hashtable result = new Hashtable();

    Enumeration e = m_proxies.keys();
    while (e.hasMoreElements()) {
      String offerId = (String)e.nextElement();
      ProxyOffer proxy = (ProxyOffer)m_proxies.get(offerId);
      ProxyInfo info = proxy.describe();
      info.type = m_serviceType;
      result.put(offerId, info);
    }

    return result;
  }


  public String[] getProxyOfferIds()
  {
    String[] result = new String[m_proxies.size()];

    int count = 0;
    Enumeration e = m_proxies.keys();
    while (e.hasMoreElements())
      result[count++] = (String)e.nextElement();

    return result;
  }


  public static String whichService(String offerId)
  {
    String result = null;

    int index = offerId.indexOf(ID_SEP);
    if (index > 0)
      result = offerId.substring(0, index);

    return result;
  }


  public int hashCode()
  {
    return m_serviceType.hashCode();
  }


  public boolean equals(java.lang.Object o)
  {
    OfferList list = (OfferList)o;
    return m_serviceType.equals(list.m_serviceType);
  }
}










