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


import org.omg.CosTransactions.NotPrepared;
import org.omg.CosTransactions.RecoveryCoordinatorOperations;
import org.omg.CosTransactions.Resource;
import org.omg.CosTransactions.Status;

public class RecoveryCoordinatorImpl implements RecoveryCoordinatorOperations {

    public Status replay_completion(Resource r) throws NotPrepared{
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

}






