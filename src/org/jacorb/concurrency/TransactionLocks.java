package org.jacorb.concurrency;

/*
 *        JacORB concurrency control service - a free CCS for JacORB
 *
 *   Copyright (C) 1999-2000  LogicLand group, Viacheslav Tararin.
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

import org.omg.CosTransactions.*;
import org.omg.CosConcurrencyControl.*;

public class TransactionLocks {
    TransactionCoordinator current;
    private int read = 0;
    private int write = 0;
    private int upgrade = 0;
    private int intention_read = 0;
    private int intention_write = 0;
    TransactionLocks( TransactionCoordinator current ){
        this.current = current;
    };
    synchronized boolean no_conflict( lock_mode mode ){
        if( mode.equals( lock_mode.read ) ){
            return write == 0 && intention_write == 0;
        } else if( mode.equals( lock_mode.write ) ) {
            return write == 0 && read == 0 && upgrade == 0 && intention_read == 0 && intention_write == 0;
        } else if( mode.equals(lock_mode.upgrade ) ) {
            return upgrade == 0 && intention_write == 0 && write == 0;
        } else if( mode.equals ( lock_mode.intention_read ) ) {
            return write == 0;
        } else if( mode.equals( lock_mode.intention_write ) ) {
            return write == 0 && read == 0 && upgrade == 0;
        }
        return false;
    };
    synchronized void lock( lock_mode mode ){
        if( mode.equals( lock_mode.read ) ) {
            read++;
        } else if( mode.equals( lock_mode.write ) ) {
            write++;
        } else if( mode.equals( lock_mode.upgrade ) ) {
            upgrade++;
        } else if( mode.equals( lock_mode.intention_read ) ) {
            intention_read++;
        } else if( mode.equals( lock_mode.intention_write ) ) {
            intention_write++;
        }
    };
    synchronized void unlock( lock_mode mode ) throws LockNotHeld {
        if( mode.equals( lock_mode.read ) ) {
            check_held( read );
            read--;
        } else if( mode.equals( lock_mode.write ) ) {
            check_held( write );
            write--;
        } else if( mode.equals( lock_mode.upgrade ) ) {
            check_held( upgrade );
            upgrade--;
        } else if( mode.equals( lock_mode.intention_read ) ) {
            check_held( intention_read );
            intention_read--;
        } else if( mode.equals( lock_mode.intention_write ) ) {
            check_held( intention_write );
            intention_write--;
        }
    }
    private void check_held( int i )  throws LockNotHeld {
        if ( i == 0 ){
            throw new LockNotHeld();
        }
    };
    boolean is_held( lock_mode mode ) {
        if( mode.equals( lock_mode.read ) ) {
            return read > 0;
        } else if( mode.equals( lock_mode.write ) ) {
            return write > 0;
        } else if( mode.equals( lock_mode.upgrade ) ) {
            return upgrade > 0;
        } else if( mode.equals( lock_mode.intention_read ) ) {
            return intention_read > 0;
        } else if( mode.equals( lock_mode.intention_write ) ) {
            return intention_write > 0;
        }
        return false;
    };
    boolean any_locks(){
        return read!=0 || write!=0 || upgrade!=0 || intention_read!=0 || intention_write!=0;
    };
    public String toString() {
        return current.get_coordinator().get_transaction_name()+": "+
            " read="+read+
            " write="+write+
            " upgrade="+upgrade+
            " intention_read="+intention_read+
            " intention_write="+intention_write;
    };
