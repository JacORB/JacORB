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

import org.omg.dds.DomainParticipant;
import org.omg.dds.InconsistentTopicStatus;
import org.omg.dds.StatusCondition;
import org.omg.dds.TopicListener;
import org.omg.dds.TopicPOA;
import org.omg.dds.TopicQos;

/**
 * Topic is the most basic description of the data to be published and subscribed.
 * A Topic is identified by its name, which must be unique in the whole Domain. In addition 
 * (by virtue of extending TopicDescription) it fully specifies the type of the data
 * that can be communicated when publishing or subscribing to the Topic.Topic is the only 
 * TopicDescription that can be used for publications and therefore associated to a 
 * DataWriter.
 */
public class TopicImpl extends TopicPOA {
	
	private String topic_name;
	private String type_name;
	private TopicQos qos ;
	private TopicListener a_listener ;
	private DomainParticipant DP_Parent ;
	private org.omg.CORBA.ORB orb ;
	private org.omg.PortableServer.POA poa ;
	
	
	/**
	 * @return Returns the topic_name.
	 */
	public String getTopic_name() {
		return topic_name;
	}

	/**
	 * @return Returns the type_name.
	 */
	public String getType_name() {
		return type_name;
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
	 * @param topic_name
	 * @param type_name
	 * @param qos
	 * @param a_listener
	 * @param parent
	 */
	public TopicImpl(String topic_name, String type_name, TopicQos qos,
			TopicListener a_listener, DomainParticipant parent) {
		this.topic_name = topic_name;
		this.type_name = type_name;
		this.qos = qos;
		this.a_listener = a_listener;
		DP_Parent = parent;
	}

	/**
	 * Not Implemented
	 * @return
	 */	
	public InconsistentTopicStatus get_inconsistent_topic_status() {
		return null;
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
	 * @return
	 */
	public String get_type_name() {
		return getType_name();
	}

	/**
	 * @return
	 */
	public String get_name() {
		return getTopic_name();
	}

	/**
	 * @return
	 */
	public DomainParticipant get_participant() {
		return DP_Parent ;
	}

}
