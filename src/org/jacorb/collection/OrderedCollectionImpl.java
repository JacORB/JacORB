package org.jacorb.collection;

import org.omg.CosCollection.*;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;
import org.omg.CORBA.Any;
import org.omg.CORBA.AnyHolder;

class OrderedCollectionImpl extends CollectionImpl 
      implements OrderedCollectionOperations {
/* ========================================================================= */
    OrderedCollectionImpl( OperationsOperations ops, POA poa, IteratorFactory iterator_factory ){
        super( ops, poa, iterator_factory );
    }
/* ========================================================================= */
    public synchronized void remove_element_at_position(int position) throws PositionInvalid {
        try {
            element_remove( position );
        } catch ( EmptyCollection e ){
            throw new PositionInvalid();
        }
    }
/* ------------------------------------------------------------------------- */
    public synchronized void remove_first_element() throws EmptyCollection {
        try { 
            remove_element_at_position(0);
        } catch ( PositionInvalid e ){
            throw new EmptyCollection();
        }
    }
/* ------------------------------------------------------------------------- */
    public synchronized void remove_last_element() throws EmptyCollection {
        int pos = data.size()-1;
        try { 
            remove_element_at_position(pos);
        } catch ( PositionInvalid e ){
            throw new EmptyCollection();
        }
    }
/* ------------------------------------------------------------------------- */
    public synchronized boolean retrieve_element_at_position(int position, org.omg.CORBA.AnyHolder element) throws PositionInvalid {
        element.value = element_retrieve( position );
        return true;
    }
/* ------------------------------------------------------------------------- */
    public synchronized boolean retrieve_first_element(org.omg.CORBA.AnyHolder element) throws EmptyCollection {
        try {
            return retrieve_element_at_position( 0, element );
        } catch ( PositionInvalid e ){
            throw new EmptyCollection();
        }
    }
/* ------------------------------------------------------------------------- */
    public synchronized boolean retrieve_last_element(org.omg.CORBA.AnyHolder element) throws EmptyCollection {
        int pos = data.size()-1;
        try {
            return retrieve_element_at_position( pos, element );
        } catch ( PositionInvalid e ){
            throw new EmptyCollection();
        }
    }
/* ------------------------------------------------------------------------- */
    public synchronized OrderedIterator create_ordered_iterator(boolean read_only, boolean reverse_iteration) {
        PositionalIteratorImpl iter = iterator_factory.create_iterator( this, read_only, reverse_iteration );
        IteratorPOATie servant = new IteratorPOATie( iter );
        try {
            OrderedIterator i = OrderedIteratorHelper.narrow( poa.servant_to_reference( servant ));
            iter.set_servant( servant );
            return i;
        } catch ( Exception e ){
            e.printStackTrace( System.out );
            throw new org.omg.CORBA.INTERNAL();
        }
    }
/* ========================================================================= */
/* Overrided                                                                 */
/* ========================================================================= */
    public synchronized Iterator create_iterator(boolean read_only) {
        return create_ordered_iterator( read_only, false );
    }

}









