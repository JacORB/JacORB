package org.jacorb.collection;

import org.omg.CosCollection.*;
import org.jacorb.collection.util.*;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;
import org.omg.CORBA.Any;
import org.omg.CORBA.AnyHolder;

class SequenceImpl extends SequentialCollectionImpl 
                   implements CSequenceOperations {
/* ========================================================================= */
    SequenceImpl( OperationsOperations ops, POA poa, IteratorFactory iterator_factory, int expected_size ){
        super( ops, poa, iterator_factory );
        data = new SortedVector( new SortedRelationComparator(ops), expected_size );
    };
/* ========================================================================= */
    public int compare( CSequence collector, Comparator comparison ) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    };
};
