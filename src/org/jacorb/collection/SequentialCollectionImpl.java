package org.jacorb.collection;

import org.omg.CosCollection.*;
import org.jacorb.collection.util.*;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;
import org.omg.CORBA.Any;
import org.omg.CORBA.AnyHolder;

class SequentialCollectionImpl 
    extends OrderedCollectionImpl 
    implements SequentialCollectionOperations 
{
    /* ========================================================================= */
    SequentialCollectionImpl( OperationsOperations ops, POA poa, IteratorFactory iterator_factory ){
        super( ops, poa, iterator_factory );
    }
    /* ========================================================================= */
    public synchronized  void add_element_as_first(Any element) throws ElementInvalid{
        try {
            add_element_at_position( 0, element );
        } catch ( PositionInvalid e ){
            throw new ElementInvalid( ElementInvalidReason.positioning_property_invalid );
        }
    }
    /* ------------------------------------------------------------------------- */
    public synchronized  void add_element_as_first_set_iterator(Any element, Iterator where) throws ElementInvalid,IteratorInvalid{
        PositionalIteratorImpl i = check_iterator( where );
        add_element_as_first( element );
        i.set_pos(0);
        i.set_in_between(false);
    }
    /* ------------------------------------------------------------------------- */
    public synchronized  void add_element_as_last(Any element) throws ElementInvalid{
        int pos = data.size();
        try {
            add_element_at_position(pos, element );
        } catch ( PositionInvalid e ){
            throw new ElementInvalid( ElementInvalidReason.positioning_property_invalid );
        }
    }
    /* ------------------------------------------------------------------------- */
    public synchronized  void add_element_as_last_set_iterator(Any element, Iterator where) throws ElementInvalid,IteratorInvalid{
        PositionalIteratorImpl i = check_iterator( where );
        add_element_as_last( element );
        i.set_pos(data.size()-1);
        i.set_in_between(false);
    }
    /* ------------------------------------------------------------------------- */
    public synchronized  void add_element_as_next(Any element, Iterator where) throws ElementInvalid,IteratorInvalid{
        PositionalIteratorImpl i = check_iterator( where );
        int pos = i.get_pos();
        try {
            add_element_at_position( pos+1, element );
        } catch ( PositionInvalid e ){
            throw new ElementInvalid( ElementInvalidReason.positioning_property_invalid );
        }
    }
    /* ------------------------------------------------------------------------- */
    public synchronized  void add_element_as_previous(Any element, Iterator where) throws ElementInvalid,IteratorInvalid{
        PositionalIteratorImpl i = check_iterator( where );
        int pos = i.get_pos();
        try {
            add_element_at_position( pos, element );
        } catch ( PositionInvalid e ){
            throw new ElementInvalid( ElementInvalidReason.positioning_property_invalid );
        }
    }
    /* ------------------------------------------------------------------------- */
    public synchronized  void add_element_at_position(int position, Any element) throws PositionInvalid,ElementInvalid{
        check_element(element);
        try {
            if( data.insertElementAt( element, position ) ){
                element_inserted( position );
            } else {
                throw new ElementInvalid( ElementInvalidReason.positioning_property_invalid );
            }
        } catch ( ObjectInvalid e ){
            throw new ElementInvalid( ElementInvalidReason.element_type_invalid );
        }
    }
    /* ------------------------------------------------------------------------- */
    public synchronized  void add_element_at_position_set_iterator(int position, Any element, Iterator where) throws PositionInvalid,ElementInvalid,IteratorInvalid{
        PositionalIteratorImpl i = check_iterator( where );
        add_element_at_position( position, element );
        i.set_pos(position);
        i.set_in_between(false);
    }
    /* ------------------------------------------------------------------------- */
    public synchronized  void replace_element_at_position( int position, Any element ) throws PositionInvalid,ElementInvalid{
        element_replace( position, element );
    }
    /* ------------------------------------------------------------------------- */
    public synchronized  void replace_first_element(Any element) throws ElementInvalid,EmptyCollection{
        try {
            replace_element_at_position( 0, element );
        } catch ( PositionInvalid e ){
            throw new ElementInvalid( ElementInvalidReason.positioning_property_invalid );
        }
    }
    /* ------------------------------------------------------------------------- */
    public synchronized  void replace_last_element(Any element) throws ElementInvalid,EmptyCollection {
        if( data.size() == 0 ){
            throw new EmptyCollection();
        }
        try {
            replace_element_at_position( data.size()-1, element );
        } catch ( PositionInvalid e ){
            throw new ElementInvalid( ElementInvalidReason.positioning_property_invalid );
        }
    }
    /* ------------------------------------------------------------------------- */
    public synchronized  void sort(Comparator comparison){
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }
    /* ------------------------------------------------------------------------- */
    public synchronized  void reverse(){
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }


}






