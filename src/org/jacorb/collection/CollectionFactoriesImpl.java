package org.jacorb.collection;

import org.omg.CosCollection.*;
import org.omg.CORBA.*;
import java.util.*;

public class CollectionFactoriesImpl extends CollectionFactoriesPOA 
{
    private Hashtable factories = new Hashtable();

    public boolean add_factory(java.lang.String collection_interface, 
			       java.lang.String impl_category, 
			       java.lang.String impl_interface, 
			       CollectionFactory a_factory)
    {
        Hashtable impl_intrf;
        Hashtable impl_cat;
        if ( ! factories.containsKey( collection_interface ) ) 
	{
            factories.put( collection_interface, new Hashtable() );
        }
        impl_intrf = (Hashtable)factories.get( collection_interface );
        if( ! impl_intrf.containsKey( impl_interface ) ){
            impl_intrf.put( impl_interface, new Hashtable() );
        }
        impl_cat = (Hashtable) impl_intrf.get( impl_interface );
        if ( ! impl_cat.containsKey( impl_category ) ) {
            impl_cat.put( impl_category, a_factory );
            return true;
        }
        return false;
    };

    public boolean remove_factory(java.lang.String collection_interface, 
				  java.lang.String impl_category, 
				 java.lang.String impl_interface)
    {
        Hashtable impl_intrf;
        Hashtable impl_cat;
        if ( ! factories.containsKey( collection_interface ) ) {
            return false;
        }
        impl_intrf = (Hashtable)factories.get( collection_interface );
        if( ! impl_intrf.containsKey( impl_interface ) ){
            return false;
        }
        impl_cat = (Hashtable) impl_intrf.get( impl_interface );
        if ( impl_cat.remove(impl_category) != null ) {
            return true;
        }

        return true;
    };


    public org.omg.CosCollection.Collection create( NVPair[] parameters) 
	throws ParameterInvalid
    {
        NVPairManager pm = new NVPairManager( parameters );
        CollectionFactory factory = null;
        String collection_interface = pm.find_string_param( CollectionService.COL_INTRF );
        if ( collection_interface == null ) {
            throw new ParameterInvalid( -1, "Absent mondatory <collection_interface> parameter" );
        }
        Hashtable impl_intrf  = (Hashtable)factories.get( collection_interface );

        String impl_interface = pm.find_string_param( CollectionService.IMPL_INTRF );
        String impl_category  = pm.find_string_param( CollectionService.IMPL_CAT );

        Hashtable impl_cat;

        if ( impl_interface != null ) {
            impl_cat = (Hashtable)impl_intrf.get( impl_interface );
            if( impl_cat == null ) {
                throw new ParameterInvalid( pm.find_param_idx( CollectionService.COL_INTRF ), "Collection :"+impl_interface+" is not registered" );
            }
            if ( impl_category != null ) {
                factory = (CollectionFactory)impl_cat.get( impl_category );
                if( factory == null ){
                    throw new ParameterInvalid( pm.find_param_idx( CollectionService.IMPL_CAT ), "Collection :"+impl_category+" is not registered" );
                }
            } else {
                Enumeration enum = impl_cat.elements();
                if ( enum.hasMoreElements() ) {
                    factory = (CollectionFactory)enum.nextElement();
                } else {
                    throw new ParameterInvalid( pm.find_param_idx( CollectionService.IMPL_INTRF ), "Collection :"+impl_interface+" is not registered" );
                }
            }
        } else if ( impl_category != null ) {
            Enumeration enum = impl_intrf.elements();
            while ( factory == null && enum.hasMoreElements() ) {
                impl_cat = (Hashtable)enum.nextElement();
                factory = (CollectionFactory)impl_cat.get( impl_category );
            }
            if( factory == null ){
                throw new ParameterInvalid( pm.find_param_idx( CollectionService.IMPL_CAT ), "Collection :"+impl_category+" is not registered" );
            }
        } else {
            throw new org.omg.CORBA.NO_IMPLEMENT();
        }
        return factory.generic_create( parameters );
    };

    public org.omg.CosCollection.Collection generic_create( NVPair[] parameters ) 
	throws ParameterInvalid
    {
        return create( parameters );
    };
};
