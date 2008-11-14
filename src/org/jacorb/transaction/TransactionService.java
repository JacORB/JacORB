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

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import org.apache.avalon.framework.logger.Logger;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosTransactions.Control;
import org.omg.CosTransactions.PropagationContext;
import org.omg.CosTransactions.TransactionFactory;
import org.omg.CosTransactions.TransactionFactoryHelper;
import org.omg.CosTransactions.TransactionFactoryPOA;

public class TransactionService 
    extends TransactionFactoryPOA 
{
    private static boolean                    initialized = false;
    private static TransactionService         factory;
    private static TransactionFactory         fact_ref;
    private static CoordinatorImpl[]          coordinators;
    private static Timer                      timer;
    private static int                        trans_id = 0;
    private static org.omg.PortableServer.POA poa;

    private static Logger logger;

    static Timer get_timer(){
        return timer;
    }


    public static boolean is_initialized(){
        return initialized;
    }

    public static TransactionFactory get_reference(){
        return fact_ref;
    }

    static void release_coordinator(int hash_code)
    {
        coordinators[hash_code] = null;
    }

    private int find_free(){
        for (int i = 0;i < coordinators.length;i++){
            if (coordinators[i] == null){
                return i;
            }
        }
        throw new org.omg.CORBA.INTERNAL();
    }

    public Control create(int time_out)
    {
        trans_id++;
        int ix;
        synchronized(coordinators){
            ix = find_free();
            coordinators[ix] = new CoordinatorImpl(poa, trans_id, ix, time_out);
        }
        return coordinators[ix]._get_control();
    }

    public Control recreate(PropagationContext ctx){
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public static void start(org.omg.PortableServer.POA _poa, int max_of_trans)
    {
        if (initialized)
	{
            throw new org.omg.CORBA.INTERNAL();
        }
        try 
	{
            poa = _poa;
            factory = new TransactionService();
            fact_ref = 
                TransactionFactoryHelper.narrow(poa.servant_to_reference(factory));

            coordinators = new CoordinatorImpl [max_of_trans];

            for (int i = 0;i < coordinators.length;i++)
            {
                coordinators[i] = null;
            }
            timer = new Timer(max_of_trans);

        } 
	catch(Exception e) 
	{
            e.printStackTrace();
            System.exit(1);
        }
        initialized = true;
    }


    public static void main( String[] args )
    {
        org.omg.CORBA.ORB orb = 
            org.omg.CORBA.ORB.init(args, null);
        logger = 
            ((org.jacorb.orb.ORB)orb).getConfiguration().getNamedLogger("jacorb.tx_service");
        try
        {
            org.omg.PortableServer.POA poa = 
                org.omg.PortableServer.POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
	    poa.the_POAManager().activate();
            
	    TransactionService transactionService = 
                new TransactionService();
	    transactionService.start(poa,10);	    

            if( args.length == 1 ) 
            {
                // write the object reference to args[0]

                PrintWriter ps = 
                    new PrintWriter(new FileOutputStream(new File( args[0])));
                ps.println( orb.object_to_string( transactionService.get_reference() ) );
                ps.close();
            } 
            else
            {
                NamingContextExt nc = 
		    NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));
                NameComponent [] name = new NameComponent[1];
                name[0] = new NameComponent( "TransactionService", "service");
                nc.bind(name, transactionService.get_reference());
            }
            if (logger.isInfoEnabled())
                logger.info("TransactionService up");
        } 
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        orb.run();
    }

}


