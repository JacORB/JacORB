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

import org.omg.CORBA.Any;
import org.omg.CosCollection.ElementInvalid;
import org.omg.CosCollection.IteratorInvalid;
import org.omg.CosCollection.IteratorInvalidReason;
import org.omg.CosCollection.PositionInvalid;
import org.omg.CosCollection.SequentialIteratorOperations;

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









