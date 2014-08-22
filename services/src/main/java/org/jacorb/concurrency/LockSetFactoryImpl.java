package org.jacorb.concurrency;

/*
 *        JacORB concurrency control service - a free CCS for JacORB
 *
 *   Copyright (C) 1999-2004 LogicLand group, Viacheslav Tararin.
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
 */

import java.util.Enumeration;
import java.util.Hashtable;
import org.omg.CosConcurrencyControl.LockSet;
import org.omg.CosConcurrencyControl.LockSetFactoryPOA;
import org.omg.CosConcurrencyControl.TransactionalLockSet;
import org.omg.CosConcurrencyControl.TransactionalLockSetHelper;
import org.omg.CosTransactions.Coordinator;
import org.omg.CosTransactions.Resource;
import org.omg.CosTransactions.ResourceHelper;
import org.omg.PortableServer.POA;

public class LockSetFactoryImpl extends LockSetFactoryPOA {
    public static final int REQUEST   = 1;
    public static final int SATISFIED = 2;
    public static final int COMMIT    = 3;
    public static final int ROLLBACK  = 4;
    public static final int NO_TRANS  = 5;
    public static final int REJECT    = 6;

/* -------------------------------------------------------------------------- */
    private Hashtable coordinators = new Hashtable();
    private POA poa;
/* -------------------------------------------------------------------------- */
    public LockSetFactoryImpl( POA poa ){
        this.poa = poa;
    };
/* -------------------------------------------------------------------------- */

    public LockSet create(){
        throw new org.omg.CORBA.NO_IMPLEMENT();
    };
/* -------------------------------------------------------------------------- */
    public LockSet create_related(LockSet which){
        throw new org.omg.CORBA.NO_IMPLEMENT();
    };
/* -------------------------------------------------------------------------- */
    public TransactionalLockSet create_transactional(){
        TransactionalLockSetImpl ls = new TransactionalLockSetImpl( this );
        try {
            return TransactionalLockSetHelper.narrow( poa.servant_to_reference( ls ) );
        } catch ( Exception e ){
            e.printStackTrace( System.out );
            throw new org.omg.CORBA.INTERNAL();
        }
    };
/* -------------------------------------------------------------------------- */
    public TransactionalLockSet create_transactional_related(TransactionalLockSet which){
        TransactionalLockSetImpl ls = new TransactionalLockSetImpl( this );
        ls.add_related( which );
        try {
            return TransactionalLockSetHelper.narrow( poa.servant_to_reference( ls ) );
        } catch ( Exception e ){
            e.printStackTrace( System.out );
            throw new org.omg.CORBA.INTERNAL();
        }
    };
/* -------------------------------------------------------------------------- */
    synchronized TransactionCoordinator get_transaction_coordinator( Coordinator current )
    {
        if( coordinators.containsKey( current.get_transaction_name() ) )
        {
            return (TransactionCoordinator)coordinators.get( current.get_transaction_name() );
        }

        TransactionCoordinator coordinator = new TransactionCoordinator( this, current, poa );
        try
        {
            Resource res = ResourceHelper.narrow( poa.servant_to_reference( coordinator ) );
            current.register_resource( res );
        }
        catch ( Exception e )
        {
            e.printStackTrace( System.out );
        }
        coordinators.put( current.get_transaction_name(), coordinator );
        return coordinator;
    };
/* -------------------------------------------------------------------------- */
    synchronized void remove_me( TransactionCoordinator i_am ){
        coordinators.remove( i_am.get_coordinator() );
        try {
            byte [] ObjId = poa.servant_to_id( i_am );
            poa.deactivate_object( ObjId );
        } catch ( Exception e ){
            e.printStackTrace( System.out );
            throw new org.omg.CORBA.INTERNAL();
        }
    }
/* -------------------------------------------------------------------------- */
    synchronized void remove_me( TransactionalLockSetImpl i_am ){
        Enumeration enumeration = coordinators.elements();
        while( enumeration.hasMoreElements() ){
            TransactionCoordinator tc = (TransactionCoordinator)enumeration.nextElement();
            tc.remove_coordinator( i_am );
        }
        try {
            byte [] ObjId = poa.servant_to_id( i_am );
            poa.deactivate_object( ObjId );
        } catch ( Exception e ){
            e.printStackTrace( System.out );
            throw new org.omg.CORBA.INTERNAL();
        }
    };
};






