package org.jacorb.trading.impl;

import org.omg.CORBA.*;
import org.omg.CosTrading.LinkPackage.*;
import org.omg.CosTrading.*;
import java.util.*;

/**
 * This class implements the Link-interface from CosTrading.
 * It is mainly for administrating the links of the trader
 *
 * @author Nicolas Noffke
 */

public class LinkImpl 
    extends org.omg.CosTrading.LinkPOA 
    implements LinkOperations  
{

    private TraderComp m_traderComp;
    private SupportAttrib m_support;
    private LinkAttrib m_link;

    private Hashtable m_current_links; // all LinkInfo-Instances are stored here
    private boolean m_links_changed = true; // indicates, if links have been added, removed or modidfied


    /**
     * The constructor of LinkImpl.
     * 
     * @param trader_comp The TraderComp-object of this trader
     * @param support The SupportAttrib-object of this trader
     * @param link The LinkAttrib-object of this trader
     */
    public LinkImpl (TraderComp trader_comp, SupportAttrib support, LinkAttrib link){
	m_traderComp = trader_comp;
	m_support = support;
	m_link = link;
	m_current_links = new Hashtable();
    }

    // operations inherited from CosTrading::TraderComponents

    /**
     * Returns the Lookup-Interface of this trader.<br>
     * Inherited from CosTrading::TraderComponents
     * @return The Lookup-Interface
     */
    public Lookup lookup_if()
    {
	return m_traderComp.getLookupInterface();
    }
    
    /**
     * Returns the Register-Interface of this trader.<br>
     * Inherited from CosTrading::TraderComponents
     * @return The Register-Interface
     */   
    public Register register_if()
    {
	return m_traderComp.getRegisterInterface();
    }
    
    /**
     * Returns the Link-Interface of this trader.<br>
     * Inherited from CosTrading::TraderComponents
     * @return The Link-Interface
     */
    public Link link_if()
    {
	return m_traderComp.getLinkInterface();
    }
    
    /**
     * Returns the Proxy-Interface of this trader.<br>
     * Inherited from CosTrading::TraderComponents
     * @return The Proxy-Interface
     */
    public Proxy proxy_if()
    {
	return m_traderComp.getProxyInterface();
    }
    
    /**
     * Returns the Admin-Interface of this trader.<br>
     * Inherited from CosTrading::TraderComponents
     * @return The Admin-Interface
     */
    public Admin admin_if()
    {
	return m_traderComp.getAdminInterface();
    }
    
    // operations inherited from CosTrading::SupportAttributes

    /**
     * Does this trader support modifiable properties.<br>
     * Inherited from CosTrading::SupportAttributes
     * @return True, if it supports them
     */
    public boolean supports_modifiable_properties()
    {
	return m_support.getModifiableProperties();
    }
    

    /**
     * Does this trader support dynamic properties.<br>
     * Inherited from CosTrading::SupportAttributes
     * @return True, if it supports them
     */    
    public boolean supports_dynamic_properties()
    {
	return m_support.getDynamicProperties();
    }
    
    
   /**
     * Does this trader support proxy offers.<br>
     * Inherited from CosTrading::SupportAttributes
     * @return True, if it supports them
     */
    public boolean supports_proxy_offers()
    {
	return m_support.getProxyOffers();
    }
    
    
   /**
     * Returns the TypeRepository.<br>
     * Inherited from CosTrading::SupportAttributes
     * @return The TypeRepository
     */
    public org.omg.CORBA.Object type_repos()
    {
	return m_support.getTypeRepos();
    }   
    

    // operations inherited from CosTrading::LinkAttributes
   /**
     * Get the max_link_follow_policy of this trader.<br>
     * Inherited from CosTrading::LinkAttributes
     * @return The max_link_follow_policy
     */
    public FollowOption max_link_follow_policy()
    {
	return m_link.getMaxLinkFollowPolicy();
    } 

    // operations inherited from CosTrading::LinkOperations
    /**
     * Adds a link to the specified Trader.<br>
     * Inherited from CosTrading::LinkOperations
     *
     * @param name The name for this new link
     * @param target The Lookup-Interface of the Trader to link to
     * @param default_follow_rule The default_follow_rule
     * @param limiting_follow_rule The limiting_follow_rule
     * @exception org.omg.CosTrading.LinkPackage.IllegalLinkName Link name not allowed
     * @exception org.omg.CosTrading.LinkPackage.DuplicateLinkName Link name already in usage
     * @exception org.omg.CosTrading.InvalidLookupRef Lookup-reference is not ok
     * @exception org.omg.CosTrading.LinkPackage.DefaultFollowTooPermissive default_follow_rule more permissive
     * than limiting_follow_rule
     * @exception org.omg.CosTrading.LinkPackage.LimitingFollowTooPermissive limiting_follow_rule more permissive
     * than traders max_link_follow_policy     
     */
    public void add_link(String name, 
			 Lookup target, 
			 FollowOption default_follow_rule, 
			 FollowOption limiting_follow_rule) 
	throws IllegalLinkName, 
	DuplicateLinkName, 
	InvalidLookupRef, 
	DefaultFollowTooPermissive, 
	LimitingFollowTooPermissive {

	// check parameters

	// since there is no specification for a correct link name,
	// we do just a simple check
	// is name legal?
	if (name == null || name.length() == 0)
	    throw new IllegalLinkName(name);

	// duplicate name?
	if (m_current_links.containsKey(name))
	    throw new DuplicateLinkName(name);

	//invalid lookup refence?
	Register _reg = null;
	try {
	    org.omg.CORBA.Object _obj = target.register_if();
	    _reg = RegisterHelper.narrow(_obj);
	}
        catch (Exception e){
	    // if we get an exception here, we assume that the lookup reference is
	    // invalid. It might as well be an network error, but since we cannot 
	    // confirm the lookup reference, we don't accept it.
            throw new InvalidLookupRef(target);
	}

	// default_follow_rule too permissive?
	if (default_follow_rule.value() > limiting_follow_rule.value())
	    throw new DefaultFollowTooPermissive(default_follow_rule, limiting_follow_rule);

	// limiting_follow_rule too permissive?
	if (limiting_follow_rule.value() > max_link_follow_policy().value())
	    throw new LimitingFollowTooPermissive(default_follow_rule, limiting_follow_rule);
	
	// everything o.k., so we create the new link
	LinkInfo _info = new LinkInfo(target, _reg, default_follow_rule, limiting_follow_rule);
	m_current_links.put(name, _info);

	//set flag
	m_links_changed = true;
    }
    
    /**
     * Get the LinkInfo-Object of a specific link.
     * @param name The name of the link
     * @return The LinkInfo-Object of this link
     * @exception org.omg.CosTrading.LinkPackage.IllegalLinkName Link name not allowed
     * @exception org.omg.CosTrading.LinkPackage.UnknownLinkName No link with that name 
     */
    public LinkInfo describe_link(String name) throws IllegalLinkName, UnknownLinkName {
	// is name legal?
	if (name == null || name.length() == 0)
	    throw new IllegalLinkName(name);

	// get LinkInfo
	LinkInfo _result = (LinkInfo) m_current_links.get(name);

	//check for UnknownLinkName (the key has no associated object)
	if (_result == null)
	    throw new UnknownLinkName(name);

	return _result;
    }
    
    /**
     * Lists all link-names.
     * @return An array with all link names
     */
    public String[] list_links() {
	String[] _links_array = new String[m_current_links.size()];
	Enumeration _links = m_current_links.keys();
	int _i = 0;

	while (_links.hasMoreElements())
	    _links_array[_i++] = (String) _links.nextElement();

	return _links_array;
    }
    
    /**
     * Modify an existing link.
     * @param name The name of the link to modify
     * @param default_follow_rule The new default_follow_rule for this link
     * @param limiting_follow_rule The new limiting_follow_rule for this link
     * @exception org.omg.CosTrading.LinkPackage.IllegalLinkName Link name not allowed
     * @exception org.omg.CosTrading.LinkPackage.UnknownLinkName No link for that name exists
     * @exception org.omg.CosTrading.LinkPackage.DefaultFollowTooPermissive default_follow_rule more permissive
     * than limiting_follow_rule
     * @exception org.omg.CosTrading.LinkPackage.LimitingFollowTooPermissive limiting_follow_rule more permissive
     * than traders max_link_follow_policy
     */
    public void modify_link(String name, 
			    FollowOption default_follow_rule, 
			    FollowOption limiting_follow_rule) 
	throws IllegalLinkName, UnknownLinkName, 
	DefaultFollowTooPermissive, LimitingFollowTooPermissive {
	// check parameters

	// since there is no specification for a correct link name,
	// we do just a simple check
	// is name legal?
	if (name == null || name.length() == 0)
	    throw new IllegalLinkName(name);

	// is name known?
	if (! m_current_links.containsKey(name))
	    throw new UnknownLinkName(name);

	// default_follow_rule too permissive?
	if (default_follow_rule.value() > limiting_follow_rule.value())
	    throw new DefaultFollowTooPermissive(default_follow_rule, limiting_follow_rule);

	// limiting_follow_rule too permissive?
	if (limiting_follow_rule.value() > max_link_follow_policy().value())
	    throw new LimitingFollowTooPermissive(default_follow_rule, limiting_follow_rule);
	
	// everything o.k., so we can fetch and modify the link
	LinkInfo _link = (LinkInfo) m_current_links.get(name);
	_link.def_pass_on_follow_rule = default_follow_rule;
	_link.limiting_follow_rule = limiting_follow_rule;

	//set flag
	m_links_changed = true;
    }
    
    /**
     * Remove a specific link.
     * @param name The name of the link to remove
     * @exception org.omg.CosTrading.LinkPackage.IllegalLinkName Link name not allowed
     * @exception org.omg.CosTrading.LinkPackage.UnknownLinkName No link for that name exists
     */
    public void remove_link(String name) throws IllegalLinkName, UnknownLinkName {
	// check parameters

	// since there is no specification for a correct link name,
	// we do just a simple check
	// is name legal?
	if (name == null || name.length() == 0)
	    throw new IllegalLinkName(name);

	// is name known?
	if (! m_current_links.containsKey(name))
	    throw new UnknownLinkName(name);

	// o.k. to remove
	m_current_links.remove(name);
	
	//set flag
	m_links_changed = true;
    }

    /**
     * Check, if any links have been added, removed or modified.<br>
     * *NOT* inherited from anywhere.
     * @return True, if anything changed
     */
    public boolean linksChanged(){
	return m_links_changed;
    }

    /**
     * Get the links of this trader.<br>
     * *NOT* inherited from anywhere.
     * @return All LinkInfo-objects of this trader
     */
    public LinkInfo[] getLinks(){
	LinkInfo[] _links_array = new LinkInfo[m_current_links.size()];
	Enumeration _links = m_current_links.elements();
	int _i = 0;

	while (_links.hasMoreElements())
	    _links_array[_i++] = (LinkInfo) _links.nextElement();

	//reset flag
	m_links_changed = false;

	return _links_array;
    }    
    
} // LinkImpl



