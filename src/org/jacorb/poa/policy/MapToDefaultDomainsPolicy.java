package org.jacorb.poa.policy;

import org.jacorb.orb.domain.*;
import java.util.StringTokenizer;
import org.jacorb.util.Debug;

/**
 * MapToDefaultDomainsPolicy.java
 * implements the IDL-interface InitialMapPolicy. An instance of 
 * this class maps a newly
 * created object reference to a set of default domains.
 * One special default domain is set and read via the
 * operations "setDefaultDomain" and  "getDefaultDomain".
 *
 * Created: Thu Apr 20 12:06:43 2000
 *
 * @author Herbert Kiefer
 * @version $Revision$
 */

public class MapToDefaultDomainsPolicy
    extends org.jacorb.orb.LocalityConstrainedObject
    implements org.jacorb.orb.domain.InitialMapToDefaultDomainPolicy
{
  
    /** the default domains which are always returned 
        from the function "OnReferenceCreation" */
    private Domain _default_domains[];

    /** an array of the pathname of the default domains */
    private String _default_pathnames[];

    /** the string containing all default domains, 
        the domain names are separated by whitespaces */
    private String _pathnames;

    public MapToDefaultDomainsPolicy( String pathnames )
    { 
        _pathnames = pathnames;
        _default_pathnames = createStringArrayFromString(pathnames);      
        Debug.output(Debug.DOMAIN | Debug.INFORMATION, 
                     "MapToDefaultDomainsPolicy: pathnames "
                     + pathnames );
    } // MapToDefaultDomainsPolicy

    public Domain getDefaultDomain()                        
    {
        Debug.myAssert(1, _default_domains != null, 
                     "default domains are invalid (== null)");
        return   _default_domains[0]; 
    }

    public void setDefaultDomain(Domain newDefaultDomain) 
    { 
        _default_domains= new Domain[1];
        _default_domains[0]= newDefaultDomain; 
    }

    /** maps a newly created object reference to 
       the default domain. */

    public Domain[] OnReferenceCreation( org.omg.CORBA.Object newReference, 
                                         Domain startDomain)
    {
        // and here comes the on-demand conversion from 
        // the pathnames to the domain references
        if (_default_domains == null) 
        {
            _default_domains = resolveAllDomains( _default_pathnames , 
						 startDomain);
            if (_default_domains.length == 0)
            {
                Debug.output(Debug.DOMAIN | Debug.INFORMATION, 
                             "MapToDefaultDomainsPolicy: no default domains provided"
                             +", using supplied root domain");
                Domain result[]= new Domain[1];	
                result[0]= startDomain;
                return result;
            }
        } // if

        return _default_domains;
    }

    /**
     * breaks a string into an array of strings. The 
     * strings in the array were separated by
     *  colons in the original string.
     */

    private String[] createStringArrayFromString(String list)
    {
        if (list == null)
            return new String[0];

        StringTokenizer tokenizer = new StringTokenizer(list, ",");
        String result[] = new String[ tokenizer.countTokens() ];

        int i = 0;
        while( tokenizer.hasMoreTokens() )
        {
            result[i]= tokenizer.nextToken();
            i++;
        }
        return result;
    } // createStringArrayFromString

    /** 
     *  resolves an array of pathnames to an array of domains. 
     *  The array may shrink in size
     *  because of invalid pathnames. 
     */

    private Domain[] resolveAllDomains( String pathnames [], 
                                        Domain nameResolver)
    {
        int failures= 0;
        Domain mayBeResult[] = new Domain[ pathnames.length ];
        for (int i = 0; i < pathnames.length ; i++)
        {
            try 
            {
                Debug.output(Debug.DOMAIN | Debug.INFORMATION, 
                             "MapToDefaultDomainsPolicy.resolveAllDomains, "
                             + "trying to resolve " + pathnames[i] +
                             " at domain " + nameResolver.name());

                mayBeResult[i] = 
                    nameResolver.resolveDomainPathName( pathnames[i] );
            }
            catch (org.jacorb.orb.domain.InvalidName inv)
            {
                failures++;
                Debug.output(Debug.DOMAIN | Debug.INFORMATION, 
                             "MapToDefaultDomainsPolicy.resolveAllDomains, "
                             + "resolving " + pathnames[i] +
                             " at domain " + nameResolver.name() + " failed!");
            }
        }

        if ( failures == 0) 
            return mayBeResult;
        else
        {
            Domain result[]= new Domain[pathnames.length - failures];
            int j = 0;
            for (int i= 0; i < pathnames.length ; i++)
                if (mayBeResult[i] != null)
                {
                    result[j]= mayBeResult[i];
                    j++;
                }            
            return result;
        }
    } // resolveAllDomains


    // inherited member functions

    public java.lang.String short_description() 
    { 
        return ""; 
    }

    public void short_description(java.lang.String arg)
    // { _short_description= arg; }
    {}

  
    public java.lang.String long_description()  
    // { return _long_description; }
    { return ""; }

    public void long_description(java.lang.String arg)
    //  { _long_description= arg; }
    {}

    public short strategy()
    { return org.jacorb.orb.domain.InitialMapPolicy.DEFAULT_DOMAIN; } 

    public int policy_type()
    { return org.jacorb.orb.domain.INITIAL_MAP_POLICY_ID.value; }

    public org.omg.CORBA.Policy copy() 
    { 
        return new MapToDefaultDomainsPolicy(_pathnames);
        // return null; //new MapToDefaultDomainsPolicy(_pathnames);
    }
  
    public void destroy() 
    {
        _default_domains  = null;
        _default_pathnames= null;
        _pathnames        = null;
    }

} // MapToDefaultDomainsPolicy







