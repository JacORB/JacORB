package org.jacorb.concurrency;

/*
 *        JacORB concurrency control service - a free CCS for JacORB
 *
 *   Copyright (C) 1999-2001  LogicLand group, Viacheslav Tararin.
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
import org.omg.CosTransactions.*;

public class Request 
{
    public static final int LOCK    = 1;
    public static final int CHANGE  = 3;
   
    public int state;
    public TransactionCoordinator current;
    public int to_do;
    public lock_mode set_mode;
    public lock_mode reset_mode;

    public String toString() 
    {
        String s = 
            current.get_coordinator().get_transaction_name()+
            ": state="+state+" to_do="+(to_do==LOCK?"lock":"chng")+" set=";

        if( set_mode==null )
        { 
            s = s + "null";
        }
        else 
        { 
            s = s + set_mode.value();
        } 
        s =s +" reset=";
        if( reset_mode==null ) 
        { 
            s = s +"null"; 
        } 
        else 
        {
            s = s + reset_mode.value();
        }
        return s;
    };


}


