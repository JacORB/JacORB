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

import org.jacorb.collection.util.ObjectInvalid;
import org.omg.CORBA.Any;
import org.omg.CosCollection.Comparator;
import org.omg.CosCollection.ElementInvalid;
import org.omg.CosCollection.ElementInvalidReason;
import org.omg.CosCollection.EmptyCollection;
import org.omg.CosCollection.Iterator;
import org.omg.CosCollection.IteratorInvalid;
import org.omg.CosCollection.OperationsOperations;
import org.omg.CosCollection.PositionInvalid;
import org.omg.CosCollection.SequentialCollectionOperations;
import org.omg.PortableServer.POA;

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






