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
import org.omg.dds.DataWriter;
import org.omg.dds.DataWriterListener;
import org.omg.dds.DataWriterQos;
import org.omg.dds.DataWriterQosHolder;
import org.omg.dds.DomainParticipant;
import org.omg.dds.Publisher;
import org.omg.dds.PublisherListener;
import org.omg.dds.PublisherPOA;
import org.omg.dds.PublisherQos;
import org.omg.dds.PublisherQosHolder;
import org.omg.dds.RETCODE_OK;
import org.omg.dds.RETCODE_PRECONDITION_NOT_MET;
import org.omg.dds.StatusCondition;
import org.omg.dds.Topic;
import org.omg.dds.TopicQos;

/**
 * A Publisher is the object responsible for the actual dissemination of publications.
 * The Publisher acts on the behalf of one or several DataWriter objects that belong to
 * it. When it is informed of a change to the data associated with one of its DataWriter
 * objects, it decides when it is appropriate to actually send the data-update message.
 * In making this decision, it considers any extra information that goes with the data
 * (timestamp, writer, etc.) as well as the QoS of the Publisher and the DataWriter.
 */
public class PublisherImpl extends PublisherPOA  {

	private org.omg.CORBA.ORB orb ;
	private org.omg.PortableServer.POA poa ;
	private Vector Vector_DataWriter ;
	private PublisherQos qos ;
	private DomainParticipant DP_Parent ;
	private DataWriterQos Default_DataWriterqos ;
	//put  message of publisher  in channel event
	private PublisherListener listner ;

	public PublisherImpl(PublisherQos qos ,PublisherListener listner , DomainParticipant DP ){
		this.qos = qos ;
		this.listner = listner ;
		Vector_DataWriter = new Vector() ;
		this.DP_Parent = DP ;
	}


	/**
	 * @param a_topic
	 * @param qos
	 * @param a_listener
	 * @return
	 */
	public DataWriter create_datawriter(Topic a_topic, DataWriterQos qos,
			DataWriterListener a_listener) {

		DataWriter DW = null ;
		Servant impl;

		try{
			Class type = Class.forName(a_topic.get_type_name()+"DataWriterImpl") ;
			Class typehelper = Class.forName(a_topic.get_type_name()+"DataWriterHelper") ;
			Class type_param_constructor [] = new  Class[6] ;
			Object valu_param_constructor [] = new Object[6] ;
			type_param_constructor[0] = DataWriterQos.class ;
			type_param_constructor[1] = DataWriterListener.class ;
			type_param_constructor[2] = Publisher.class ;
			type_param_constructor[3] = Topic.class ;
			type_param_constructor[4] = org.omg.CORBA.ORB .class ;
			type_param_constructor[5] = org.omg.PortableServer.POA.class ;
			valu_param_constructor[0] = qos ;
			valu_param_constructor[1] = a_listener ;
			valu_param_constructor[2] = this._this();
			valu_param_constructor[3] = a_topic;
			valu_param_constructor[4] = orb ;
			valu_param_constructor[5] =  poa ;
			impl = (Servant)type.getConstructor(type_param_constructor).newInstance(valu_param_constructor);

			org.omg.CORBA.Object oref = poa.servant_to_reference(impl);
			Class  type_param_narrow [] = new Class [1 ];
			org.omg.CORBA.Object valu_param_narrow [] = new org.omg.CORBA.Object[1];
			valu_param_narrow[0] = oref ;
			type_param_narrow[0] = Class.forName("org.omg.CORBA.Object") ;
			Method Narrow = typehelper.getMethod("narrow",type_param_narrow );
			DW = (DataWriter) Narrow.invoke(null, (Object[])valu_param_narrow);

			add( DW);
		}
		catch(Exception e){

			System.out.println(e);
			e.printStackTrace();
		}

		return DW;
	}

	/**
	 * @param a_datawriter
	 * @return
	 */
	public int delete_datawriter(DataWriter a_datawriter) {

		if(_this() ==   a_datawriter.get_publisher()){
			remove(a_datawriter);
			return RETCODE_OK.value ;
		}
		else return RETCODE_PRECONDITION_NOT_MET.value ;
	}

	/**
	 * @param topic_name
	 * @return
	 */
	public DataWriter lookup_datawriter(String topic_name) {
		Iterator It = Vector_DataWriter.iterator();
		DataWriter  temp ;
		while(It.hasNext()){
			temp = (DataWriter)It.next() ;
			if(temp.get_topic().get_name().equals(topic_name)) return temp ;
		}
		return null ;
	}

	/**
	 * Not Implemented
	 * @return
	 */
	public int delete_contained_entities() {
		return 0;
	}

	/**
	 * @param qos
	 * @return
	 */
	public int set_qos(PublisherQos qos) {
		this.qos = qos ;

		return RETCODE_OK.value ;
	}

	/**
	 * @param qos
	 */
	public void get_qos(PublisherQosHolder qos) {
		qos.value = this.qos ;
	}

	/**
	 * @param a_listener
	 * @param mask
	 * @return
	 */
	public int set_listener(PublisherListener a_listener, int mask) {
		this.listner = a_listener ;
		return RETCODE_OK.value ;
	}

	/**
	 * @return
	 */
	public PublisherListener get_listener() {
		return listner ;
	}

	/**
	 * Not Implemented
	 * @return
	 */
	public int suspend_publications() {
		return 0;
	}

	/**
	 * Not Implemented
	 * @return
	 */
	public int resume_publications() {
		return 0;
	}

	/**
	 * Not Implemented
	 * @return
	 */
	public int begin_coherent_changes() {
		return 0;
	}

	/**
	 * Not Implemented
	 * @return
	 */
	public int end_coherent_changes() {
		return 0;
	}

	/**
	 * @return
	 */
	public DomainParticipant get_participant() {
		return DP_Parent;
	}

	/**
	 * @param qos
	 * @return
	 */
	public int set_default_datawriter_qos(DataWriterQos qos) {
		this.Default_DataWriterqos = qos ;
		return RETCODE_OK.value;
	}

	/**
	 * @param qos
	 */
	public void get_default_datawriter_qos(DataWriterQosHolder qos) {
		qos.value = this.Default_DataWriterqos ;
	}

	/**
	 * @param a_datawriter_qos
	 * @param a_topic_qos
	 * @return
	 */
	public int copy_from_topic_qos(DataWriterQosHolder a_datawriter_qos,
			TopicQos a_topic_qos) {

		a_datawriter_qos.value.deadline = a_topic_qos.deadline ;
		a_datawriter_qos.value.destination_order = a_topic_qos.destination_order ;
		a_datawriter_qos.value.durability = a_topic_qos.durability ;
		a_datawriter_qos.value.history = a_topic_qos.history ;
		a_datawriter_qos.value.latency_budget = a_topic_qos.latency_budget ;
		a_datawriter_qos.value.liveliness = a_topic_qos.liveliness ;
		a_datawriter_qos.value.reliability = a_topic_qos.reliability ;
		a_datawriter_qos.value.resource_limits = a_topic_qos.resource_limits ;

		return RETCODE_OK.value;
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
	 * @return Returns the vector_DataWriter.
	 */
	public Vector getVector_DataWriter() {
		return Vector_DataWriter;
	}

	/**
	 * @param arg0
	 * @return
	 */
	public boolean add(Object arg0) {
		return Vector_DataWriter.add(arg0);
	}

	/**
	 * @param arg0
	 * @return
	 */
	public boolean remove(Object arg0) {
		return Vector_DataWriter.remove(arg0);
	}

	/**
	 * @return
	 */
	public boolean isDeletable(){
		return getVector_DataWriter().isEmpty();
	}

}
