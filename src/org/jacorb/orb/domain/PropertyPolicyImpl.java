package org.jacorb.orb.domain;

import java.io.*;
import java.util.Properties;
import java.util.Hashtable;
import java.util.Enumeration;

import org.jacorb.util.Debug;

/**
 * This class implements a  generic property list. A Property consists
 * of a name and a value. The value of a property can be retrieved via
 * its name with the operation "getValueOfProperty".
 * 
 * A property  policy  is itself  named.  It's name  can  be set  and
 * retrived via the name getter and setter. The name attribute and the
 * attribute   "short_description"    of   the   the   super   class
 * ManagementPolicy are equivalent.
 *
 * Because  this  class acts  as  a  template  for various  types  of
 * policies,   its  policy   type  can  be   set  by   the  operation
 * "setPolicyType".
 * This class is implemented with a java.util.Property
 * @author Herbert Kiefer
 * @version $Id$ */

public class PropertyPolicyImpl 
    extends ManagementPolicyImpl
    implements PropertyPolicyOperations 
{
    /** holds all properties */
    private Properties theProperties;

    /** the policy type of this policy */
    private int theType;  

    /// constructors
    public PropertyPolicyImpl() 
    {
        this("property policy");   
    }

    public PropertyPolicyImpl(String name)
    { 
        super(name);
    
        theProperties= new Properties();
        theType= PROPERTY_POLICY_ID.value;
    }


    /// self description

    /** 
     * returns the name of this policy 
     */
    public String name() 
    { 
        return short_description();
    }

    /** 
     * sets the name of this policy 
     */
    public void name(String newName) 
    { 
        short_description(newName); 
    }

    /** 
     * sets the policy type  of this policy. This is necessary because
     * in a  domain there  are  no two  policies with  the same  type
     * allowed. 
     */
    public void setPolicyType(int type) 
    { 
        theType= type; 
    }
  
    /// inherited members
    public int policy_type()
    { 
        return theType; 
    }

    public org.omg.CORBA.Policy copy() 
    { 
        // the _this() may be dangerous if the orb is not set
        return (new PropertyPolicyImpl())._this();
    }

    /** 
     * adds  a  property. The  property  consists  of  a name  and  a
     * value.   The  name   must  be  unique   for  an   instance  of
     * PropertyPolicyImpl.  If you  want to override  the value  of an
     * already defined named property use ??? instead.
     *
     *  @param name the name of the property
     *  @param value the value of the property 
     */

    public void put(String name, String value) 
        throws org.jacorb.orb.domain.PropertyAlreadyDefined
    {
        if (! theProperties.containsKey(name) )
            theProperties.put(name, value);
        else 
            throw new org.jacorb.orb.domain.PropertyAlreadyDefined();
    } // put


    /** 
     * returns a list of valid property names. 
     */

    public String[] getPropertyNames()
    { 
        Enumeration nameEnum = theProperties.keys();
        // convert Enumeration to array
        String[] result= new String[theProperties.size()];
        int i= 0;
        while ( nameEnum.hasMoreElements() ) 
        {
            result[i]= (String) nameEnum.nextElement();
            i++;
        }
        return result;
    } // getPropertyNames


    /** 
     * returns the value of  a property. The property is identified by
     * its name.  If the property  is not defined, the empty string is
     * returned.  
     */

    public String getValueOfProperty(String name)
    {
        java.lang.Object result= theProperties.get(name);
        if (result != null)
            return (String) result;
        else return "";
    }

    /** 
     * changes the value of  a property. The property is identified by
     * its  name. If  the property does  not exist prior  to operation
     * call, the property is inserted as a new propery. 
     */
    public void changeValueOfProperty(String name, String newValue)
    {
        // theProperties.remove(name);         // remove old value
        theProperties.put(name, newValue);  // insert new value
    }


    /**
     * checks whether this instancs has a valid property named "name". 
     */
    public boolean containsProperty(String name)
    {
        return theProperties.containsKey(name);
    }


    /**
     * removes a property. The property is identified by its name. 
     */
    public void removeProperty(String name)
    {
        theProperties.remove(name);
    }

    /** 
     * returns the number of valid properties. 
     */
    public int getPropertyCount()
    {
        return theProperties.size();
    }

    /// ********* import / export ****************


    /** 
     *  load properties  from a  file to  a property  policy.  All old
     * properties are  overwritten. The name and type  of the property
     * policy are preserved.
     *  @param filename the name of the file to read from. May be 
     *         operating system dependent 
     *  @param pol the property policy where to put the results.
     *  @param prefix the prefix which is used to indicate a org.jacorb 
     *         policy. May be null which then defaults to "jacorb.policy.".
     * Note: This  operation is executed in the  process of the caller
     *  of  this operation.  It  is  *not*  delegated to  the  process
     *  implementing   the  property  policy   "pol".  Therefore  this
     * operation is static and the  file is taken from the file system
     * of the caller.
     * @see org.jacorb.orb.domain.PropertyPolicy#storeToFile 
     */

    public static void loadFromFile(PropertyPolicy pol, 
                                    File file, 
                                    String prefix)
        throws IOException
    {
        if (prefix == null) 
            prefix= "jacorb.policy.";
   
        BufferedInputStream in = 
            new BufferedInputStream(new FileInputStream(file) );
        Properties prop= new Properties();
        prop.load(in);
        in.close();
	
        // transfer data from prop into theProperties and cut 
        // prefix by the usage of  updatePropertyPolicies
        Hashtable map= new Hashtable(1);       
        map.put(pol.short_description(), pol); 
        try 
        {
            updatePropertyPolicies(prop, prefix, map, null);
        }
        catch (NullPointerException e) 
        { 
            // occurs when the file contains policy name which are not 
            // named pol.short_description(). Normally the factory 
            // creates new property  policies. Because this is not
            // what we want, a null reference was given
            // as the factory. This case we want to catch and eliminate.
            Debug.output(Debug.DOMAIN | 4, 
                         "PropertyPolicyImpl.loadFromFile: harmless exception");
        }
    } // loadFromFile
  

    /** 
     *  stores the properties of a property policy to a file.
     *  @param the property policy tor store
     *  @param file the (java.io.File) file where to store the properties 
     *  @param prefix the prefix, which gets added before the property name
     *  Note: This operation is executed in the process of the caller
     *  of this  operation. It  is  *not* delegated  to the  process
     *  implementing  the  property  policy  "pol".  Therefore  this
     *  operation is  static and  the file  is taken  from  the file
     *  system of the caller.
     * @see PropertyPolicyImpl#loadFromFile 
     */
    public static void storeToFile( PropertyPolicy pol, 
                                    File file, 
                                    String prefix)
        throws IOException
    {
        if (prefix == null) 
            prefix= "jacorb.policy.";
        String sep= ".";       // seperator
        Properties outProperties= new Properties();
        String names[]= pol.getPropertyNames();    

        // put type and description into temporary 
        // properties to prepare for write out
        outProperties.put(prefix + pol.short_description()+ sep + "type" , 
                          (new Integer(pol.policy_type())).toString() );
        outProperties.put( prefix + pol.short_description()+ sep + 
                           "description", 
                           pol.long_description() );

        for (int i= 0; i < names.length; i++)
            outProperties.put(prefix + pol.short_description() + 
                              sep + names[i], 
                              pol.getValueOfProperty(names[i]) );

        OutputStream out =
            new BufferedOutputStream(new FileOutputStream(file));
        outProperties.save(out, "properties of policy " + 
                           pol.short_description() );
        out.flush();
        out.close();
    
    } // storeToFile

    /** 
     * updates a  list of property policies.  A  property policy is an
     * instance of org.jacorb.orb.domain.PropertyPolicy. Property policies
     *  are  instantiated from  values  from  a  properties file.  The
     * contents of a property  file can create many property policies.
     * A property of a property  policy consists of a name and a value
     *  (as usual).   Any org.jacorb  property which  starts with  a given
     * prefix,  default is "jacorb.policy.", is treated  as a property
     * of  a property policy. All  properties which do  not start with
     * the  prefix are  ignored. The properties  of a  property policy
     * have the format: <p>
     *
     *  org.jacorb.policy.<policy_name>.<property_name>=<property_value> <p>
     *
     *  For example:
     *   org.jacorb.policy.Price.AppleCosts = 100
     *   org.jacorb.policy.Price.PotatoCosts= 20
     *  
     *  From the  above example  this operation  creates or  updates a
     * Property  Policy with the  name "Price" which has  two property
     *  names "AppleCosts"  and PotatoCost  with the  obvious property
     *  values.  The  hashtable  is  used to  identify  already  known
     * property policies.
     * @param source the java.lang.Properties where to read from
     * @param knownPropertyPolicies a hashtable of known property 
     *        policies. The contents of the table is of the format:
     *        property name -> property policy. That means a property
     *        policy is identified by its name and created only if its 
     *        name  isn't in the table.
     * @param factory the policy factory used to create property 
     *          policies if needed 
     */

    public static void updatePropertyPolicies(Properties source,
                                              String prefix,
                                              Hashtable knownPropertyPolicies,
                                              org.jacorb.orb.domain.PolicyFactory factory)
    {
        Enumeration prop_names = source.propertyNames();

        if ( prefix == null) 
            prefix= "jacorb.policy.";
        String seperator = ".";

        org.jacorb.orb.domain.PropertyPolicy PropPol = null; // property policy
        String PropPolName; // the name of the property policy
    
        // properties OF the property policy
        String nameOfProperty;
        String valueOfProperty;  
	
        // Test EVERY property if prefix matches.
        // I'm open to suggestions for more efficient ways 
        // (noffke, Herb too)

        while( prop_names.hasMoreElements() )
	{
	    String prop_name = (String) prop_names.nextElement();
	    if ( prop_name.startsWith(prefix) )
	    {
                // extract policy name
                int startIndex= prefix.length();
                int endIndex  = prop_name.indexOf(seperator, startIndex);
                if (endIndex < 0) 
		{
                    Debug.output(Debug.DOMAIN | 3, 
                                 "Environment.updatePropertyPolicies: policy "
                                 +" name not found ");
                    continue; // not found, skip
		}
                PropPolName = prop_name.substring(startIndex, endIndex);
                Debug.output(Debug.DOMAIN | 3, 
                             "found policy name:" + PropPolName);

                // get / create Property Policy

                Object o = knownPropertyPolicies.get(PropPolName);

                if( o != null )
                {
                    try
                    {
                        PropPol = 
                            org.jacorb.orb.domain.PropertyPolicyHelper.narrow( 
                                (org.omg.CORBA.Object)o);
                    }
                    catch( org.omg.CORBA.BAD_PARAM bp )
                    {
                        bp.printStackTrace(); // should not happen
                    }
                }
                else
                {
                    PropPol = factory.createPropertyPolicy();
                    PropPol.name(PropPolName);
                    knownPropertyPolicies.put(PropPolName, PropPol);
                    Debug.output(Debug.DOMAIN | 4, "created policy " + 
                                 PropPolName);                    
                }

                // now the property policy can be retrieved by its name
                // now look at the properties of the property policy
                nameOfProperty = 
                    prop_name.substring( endIndex+1, prop_name.length() );
                valueOfProperty= (String) source.get(prop_name);

                // do special treatment for some property names
                if ( nameOfProperty.equalsIgnoreCase("Name") )
		{
                    PropPol.name(valueOfProperty);
                    continue;
		}

                Debug.output(Debug.DOMAIN | 3, "property name: " + 
                             nameOfProperty + 
                             " property valuee: " + 
                             valueOfProperty + 
                             " property policy object : " + PropPol);

                if ( nameOfProperty.equalsIgnoreCase("Type") )
		{
                    try 
		    {
                        PropPol.setPolicyType(Integer.parseInt(valueOfProperty));
		    }
                    catch (NumberFormatException e)
		    {
                        Debug.output(Debug.DOMAIN | 1, 
                                     "Environment.updatePropertyPolicies: "
                                     +"couldn't parse value to integer, skipping.");
		    }
                    continue;
		}

                if ( nameOfProperty.equalsIgnoreCase("description") )
		{
                    PropPol.long_description(valueOfProperty);
                    continue;
		}
	      
                // normal property
                try 
		{
                    PropPol.put(nameOfProperty, valueOfProperty);
		}
                catch (org.jacorb.orb.domain.PropertyAlreadyDefined already)
		{ 
                    // overwrite value
                    PropPol.removeProperty(nameOfProperty);
		   
                    try { PropPol.put(nameOfProperty, valueOfProperty);}
                    catch (org.jacorb.orb.domain.PropertyAlreadyDefined never) {
                        Debug.output(Debug.DOMAIN | 1, 
                                     "Environment.updatePropertyPolicies:"
                                     +" impossible ERROR occured !!!");
                    }
		}

                Debug.output(Debug.DOMAIN | 4,
                             "added ( " + nameOfProperty+", "+
                             valueOfProperty + ")");
	
	    }
	} // while
	
	
    } // updatePropertyPolicies

} // PropertyPolicyImpl

