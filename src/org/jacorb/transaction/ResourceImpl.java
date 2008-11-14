package org.jacorb.transaction;

/*
 *        JacORB transaction service - a free TS for JacORB
 *
 *   Copyright (C) 1999-2004 LogicLand group Alex Sinishin.
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


import org.omg.CosTransactions.HeuristicCommit;
import org.omg.CosTransactions.HeuristicHazard;
import org.omg.CosTransactions.HeuristicMixed;
import org.omg.CosTransactions.HeuristicRollback;
import org.omg.CosTransactions.NotPrepared;
import org.omg.CosTransactions.ResourceOperations;
import org.omg.CosTransactions.Vote;

public class ResourceImpl implements ResourceOperations {
                          
    public Vote prepare() throws HeuristicMixed, HeuristicHazard{
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void rollback() throws HeuristicCommit,
                                  HeuristicMixed, HeuristicHazard{
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void commit() throws NotPrepared, HeuristicRollback,
                                HeuristicMixed, HeuristicHazard{
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void commit_one_phase() throws HeuristicHazard{
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void forget(){
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

}






