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
package demo.dds.dcps.temperaturesample;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.dds.DataReader;
import org.omg.dds.DataReaderListener;
import org.omg.dds.DataReaderListenerHelper;
import org.omg.dds.DataReaderQos;
import org.omg.dds.DeadlineQosPolicy;
import org.omg.dds.DestinationOrderQosPolicyKind;
import org.omg.dds.DomainParticipant;
import org.omg.dds.DomainParticipantFactory;
import org.omg.dds.DomainParticipantFactoryHelper;
import org.omg.dds.DomainParticipantQos;
import org.omg.dds.DurabilityQosPolicy;
import org.omg.dds.DurabilityQosPolicyKind;
import org.omg.dds.Duration_t;
import org.omg.dds.EntityFactoryQosPolicy;
import org.omg.dds.GroupDataQosPolicy;
import org.omg.dds.LivelinessQosPolicyKind;
import org.omg.dds.PartitionQosPolicy;
import org.omg.dds.ReliabilityQosPolicyKind;
import org.omg.dds.Subscriber;
import org.omg.dds.SubscriberQos;
import org.omg.dds.TopicDataQosPolicy;
import org.omg.dds.TopicQos;
import org.omg.dds.UserDataQosPolicy;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

public class TemperatureConsumer implements Runnable {

    private String[] args;

    public static void main(String[] args) {

        TemperatureConsumer TPCons = new TemperatureConsumer();
        TPCons.setArgs(args);
        new Thread(TPCons).start();
    }

    /**
     * 
     */
    public void run() {
        try {
            // create and initialize the ORB

            ORB orb = ORB.init(args, null);
            POA poa = POAHelper.narrow(orb
                    .resolve_initial_references("RootPOA"));
            poa.the_POAManager().activate();
            DomainParticipantFactory domainparticipantFactory;
            DomainParticipant domainparticipant;
            Subscriber suscriber;
            TemperatureDataReader temperdatareader;
            DataReader datareader;
            org.omg.dds.Topic topic;
            SubscriberQos suscriberqos;
            DataReaderQos datareaderqos;
            org.omg.CORBA.Object objRef = orb
                    .resolve_initial_references("NameService");
            // Use NamingContextExt which is part of the Interoperable
            // Naming Service (INS) specification.
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            // resolve the Object Reference in Naming
            String rname = "DomainParticipantFactory";
            byte tab[] = new byte[1];
            tab[0] = 1;
            org.omg.dds.UserDataQosPolicy UDQP = new UserDataQosPolicy(tab);
            DomainParticipantQos DPQOS = new DomainParticipantQos(UDQP,
                    new EntityFactoryQosPolicy());
            domainparticipantFactory = DomainParticipantFactoryHelper
                    .narrow(ncRef.resolve_str(rname));
            TopicQos tq = new TopicQos(
                    new TopicDataQosPolicy(tab),
                    new DurabilityQosPolicy(
                            DurabilityQosPolicyKind.from_int(0),
                            new Duration_t(0, 0)),
                    new DeadlineQosPolicy(new Duration_t(0, 0)),
                    new org.omg.dds.LatencyBudgetQosPolicy(new Duration_t(0, 0)),
                    new org.omg.dds.LivelinessQosPolicy(LivelinessQosPolicyKind
                            .from_int(0), new Duration_t(0, 0)),
                    new org.omg.dds.ReliabilityQosPolicy(
                            ReliabilityQosPolicyKind.from_int(0),
                            new Duration_t(0, 0)),
                    new org.omg.dds.DestinationOrderQosPolicy(
                            DestinationOrderQosPolicyKind.from_int(0)),
                    new org.omg.dds.HistoryQosPolicy(
                            org.omg.dds.HistoryQosPolicyKind.from_int(0), 0),
                    new org.omg.dds.ResourceLimitsQosPolicy(0, 0, 0),
                    new org.omg.dds.TransportPriorityQosPolicy(0),
                    new org.omg.dds.LifespanQosPolicy(new Duration_t(0, 0)),
                    new org.omg.dds.OwnershipQosPolicy(
                            org.omg.dds.OwnershipQosPolicyKind.from_int(0)));
            domainparticipant = domainparticipantFactory.create_participant(0,
                    DPQOS, null);
            topic = domainparticipant.create_topic("tamperature",
                    "demo.dds.dcps.temperaturesample.Temperature", tq, null);
            String st[] = new String[1];
            st[0] = "";
            suscriberqos = new SubscriberQos(
                    new org.omg.dds.PresentationQosPolicy(
                            org.omg.dds.PresentationQosPolicyAccessScopeKind
                                    .from_int(0), false, false),
                    new PartitionQosPolicy(st), new GroupDataQosPolicy(tab),
                    new org.omg.dds.EntityFactoryQosPolicy(false));
            suscriber = domainparticipant.create_subscriber(suscriberqos, null);
            datareaderqos = new DataReaderQos(
                    new DurabilityQosPolicy(
                            DurabilityQosPolicyKind.from_int(0),
                            new Duration_t(0, 0)),
                    new DeadlineQosPolicy(new Duration_t(0, 0)),
                    new org.omg.dds.LatencyBudgetQosPolicy(new Duration_t(0, 0)),
                    new org.omg.dds.LivelinessQosPolicy(LivelinessQosPolicyKind
                            .from_int(0), new Duration_t(0, 0)),
                    new org.omg.dds.ReliabilityQosPolicy(
                            ReliabilityQosPolicyKind.from_int(0),
                            new Duration_t(0, 0)),
                    new org.omg.dds.DestinationOrderQosPolicy(
                            DestinationOrderQosPolicyKind.from_int(0)),
                    new org.omg.dds.HistoryQosPolicy(
                            org.omg.dds.HistoryQosPolicyKind.from_int(0), 0),
                    new org.omg.dds.ResourceLimitsQosPolicy(0, 0, 0),
                    new org.omg.dds.UserDataQosPolicy(tab),
                    new org.omg.dds.TimeBasedFilterQosPolicy(new Duration_t(0,
                            0)), new org.omg.dds.ReaderDataLifecycleQosPolicy(
                            new Duration_t(0, 0)));
            datareader = suscriber
                    .create_datareader(topic, datareaderqos, null);
            temperdatareader = TemperatureDataReaderHelper.narrow(datareader);
            DataReaderListener listener = DataReaderListenerHelper
                    .narrow(poa
                            .servant_to_reference(new TemperatureDataReaderListenerImpl(
                                    orb, poa)));
            temperdatareader.set_listener(listener, 0);
            orb.run();
        } catch (Exception e) {
            System.out.println(" ERROR : " + e);
            e.printStackTrace();
        }
    }

    /**
     * @param args The args to set.
     */
    public void setArgs(String[] args) {
        this.args = args;
    }

    public void end() {
        Thread.currentThread().destroy();
    }
}
