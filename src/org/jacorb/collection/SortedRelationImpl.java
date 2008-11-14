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

import org.jacorb.collection.util.SortedVector;
import org.omg.CosCollection.OperationsOperations;
import org.omg.CosCollection.SortedRelation;
import org.omg.CosCollection.SortedRelationOperations;
import org.omg.PortableServer.POA;

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






