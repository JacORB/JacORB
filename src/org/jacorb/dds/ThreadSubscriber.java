
/*
 *  DDS (Data Distribution Service) for JacORB
 *
 * Copyright (C) 2005  , Ahmed yehdih <ahmed.yehdih@gmail.com>, fouad
 allaoui <fouad.allaoui@gmail.com>, Didier Donsez (didier.donsez@ieee.org)
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
 * You should have received a copy of the GNU Library General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 02111-1307, USA.
 *
 * Coontact: Ahmed yehdih <ahmed.yehdih@gmail.com>, fouad allaoui
 <fouad.allaoui@gmail.com>, Didier Donsez (didier.donsez@ieee.org)
 * Contributor(s):
 *
 **/

package org.jacorb.dds;


import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Vector;
import org.omg.CORBA.Any;
import org.omg.CosEventChannelAdmin.ConsumerAdmin;
import org.omg.CosEventChannelAdmin.EventChannel;
import org.omg.CosEventChannelAdmin.EventChannelHelper;
import org.omg.CosEventChannelAdmin.ProxyPushSupplier;
import org.omg.CosEventComm.PushConsumer;
import org.omg.CosEventComm.PushConsumerHelper;
import org.omg.CosEventComm.PushConsumerOperations;
import org.omg.CosEventComm.PushConsumerPOATie;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.dds.DataReader;
import org.omg.dds.DomainParticipant;
import org.omg.dds.Subscriber;
import org.omg.dds.Topic;
import org.omg.dds.TopicHelper;

public class ThreadSubscriber extends Thread  implements PushConsumerOperations{   
        
    private  EventChannel ecs ;
    private ConsumerAdmin ca ;
    private PushConsumer pushConsumer ;
    private ProxyPushSupplier pps ;
    private  org.omg.PortableServer.POA poa ;
    private Vector references_domaines_participant     ;
    private NamingContextExt nc ;
    //all subscriber interested of topic 
    private Vector  all_Sub ;
    private Topic topic ;
    private  org.omg.CORBA.ORB orb = null;
    
    public ThreadSubscriber( org.omg.CORBA.ORB orb , org.omg.PortableServer.POA   poa )
    {
        ecs = null;
        ca = null;
        pushConsumer = null;
        pps = null;
        references_domaines_participant = new Vector();
        all_Sub = new Vector();
        
        try
        {                       
            this.orb = orb;
            this.poa = poa;
            NamingContextExt nc =  NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));
            ecs = EventChannelHelper.narrow(nc.resolve(nc.to_name("eventchannel")));
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        
        ca  = ecs.for_consumers();
        pps = ca.obtain_push_supplier();             
    }   
    
    public void disconnect_push_consumer(){
        System.out.println("Consumer disconnected.");
    }
    
    public   void run  (){
        
        try 
        {
            PushConsumerPOATie pt = new PushConsumerPOATie( this);
            pt._this_object(orb);
            pushConsumer = PushConsumerHelper.narrow(poa.servant_to_reference(pt) );
            pps.connect_push_consumer( pushConsumer );
            System.out.println("PushConsumerImpl registered.");
            orb.run();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }        
        
        System.out.println("Quit.");
    }
    
    /*
     * lookup the interested dataReader   by topic name 
     */
    public void Lookup_Sub_interesded_of_topic(String topic_name){
        
        org.omg.dds.DomainParticipant  domain_temp ;
        DomainParticipantImpl domain_impl ;
        Subscriber sub_temp ;
        SubscriberImpl sub_impl ;
        Iterator _it_DomainParticipant = references_domaines_participant.iterator();
        Iterator _it_Subscriber ;
        
        while(_it_DomainParticipant.hasNext()){
            
            try{
                domain_temp = (DomainParticipant)_it_DomainParticipant.next();
                domain_impl =(DomainParticipantImpl) poa.reference_to_servant(domain_temp);
                _it_Subscriber = domain_impl.getVector_Subscriber().iterator() ;
                
                while(_it_Subscriber.hasNext()){                    
                    sub_temp =  (Subscriber) _it_Subscriber.next();
                    if(sub_temp.lookup_datareader(topic_name) != null){                        
                        all_Sub.add(sub_temp);                    
                    }                    
                }
            }
            catch(Exception e){
                System.out.println("Exception "+e);
                e.printStackTrace();
            }
        }	               
    }
    
    public synchronized void push(org.omg.CORBA.Any data)
    throws org.omg.CosEventComm.Disconnected
    {
        
        Object instance = null  ;
        boolean is_topic= false ;
        boolean is_instance = false ;
        Subscriber sub_temp ;
        SubscriberImpl sub_impl_temp ;
        DataReader DR ;
        Class typehelper = null ;
        Class  type_param_extract [] = new Class [1 ];
        java.lang.Object valu_param_extract[] = new java.lang.Object[1];
        Iterator It ;
        
        if(data.type().equal(TopicHelper.type()))
        {                        
            is_topic = true ;
            topic = TopicHelper.extract(data) ;           
        }
        else 
        {            
            is_instance = true ;
            valu_param_extract[0] = data ;
            type_param_extract[0] = Any.class ;
            
            try{
                typehelper  = Class.forName(topic.get_type_name()+"Helper") ;
                Method extract = typehelper.getMethod("extract",type_param_extract);
                instance = extract.invoke(null ,valu_param_extract);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }          
        }
        
        if(is_topic){            
            Lookup_Sub_interesded_of_topic(topic.get_name());           
        }
        else if( is_instance)
        {           
            try{                
                It = all_Sub.iterator() ;
                while(It.hasNext()){
                    sub_temp = (Subscriber)It.next() ;
                    sub_impl_temp = (SubscriberImpl)poa.reference_to_servant(sub_temp);
                    sub_impl_temp.setInstance(instance);                    
                    DR = sub_temp.lookup_datareader(topic.get_name());                    
                    DR.take_instance_from_subscriber() ;
                    
                    if(DR.get_listener()!= null ){                        
                        DR.get_listener().on_data_available(DR);                        
                    }                                                           
                }
                all_Sub.removeAllElements();                                
            }
            catch (Exception e){
                System.out.println("Exep = "+e);
                e.printStackTrace();
            }
        }
    }
    
    /**
     * @param arg0
     * @return
     */
    public boolean add(Object arg0) {
        
        return references_domaines_participant.add(arg0);
    }
}

