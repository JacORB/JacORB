package org.jacorb.collection;

import org.omg.CosCollection.*;
import org.jacorb.collection.util.*;
import java.util.*;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;
import org.omg.CORBA.Any;
import org.omg.CORBA.AnyHolder;
import org.omg.CORBA.BooleanHolder;

class SequentialIteratorImpl extends OrderedIteratorImpl 
                             implements SequentialIteratorOperations {

/* ========================================================================= */
    SequentialIteratorImpl( SequentialCollectionImpl collection ){
        super( collection );
    }
/* ------------------------------------------------------------------------- */
    SequentialIteratorImpl( SequentialCollectionImpl collection, boolean read_only ){
        super( collection, read_only );
    }
/* ------------------------------------------------------------------------- */
    SequentialIteratorImpl( SequentialCollectionImpl collection, boolean read_only, boolean reverse ){
        super( collection, read_only, reverse );
    }
/* ========================================================================= */
    public boolean add_element_as_next_set_iterator( Any element ) throws IteratorInvalid,ElementInvalid {
        synchronized( collection ){
            check_invalid();
            try {
                ((SequentialCollectionImpl)collection).add_element_at_position( get_pos()+1, element );
                set_to_position( get_pos()+1 );
            } catch ( PositionInvalid e ){
                invalidate();
                throw new IteratorInvalid( IteratorInvalidReason.is_invalid );
            }
            return true;
        }
    }
/* ------------------------------------------------------------------------- */
    public void add_n_elements_as_next_set_iterator( Any[] elements ) throws IteratorInvalid,ElementInvalid {
        throw new org.omg.CORBA.NO_IMPLEMENT();
//        synchronized( collection ){
//        }
    }
/* ------------------------------------------------------------------------- */
    public boolean add_element_as_previous_set_iterator( Any element ) throws IteratorInvalid, ElementInvalid {
        synchronized( collection ){
            check_invalid();
            try {
                collection.element_replace( get_pos()-1, element );
                set_to_position( get_pos()-1 );
            } catch ( PositionInvalid e ){
                invalidate();
               throw new IteratorInvalid( IteratorInvalidReason.is_invalid );
            }
            return true;
        }
    }
/* ------------------------------------------------------------------------- */
    public void add_n_elements_as_previous_set_iterator( Any[] elements ) throws IteratorInvalid, ElementInvalid {
        throw new org.omg.CORBA.NO_IMPLEMENT();
//        synchronized( collection ){
//        }
    }

}









