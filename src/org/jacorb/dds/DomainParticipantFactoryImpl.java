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
package org.jacorb.dds;

import java.util.Iterator;
import java.util.Vector;

import org.jacorb.events.EventChannelImpl;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.dds.DomainParticipant;
import org.omg.dds.DomainParticipantFactory;
import org.omg.dds.DomainParticipantFactoryPOA;
import org.omg.dds.DomainParticipantHelper;
import org.omg.dds.RETCODE_OK;
import org.omg.dds.RETCODE_PRECONDITION_NOT_MET;

/**
 * The sole purpose of this class is to allow the creation and destruction of
 * DomainParticipant objects. DomainParticipantFactory itself has no factory. It
 * is either a pre-existing singleton object that can be accessed by means of
 * the get_instance class operation on the DomainParticipantFactory.
 */
public class DomainParticipantFactoryImpl extends DomainParticipantFactoryPOA
{

    private org.omg.CORBA.ORB orb;

    private org.omg.PortableServer.POA poa;

    private org.omg.dds.DomainParticipantQos defaultqos;

    private ThreadSubscriber Consummer;

    private Vector allParticipant;

    private DomainParticipantFactoryImpl impl;

    private DomainParticipantFactory ref;

    /**
     * Sets the pOA attribute of this object
     * 
     * @param poa
     *            The new pOA value
     */
    public void setPOA (org.omg.PortableServer.POA poa)
    {
        this.poa = poa;
    }

    /**
     * Sets the oRB attribute of this object
     * 
     * @param orb
     *            The new oRB value
     */
    public void setORB (org.omg.CORBA.ORB orb)
    {
        this.orb = orb;
    }

    public DomainParticipantFactoryImpl (org.omg.CORBA.ORB orb,
            org.omg.PortableServer.POA poa)
    {
        this.orb = orb;
        this.poa = poa;
        try
        {
            NamingContextExt nc = NamingContextExtHelper.narrow (orb.resolve_initial_references ("NameService"));
            EventChannelImpl channel = new EventChannelImpl (orb, poa);
            org.omg.CORBA.Object o = poa.servant_to_reference (channel);
            /* event channel used by event service */
            nc.rebind (nc.to_name ("eventchannel"), o);
        }
        catch (Exception e)
        {

        }
        allParticipant = new Vector ();
        /* thread send message for all suscriber */
        Consummer = new ThreadSubscriber (orb, poa);
        Consummer.start ();
    }

    /**
     * This operation creates a new DomainParticipant object. The
     * DomainParticipant signifies that the calling application intends to join
     * the Domain identified by the domainId argument.
     */
    public org.omg.dds.DomainParticipant create_participant (int domainId,
            org.omg.dds.DomainParticipantQos qos,
            org.omg.dds.DomainParticipantListener a_listener)
    {

        org.omg.dds.DomainParticipant ref = null;
        org.jacorb.dds.DomainParticipantImpl impl = new org.jacorb.dds.DomainParticipantImpl (domainId,
                                                                                              qos,
                                                                                              a_listener);
        impl.setORB (orb);
        impl.setPOA (poa);

        try
        {
            // get the root naming context

            ref = has_domainId (domainId);
            if (ref != null) return ref;
            org.omg.CORBA.Object objRef = orb.resolve_initial_references ("NameService");
            // Use NamingContextExt which is part of the Interoperable
            // Naming Service (INS) specification.
            NamingContextExt ncRef = NamingContextExtHelper.narrow (objRef);
            // get object reference from the servant (and implicitly register
            // it)
            org.omg.CORBA.Object oref = poa.servant_to_reference (impl);
            ref = org.omg.dds.DomainParticipantHelper.narrow (oref);

            if (ncRef != null)
            {
                // bind the Object Reference in Naming
                NameComponent path[] = ncRef.to_name (new Integer (domainId).toString ());
                ncRef.rebind (path, ref);
                allParticipant.add (ref);
                Consummer.add (ref);
            }
        }
        catch (Exception e)
        {
        }
        return ref;
    }

    /**
     * This operation deletes an existing DomainParticipant. This operation can
     * only be invoked if all domain entities belonging to the participant have
     * already been deleted.
     * 
     * @param a_participant
     * @return RETCODE_OK.value if succes Otherwise the error
     *         PRECONDITION_NOT_MET is returned.
     */
    public int delete_participant (org.omg.dds.DomainParticipant a_participant)
    {

        try
        {
            boolean delete_ok = ((DomainParticipantImpl) poa.reference_to_servant (a_participant)).isDeletable ();
            if (delete_ok)
            {
                org.omg.CORBA.Object objRef = orb.resolve_initial_references ("NameService");
                // Use NamingContextExt which is part of the Interoperable
                // Naming Service (INS) specification.
                NamingContextExt ncRef = NamingContextExtHelper.narrow (objRef);
                String id = new Integer (a_participant.get_domain_id ()).toString ();
                NameComponent path[] = ncRef.to_name (id);
                ncRef.unbind (path);
            }
            else
            {
                return RETCODE_PRECONDITION_NOT_MET.value;
            }
        }
        catch (Exception e)
        {
            System.err.println ("ERROR: " + e);
            e.printStackTrace (System.out);
        }

        return RETCODE_OK.value;
    }

    /**
     * @param domainId
     * @return a participant has a same domainId
     */
    public DomainParticipant has_domainId (int domainId)
    {
        Iterator it = allParticipant.iterator ();
        DomainParticipant temp;
        while (it.hasNext ())
        {
            temp = (DomainParticipant) it.next ();
            if (temp.get_domain_id () == domainId)
            {
                return temp;
            }
        }
        return null;
    }

    /**
     * @param domainId
     * @return a Participant has a same domaiId
     */
    public org.omg.dds.DomainParticipant lookup_participant (int domainId)
    {

        org.omg.dds.DomainParticipant ref = null;
        try
        {
            org.omg.CORBA.Object objRef = orb.resolve_initial_references ("NameService");
            // Use NamingContextExt instead of NamingContext. This is
            // part of the Interoperable naming Service.
            NamingContextExt ncRef = NamingContextExtHelper.narrow (objRef);
            ref = DomainParticipantHelper.narrow (ncRef.resolve_str (new Integer (domainId).toString ()));
        }

        catch (Exception e)
        {
            System.err.println ("ERROR: " + e);
            e.printStackTrace (System.out);
        }
        return ref;
    }

    /**
     * @param qos
     * @return
     */
    public int set_default_participant_qos (org.omg.dds.DomainParticipantQos qos)
    {

        this.defaultqos = qos;
        return 0;
    }

    public void get_default_participant_qos (
            org.omg.dds.DomainParticipantQosHolder qos)
    {

        qos.value = this.defaultqos;
    }

    /**
     * @return Returns the orb.
     */
    public org.omg.CORBA.ORB getOrb ()
    {
        return orb;
    }

    /**
     * @return Returns the poa.
     */
    public org.omg.PortableServer.POA getPoa ()
    {
        return poa;
    }
}
