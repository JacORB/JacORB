/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Library General Public
 *   License as published by the Free Software Foundation; either
 *   version 2 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this library; if not, write to the Free
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */
package org.jacorb.collection;

import org.jacorb.util.ObjectUtil;
import org.omg.CosCollection.Collection;
import org.omg.CosCollection.NVPair;
import org.omg.CosCollection.Operations;
import org.omg.CosCollection.OperationsOperations;
import org.omg.CosCollection.ParameterInvalid;
import org.omg.CosCollection.SortedRelation;
import org.omg.CosCollection.SortedRelationFactoryPOA;
import org.omg.CosCollection.SortedRelationHelper;
import org.omg.CosCollection.SortedRelationPOATie;

public class  SortedRelationFactoryImpl 
    extends SortedRelationFactoryPOA implements IteratorFactory 
{

    public final static String IMPL_CATEGORY = "ArrayBased";
    private org.omg.PortableServer.POA poa;

    public SortedRelationFactoryImpl( org.omg.PortableServer.POA poa )
    {
        this.poa = poa;
        try {
            poa.servant_to_reference(this);
        } catch( Exception e ){
            System.out.println( "Internal error: Can not activate factory" );
            e.printStackTrace();
            throw new org.omg.CORBA.INTERNAL();
        }
    };

    public SortedRelation create( Operations ops, int expected_size ){
        return create( ops, expected_size, poa );
    };

    public SortedRelation create( String ops_class, int expected_size )
    {
        OperationsOperations ops = null;
        try {
            Class operation_class = ObjectUtil.classForName( ops_class );
            ops = (OperationsOperations)operation_class.newInstance();
        } catch ( Exception e ){
            System.out.println( "Internal error: Can not instantiate object of class \""+ops_class+"\"" );
            throw new org.omg.CORBA.INTERNAL();
        };
        return create( ops, expected_size, poa );
    };
    public SortedRelation create( OperationsOperations ops, int expected_size, org.omg.PortableServer.POA poa ){
        SortedRelationImpl collection = new SortedRelationImpl( ops, poa, this, expected_size );
        SortedRelation collection_ref = null;
        SortedRelationPOATie srvnt = new SortedRelationPOATie( collection );
        try {
            collection_ref = SortedRelationHelper.narrow(poa.servant_to_reference(srvnt));
            collection.set_servant( srvnt );
        } catch(Exception e) {
            System.out.println("Internal error: Can not Activate collection");
            e.printStackTrace();
            throw new org.omg.CORBA.INTERNAL();
        }
        return collection_ref;
    };
    public Collection generic_create( NVPair[] parameters) throws ParameterInvalid{
        NVPairManager pm = new NVPairManager( parameters );
        String collection_interface     = pm.find_string_param( CollectionService.COL_INTRF );
        String implementation_interface = pm.find_string_param( CollectionService.IMPL_INTRF );
        String implementation_category  = pm.find_string_param( CollectionService.IMPL_CAT );
        if( implementation_category != null && !implementation_category.equals( IMPL_CATEGORY ) ) {
            throw new ParameterInvalid( pm.find_param_idx( CollectionService.IMPL_CAT ), "CollectionFactory : not support implementation category "+implementation_category );
        }
        Integer   size = pm.find_ulong_param( CollectionService.EXP_SIZE );
        if ( size == null ) {
            size = new Integer(10);
        }
        Operations ops = pm.find_operations_param( CollectionService.OPERATIONS );
        if ( ops == null ) {
            String ops_class = pm.find_string_param( CollectionService.OPERATIONS_CLASS );
            if( ops_class == null ){
                throw new ParameterInvalid( pm.find_param_idx(CollectionService.OPERATIONS), "CollectionFactory: OPERATION object not defined" );
            }
            return create( ops_class, size.intValue() );
        } else {
            return create( ops, size.intValue() );
        }
    };
    public PositionalIteratorImpl create_iterator( CollectionImpl collection, boolean read_only ){
        return create_iterator( collection, read_only, false );
    };
    public PositionalIteratorImpl create_iterator( CollectionImpl collection, boolean read_only, boolean reverse ){
        return new EqualityKeySortedIteratorImpl( (SortedRelationImpl)collection, read_only, reverse );
    };
}






