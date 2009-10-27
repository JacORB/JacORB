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

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Vector;
import org.omg.PortableServer.Servant;
import org.omg.dds.DataReader;
import org.omg.dds.DataReaderListener;
import org.omg.dds.DataReaderQos;
import org.omg.dds.DataReaderQosHolder;
import org.omg.dds.DataReaderSeqHolder;
import org.omg.dds.DomainParticipant;
import org.omg.dds.RETCODE_OK;
import org.omg.dds.RETCODE_PRECONDITION_NOT_MET;
import org.omg.dds.StatusCondition;
import org.omg.dds.Subscriber;
import org.omg.dds.SubscriberListener;
import org.omg.dds.SubscriberPOA;
import org.omg.dds.SubscriberQos;
import org.omg.dds.SubscriberQosHolder;
import org.omg.dds.Topic;
import org.omg.dds.TopicDescription;
import org.omg.dds.TopicHelper;
import org.omg.dds.TopicQos;

/**
 * A Subscriber is the object responsible for the actual reception of the data resulting
 * from its subscriptions. A Subscriber acts on the behalf of one or several DataReader
 * objects that are related to it. When it receives data (from the other parts of the
 * system), it builds the list of concerned DataReader objects, and then indicates to
 * the application that data is available, through its listener or by enabling related
 * conditions. The application can access the list of concerned DataReader objects
 * through the operation get_datareaders and then access the data available though
 * operations on the DataReader.
 */
public class SubscriberImpl extends SubscriberPOA {

	private org.omg.CORBA.ORB orb ;
	private org.omg.PortableServer.POA poa ;
	private Vector Vector_DataReaders ;
	private SubscriberQos qos ;
	private DomainParticipant DP_Parent ;
	private DataReaderQos Default_DataReaderqos ;
	// represent the data wrote by the datawriter
	private Object instance ;
	private SubscriberListener listner ;

	/**
	 * @param qos
	 * @param listner
	 * @param DP
	 */
	public SubscriberImpl(SubscriberQos qos ,SubscriberListener listner , DomainParticipant DP ){
		this.qos = qos ;
		this.listner = listner ;
		Vector_DataReaders = new Vector() ;
		this.DP_Parent = DP ;
	}

	/**
	 * @param a_topic
	 * @param qos
	 * @param a_listener
	 * @return
	 */
	public DataReader create_datareader(TopicDescription a_topic,
			DataReaderQos qos, DataReaderListener a_listener) {
		DataReader DR = null ;
		Servant impl;

		try{

			Class type = Class.forName(a_topic.get_type_name()+"DataReaderImpl") ;
			Class typehelper = Class.forName(a_topic.get_type_name()+"DataReaderHelper") ;
			Class type_param_constructor [] = new  Class[6] ;
			Object valu_param_constructor [] = new Object[6] ;
			type_param_constructor[0] = DataReaderQos.class ;
			type_param_constructor[1] = DataReaderListener.class ;
			type_param_constructor[2] = Subscriber.class ;
			type_param_constructor[3] = Topic.class ;
			type_param_constructor[4] = org.omg.CORBA.ORB .class ;
			type_param_constructor[5] = org.omg.PortableServer.POA.class ;
			valu_param_constructor[0] = qos ;
			valu_param_constructor[1] = a_listener ;
			valu_param_constructor[2] = this._this();
			valu_param_constructor[3] = TopicHelper.narrow(a_topic);
			valu_param_constructor[4] = orb ;
			valu_param_constructor[5] =  poa ;
			impl = (Servant)type.getConstructor(type_param_constructor).newInstance(valu_param_constructor);

			org.omg.CORBA.Object oref = poa.servant_to_reference(impl);
			Class  type_param_narrow [] = new Class [1 ];
			org.omg.CORBA.Object valu_param_narrow [] = new org.omg.CORBA.Object[1];
			valu_param_narrow[0] = oref ;
			type_param_narrow[0] = Class.forName("org.omg.CORBA.Object") ;
			Method Narrow = typehelper.getMethod("narrow",type_param_narrow );
			DR = (DataReader) Narrow.invoke(null, (Object[])valu_param_narrow);
			add( DR);

		}
		catch(Exception e){
			System.out.println("Eroor "+e);
			e.printStackTrace();
		}

		return DR;
	}

	/**
	 * @param a_datareader
	 * @return
	 */
	public int delete_datareader(DataReader a_datareader) {
		if(_this() ==   a_datareader.get_subscriber()){
			remove(a_datareader);
			return RETCODE_OK.value ;
		}
		else return RETCODE_PRECONDITION_NOT_MET.value ;
	}

	/**
	 * Not Implemented
	 * @return
	 */
	public int delete_contained_entities() {
		return 0;
	}

	/**
	 * @param topic_name
	 * @return
	 */
	public DataReader lookup_datareader(String topic_name) {
		Iterator It = Vector_DataReaders.iterator();
		DataReader  temp ;

		while(It.hasNext()){
			temp = (DataReader)It.next() ;
			if(temp.get_topicdescription().get_name().equals(topic_name)) {
				return temp ;
			}
		}

		return null ;
	}

	/**
	 * @param readers
	 * @param sample_states
	 * @param view_states
	 * @param instance_states
	 * @return
	 */
	public int get_datareaders(DataReaderSeqHolder readers, int sample_states,
			int view_states, int instance_states) {
		readers.value = (DataReader []) getVector_DataReaders().toArray() ;

		return RETCODE_OK.value ;
	}

	/**
	 * @param
	 * @return
	 */
	public void notify_datareaders() {
		Iterator It = Vector_DataReaders.iterator();
		DataReader  temp ;

		while(It.hasNext()){
			temp = (DataReader)It.next() ;
			temp.get_listener().on_data_available(temp);
		}
	}

	/**
	 * @param qos
	 * @return
	 */
	public int set_qos(SubscriberQos qos) {
		this.qos = qos ;

		return RETCODE_OK.value ;
	}

	/**
	 * @param qos
	 */
	public void get_qos(SubscriberQosHolder qos) {
		qos.value = this.qos ;
	}

	/**
	 * @param a_listener
	 * @param mask
	 * @return
	 */
	public int set_listener(SubscriberListener a_listener, int mask) {
		this.listner = a_listener ;

		return RETCODE_OK.value ;
	}

	/**
	 * @return
	 */
	public SubscriberListener get_listener() {
		return listner ;
	}

	/**
	 * Not Implemented
	 * @return
	 */
	public int begin_access() {
		return 0;
	}

	/**
	 * Not Implemented
	 * @return
	 */
	public int end_access() {
		return 0;
	}

	/**
	 * @return
	 */
	public DomainParticipant get_participant() {
		return getDP_Parent();
	}

	/**
	 * @param qos
	 * @return
	 */
	public int set_default_datareader_qos(DataReaderQos qos) {
		this.Default_DataReaderqos = qos ;
		return RETCODE_OK.value ;
	}

	/**
	 * @param qos
	 */
	public void get_default_datareader_qos(DataReaderQosHolder qos) {
		qos.value = this.Default_DataReaderqos ;

	}

	/**
	 * @param a_datareader_qos
	 * @param a_topic_qos
	 * @return
	 */
	public int copy_from_topic_qos(DataReaderQosHolder a_datareader_qos,
			TopicQos a_topic_qos) {

		 a_datareader_qos.value.deadline = a_topic_qos.deadline ;
		 a_datareader_qos.value.destination_order = a_topic_qos.destination_order ;
		 a_datareader_qos.value.durability = a_topic_qos.durability ;
		 a_datareader_qos.value.history = a_topic_qos.history ;
		 a_datareader_qos.value.latency_budget = a_topic_qos.latency_budget ;
		 a_datareader_qos.value.liveliness = a_topic_qos.liveliness ;
		 a_datareader_qos.value.reliability = a_topic_qos.reliability ;
		 a_datareader_qos.value.resource_limits = a_topic_qos.resource_limits ;

		 return RETCODE_OK.value ;
	}

	/**
	 * Not Implemented
	 * @return
	 */
	public int enable() {
		return 0;
	}

	/**
	 * Not Implemented
	 * @return
	 */
	public StatusCondition get_statuscondition() {
		return null;
	}

	/**
	 * Not Implemented
	 * @return
	 */
	public int get_status_changes() {
		return 0;
	}

	/**
	 * Not Implemented
	 * @return
	 */
	public boolean isDeletable(){
		return getVector_DataReaders().isEmpty();
	}

	/**
	 * @return Returns the vector_DataReaders.
	 */
	public Vector getVector_DataReaders() {
		return Vector_DataReaders;
	}

	/**
	 * @param arg0
	 * @return
	 */
	public boolean add(DataReader DR) {
		return Vector_DataReaders.add(DR);
	}

	/**
	 * @param arg0
	 * @return
	 */
	public boolean remove(DataReader DR) {
		return Vector_DataReaders.remove(DR);
	}

	/**
	 * @return Returns the dP_Parent.
	 */
	public DomainParticipant getDP_Parent() {
		return DP_Parent;
	}

	/**
	 * @param orb The orb to set.
	 */
	public void setORB(org.omg.CORBA.ORB orb) {
		this.orb = orb;
	}

	/**
	 * @param poa The poa to set.
	 */
	public void setPOA(org.omg.PortableServer.POA poa) {
		this.poa = poa;
	}

	/**
	 * @return Returns the instance.
	 */
	public Object getInstance() {
		return instance;
	}

	/**
	 * @param instance The instance to set.
	 */
	public void setInstance(Object instance) {
		this.instance = instance;

	}
}
