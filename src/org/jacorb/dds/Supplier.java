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
import org.omg.CORBA.Any;
import org.omg.CosEventChannelAdmin.EventChannel;
import org.omg.CosEventChannelAdmin.EventChannelHelper;
import org.omg.CosEventChannelAdmin.ProxyPushConsumer;
import org.omg.CosEventChannelAdmin.SupplierAdmin;
import org.omg.CosEventComm.PushSupplierPOA;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.dds.Topic;
import org.omg.dds.TopicHelper;

public class Supplier extends PushSupplierPOA {
    
    EventChannel e ;
    org.omg.CORBA.ORB orb ;
    org.omg.PortableServer.POA poa ;
    SupplierAdmin supplierAdmin ;
    ProxyPushConsumer proxyPushConsumer ;
    
    public  Supplier(  org.omg.CORBA.ORB orb ,
            org.omg.PortableServer.POA poa  )
    {
        e = null;
        this.orb = orb;
        this.poa = poa ;
        
        try
        {                        
            NamingContextExt nc =NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));            
            e = EventChannelHelper.narrow(nc.resolve( nc.to_name("eventchannel")));
            supplierAdmin = e.for_suppliers();
            proxyPushConsumer = supplierAdmin.obtain_push_consumer();
            proxyPushConsumer.connect_push_supplier( _this(orb) );                                   
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    public void disconnect_push_supplier (){
        System.out.println ("Supplier disconnected");
    }
    
    public synchronized void Write(Topic topic , java.lang.Object instance  ){    
        Class typehelper ;
        Class  type_param_insert [] = new Class [2 ];
        java.lang.Object valu_param_insert[] = new java.lang.Object [2];
        
        try
        {    
            Any any = orb.create_any();
            TopicHelper.insert(any,topic);
            proxyPushConsumer.push( any );               
            typehelper  = Class.forName(topic.get_type_name()+"Helper") ;            
            valu_param_insert[0] = any ;
            valu_param_insert[1] = instance ;
            type_param_insert[0] = Any.class ;
            type_param_insert[1] = Class.forName(topic.get_type_name()) ;
            Method Insert = typehelper.getMethod("insert",type_param_insert);
            Insert.invoke(null ,valu_param_insert);                  
            proxyPushConsumer.push( any );                                    
        }
        catch(Exception e )
        {            
            System.out.println("Exception : "+e);
            e.printStackTrace();
        }
    }   
}

