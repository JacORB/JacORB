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

import org.omg.CosCollection.EmptyCollection;
import org.omg.CosCollection.Iterator;
import org.omg.CosCollection.IteratorPOATie;
import org.omg.CosCollection.OperationsOperations;
import org.omg.CosCollection.OrderedCollectionOperations;
import org.omg.CosCollection.OrderedIterator;
import org.omg.CosCollection.OrderedIteratorHelper;
import org.omg.CosCollection.PositionInvalid;
import org.omg.PortableServer.POA;

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









