package org.jacorb.collection;

import org.omg.CosCollection.*;
import org.jacorb.collection.util.*;
import java.util.*;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;
import org.omg.CORBA.Any;
import org.omg.CORBA.AnyHolder;

public class SortedRelationImpl 
    extends EqualityKeySortedCollectionImpl 
    implements SortedRelationOperations 
{

/* ========================================================================= */
    public SortedRelationImpl( OperationsOperations ops, POA poa, 
			       IteratorFactory iterator_factory, int expected_size )
    {
        super( ops, poa, iterator_factory );
        data = new SortedVector( new SortedRelationComparator(ops), expected_size );
        keys = new SortedVector( new KeyComparator(ops) );
    };

/* ========================================================================= */
    public synchronized int compare(SortedRelation collector, 
				    org.omg.CosCollection.Comparator comparison)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    };

/* ========================================================================= */
};






