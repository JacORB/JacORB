
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

import java.util.*;
import org.omg.CORBA.*;
import org.omg.CosTrading.*;
import org.omg.CosTrading.ProxyPackage.*;
import org.omg.CosTradingRepos.*;
import org.omg.CosTradingRepos.ServiceTypeRepositoryPackage.*;
import org.jacorb.trading.db.OfferDatabase;
import org.jacorb.trading.util.*;


/**
 * Implementation of CosTrading::Proxy
 */
public class ProxyImpl extends ProxyPOA // GB: instead of _ProxyBaseImpl
{
  private TraderComp m_traderComp;
  private SupportAttrib m_support;
  private OfferDatabase m_db;
  private ServiceTypeRepository m_repos;


  private ProxyImpl()
  {
  }


  public ProxyImpl(
    TraderComp traderComp,
    SupportAttrib supportAttrib,
    OfferDatabase db)
  {
    m_traderComp = traderComp;
    m_support = supportAttrib;
    m_db = db;
    org.omg.CORBA.Object obj = supportAttrib.getTypeRepos();
    m_repos = ServiceTypeRepositoryHelper.narrow(obj);
  }


    // operations inherited from CosTrading::TraderComponents

  public Lookup lookup_if()
  {
    return m_traderComp.getLookupInterface();
  }


  public Register register_if()
  {
    return m_traderComp.getRegisterInterface();
  }


  public Link link_if()
  {
    return m_traderComp.getLinkInterface();
  }


  public Proxy proxy_if()
  {
    return m_traderComp.getProxyInterface();
  }


  public Admin admin_if()
  {
    return m_traderComp.getAdminInterface();
  }


    // operations inherited from CosTrading::SupportAttributes

  public boolean supports_modifiable_properties()
  {
    return m_support.getModifiableProperties();
  }


  public boolean supports_dynamic_properties()
  {
    return m_support.getDynamicProperties();
  }


  public boolean supports_proxy_offers()
  {
    return m_support.getProxyOffers();
  }


  public org.omg.CORBA.Object type_repos()
  {
    return m_support.getTypeRepos();
  }


    // operations inherited from CosTrading::Proxy

  public String export_proxy(
    Lookup target,
    String type,
    Property[] properties,
    boolean if_match_all,
    String recipe,
    org.omg.CosTrading.Policy[] policies_to_pass_on)
    throws IllegalServiceType,
           UnknownServiceType,
           InvalidLookupRef,
           IllegalPropertyName,
           PropertyTypeMismatch,
           ReadonlyDynamicProperty,
           MissingMandatoryProperty,
           IllegalRecipe,
           DuplicatePropertyName,
           DuplicatePolicyName
  {
    String result = null;

    if (target == null)
      throw new InvalidLookupRef(target);

      // retrieve complete information about the service type from the
      // repository - may throw IllegalServiceType, UnknownServiceType
    TypeStruct ts = m_repos.fully_describe_type(type);

      // do not allow exporting for a masked service type
    if (ts.masked)
      throw new UnknownServiceType(type);

      // validate the exported properties - may throw
      // IllegalPropertyName, PropertyTypeMismatch,
      // MissingMandatoryProperty, DuplicatePropertyName
    OfferUtil.validateProperties(m_db, properties, type, ts);

      // validate the recipe
    if (! Recipe.validate(recipe, properties))
      throw new IllegalRecipe(recipe);

      // check for duplicate policies
    Hashtable policyTable = new Hashtable();
    for (int i = 0; i < policies_to_pass_on.length; i++) {
      if (policyTable.containsKey(policies_to_pass_on[i].name))
        throw new DuplicatePolicyName(policies_to_pass_on[i].name);
      else
        policyTable.put(policies_to_pass_on[i].name, policies_to_pass_on[i]);
    }

      // save the offer in the database

    m_db.begin(OfferDatabase.WRITE);

    try {
      result = m_db.createProxy(target, type, properties, if_match_all,
        recipe, policies_to_pass_on);
    }
    finally {
      m_db.end();
    }

    return result;
  }


  public void withdraw_proxy(String id)
    throws IllegalOfferId,
           UnknownOfferId,
           NotProxyOfferId
  {
    if (! m_db.validateOfferId(id))
      throw new IllegalOfferId(id);

    m_db.begin(OfferDatabase.WRITE);

    try {
      if (! m_db.exists(id))
        throw new UnknownOfferId(id);

      if (! m_db.isProxy(id))
        throw new NotProxyOfferId(id);

      m_db.removeProxy(id);
    }
    finally {
      m_db.end();
    }
  }


  public ProxyInfo describe_proxy(String id)
    throws IllegalOfferId,
           UnknownOfferId,
           NotProxyOfferId
  {
    ProxyInfo result;

    if (! m_db.validateOfferId(id))
      throw new IllegalOfferId(id);

    m_db.begin(OfferDatabase.READ);

    try {
      if (! m_db.exists(id))
        throw new UnknownOfferId(id);

      if (! m_db.isProxy(id))
        throw new NotProxyOfferId(id);

      result = m_db.describeProxy(id);
    }
    finally {
      m_db.end();
    }

    return result;
  }
}










