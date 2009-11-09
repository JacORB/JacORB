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
import org.omg.PortableServer.POA;
import org.omg.dds.DataReader;
import org.omg.dds.DataReaderListenerPOA;
import org.omg.dds.DataWriter;
import org.omg.dds.DataWriterQos;
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
import org.omg.dds.LifespanQosPolicy;
import org.omg.dds.LivelinessChangedStatus;
import org.omg.dds.LivelinessQosPolicyKind;
import org.omg.dds.OwnershipStrengthQosPolicy;
import org.omg.dds.PartitionQosPolicy;
import org.omg.dds.Publisher;
import org.omg.dds.PublisherQos;
import org.omg.dds.ReliabilityQosPolicyKind;
import org.omg.dds.RequestedDeadlineMissedStatus;
import org.omg.dds.RequestedIncompatibleQosStatus;
import org.omg.dds.SampleInfo;
import org.omg.dds.SampleInfoSeqHolder;
import org.omg.dds.SampleLostStatus;
import org.omg.dds.SampleRejectedStatus;
import org.omg.dds.SubscriptionMatchStatus;
import org.omg.dds.Time_t;
import org.omg.dds.TopicDataQosPolicy;
import org.omg.dds.TopicQos;
import org.omg.dds.TransportPriorityQosPolicy;
import org.omg.dds.UserDataQosPolicy;

public class TemperatureDataReaderListenerImpl extends DataReaderListenerPOA {
    

    private TemperatureFrame fenetre ;
    private ORB orb ;
    private POA poa ;
    private MessageDataWriter messagedatawriter ;
    private static final double MIN = -15;
    private static final double MAX = 40;
     
    /**
     * @param orb
     * @param poa
     */
    public TemperatureDataReaderListenerImpl(ORB orb, POA poa) {
        fenetre = new TemperatureFrame(this) ; 
        this.orb = orb;
        this.poa = poa;
        init();
    }
    
	/**
	 * Not Implemented
	 */	    
    public void on_requested_deadline_missed(DataReader reader,
            RequestedDeadlineMissedStatus status) {      
    }
    
	/**
	 * Not Implemented
	 */	
    public void on_requested_incompatible_qos(DataReader reader,
            RequestedIncompatibleQosStatus status) {     
    }
    
	/**
	 * Not Implemented
	 */	
    public void on_sample_rejected(DataReader reader,
            SampleRejectedStatus status) {   
    }
    
	/**
	 * Not Implemented
	 */	
    public void on_liveliness_changed(DataReader reader,
            LivelinessChangedStatus status) {        
    }
    
    /**
     * @param reader
     */
    public void on_data_available(DataReader reader) {
        
        TemperatureDataReader	temperaturedatareader = TemperatureDataReaderHelper.narrow(reader);        
        Temperature  tab_temperature []  = new Temperature [1] ;
        SampleInfo tab_Sample [] = new SampleInfo[1] ;
        
        tab_Sample[0] = new SampleInfo(0,0,0,new Time_t(0,0),0,0,0,0,0,0);
        tab_temperature[0] = new Temperature(0,0,Unit.Celsius,0);
        
        TemperatureSeqHolder temperatureholder = new TemperatureSeqHolder(tab_temperature);
        SampleInfoSeqHolder seqofsample = new SampleInfoSeqHolder(tab_Sample);
        
        temperaturedatareader.take(temperatureholder,seqofsample,0,0,0,0);
        
        fenetre.SetText((int)temperatureholder.value[0].value);
        if(temperatureholder.value[0].value <= MIN || temperatureholder.value[0].value >= MAX  ){
            fenetre.setjButton1Visible(true);
        }
        fenetre.setVisible(true);
    }
    
	/**
	 * Not Implemented
	 */	
    public void on_subscription_match(DataReader reader,
            SubscriptionMatchStatus status) {  
    }
    
	/**
	 * Not Implemented
	 */	
    public void on_sample_lost(DataReader reader, SampleLostStatus status) {
    }
    
    /**
     * @param string
     */
    public void sendMessageForProducer(String message) {
        messagedatawriter.write(new Message(message),0) ;
    }
    
    private void init() {
        
        DomainParticipantFactory domainpartiFactory ;
        DomainParticipant domainparticipant ;
        org.omg.dds.Topic  topic ;
        Publisher publisher;
        DataWriter datawriter ;
        PublisherQos publisherqos ;
        DataWriterQos datawriterqos ;
        
        try {
	        org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
	        // Use NamingContextExt which is part of the Interoperable
	        // Naming Service (INS) specification.
	        NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
	        // resolve the Object Reference in Naming
	        String rname = "DomainParticipantFactory";
	        byte tab[ ] = new byte [1];
	        tab[0] = 1 ;
	        org.omg.dds.UserDataQosPolicy UDQP  = new UserDataQosPolicy(tab);
	        DomainParticipantQos DPQOS = new DomainParticipantQos(UDQP,new EntityFactoryQosPolicy()) ;
	        domainpartiFactory = DomainParticipantFactoryHelper.narrow(ncRef.resolve_str(rname));
	        TopicQos tq = new TopicQos(new TopicDataQosPolicy(tab),new DurabilityQosPolicy( DurabilityQosPolicyKind.from_int(0),new Duration_t(0,0)),new DeadlineQosPolicy(new Duration_t(0,0)),new org.omg.dds.LatencyBudgetQosPolicy(new Duration_t(0,0)),new org.omg.dds.LivelinessQosPolicy (LivelinessQosPolicyKind.from_int(0),new Duration_t(0,0)),new org.omg.dds.ReliabilityQosPolicy(ReliabilityQosPolicyKind.from_int(0),new Duration_t(0,0)),new org.omg.dds.DestinationOrderQosPolicy(DestinationOrderQosPolicyKind.from_int(0)),new org.omg.dds.HistoryQosPolicy(org.omg.dds.HistoryQosPolicyKind.from_int(0),0),new org.omg.dds.ResourceLimitsQosPolicy(0,0,0),new org.omg.dds.TransportPriorityQosPolicy(0),new org.omg.dds.LifespanQosPolicy (new Duration_t(0,0)),new org.omg.dds.OwnershipQosPolicy(org.omg.dds.OwnershipQosPolicyKind.from_int(0)));
	        domainparticipant = domainpartiFactory.create_participant(0,DPQOS,null);
	        topic = domainparticipant.create_topic("messsage","demo.dds.dcps.temperaturesample.Message",tq ,null);
	        
	        String  st  [] = new String [1];
	        st[0] = "" ;
	        publisherqos =  new PublisherQos(new org.omg.dds.PresentationQosPolicy( org.omg.dds.PresentationQosPolicyAccessScopeKind.from_int(0), false  ,false ), new PartitionQosPolicy(st),new GroupDataQosPolicy(tab),new org.omg.dds.EntityFactoryQosPolicy(false));
	        publisher = domainparticipant.create_publisher( publisherqos, null);
	        datawriterqos = new DataWriterQos(new DurabilityQosPolicy( DurabilityQosPolicyKind.from_int(0)
	                ,new Duration_t(0,0)),new DeadlineQosPolicy(new Duration_t(0,0))
	                ,new org.omg.dds.LatencyBudgetQosPolicy(new Duration_t(0,0))
	                ,new org.omg.dds.LivelinessQosPolicy (LivelinessQosPolicyKind.from_int(0),new Duration_t(0,0))
	                ,new org.omg.dds.ReliabilityQosPolicy(ReliabilityQosPolicyKind.from_int(0),new Duration_t(0,0))
	                ,new org.omg.dds.DestinationOrderQosPolicy(DestinationOrderQosPolicyKind.from_int(0))
	                ,new org.omg.dds.HistoryQosPolicy(org.omg.dds.HistoryQosPolicyKind.from_int(0),0)
	                ,new org.omg.dds.ResourceLimitsQosPolicy(0,0,0),new TransportPriorityQosPolicy(0)
	                ,new LifespanQosPolicy(new Duration_t(0,0)), new org.omg.dds.UserDataQosPolicy(tab) 
	                , new OwnershipStrengthQosPolicy(0) 
	                , new org.omg.dds.WriterDataLifecycleQosPolicy(true));
	        
	        datawriter = publisher. create_datawriter(topic,datawriterqos,null);
	        messagedatawriter = MessageDataWriterHelper.narrow(datawriter);
        }
        catch(Exception e){
            e.printStackTrace() ;
        }
    }
}
