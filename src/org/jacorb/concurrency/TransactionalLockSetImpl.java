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
import java.util.Vector;
import org.omg.CosConcurrencyControl.LockCoordinator;
import org.omg.CosConcurrencyControl.LockNotHeld;
import org.omg.CosConcurrencyControl.TransactionalLockSet;
import org.omg.CosConcurrencyControl.TransactionalLockSetPOA;
import org.omg.CosConcurrencyControl.lock_mode;
import org.omg.CosTransactions.Coordinator;
import org.omg.CosTransactions.Status;

class TransactionalLockSetImpl extends TransactionalLockSetPOA {
    private Hashtable locks = new Hashtable();
    private Vector queue = new Vector();
    private Vector related  = new Vector();
    private LockSetFactoryImpl factory;
    private boolean is_active = true;
    TransactionalLockSetImpl( LockSetFactoryImpl factory ) {
        this.factory = factory;
    };
    public void lock( Coordinator current, lock_mode mode ) {
        synchronized( queue ){
            check_active();
            TransactionCoordinator tc = factory.get_transaction_coordinator(current);
            Request rqst = null;
            synchronized( tc ){
                check_status(tc);
                if(attempt_lock( tc, mode )){
                    return;
                }
                rqst = new Request();
                rqst.state = LockSetFactoryImpl.REQUEST;
                rqst.current = tc;
                rqst.to_do = Request.LOCK;
                rqst.set_mode = mode;
                rqst.reset_mode = null;
                queue.addElement( rqst );
                tc.set_lock_coordinator( this );
            }
            while( rqst.state == LockSetFactoryImpl.REQUEST ){
                try {
                    queue.wait();
                } catch ( Exception e ){
                    e.printStackTrace( System.out );
                    throw new org.omg.CORBA.INTERNAL();
                }
            };
            switch( rqst.state ){
                case LockSetFactoryImpl.COMMIT   :
                case LockSetFactoryImpl.NO_TRANS :
                    throw new org.omg.CORBA.INVALID_TRANSACTION();
                case LockSetFactoryImpl.ROLLBACK :
                    throw new org.omg.CORBA.TRANSACTION_ROLLEDBACK();
            }
        }
    };
    public boolean try_lock( Coordinator current, lock_mode mode ) {
        synchronized( queue ){
            check_active();
            TransactionCoordinator tc = factory.get_transaction_coordinator(current);
            synchronized( tc ){
                check_status(tc);
                return attempt_lock( tc, mode );
            }
        }
    };
    public void unlock( Coordinator current, lock_mode mode ) throws LockNotHeld {
        synchronized( queue ){
            check_active();
            TransactionCoordinator tc = factory.get_transaction_coordinator(current);
            synchronized( tc ){
                check_status(tc);
                TransactionLocks current_locks = (TransactionLocks)locks.get( tc );
                if( current_locks == null ){
                    throw new LockNotHeld();
                }
                current_locks.unlock(mode);
            }
            if( attempt_lock_from_queue() ){
                queue.notifyAll();
            };
        }
    };
    public void change_mode( Coordinator current, lock_mode held_mode, lock_mode new_mode ) throws LockNotHeld {
        synchronized( queue ){
            check_active();
            TransactionCoordinator tc = factory.get_transaction_coordinator(current);
            Request rqst = null;
            synchronized( tc ){
                check_status(tc);
                if(attempt_change( tc, new_mode, held_mode )){
                    return;
                }
                rqst = new Request();
                rqst.state = LockSetFactoryImpl.REQUEST;
                rqst.current = tc;
                rqst.to_do = Request.CHANGE;
                rqst.set_mode = new_mode;
                rqst.reset_mode = held_mode;
                queue.addElement( rqst );
                tc.get_lock_coordinator( this );
            }
            while( rqst.state == LockSetFactoryImpl.REQUEST ){
                try {
                    queue.wait();
                } catch ( Exception e ){
                    e.printStackTrace( System.out );
                    throw new org.omg.CORBA.INTERNAL();
                }
            };
            switch( rqst.state ){
                case LockSetFactoryImpl.COMMIT   :
                case LockSetFactoryImpl.NO_TRANS :
                    throw new org.omg.CORBA.INVALID_TRANSACTION();
                case LockSetFactoryImpl.ROLLBACK :
                    throw new org.omg.CORBA.TRANSACTION_ROLLEDBACK();
                case LockSetFactoryImpl.REJECT :
                    throw new LockNotHeld();
            }
            if( attempt_lock_from_queue() ){
                queue.notifyAll();
            };
        }
    };
    public LockCoordinator get_coordinator( Coordinator which ) {
       TransactionCoordinator tc = factory.get_transaction_coordinator( which );
       return tc.get_lock_coordinator( this );
    };
    synchronized void add_related( TransactionalLockSet which ){
        related.addElement( which );
    };
    synchronized boolean attempt_lock( TransactionCoordinator tc, lock_mode mode ){
        Enumeration enumeration = locks.elements();
        TransactionLocks current_transaction_locks = null;
        while( enumeration.hasMoreElements() ){
            TransactionLocks lock = (TransactionLocks) enumeration.nextElement();
            if( lock.current == tc ){
                current_transaction_locks = lock;
                continue;
            }
            if( !lock.no_conflict( mode ) ){
                return false;
            }
        };
        if( current_transaction_locks == null ){
            current_transaction_locks = new TransactionLocks( tc );
            tc.get_lock_coordinator( this );
            locks.put( tc, current_transaction_locks );
        }
        current_transaction_locks.lock( mode );
        return true;
    };
    private void check_status( TransactionCoordinator tc ){
        Status status = tc.get_state();
        if( status.equals( Status.StatusActive ) ){
            return;
        } else if( status.equals( Status.StatusPrepared )      ||
                   status.equals( Status.StatusCommitted )     ||
                   status.equals( Status.StatusUnknown )       ||
                   status.equals( Status.StatusNoTransaction ) ||
                   status.equals( Status.StatusPreparing )     ||
                   status.equals( Status.StatusCommitting ) )  {
            throw new org.omg.CORBA.INVALID_TRANSACTION();
        } else if (status.equals( Status.StatusRollingBack )   ||
                   status.equals( Status.StatusMarkedRollback) ||
                   status.equals( Status.StatusRolledBack) )   {
            throw new org.omg.CORBA.TRANSACTION_ROLLEDBACK();
        }
    }
    private synchronized boolean attempt_lock_from_queue(){
        boolean do_recursive = false;
        Vector executed = new Vector();
        Enumeration enumeration = queue.elements();
        while( enumeration.hasMoreElements() ){
            Request r = (Request)enumeration.nextElement();
            synchronized( r.current ){
                try {
                    check_status( r.current );
                } catch ( org.omg.CORBA.INVALID_TRANSACTION e ) {
                    r.state = LockSetFactoryImpl.NO_TRANS;
                    executed.addElement( r );
                    continue;
                } catch ( org.omg.CORBA.TRANSACTION_ROLLEDBACK e ) {
                    r.state = LockSetFactoryImpl.ROLLBACK;
                    executed.addElement( r );
                    continue;
                }
                switch( r.to_do ) {
                    case Request.LOCK:
                        if( !attempt_lock( r.current, r.set_mode ) ) {
                            continue;
                        }
                        r.state = LockSetFactoryImpl.SATISFIED;
                        break;
                    case Request.CHANGE:
                        try {
                            if( !attempt_change( r.current, r.set_mode, r.reset_mode ) ) {
                                continue;
                            }
                            r.state = LockSetFactoryImpl.SATISFIED;
                            do_recursive = true;
                        } catch ( LockNotHeld e ) {
                            r.state = LockSetFactoryImpl.REJECT;
                        }
                        break;
                }
                executed.addElement( r );
            }
        };
        enumeration = executed.elements();
        while( enumeration.hasMoreElements() ){
            queue.removeElement( enumeration.nextElement() );
        }
        if( do_recursive ){
            attempt_lock_from_queue();
        }
        return executed.size() > 0;
    };
    private synchronized boolean attempt_change( TransactionCoordinator tc, lock_mode set_mode, lock_mode reset_mode ) throws LockNotHeld {
        TransactionLocks current_locks = (TransactionLocks)locks.get( tc );
        if( current_locks == null || !current_locks.is_held( reset_mode ) ){
            throw new LockNotHeld();
        }
        if( attempt_lock( tc, set_mode ) ){
            current_locks.unlock( reset_mode );
            return true;
        }
        return false;
    };
    synchronized void transaction_finished( TransactionCoordinator tc ){
        Vector executed = new Vector();
        Enumeration enumeration;
        boolean do_notify = false;
        synchronized( queue ){
            enumeration = queue.elements();
            while( enumeration.hasMoreElements() ){
                Request r = (Request)enumeration.nextElement();
                if( r.current == tc ){
                    r.state = LockSetFactoryImpl.ROLLBACK;
                    executed.addElement( r );
                }
            }
            if( executed.size() > 0 ) {
                enumeration = executed.elements();
                while( enumeration.hasMoreElements() ){
                    queue.removeElement( enumeration.nextElement() );
                }
                do_notify = true;
            }
            if( locks.remove( tc ) != null ) {
                do_notify = attempt_lock_from_queue()?true:do_notify;
            }
            if( do_notify ) {
                queue.notifyAll();
            }
        }
        if( related.size() > 0 ) {
            enumeration = related.elements();
            while( enumeration.hasMoreElements() ){
                TransactionalLockSet ls = (TransactionalLockSet)enumeration.nextElement();
                ls.get_coordinator( tc.get_coordinator() ).drop_locks();
            }
        }
    };
    public void print(){
        Enumeration enumeration;
        System.out.println("\n=============================================================================");
        System.out.println(" LOCKS"+locks.size() );
        System.out.println("-----------------------------------------------------------------------------");
        synchronized ( queue ) {
            enumeration = locks.elements();
            while( enumeration.hasMoreElements() ){
                TransactionLocks r = (TransactionLocks)enumeration.nextElement();
                System.out.println( r.toString() );
            }
            System.out.println("\n-----------------------------------------------------------------------------");
            System.out.println(" QUEUE"+queue.size() );
            System.out.println("-----------------------------------------------------------------------------");
            enumeration = queue.elements();
            while( enumeration.hasMoreElements() ){
               Request r = (Request)enumeration.nextElement();
               System.out.println( r.toString() );
           }
        };
        System.out.println("=============================================================================\n");
    };
    //    public void destroy() throws LockExists {
    public void destroy()  {
        synchronized( queue ){
            check_active();
            is_active = false;
            if( locks.size() > 0 ){
                Enumeration enumeration = locks.elements();
                while( enumeration.hasMoreElements() ){
                    TransactionLocks ls = (TransactionLocks)enumeration.nextElement();
                    if( ls.any_locks() ){
                        throw new RuntimeException("LockExists");
                    }
                }
            }
            factory.remove_me( this );
        };
    };
    private void check_active(){
        if( !is_active ){
            throw new org.omg.CORBA.OBJECT_NOT_EXIST();
        }
    }
};






