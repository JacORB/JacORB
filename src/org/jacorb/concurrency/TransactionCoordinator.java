package org.jacorb.concurrency;

/*
 *        JacORB concurrency control service - a free CCS for JacORB
 *
 *   Copyright (C) 1999-2002  LogicLand group, Viacheslav Tararin.
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

import org.omg.CosConcurrencyControl.*;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAPackage.*;
import org.omg.CosTransactions.*;
import java.util.*;

class TransactionCoordinator extends ResourcePOA /* implements Runnable */ {
    static final int ACTIVE = 0;
    static final int COMMITED = 1;
    static final int PREPARED = 2;
    static final int ROLLEDBACK = 3;

    private Coordinator current;
    private POA poa;
    private Hashtable locksets = new Hashtable();
    private int state;
    private LockSetFactoryImpl factory;
    TransactionCoordinator( LockSetFactoryImpl factory, Coordinator current, POA poa ) {
        this.current = current;
        this.poa = poa;
        this.factory = factory;
        Status status = current.get_status();
        if( status.equals( Status.StatusActive ) ){
            state = ACTIVE;
        } else if( status.equals( Status.StatusPrepared )      || 
                   status.equals( Status.StatusPreparing ) ) {
            state = PREPARED;
        } else if ( status.equals( Status.StatusCommitted )     ||
                   status.equals( Status.StatusUnknown )       ||
                   status.equals( Status.StatusNoTransaction ) ||
                   status.equals( Status.StatusCommitting ) )  {
            state = COMMITED;
        } else if (status.equals( Status.StatusRollingBack )   || 
                   status.equals( Status.StatusMarkedRollback) ||
                   status.equals( Status.StatusRolledBack) )   {
            state = ROLLEDBACK;
        }
    };
    Coordinator get_coordinator() {
        return current;
    };
    synchronized Status get_state(){
        switch( state ){
            case ACTIVE:
               return Status.StatusActive;
            case COMMITED:
               return Status.StatusCommitted;
            case PREPARED:
               return Status.StatusPrepared;
            case ROLLEDBACK:
               return Status.StatusRolledBack;
        }
        return Status.StatusNoTransaction;
    };
    public synchronized Vote prepare() throws HeuristicMixed,HeuristicHazard {
        if( state == ACTIVE ){
            state = PREPARED;
            return Vote.VoteCommit;
        } 
        return Vote.VoteRollback;
    };
    public synchronized void rollback() throws HeuristicCommit,HeuristicMixed,HeuristicHazard {
        if( state == ACTIVE ) {
            state = ROLLEDBACK;
            run();
        }
    };
    public synchronized void commit() throws NotPrepared,HeuristicRollback,HeuristicMixed,HeuristicHazard{
        if( state == PREPARED ) {
            state = COMMITED;
            run();
        } else {
            throw new NotPrepared();
        } 
    };
    public synchronized void commit_one_phase() throws HeuristicHazard {
        if( state == ACTIVE ){
            state = COMMITED;
            run();
        }
    };
    public synchronized void forget(){
        throw new org.omg.CORBA.NO_IMPLEMENT();
    };
    synchronized LockCoordinator get_lock_coordinator( TransactionalLockSetImpl ls ){
         LockCoordinatorImpl lc = (LockCoordinatorImpl)locksets.get( ls );
         if( lc == null ){
             lc = new LockCoordinatorImpl( this, ls );
             locksets.put( ls, lc );
         }
         try {
             return LockCoordinatorHelper.narrow(poa.servant_to_reference(lc));
         } catch ( Exception e ){
             e.printStackTrace( System.out );
             throw new org.omg.CORBA.INTERNAL();
         }
    };

    synchronized void set_lock_coordinator( TransactionalLockSetImpl ls ){
        check_state();
        LockCoordinatorImpl lc = (LockCoordinatorImpl)locksets.get( ls );
        if( lc == null ){
            lc = new LockCoordinatorImpl( this, ls );
            locksets.put( ls, lc );
        }
    };
    synchronized void remove_coordinator( TransactionalLockSetImpl ls ){
        LockCoordinatorImpl lc = (LockCoordinatorImpl)locksets.get( ls );
        if( lc != null ){
            try {
                byte [] ObjId = poa.servant_to_id( lc );
                poa.deactivate_object( ObjId );
            } catch ( Exception e ) {
            }
            locksets.remove( ls );
        }
    };
    public void run() {
        Enumeration enum = locksets.elements();
        while( enum.hasMoreElements() ){
            LockCoordinatorImpl lc = (LockCoordinatorImpl)enum.nextElement();
            lc.drop_locks();
            try {
                byte [] ObjId = poa.servant_to_id( lc );
                poa.deactivate_object( ObjId );
            } catch ( ServantNotActive e ) {
            } catch ( Exception e ){
                e.printStackTrace( System.out );
                throw new org.omg.CORBA.INTERNAL();
            }
        }; 
        factory.remove_me( this );
    };
    private void check_state(){
        if( state == PREPARED || state == COMMITED ) {
            throw new org.omg.CORBA.INVALID_TRANSACTION();
        } else if( state == ROLLEDBACK ) {
            throw new org.omg.CORBA.TRANSACTION_ROLLEDBACK();
        }
    };
};






