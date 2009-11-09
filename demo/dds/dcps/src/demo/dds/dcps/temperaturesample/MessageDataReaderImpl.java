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

import java.util.Iterator;
import java.util.Vector;

import org.jacorb.dds.SubscriberImpl;
import org.omg.dds.DataReaderListener;
import org.omg.dds.DataReaderQos;
import org.omg.dds.DataReaderQosHolder;
import org.omg.dds.Duration_t;
import org.omg.dds.InstanceHandleSeqHolder;
import org.omg.dds.LivelinessChangedStatus;
import org.omg.dds.PublicationBuiltinTopicDataHolder;
import org.omg.dds.QueryCondition;
import org.omg.dds.RETCODE_OK;
import org.omg.dds.ReadCondition;
import org.omg.dds.RequestedDeadlineMissedStatus;
import org.omg.dds.RequestedIncompatibleQosStatus;
import org.omg.dds.SampleInfoHolder;
import org.omg.dds.SampleInfoSeqHolder;
import org.omg.dds.SampleLostStatus;
import org.omg.dds.SampleRejectedStatus;
import org.omg.dds.StatusCondition;
import org.omg.dds.Subscriber;
import org.omg.dds.SubscriptionMatchStatus;
import org.omg.dds.Topic;
import org.omg.dds.TopicDescription;


public class MessageDataReaderImpl extends MessageDataReaderPOA {
    
    // represent the data stockage space of a dataReader
    private Vector Vector_Message ;
    private DataReaderQos qos;
    private DataReaderListener a_listener ;
    private Subscriber SubParent ;
    private Topic topic ;
    private org.omg.CORBA.ORB orb ;
    private org.omg.PortableServer.POA poa ;
    private DataReaderListener _listener ;

    /**
     * @param qos
     * @param a_listener
     * @param subParent
     * @param topic_name
     */
    public MessageDataReaderImpl(DataReaderQos qos, DataReaderListener a_listener,
            Subscriber subParent, Topic topic,org.omg.CORBA.ORB orb,org.omg.PortableServer.POA poa) {
        this.qos = qos;
        this.a_listener = a_listener;
        SubParent = subParent;
        this.topic = topic;
        Vector_Message = new Vector();
        this.orb = orb ;
        this.poa = poa ;
    }

    /**
     * @param received_data
     * @param info_seq
     * @param max_samples
     * @param sample_states
     * @param view_states
     * @param instance_states
     * @return
     */
    public int read(MessageSeqHolder received_data, SampleInfoSeqHolder info_seq,
            int max_samples, int sample_states, int view_states,
            int instance_states) {    
        
        try{           
            int i = 0 ;
            Message tab [] = new Message [getVector_Message().size()] ;
            Iterator it = getVector_Message().iterator() ;
        
            while(it.hasNext()){                
                tab[i] = (Message)it.next() ;
                i++ ;
            }
            received_data.value = tab ;                        
        }
        catch(Exception e){
            System.out.println("Error e = "+e);
            e.printStackTrace();
        }
        
        return RETCODE_OK.value ;
    }
    
    /**
     * @param received_data
     * @param info_seq
     * @param max_samples
     * @param sample_states
     * @param view_states
     * @param instance_states
     * @return
     */
    public int take(MessageSeqHolder received_data, SampleInfoSeqHolder info_seq,
            int max_samples, int sample_states, int view_states,
            int instance_states) {        
        
        int i = 0 ;
        Message tab [] = new Message [getVector_Message().size()] ;
        Iterator it = getVector_Message().iterator() ;
        
        while(it.hasNext()){           
            tab[i] = (Message)it.next() ;
            i++ ;
        }
        received_data.value = tab ;
        // remove all element from the vector 
        clear();
        
        return RETCODE_OK.value;
    }
    
	/**
	 * Not Implemented
	 */	
    public int read_w_condition(MessageSeqHolder received_data,
            SampleInfoSeqHolder info_seq, int max_samples,
            ReadCondition a_condition) {
        return 0;
    }
	
	/**
	 * Not Implemented
	 */	
    public int take_w_condition(MessageSeqHolder received_data,
            SampleInfoSeqHolder info_seq, int max_samples,
            ReadCondition a_condition) {
        return 0;
    }
	
	/**
	 * Not Implemented
	 */	
    public int read_next_sample(MessageHolder received_data,
            SampleInfoHolder sample_info) {
        return 0;
    }
	
	/**
	 * Not Implemented
	 */	
    public int take_next_sample(MessageHolder received_data,
            SampleInfoHolder sample_info) {
        return 0;
    }
	
	/**
	 * Not Implemented
	 */	
    public int read_instance(MessageSeqHolder received_data,
            SampleInfoSeqHolder info_seq, int max_samples, int a_handle,
            int sample_states, int view_states, int instance_states) {
        return 0;
    }
	
	/**
	 * Not Implemented
	 */	
    public int take_instance(MessageSeqHolder received_data,
            SampleInfoSeqHolder info_seq, int max_samples, int a_handle,
            int sample_states, int view_states, int instance_states) {
        return 0;
    }
	
	/**
	 * Not Implemented
	 */	
    public int read_next_instance(MessageSeqHolder received_data,
            SampleInfoSeqHolder info_seq, int max_samples, int a_handle,
            int sample_states, int view_states, int instance_states) {
        return 0;
    }
	
	/**
	 * Not Implemented
	 */	
    public int take_next_instance(MessageSeqHolder received_data,
            SampleInfoSeqHolder info_seq, int max_samples, int a_handle,
            int sample_states, int view_states, int instance_states) {
        return 0;
    }
	
	/**
	 * Not Implemented
	 */	
    public int read_next_instance_w_condition(MessageSeqHolder received_data,
            SampleInfoSeqHolder info_seq, int max_samples, int a_handle,
            ReadCondition a_condition) {
        return 0;
    }
	
	/**
	 * Not Implemented
	 */	
    public int take_next_instance_w_condition(MessageSeqHolder received_data,
            SampleInfoSeqHolder info_seq, int max_samples, int a_handle,
            ReadCondition a_condition) {
        return 0;
    }
	
	/**
	 * Not Implemented
	 */	
    public int return_loan(MessageSeqHolder received_data,
            SampleInfoSeqHolder info_seq) {
        return 0;
    }
	
	/**
	 * Not Implemented
	 */	
    public int get_key_value(MessageHolder key_holder, int handle) {
        return 0;
    }
	
	/**
	 * Not Implemented
	 */	
    public ReadCondition create_readcondition(int sample_states,
            int view_states, int instance_states) {
        return null;
    }
	
	/**
	 * Not Implemented
	 */	
    public QueryCondition create_querycondition(int sample_states,
            int view_states, int instance_states, String query_expression,
            String[] query_parameters) {
        return null;
    }
	
	/**
	 * Not Implemented
	 */	
    public int delete_readcondition(ReadCondition a_condition) {
        return 0;
    }
	
	/**
	 * Not Implemented
	 */	
    public int delete_contained_entities() {
        return 0;
    }
	
	/**
	 * Not Implemented
	 */	
    public int set_qos(DataReaderQos qos) {
        return 0;
    }
	
	/**
	 * Not Implemented
	 */	
    public void get_qos(DataReaderQosHolder qos) {        
    }
	
    /**
     * @param a_listener
     * @param mask
     * @return
     */
    public int set_listener(org.omg.dds.DataReaderListener a_listener, int mask) {
        
        try{    
            this._listener = a_listener;
        }
        catch(Exception e){
            System.out.println(" Exception e :"+e);
            e.printStackTrace();
        }        
        return 0;
    }
    
    /**
     * @return _listener
     */
    public DataReaderListener get_listener() {               
        return _listener;  
    }
    
    /**
     * @return _topic
     */
    public TopicDescription get_topicdescription() {
        return topic ;
    }
    
	/**
	 * Not Implemented
	 */	
    public Subscriber get_subscriber() {
        return null;
    }
    
	/**
	 * Not Implemented
	 */	
    public SampleRejectedStatus get_sample_rejected_status() {
        return null;
    }
    
	/**
	 * Not Implemented
	 */	
    public LivelinessChangedStatus get_liveliness_changed_status() {

        return null;
    }
    
	/**
	 * Not Implemented
	 */	
    public RequestedDeadlineMissedStatus get_requested_deadline_missed_status() {
        return null;
    }
    
	/**
	 * Not Implemented
	 */	
    public RequestedIncompatibleQosStatus get_requested_incompatible_qos_status() {
        return null;
    }
    
	/**
	 * Not Implemented
	 */	
    public SubscriptionMatchStatus get_subscription_match_status() {
        return null;
    }
    
	/**
	 * Not Implemented
	 */	
    public SampleLostStatus get_sample_lost_status() {
        return null;
    }
    
	/**
	 * Not Implemented
	 */	
    public int wait_for_historical_data(Duration_t max_wait) {
        return 0;
    }
    
	/**
	 * Not Implemented
	 */	
    public int get_matched_publications(
            InstanceHandleSeqHolder publication_handles) {
        return 0;
    }
    
	/**
	 * Not Implemented
	 */	
    public int get_matched_publication_data(
            PublicationBuiltinTopicDataHolder publication_data,
            int publication_handle) {
        return 0;
    }
    
	/**
	 * Not Implemented
	 */	
    public int enable() {
        return 0;
    }
    
	/**
	 * Not Implemented
	 */	
    public StatusCondition get_statuscondition() {
        return null;
    }
    
	/**
	 * Not Implemented
	 */	
    public int get_status_changes() {
        return 0;
    }
    
    
    /**
     * @param arg0
     * @return
     */
    public boolean add(Object arg0) {
        return Vector_Message.add(arg0);
    }

    public void clear() {
        Vector_Message.clear();
    }
    /**
     * @param arg0
     * @return
     */
    public boolean remove(Object arg0) {
        return Vector_Message.remove(arg0);
    }
    /**
     * @return Returns the vector_Message.
     */
    public Vector getVector_Message() {
        return Vector_Message;
    }
    /**
     * @return Returns the a_listener.
     */
    public DataReaderListener getA_listener() {
        return a_listener;
    }
    /**
     * @return Returns the qos.
     */
    public DataReaderQos getQos() {
        return qos;
    }
    /**
     * @return Returns the subParent.
     */
    public Subscriber getSubParent() {
        return SubParent;
    }
        
    /**
     * take instance of Message from suscriber
     */    
    public void take_instance_from_subscriber()
    {
        try
        {            
            SubscriberImpl Subscriber_Parent  = (SubscriberImpl)_poa().reference_to_servant(getSubParent())   ;
            add((Message)Subscriber_Parent.getInstance());
        }	
        
        catch(Exception e )
        {
            
        }   
    }
}
