package org.jacorb.collection;

import org.omg.CosCollection.*;
import org.jacorb.collection.util.*;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;
import org.omg.CORBA.Any;
import org.omg.CORBA.AnyHolder;

class EqualityKeySortedIteratorImpl extends KeySortedIteratorImpl 
                                    implements EqualityKeySortedIteratorOperations {
/* ========================================================================= */
    EqualityKeySortedIteratorImpl( KeySortedCollectionImpl collection ){
        super( collection );
    };
/* ------------------------------------------------------------------------- */
    EqualityKeySortedIteratorImpl( KeySortedCollectionImpl collection, boolean read_only ){
        super( collection, read_only );
    };
/* ------------------------------------------------------------------------- */
    EqualityKeySortedIteratorImpl( KeySortedCollectionImpl collection, boolean read_only, boolean reverse ){
        super( collection, read_only, reverse );
    };
/* ========================================================================= */
    public boolean set_to_element_with_value( Any element ) throws ElementInvalid {
        synchronized( collection ){
            collection.check_element( element );
            try {
                int pos = collection.data.indexOf( element );
                if( pos >= 0 ){
                    set_pos( pos );
                    set_in_between( false );
                    return true;
                }
                invalidate();
                return false;
            } catch ( ObjectInvalid e ) {
                e.printStackTrace( System.out );
                throw new org.omg.CORBA.INTERNAL();
            }
        }
    };
/* ------------------------------------------------------------------------- */
    public boolean set_to_next_element_with_value( Any element ) throws IteratorInvalid, ElementInvalid {
        synchronized( collection ){
            check_invalid();
            collection.check_element( element );
            try {
                int pos = collection.data.indexOf( element );
                int start_pos = is_in_between()?get_pos():get_pos()+1;
                if( pos >= 0 && start_pos < collection.data.size()-1 ){
                    if( start_pos > pos && !collection.ops.equal( element, (Any)collection.data.elementAt(start_pos) ) ){
                        invalidate();
                        return false;
                    } 
                    set_pos( start_pos );
                    set_in_between( false );
                    return true;
                }
                invalidate();
                return false;
            } catch ( ObjectInvalid e ) {
                e.printStackTrace( System.out );
                throw new org.omg.CORBA.INTERNAL();
            }
        }
    };
/* ------------------------------------------------------------------------- */
    public boolean set_to_next_element_with_different_value() throws IteratorInBetween, IteratorInvalid {
        synchronized( collection ){
            check_iterator();
            Any element = (Any)collection.data.elementAt( get_pos() );
            int pos = get_pos()+1;
            while( pos < collection.data.size() && collection.ops.equal( element, (Any)collection.data.elementAt( pos ) ) ){
                pos++;
            }
            if( pos >= collection.data.size() ) {
                invalidate();
                return false;
            } else {
                set_pos( pos );
                return true;
            }
        }
    };
/* ------------------------------------------------------------------------- */
    public boolean set_to_first_element_with_value( Any element, LowerBoundStyle style) throws ElementInvalid {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    };
/* ------------------------------------------------------------------------- */
    public boolean set_to_last_element_with_value( Any element, UpperBoundStyle style) throws ElementInvalid {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    };
/* ------------------------------------------------------------------------- */
    public boolean set_to_previous_element_with_value( Any element ) throws IteratorInvalid, ElementInvalid {
        synchronized( collection ){
            check_invalid();
            collection.check_element( element );
            try {
                int pos = collection.data.indexOf( element );
                int start_pos = get_pos()-1;
                if( pos >= 0 && start_pos < collection.data.size() ){
                    if( start_pos < pos && !collection.ops.equal( element, (Any)collection.data.elementAt(start_pos) ) ){
                        invalidate();
                        return false;
                    } 
                    set_pos( start_pos );
                    set_in_between( false );
                    return true;
                }
                invalidate();
                return false;
            } catch ( ObjectInvalid e ) {
                e.printStackTrace( System.out );
                throw new org.omg.CORBA.INTERNAL();
            }
        }
    };
/* ------------------------------------------------------------------------- */
    public boolean set_to_previous_element_with_different_value() throws IteratorInBetween, IteratorInvalid {
        synchronized( collection ){
            check_iterator();
            Any element = (Any)collection.data.elementAt( get_pos() );
            int pos = get_pos()-1;
            while( pos >= 0 && collection.ops.equal( element, (Any)collection.data.elementAt( pos ) ) ){
                pos--;
            }
            if( pos < 0 ) {
                invalidate();
                return false;
            } else {
                set_pos( pos );
                return true;
            }
        }
    }
}







