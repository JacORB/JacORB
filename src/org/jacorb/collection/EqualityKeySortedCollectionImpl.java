package org.jacorb.collection;

import org.omg.CosCollection.*;
import org.jacorb.collection.util.*;
import java.util.*;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;
import org.omg.CORBA.Any;
import org.omg.CORBA.AnyHolder;

class EqualityKeySortedCollectionImpl 
    extends KeySortedCollectionImpl 
    implements EqualityKeySortedCollectionOperations 
{

    /* ========================================================================= */
    EqualityKeySortedCollectionImpl( OperationsOperations ops, 
				     POA poa, 
				     IteratorFactory iterator_factory )
    {
        super( ops, poa, iterator_factory );
    }

    /* ========================================================================= */
    public synchronized boolean contains_element(Any element) 
	throws ElementInvalid
    {
        check_element( element );
        try 
        { 
            return data.indexOf( element ) >= 0;
        } 
        catch ( ObjectInvalid e )
        {
            throw new ElementInvalid( ElementInvalidReason.element_type_invalid );
        }
    }

    /* ------------------------------------------------------------------------- */

    public synchronized boolean contains_all_from(org.omg.CosCollection.Collection collector) 
	throws ElementInvalid
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /* ------------------------------------------------------------------------- */
    public synchronized boolean locate_or_add_element(Any element) throws ElementInvalid{
        check_element( element );
        try {
            if( data.indexOf( element ) < 0 ){
                element_add( element );
                return false;
            }
            return true;
        } catch ( ObjectInvalid e ){
            throw new ElementInvalid( ElementInvalidReason.element_type_invalid );
        }
    }
    /* ------------------------------------------------------------------------- */
    public synchronized boolean locate_or_add_element_set_iterator(Any element, org.omg.CosCollection.Iterator where) throws ElementInvalid,IteratorInvalid{
        check_element( element );
        PositionalIteratorImpl i = check_iterator( where );
        try {
            int pos = data.indexOf( element );
            if( pos < 0 ){
                pos = element_add( element );
                i.set_pos( pos );
                i.set_in_between( false );
                return false;
            } else { 
                i.set_pos( pos );
                i.set_in_between( false );
                return true;
            }
        } catch ( ObjectInvalid e ){
            throw new ElementInvalid( ElementInvalidReason.element_type_invalid );
        }
    }
    /* ------------------------------------------------------------------------- */
    public synchronized boolean locate_element(Any element, org.omg.CosCollection.Iterator where) throws ElementInvalid,IteratorInvalid{
        check_element( element );
        PositionalIteratorImpl i = check_iterator( where );
        try {
            int pos = data.indexOf( element );
            if( pos >= 0 ){ 
                i.set_pos( pos );
                i.set_in_between( false );
                return true;
            } else {
                i.invalidate();
                return false;
            }
        } catch ( ObjectInvalid e ){
            throw new ElementInvalid( ElementInvalidReason.element_type_invalid );
        }
    }
    /* ------------------------------------------------------------------------- */
    public synchronized boolean locate_next_element(Any element, org.omg.CosCollection.Iterator where) throws ElementInvalid,IteratorInvalid{
        check_element( element );
        PositionalIteratorImpl i = check_iterator( where );
        i.check_invalid();
        try {
            int pos = data.indexOf( element );
            if( pos >= 0 ){ 
                int new_pos = i.is_in_between()?pos:pos+1; 
                while( new_pos < data.size() && ops.compare( element, (Any)data.elementAt(new_pos) ) == 0 ){
                    if( ops.equal( element, (Any)data.elementAt( new_pos ) ) ){
                        i.set_pos( new_pos );
                        i.set_in_between( false );
                        return true;
                    }
                    new_pos++;
                }
            } 
            i.invalidate();
            return false;
        } catch ( ObjectInvalid e ){
            throw new ElementInvalid( ElementInvalidReason.element_type_invalid );
        }
    }
    /* ------------------------------------------------------------------------- */
    public synchronized boolean locate_next_different_element(org.omg.CosCollection.Iterator where) 
        throws IteratorInvalid,IteratorInBetween
    {
        PositionalIteratorImpl i = check_iterator( where );
        i.check_iterator();
        int pos = i.get_pos();
        Any element = (Any)data.elementAt( pos );
        if( pos >= 0 ){ 
            int new_pos = pos+1; 
            while( new_pos < data.size() ){
                if( ops.compare( element, (Any)data.elementAt( new_pos ) ) != 0 
                    || !ops.equal( element, (Any)data.elementAt( new_pos ) ) ){
                    i.set_pos( new_pos );
                    i.set_in_between( false );
                    return true;
                }
                new_pos++;
            }
        } 
        i.invalidate();
        return false;
    }
    /* ------------------------------------------------------------------------- */
    public synchronized boolean remove_element(Any element) 
        throws ElementInvalid
    {
        check_element( element );
        try {
            int pos = data.indexOf( element );
            if( pos >= 0 ){
                element_remove( 0 );
                return true;
            }
            return false;
        } catch ( ObjectInvalid e ){
            throw new ElementInvalid( ElementInvalidReason.element_type_invalid );
        } catch ( Exception e ){
            e.printStackTrace( System.out );
            throw new org.omg.CORBA.INTERNAL();
        }
    }

    /* ------------------------------------------------------------------------- */
    public synchronized int remove_all_occurrences(Any element) throws ElementInvalid{
        check_element( element );
        try {
            int pos = data.indexOf( element );
            int count = 0;
            while( pos < data.size() && ops.equal( element, (Any)data.elementAt( pos ) ) ){
                element_remove( pos );
                count++;
            }
            return count;
        } catch ( ObjectInvalid e ){
            throw new ElementInvalid( ElementInvalidReason.element_type_invalid );
        } catch ( Exception e ){
            e.printStackTrace( System.out );
            throw new org.omg.CORBA.INTERNAL();
        }
    }

    /* ------------------------------------------------------------------------ */
    public synchronized int number_of_different_elements(){
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /* ------------------------------------------------------------------------- */
    public synchronized int number_of_occurrences(Any element) throws ElementInvalid{
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /* ========================================================================= */
    /* Overrided                                                                 */
    /* ========================================================================= */

    public synchronized org.omg.CosCollection.Iterator create_iterator(boolean read_only) 
    {
        return create_ordered_iterator( read_only, false );
    }


}

