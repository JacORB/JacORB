
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


import java.io.*;
import java.util.*;
import COM.odi.*;
import COM.odi.util.*;
import org.omg.CORBA.*;
import org.omg.CosTrading.Lookup;
import org.omg.CosTrading.RegisterPackage.*;
import org.omg.CosTrading.ProxyPackage.*;
import org.omg.CosTrading.Property;
import org.omg.CosTrading.Policy;
import org.omg.CosTradingDynamic.*;
import org.jacorb.trading.db.OfferDatabase;
import org.jacorb.trading.db.pse.util.TransactionMgr;
import org.jacorb.trading.util.*;


public class OfferDatabaseImpl implements OfferDatabase
{
  private Database m_database;
  private TransactionMgr m_txnMgr;
  private OSHashtable m_offerLists;

  private static final String LISTS_ROOT = "offer_lists";


  private OfferDatabaseImpl()
  {
  }


  public OfferDatabaseImpl(COM.odi.Database database, TransactionMgr txnMgr)
  {
    m_database = database;
    m_txnMgr = txnMgr;

    boolean foundRoot = false;
    Transaction tr = null;

    try {
      tr = Transaction.begin(ObjectStore.READONLY);
      m_offerLists = (OSHashtable)m_database.getRoot(LISTS_ROOT);
      tr.commit(ObjectStore.RETAIN_HOLLOW);
      foundRoot = true;
    }
    catch (DatabaseRootNotFoundException e) {
      tr.abort(ObjectStore.RETAIN_HOLLOW);
    }

    if (! foundRoot) {
      tr = Transaction.begin(ObjectStore.UPDATE);
      m_offerLists = new OSHashtable();
      m_database.createRoot(LISTS_ROOT, m_offerLists);
      tr.commit(ObjectStore.RETAIN_HOLLOW);
    }
  }


  /** Returns true if the offerId is legal, false otherwise */
  public boolean validateOfferId(String offerId)
  {
    return OfferList.validateOfferId(offerId);
  }


  /** Returns true if the database can store a property with the given value */
  public boolean isTypeSupported(org.omg.CORBA.Any any)
  {
    boolean result;

      // if the given value is a dynamic property, then make sure
      // we can store the value of the extra_info field
    if (PropUtil.isDynamicProperty(any.type())) {
      DynamicProp dp = DynamicPropHelper.extract(any);
      result = AnyValue.isTypeSupported(dp.extra_info.type());
    }
    else
      result = AnyValue.isTypeSupported(any.type());

    return result;
  }


  /** Must precede any use of the database */
  public void begin(int mode)
  {
    m_txnMgr.begin();
  }


  /** Must follow any use of the database */
  public void end()
  {
    m_txnMgr.commit(ObjectStore.RETAIN_HOLLOW);
  }


  /** Returns true if the offer with the given ID exists */
  public boolean exists(String offerId)
  {
    boolean result = false;

    OfferList list = (OfferList)m_offerLists.get(whichService(offerId));
    if (list != null)
      result = list.exists(offerId);

    return result;
  }


  /** Returns true if the offer with the given ID is a proxy offer */
  public boolean isProxy(String offerId)
  {
    boolean result = false;

    OfferList list = (OfferList)m_offerLists.get(whichService(offerId));
    if (list != null)
      result = list.isProxy(offerId);

    return result;
  }


  /** Creates a new offer, returning the assigned offer ID */
  public String create(
    String serviceType,
    org.omg.CORBA.Object obj,
    Property[] props)
  {
    OfferList list = (OfferList)m_offerLists.get(serviceType);
    if (list == null) {
      list = new OfferList(serviceType);
      m_offerLists.put(serviceType, list);
    }

    return list.create(obj, props);
  }


  /** Creates a new proxy offer, returning the assigned offer ID */
  public String createProxy(
    Lookup target,
    String serviceType,
    Property[] props,
    boolean ifMatchAll,
    String recipe,
    Policy[] policies)
  {
    OfferList list = (OfferList)m_offerLists.get(serviceType);
    if (list == null) {
      list = new OfferList(serviceType);
      m_offerLists.put(serviceType, list);
    }

    return list.createProxy(target, props, ifMatchAll, recipe, policies);
  }


  /** Removes the offer with the given ID */
  public void remove(String offerId)
  {
    OfferList list = (OfferList)m_offerLists.get(whichService(offerId));
    if (list != null)
      list.remove(offerId);
  }


  /** Removes the proxy offer with the given ID */
  public void removeProxy(String offerId)
  {
    OfferList list = (OfferList)m_offerLists.get(whichService(offerId));
    if (list != null)
      list.removeProxy(offerId);
  }


  /** Returns a description of the offer with the given ID */
  public OfferInfo describe(String offerId)
  {
    OfferInfo result = null;

    OfferList list = (OfferList)m_offerLists.get(whichService(offerId));
    if (list != null)
      result = list.describe(offerId);

    return result;
  }


  /** Returns a description of the proxy offer with the given ID */
  public ProxyInfo describeProxy(String offerId)
  {
    ProxyInfo result = null;

    OfferList list = (OfferList)m_offerLists.get(whichService(offerId));
    if (list != null)
      result = list.describeProxy(offerId);

    return result;
  }


  /** Updates the properties of an offer */
  public void modify(String offerId, Property[] props)
  {
    OfferList list = (OfferList)m_offerLists.get(whichService(offerId));
    if (list != null)
      list.modify(offerId, props);
  }


  /** Returns all offers of the given service type */
  public Hashtable getOffers(String serviceType)
  {
    Hashtable result = null;

    OfferList list = (OfferList)m_offerLists.get(serviceType);
    if (list != null)
      result = list.getOffers();

    return result;
  }


  /** Returns all offer IDs of the given service type */
  public String[] getOfferIds(String serviceType)
  {
    String[] result = null;

    OfferList list = (OfferList)m_offerLists.get(serviceType);
    if (list != null)
      result = list.getOfferIds();

    return result;
  }


  /** Returns all proxy offers of the given service type */
  public Hashtable getProxyOffers(String serviceType)
  {
    Hashtable result = null;

    OfferList list = (OfferList)m_offerLists.get(serviceType);
    if (list != null)
      result = list.getProxyOffers();

    return result;
  }


  /** Returns all proxy offer IDs of the given service type */
  public String[] getProxyOfferIds(String serviceType)
  {
    String[] result = null;

    OfferList list = (OfferList)m_offerLists.get(serviceType);
    if (list != null)
      result = list.getProxyOfferIds();

    return result;
  }


  /** Returns the service type of the given offer */
  public String whichService(String offerId)
  {
    return OfferList.whichService(offerId);
  }
}










