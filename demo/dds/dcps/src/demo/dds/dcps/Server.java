/*
 *  DDS (Data Distribution Service) for JacORB
 *
 * Copyright (C) 2005  , Ahmed yehdih <ahmed.yehdih@gmail.com>, fouad
 * allaoui <fouad.allaoui@gmail.com>, Didier Donsez (didier.donsez@ieee.org)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public 
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * Coontact: Ahmed yehdih <ahmed.yehdih@gmail.com>, fouad allaoui
 * <fouad.allaoui@gmail.com>, Didier Donsez (didier.donsez@ieee.org)
 * Contributor(s)
 *
 **/

package demo.dds.dcps;

import org.jacorb.dds.DomainParticipantFactoryImpl;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.dds.DomainParticipantFactory;
import org.omg.dds.DomainParticipantFactoryHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

public class Server implements Runnable {

    String[] args;

    public static void main(String[] args) {
        Server server = new Server();
        server.setArgs(args);
        new Thread(server).start();
    }

    /**
     * 
     */
    public void run() {

        try {
            ORB orb = ORB.init(args, null);
            // get reference to rootpoa & activate the POAManager
            POA poa = POAHelper.narrow(orb
                    .resolve_initial_references("RootPOA"));
            poa.the_POAManager().activate();

            // get the root naming context
            org.omg.CORBA.Object objRef = orb
                    .resolve_initial_references("NameService");
            // Use NamingContextExt which is part of the Interoperable
            // Naming Service (INS) specification.
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
            DomainParticipantFactoryImpl impl = new DomainParticipantFactoryImpl(
                    orb, poa);

            // get object reference from the servant (and implicitly register
            // it)
            org.omg.CORBA.Object oref = poa.servant_to_reference(impl);
            DomainParticipantFactory ref = DomainParticipantFactoryHelper
                    .narrow(oref);

            if (ncRef != null) {
                // bind the Object Reference in Naming
                NameComponent path[] = ncRef
                        .to_name("DomainParticipantFactory");
                ncRef.rebind(path, ref);
            }
            System.out.println("Server ready and waiting ...");
            orb.run();
        } catch (Exception e) {
            System.out.println("e" + e);
            e.printStackTrace();
        }
    }

    public void end() {
        Thread.currentThread().destroy();
    }

    /**
     * @param args
     *            The args to set.
     */
    public void setArgs(String[] args) {
        this.args = args;
    }
}
