package org.jacorb.transaction;

/*
 *        JacORB transaction service - a free TS for JacORB
 *
 *   Copyright (C) 1999-2000  LogicLand group Alex Sinishin.
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
import org.omg.CosTransactions.Terminator;

import java.util.*;

public class CoordinatorImpl 
    implements Sleeper, CoordinatorOperations, 
               ControlOperations, TerminatorOperations 
{
    private Terminator        term_ref;
    private TerminatorPOATie  term_skel;
    private Coordinator       coord_ref;
    private CoordinatorPOATie coord_skel;
    private Control           contr_ref;
    private ControlPOATie     contr_skel;

    private int               transaction_id;
    private int               hash_code;

    private int               status;
    private String            stat_semaphore;

    private Vector            resources;
    private Vector            votes;
    private Vector            syncs;

    private org.omg.PortableServer.POA poa;

    CoordinatorImpl(org.omg.PortableServer.POA _poa, int _trans_id,
                    int _hash_code, int time)
    {
        transaction_id = _trans_id;
        hash_code      = _hash_code;
        poa = _poa;
        stat_semaphore = new String("sss");
        resources      = new Vector();
        syncs          = new Vector();
        votes          = new Vector();
        try {
            coord_skel = new CoordinatorPOATie(this);
            coord_ref  = CoordinatorHelper.narrow(poa.servant_to_reference(coord_skel));
            term_skel  = new TerminatorPOATie(this);
            term_ref   = TerminatorHelper.narrow(poa.servant_to_reference(term_skel));

            contr_skel = new ControlPOATie(this);
            contr_ref  = ControlHelper.narrow(poa.servant_to_reference(contr_skel));

        } catch (org.omg.PortableServer.POAPackage.ServantNotActive esn){
            throw new org.omg.CORBA.INTERNAL();
        } catch (org.omg.PortableServer.POAPackage.WrongPolicy ew){
            throw new org.omg.CORBA.INTERNAL();
        }
        status = Status._StatusActive;
        if (time != 0){
            TransactionService.get_timer().add_channel(this, time);
        }
    }

    private void destroy(){
        try {
            byte[] oid = poa.reference_to_id(term_ref);
            poa.deactivate_object(oid);
            term_ref._release();

            oid = poa.reference_to_id(coord_ref);
            poa.deactivate_object(oid);
            coord_ref._release();

            oid = poa.reference_to_id(contr_ref);
            poa.deactivate_object(oid);
            contr_ref._release();

        } catch (org.omg.PortableServer.POAPackage.ObjectNotActive esn){
            throw new org.omg.CORBA.INTERNAL();
        } catch (org.omg.PortableServer.POAPackage.WrongPolicy ew){
            throw new org.omg.CORBA.INTERNAL();
        } catch (org.omg.PortableServer.POAPackage.WrongAdapter ew){
            throw new org.omg.CORBA.INTERNAL();
        }
        TransactionService.release_coordinator(hash_code);
    }

    private boolean move_to_state(int new_status){
        synchronized(stat_semaphore){
            switch(status){
            case Status._StatusActive:
                switch(new_status){
                case Status._StatusMarkedRollback:
                case Status._StatusPreparing:
                case Status._StatusRollingBack:
                    status = new_status;
                    return true;
                default:
                    return false;
                }
            case Status._StatusMarkedRollback:
                switch(new_status){
                case Status._StatusRollingBack:
                    status = new_status;
                    return true;
                default:
                    return false;
                }
            case Status._StatusPrepared:
                switch(new_status){
                case Status._StatusCommitting:
                    status = new_status;
                    return true;
                default:
                    return false;
                }
            case Status._StatusCommitted:
                switch(new_status){
                case Status._StatusNoTransaction:
                    status = new_status;
                    return true;
                default:
                    return false;
                }
            case Status._StatusRolledBack:
                switch(new_status){
                case Status._StatusNoTransaction:
                    status = new_status;
                    return true;
                default:
                    return false;
                }
            case Status._StatusUnknown:
                throw new org.omg.CORBA.INTERNAL();
            case Status._StatusNoTransaction:
                throw new org.omg.CORBA.INTERNAL();
            case Status._StatusPreparing:
                switch(new_status){
                case Status._StatusRollingBack:
                case Status._StatusPrepared:
                    status = new_status;
                    return true;
                default:
                    return false;
                }
            case Status._StatusCommitting:
                switch(new_status){
                case Status._StatusCommitted:
                    status = new_status;
                    return true;
                default:
                    return false;
                }
            case Status._StatusRollingBack:
                switch(new_status){
                case Status._StatusRolledBack:
                    status = new_status;
                    return true;
                default:
                    return false;
                }
            default:
                throw new org.omg.CORBA.INTERNAL();
            }
        }
    }

    public void wakeup(){
        TransactionService.get_timer().kill_channel(this);
        if (move_to_state(Status._StatusRollingBack)){
            rolling_to_back();
        }
    }

    int _get_transaction_id(){
        return transaction_id;
    }

    Control _get_control(){
        return contr_ref;
    }

    public Status get_status(){
        return Status.from_int(status);
    }

    public Status get_parent_status(){
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public Status get_top_level_status(){
        return Status.from_int(status);
    }

    public boolean is_same_transaction(Coordinator tc){
        return (hash_code == tc.hash_transaction());
    }

    public boolean is_related_transaction(Coordinator tc){
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public boolean is_ancestor_transaction(Coordinator tc){
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public boolean is_descendant_transaction(Coordinator tc){
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public boolean is_top_level_transaction(){
        return true;
    }

    public int hash_transaction(){
        return hash_code;
    }

    public int hash_top_level_tran(){
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public RecoveryCoordinator register_resource(Resource r) throws Inactive{
        synchronized(stat_semaphore){
            if (status == Status._StatusMarkedRollback){
                throw new org.omg.CORBA.TRANSACTION_ROLLEDBACK();
            }
            if (status != Status._StatusActive){
                throw new Inactive();
            }
            resources.addElement(r);
            votes.addElement(null);
        }
        return null;
    }

    public void register_synchronization(Synchronization sync)
        throws Inactive, SynchronizationUnavailable{
        synchronized(stat_semaphore){
            if (status == Status._StatusMarkedRollback){
                throw new org.omg.CORBA.TRANSACTION_ROLLEDBACK();
            }
            if (status != Status._StatusActive){
                throw new Inactive();
            }
            syncs.addElement(sync);
        }
    }

    public void register_subtran_aware(SubtransactionAwareResource r)
        throws Inactive, NotSubtransaction{
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void rollback_only() throws Inactive{
        if (!move_to_state(Status._StatusMarkedRollback)){
            throw new Inactive();
        }
    }

    public String get_transaction_name(){
        return "Transaction_" + transaction_id;
    }

    public Control create_subtransaction()
        throws SubtransactionsUnavailable, Inactive{
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public PropagationContext get_txcontext() throws Unavailable{
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public Terminator get_terminator() throws Unavailable{
        if (status == Status._StatusNoTransaction){
            throw new Unavailable();
        }
        return term_ref;
    }

    public Coordinator get_coordinator() throws Unavailable{
        if (status == Status._StatusNoTransaction){
            throw new Unavailable();
        }
        return coord_ref;
    }

    private void forget(){
        resources.removeAllElements();
        votes.removeAllElements();
        syncs.removeAllElements();
    }

    public void commit(boolean report_heuristics) throws HeuristicMixed,
        HeuristicHazard{
        if (!move_to_state(Status._StatusPreparing)){
            if (move_to_state(Status._StatusRollingBack)){
                TransactionService.get_timer().kill_channel(this);
                rolling_to_back();
                throw new org.omg.CORBA.TRANSACTION_ROLLEDBACK();
            }
            throw new org.omg.CORBA.INTERNAL();
        }

        TransactionService.get_timer().kill_channel(this);

        for (int i = 0;i < syncs.size();i++){
            Synchronization sync = (Synchronization)syncs.elementAt(i);
            sync.before_completion();
        }

        if (resources.size() == 1){
            if (!move_to_state(Status._StatusPrepared)){
                throw new org.omg.CORBA.INTERNAL();
            }
            if (!move_to_state(Status._StatusCommitting)){
                throw new org.omg.CORBA.INTERNAL();
            }

            Resource r = (Resource)resources.elementAt(0);
            try {
                r.commit_one_phase();
            } catch(HeuristicHazard hh) {
                throw new org.omg.CORBA.NO_IMPLEMENT();
            }
        } else {
            for (int i = 0;i < resources.size();i++){
                Resource r = (Resource)resources.elementAt(i);
                try {
                    Vote v = r.prepare();
                    votes.setElementAt(v, i);
                    if (v.value() == Vote._VoteRollback){

                        rollback();
                        throw new org.omg.CORBA.TRANSACTION_ROLLEDBACK();

                    }
                } catch(HeuristicHazard hh) {
                    throw new org.omg.CORBA.NO_IMPLEMENT();
                } catch(HeuristicMixed hm) {
                    throw new org.omg.CORBA.NO_IMPLEMENT();
                }
            }

            if (!move_to_state(Status._StatusPrepared)){
                throw new org.omg.CORBA.INTERNAL();
            }
            if (!move_to_state(Status._StatusCommitting)){
                throw new org.omg.CORBA.INTERNAL();
            }

            for (int i = 0;i < resources.size();i++){
                Resource r = (Resource)resources.elementAt(i);
                Vote     v = (Vote)votes.elementAt(i);
                
                try {
                    if (v == null){
                        throw new org.omg.CORBA.INTERNAL();
                    } else {
                        if (v.value() == Vote._VoteCommit){
                            r.commit();
                        }
                    }
                } catch(NotPrepared np) {
                    throw new org.omg.CORBA.NO_IMPLEMENT();
                } catch(HeuristicRollback hr) {
                    throw new org.omg.CORBA.NO_IMPLEMENT();
                } catch(HeuristicHazard hh) {
                    throw new org.omg.CORBA.NO_IMPLEMENT();
                } catch(HeuristicMixed hm) {
                    throw new org.omg.CORBA.NO_IMPLEMENT();
                }
            }
        }

        if (!move_to_state(Status._StatusCommitted)){
            throw new org.omg.CORBA.INTERNAL();
        }

        forget();

        if (!move_to_state(Status._StatusNoTransaction)){
            throw new org.omg.CORBA.INTERNAL();
        }
        destroy();
    }

    private void rolling_to_back(){
        for (int i = 0;i < resources.size();i++){
            Resource r = (Resource)resources.elementAt(i);
            Vote     v = (Vote)votes.elementAt(i);
            try {
                if (v == null){
                    r.rollback();
                } else {
                    if (v.value() == Vote._VoteCommit){
                        r.rollback();
                    }
                }
            } catch(HeuristicCommit hc) {
                throw new org.omg.CORBA.NO_IMPLEMENT();
            } catch(HeuristicMixed hm) {
                throw new org.omg.CORBA.NO_IMPLEMENT();
            } catch(HeuristicHazard hh) {
                throw new org.omg.CORBA.NO_IMPLEMENT();
            }
        }

        if (!move_to_state(Status._StatusRolledBack)){
            throw new org.omg.CORBA.INTERNAL();
        }

        forget();

        if (!move_to_state(Status._StatusNoTransaction)){
            throw new org.omg.CORBA.INTERNAL();
        }
        destroy();
    }

    public void rollback(){
        if (!move_to_state(Status._StatusRollingBack)){
            throw new org.omg.CORBA.TRANSACTION_REQUIRED();
        }
        TransactionService.get_timer().kill_channel(this);
        rolling_to_back();
    }
}







