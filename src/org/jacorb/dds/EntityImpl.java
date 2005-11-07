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

/** 
 * This class is the abstract base class for all the DCPS objects that support QoS 
 * policies, a listener and a status condition.
 */
public abstract class EntityImpl extends org.omg.dds.EntityPOA {
	/* list of status */
	static int Topic_INCONSISTANT_TOPIC = 0 ;
	static int Subscriber_SAMPLE_LOST = 1 ,Subscriber_DATA_ON_READERS = 2 ;
	static int DataReader_SAMPLE_REJECTED = 3 , DataReader_LIVELINESS_CHANGED = 4 ,DataReader_REQUESTED_DEADLINE_MISSED = 5 ,DataReader_REQUESTED_INCOMPATIBLE_QOS = 6 ,DataReader_DATA_AVAILABLE = 7;
	static int DataWriter_LIVELINESS_LOST = 8 ,DataWriter_OFFERED_DEADLINE_MISSED = 9,DataWriter_OFFERED_INCOMPATIBLE_QOS = 10 ; 
	
	int Status[];
	boolean StatusChangedFlag[];			
	boolean enable ;
	
	public abstract int enable();
	
	/**
	 * Not Implemented
	 * @return
	 */	
	public org.omg.dds.StatusCondition get_statuscondition(){
		return null;
	}

	/**
	 * Not Implemented
	 * @return
	 */	
	public int get_status_changes(){		 
		return 0;
	}
}
