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

import demo.dds.dcps.foosample.*;
import org.omg.dds.DataReader;
import org.omg.dds.DataReaderListenerPOA;
import org.omg.dds.LivelinessChangedStatus;
import org.omg.dds.RequestedDeadlineMissedStatus;
import org.omg.dds.RequestedIncompatibleQosStatus;
import org.omg.dds.SampleInfo;
import org.omg.dds.SampleInfoSeqHolder;
import org.omg.dds.SampleLostStatus;
import org.omg.dds.SampleRejectedStatus;
import org.omg.dds.SubscriptionMatchStatus;
import org.omg.dds.Time_t;


public class MessageDataReaderListenerImpl extends DataReaderListenerPOA {

    FooFrame fenetre ;
    
    public MessageDataReaderListenerImpl( ){
        fenetre = new FooFrame() ; 
        fenetre.setTitle("Message");
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
        
        MessageDataReader	messagedatareader = MessageDataReaderHelper.narrow(reader);        
        Message tab_message []  = new Message [1] ;
        SampleInfo tab_Sample [] = new SampleInfo[1] ;
        tab_Sample[0] = new SampleInfo(0,0,0,new Time_t(0,0),0,0,0,0,0,0);
        tab_message[0] = new Message("");
        MessageSeqHolder seqofmessage= new MessageSeqHolder(tab_message);
        SampleInfoSeqHolder seqofsample = new SampleInfoSeqHolder(tab_Sample);
        messagedatareader.take(seqofmessage,seqofsample,0,0,0,0);
        fenetre.SetText(seqofmessage.value[0].mess);
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
    
}
