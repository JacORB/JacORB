
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

package org.jacorb.trading.db;


import java.util.*;
import org.omg.CosTrading.Lookup;
import org.omg.CosTrading.RegisterPackage.OfferInfo;
import org.omg.CosTrading.ProxyPackage.ProxyInfo;
import org.omg.CosTrading.Property;
import org.omg.CosTrading.Policy;


/**
 * An abstract interface to the offer database
 */
public interface OfferDatabase
{
  /** One of these must be supplied to the begin() method */
  public static final int READ = 0;
  public static final int WRITE = 1;


  /** Returns true if the offerId is legal, false otherwise */
  public boolean validateOfferId(String offerId);

  /** Returns true if the database can store a property with the given value */
  public boolean isTypeSupported(org.omg.CORBA.Any any);


  /**
   * Must precede any use of the database methods below - may block if the
   * requested lock is not available
   */
  public void begin(int mode);

  /** Must follow any use of the database methods below */
  public void end();


  /** Returns true if the offer with the given ID exists */
  public boolean exists(String offerId);

  /** Returns true if the offer with the given ID is a proxy offer */
  public boolean isProxy(String offerId);

  /** Creates a new offer, returning the assigned offer ID */
  public String create(
    String serviceType,
    org.omg.CORBA.Object obj,
    Property[] props);

  /** Creates a new proxy offer, returning the assigned offer ID */
  public String createProxy(
    Lookup target,
    String serviceType,
    Property[] props,
    boolean ifMatchAll,
    String recipe,
    Policy[] policies);

  /** Removes the offer with the given ID */
  public void remove(String offerId);

  /** Removes the proxy offer with the given ID */
  public void removeProxy(String offerId);

  /**
    * Returns a description of the offer with the given ID, or null if
    * the offer wasn't found
    */
  public OfferInfo describe(String offerId);

  /**
    * Returns a description of the proxy offer with the given ID, or null
    * if the offer wasn't found
    */
  public ProxyInfo describeProxy(String offerId);

  /** Updates the properties of an offer */
  public void modify(String offerId, Property[] props);

  /**
    * Returns all offers of the given service type in a hashtable,
    * where the offer ID is the key and OfferInfo is the value;
    * returns null if the service type does not exist
    */
  public Hashtable getOffers(String serviceType);

  /**
    * Returns all offer IDs of the given service type;
    * returns null if the service type does not exist
    */
  public String[] getOfferIds(String serviceType);

  /**
    * Returns all proxy offers of the given service type in a hashtable,
    * where the offer ID is the key and ProxyInfo is the value;
    * returns null if the service type does not exist
    */
  public Hashtable getProxyOffers(String serviceType);

  /**
    * Returns all proxy offer IDs of the given service type;
    * returns null if the service type does not exist
    */
  public String[] getProxyOfferIds(String serviceType);

  /**
    * Returns the service type of the given offer;
    * returns null if the offer ID is malformed
    */
  public String whichService(String offerId);
}










