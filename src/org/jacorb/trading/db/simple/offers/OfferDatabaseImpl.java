
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
import org.omg.CORBA.*;
import org.omg.CosTrading.Lookup;
import org.omg.CosTrading.RegisterPackage.*;
import org.omg.CosTrading.ProxyPackage.*;
import org.omg.CosTrading.Property;
import org.omg.CosTrading.Policy;
import org.omg.CosTradingDynamic.*;
import org.jacorb.trading.db.OfferDatabase;
import org.jacorb.trading.util.*;

/**
 * Simple implementation of OfferDatabase using serialized objects
 */
public class OfferDatabaseImpl implements OfferDatabase
{
  private File m_dirPath;
  private Hashtable m_offerLists;
  private Hashtable m_offerIndex;
  private File m_indexFile;
  private int m_counter;
  private boolean m_indexDirty = false;
  private RWLock m_lock;

  private static String LIST_NAME = "OFR_";
  private static String LIST_EXT = ".dat";
  private static String INDEX_FILE = "offeridx.dat";


  private OfferDatabaseImpl()
  {
  }


  public OfferDatabaseImpl(String dirPath)
  {
    m_dirPath = new File(dirPath);
    m_indexFile = new File(dirPath, INDEX_FILE);

    if (m_indexFile.exists())
      readIndex();
    else {
      m_offerIndex = new Hashtable();
      m_counter = 0;
      writeIndex();
    }

      // once an offer list is loaded, it is cached in m_offerLists
    m_offerLists = new Hashtable();

    m_lock = new RWLock();
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
    if (mode == OfferDatabase.READ)
      m_lock.acquireRead();
    else if (mode == OfferDatabase.WRITE)
      m_lock.acquireWrite();
    else
      throw new RuntimeException("Invalid lock mode");
  }


  /** Must follow any use of the database */
  public void end()
  {
      // save any modified offer lists
    Enumeration e = m_offerLists.elements();
    while (e.hasMoreElements()) {
      OfferList list = (OfferList)e.nextElement();
      if (list.getDirty())
        writeList(list);
    }

    if (m_indexDirty) {
      writeIndex();
      m_indexDirty = false;
    }

    m_lock.release();
  }


  /** Returns true if the offer with the given ID exists */
  public boolean exists(String offerId)
  {
    boolean result = false;

    OfferList list = getList(whichService(offerId));
    if (list != null)
      result = list.exists(offerId);

    return result;
  }


  /** Returns true if the offer with the given ID is a proxy offer */
  public boolean isProxy(String offerId)
  {
    boolean result = false;

    OfferList list = getList(whichService(offerId));
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
    OfferList list = getList(serviceType);

    if (list == null)
    {
      list = createList(serviceType);
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
    OfferList list = getList(serviceType);
    if (list == null)
      list = createList(serviceType);

    return list.createProxy(target, props, ifMatchAll, recipe, policies);
  }


  /** Removes the offer with the given ID */
  public void remove(String offerId)
  {
    OfferList list = getList(whichService(offerId));
    if (list != null)
      list.remove(offerId);
  }


  /** Removes the proxy offer with the given ID */
  public void removeProxy(String offerId)
  {
    OfferList list = getList(whichService(offerId));
    if (list != null)
      list.removeProxy(offerId);
  }


  /** Returns a description of the offer with the given ID */
  public OfferInfo describe(String offerId)
  {
    OfferInfo result = null;

    OfferList list = getList(whichService(offerId));
    if (list != null)
      result = list.describe(offerId);

    return result;
  }


  /** Returns a description of the proxy offer with the given ID */
  public ProxyInfo describeProxy(String offerId)
  {
    ProxyInfo result = null;

    OfferList list = getList(whichService(offerId));
    if (list != null)
      result = list.describeProxy(offerId);

    return result;
  }


  /** Updates the properties of an offer */
  public void modify(String offerId, Property[] props)
  {
    OfferList list = getList(whichService(offerId));
    if (list != null)
      list.modify(offerId, props);
  }


  /** Returns all offers of the given service type */
  public Hashtable getOffers(String serviceType)
  {
    Hashtable result = null;

    OfferList list = getList(serviceType);
    if (list != null)
      result = list.getOffers();

    return result;
  }


  /** Returns all offer IDs of the given service type */
  public String[] getOfferIds(String serviceType)
  {
    String[] result = null;

    OfferList list = getList(serviceType);
    if (list != null)
      result = list.getOfferIds();

    return result;
  }


  /** Returns all proxy offers of the given service type */
  public Hashtable getProxyOffers(String serviceType)
  {
    Hashtable result = null;

    OfferList list = getList(serviceType);
    if (list != null)
      result = list.getProxyOffers();

    return result;
  }


  /** Returns all proxy offer IDs of the given service type */
  public String[] getProxyOfferIds(String serviceType)
  {
    String[] result = null;

    OfferList list = getList(serviceType);
    if (list != null)
      result = list.getProxyOfferIds();

    return result;
  }


  /** Returns the service type of the given offer */
  public String whichService(String offerId)
  {
    return OfferList.whichService(offerId);
  }


  /**
   * First checks for the list in the cache (m_offerLists); if not
   * found, attempts to read list from file.  Method must be
   * synchronized because multiple readers may be active.
   */
  protected synchronized OfferList getList(String serviceType)
  {
    OfferList result = null;

    result = (OfferList)m_offerLists.get(serviceType);
    if (result == null) {
      result = readList(serviceType);
      if (result != null)
        m_offerLists.put(serviceType, result);
    }

    return result;
  }


  protected OfferList createList(String serviceType)
  {
    OfferList result = new OfferList(serviceType);
    m_offerLists.put(serviceType, result);
    m_counter++;
    m_offerIndex.put(serviceType, new Integer(m_counter));
    m_indexDirty = true;
    return result;
  }


  protected OfferList readList(String serviceType)
  {
    OfferList result = null;

    File listFile = getListFile(serviceType);
    if (listFile != null && listFile.exists()) {
      try {
        FileInputStream fileIn = new FileInputStream(listFile);
        ObjectInputStream objIn = new ObjectInputStream(fileIn);
        result = (OfferList)objIn.readObject();
        fileIn.close();
      }
      catch (IOException e) {
        throw new RuntimeException(e.getMessage());
      }
      catch (ClassNotFoundException e) {
        throw new RuntimeException(e.getMessage());
      }
    }

    return result;
  }


  protected void writeList(OfferList list)
  {
    try {
      File listFile = getListFile(list.getServiceType());
      if (listFile == null)
        throw new RuntimeException("listFile should not be null!");
      FileOutputStream fileOut = new FileOutputStream(listFile);
      ObjectOutputStream objOut = new ObjectOutputStream(fileOut);
      objOut.writeObject(list);
      fileOut.close();
    }
    catch (IOException e) {
	org.jacorb.util.Debug.output(1,e);
      throw new RuntimeException(e.getMessage());
    }
  }


  protected File getListFile(String serviceType)
  {
    File result = null;

      // obtain the counter associated with this service type
      // from the offer index
    Integer counter = (Integer)m_offerIndex.get(serviceType);

    if (counter != null)
      result = new File(m_dirPath, LIST_NAME + counter + LIST_EXT);

    return result;
  }


  protected void readIndex()
  {
    try {
      FileInputStream fileIn = new FileInputStream(m_indexFile);
      ObjectInputStream objIn = new ObjectInputStream(fileIn);
      Integer i = (Integer)objIn.readObject();
      m_counter = i.intValue();
      m_offerIndex = (Hashtable)objIn.readObject();
      fileIn.close();
    }
    catch (IOException e) {
      throw new RuntimeException(e.getMessage());
    }
    catch (ClassNotFoundException e) {
      throw new RuntimeException(e.getMessage());
    }
  }


  protected void writeIndex()
  {
    try {
      FileOutputStream fileOut = new FileOutputStream(m_indexFile);
      ObjectOutputStream objOut = new ObjectOutputStream(fileOut);
      objOut.writeObject(new Integer(m_counter));
      objOut.writeObject(m_offerIndex);
      fileOut.close();
    }
    catch (IOException e) {
      throw new RuntimeException(e.getMessage());
    }
  }
}










