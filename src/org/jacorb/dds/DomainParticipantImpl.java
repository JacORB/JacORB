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
import java.util.Iterator;
import java.util.Vector;
import org.omg.dds.ContentFilteredTopic;
import org.omg.dds.DomainParticipantListener;
import org.omg.dds.DomainParticipantPOA;
import org.omg.dds.DomainParticipantQos;
import org.omg.dds.DomainParticipantQosHolder;
import org.omg.dds.Duration_t;
import org.omg.dds.MultiTopic;
import org.omg.dds.Publisher;
import org.omg.dds.PublisherListener;
import org.omg.dds.PublisherQos;
import org.omg.dds.PublisherQosHolder;
import org.omg.dds.RETCODE_OK;
import org.omg.dds.RETCODE_PRECONDITION_NOT_MET;
import org.omg.dds.RETCODE_UNSUPPORTED;
import org.omg.dds.StatusCondition;
import org.omg.dds.Subscriber;
import org.omg.dds.SubscriberListener;
import org.omg.dds.SubscriberQos;
import org.omg.dds.SubscriberQosHolder;
import org.omg.dds.Topic;
import org.omg.dds.TopicDescription;
import org.omg.dds.TopicListener;
import org.omg.dds.TopicQos;
import org.omg.dds.TopicQosHolder;

/**
 * The DomainParticipant object plays several roles:
 * - It acts as a container for all other Entity objects
 * - It acts as factory for the Publisher, Subscriber, Topic and MultiTopic Entity objects.
 * - It represents the participation of the application on a communication plane that
 * 	 isolates applications running on the same set of physical computers from each other.
 *   A domain establishes a 'virtual network' linking all applications that share the
 *   same domainId9 and isolating them from applications running on different domains.
 *   In this way, several independent distributed applications can coexist in the same
 *   physical network without interfering, or even being aware of each other.
 * - It provides administration services in the domain, offering operations that allow the
 *   application to 'ignore' locally any information about a given participant
 */
public class DomainParticipantImpl extends DomainParticipantPOA {

    private org.omg.CORBA.ORB orb ;
    private org.omg.PortableServer.POA poa ;
    private int domainId ;
    private org.omg.dds.DomainParticipantQos qos;
    private org.omg.dds.DomainParticipantListener a_listener;
    private Vector Vector_Publisher ;
    private Vector Vector_Subscriber ;
    private Vector Vector_Topic ;


    /**
     * @param domainId
     * @param qos
     * @param a_listener
     */
    public DomainParticipantImpl(int domainId , org.omg.dds.DomainParticipantQos qos , org.omg.dds.DomainParticipantListener a_listener){

        this.domainId = domainId ;
        this.qos = qos ;
        this.a_listener = a_listener ;
        Vector_Publisher = new Vector() ;
        Vector_Subscriber = new Vector();
        Vector_Topic = new Vector();
    }

    /**
     * @param Pub
     */
    public void addPublisher(Publisher Pub){
        Vector_Publisher.add(Pub);
    }

    /**
     * @param Pub
     */
    public void deletePublisher(Publisher Pub){
         Vector_Publisher.remove(Pub);
    }

    /**
     * @param Sub
     */
    public void addsubscriber(Subscriber Sub){
        Vector_Subscriber.add(Sub);
    }

    /**
     * @param Sub
     */
    public void deleteSubscriber(Subscriber Sub){
         Vector_Subscriber.remove(Sub);
    }

    /**
     * @param Top
     */
    public void addTopic(Topic Top){
        Vector_Topic.add(Top);
    }

    /**
     * @param Top
     */
    public void deleteTopic(Topic Top){
         Vector_Topic.remove( Top);
    }

    /**
     *  Sets the pOA attribute of the RepertoryImpl object
     *
     *@param  poa  The new pOA value
     */
    public void setPOA(org.omg.PortableServer.POA poa) {
        this.poa = poa;
    }

    /**
     *  Sets the oRB attribute of the RepertoryImpl object
     *
     *@param  orb  The new oRB value
     */
    public void setORB(org.omg.CORBA.ORB orb) {
        this.orb = orb;
    }

    /**
     * @param qos
     * @param a_listener
     * @return
     */
    public Publisher create_publisher(PublisherQos qos,PublisherListener a_listener) {
        org.omg.dds.Publisher ref = null  ;
        PublisherImpl impl= new PublisherImpl(  qos,  a_listener  , _this() );
        impl.setORB(orb);
        impl.setPOA(poa);

        try{
            org.omg.CORBA.Object oref = poa.servant_to_reference(impl);
            ref = org.omg.dds.PublisherHelper.narrow(oref);
            addPublisher(ref);
        }
        catch(Exception e){
        }
        return ref ;
    }

    /**
     * @param p
     * @return
     */
    public int delete_publisher(Publisher p) {

        try{
            boolean delete_ok = ((PublisherImpl) poa.reference_to_servant(p)).isDeletable();
            if(delete_ok ){
                deletePublisher(p);
            }
            else {
                return RETCODE_PRECONDITION_NOT_MET.value ;
            }
        }
        catch(Exception e){

        }

         return RETCODE_OK.value ;
    }

    /**
     * @param qos
     * @param a_listener
     * @return
     */
    public Subscriber create_subscriber(SubscriberQos qos,
            SubscriberListener a_listener) {

        org.omg.dds.Subscriber ref = null  ;
        org.jacorb.dds.SubscriberImpl impl= new org.jacorb.dds.SubscriberImpl(  qos,  a_listener , _this());
        impl.setORB(orb);
        impl.setPOA(poa);
        try{
            org.omg.CORBA.Object oref = poa.servant_to_reference(impl);
            ref = org.omg.dds.SubscriberHelper.narrow(oref);
            addsubscriber(ref);
        }
        catch(Exception e){
        }
        return ref ;
    }

    /**
     * @param s
     * @return
     */
    public int delete_subscriber(Subscriber s) {

        try{
            boolean delete_ok = ((SubscriberImpl) poa.reference_to_servant(s)).isDeletable();
            if(delete_ok ){
                deleteSubscriber(s);
            }
            else {
                return RETCODE_PRECONDITION_NOT_MET.value ;
            }
        }
        catch(Exception e){

        }

         return RETCODE_OK.value ;
    }

    /**
     * Not Implemented
     * @return
     */
    public Subscriber get_builtin_subscriber() {
        return null;
    }

    /**
     * @param topic_name
     * @param type_name
     * @param qos
     * @param a_listener
     * @return
     */
    public Topic create_topic(String topic_name, String type_name,
            TopicQos qos, TopicListener a_listener) {

        org.omg.dds.Topic ref = null  ;
        org.jacorb.dds.TopicImpl impl= new org.jacorb.dds.TopicImpl( topic_name,type_name, qos,  a_listener,_this());
        impl.setORB(orb);
        impl.setPOA(poa);
        try{
            ref = (Topic)lookup_topicdescription(topic_name);
            //not exist another topic has same name
            if(ref == null){
                org.omg.CORBA.Object oref = poa.servant_to_reference(impl);
                ref = org.omg.dds.TopicHelper.narrow(oref);
                addTopic(ref);
            }
            else {
                if(!(ref.get_type_name().equals(type_name))){
                    ref = null  ;
                }
            }
        }
        catch(Exception e){
        }

        return ref ;
    }

    /**
     * @param a_topic
     * @return
     */
    public int delete_topic(Topic a_topic) {

        Iterator It = Vector_Subscriber.iterator() ;
        Subscriber temp ;
        while(It.hasNext()){
            temp = (Subscriber)It.next() ;
            if (temp.lookup_datareader(a_topic.get_name())!= null){

                return RETCODE_PRECONDITION_NOT_MET.value ;
            }
        }
        It = Vector_Publisher.iterator() ;
        Publisher pub ;
        while(It.hasNext()){
            pub = (Publisher)It.next() ;
            if (pub.lookup_datawriter(a_topic.get_name())!= null){

                return RETCODE_PRECONDITION_NOT_MET.value ;
            }
        }
        delete_topic(a_topic );
        return RETCODE_OK.value ;
    }

    public Topic find_topic(String topic_name, Duration_t timeout) {

        return null;
    }

    /**
     * @param name
     * @return
     */
    public TopicDescription lookup_topicdescription(String name) {

        TopicDescription topic  = null ,temp;
        Iterator Iter = Vector_Topic.iterator();
        while(Iter.hasNext()){
            temp = (TopicDescription)Iter.next();
            if(temp.get_name().equals(name)) topic = temp ;
        }

        return topic;
    }

    /**
     * Not Implemented
     * @return
     */
    public ContentFilteredTopic create_contentfilteredtopic(String name,
            Topic related_topic, String filter_expression,
            String[] filter_parameters) {
        return null;
    }

    /**
     * Not Implemented
     * @return
     */
    public int delete_contentfilteredtopic(
            ContentFilteredTopic a_contentfilteredtopic) {
        return 0;
    }

    /**
     * Not Implemented
     * @return
     */
    public MultiTopic create_multitopic(String name, String type_name,
            String subscription_expression, String[] expression_parameters) {
        return null;
    }

    /**
     * Not Implemented
     * @return
     */
    public int delete_multitopic(MultiTopic a_multitopic) {
        return 0;
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
    public int set_qos(DomainParticipantQos qos) {
        this.qos = qos ;
        return 0;
    }

    /**
     * @param qos
     */
    public void get_qos(DomainParticipantQosHolder qos) {
        qos.value = this.qos ;
    }

    /**
     * @param a_listener
     * @param mask
     * @return
     */
    public int set_listener(DomainParticipantListener a_listener, int mask) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * Not Implemented
     * @return
     */
    public DomainParticipantListener get_listener() {
        return null;
    }

    /**
     * Not Implemented
     * @return
     */
    public int ignore_participant(int handle) {
        return 0;
    }

    /**
     * Not Implemented
     * @return
     */
    public int ignore_topic(int handle) {
        return 0;
    }

    /**
     * Not Implemented
     * @return
     */
    public int ignore_publication(int handle) {
        return 0;
    }

    /**
     * Not Implemented
     * @return
     */
    public int ignore_subscription(int handle) {
        return 0;
    }

    /**
     * Not Implemented
     * @return
     */
    public int get_domain_id() {
        return this.domainId ;
    }

    /**
     * Not Implemented
     * @return
     */
    public void assert_liveliness() {
    }

    /**
     * Not Implemented
     * @return
     */
    public int set_default_publisher_qos(PublisherQos qos) {
        return 0;
    }

    /**
     * Not Implemented
     * @return
     */
    public void get_default_publisher_qos(PublisherQosHolder qos) {
    }

    /**
     * Not Implemented
     * @return
     */
    public int set_default_subscriber_qos(SubscriberQos qos) {
        return 0;
    }

    /**
     * Not Implemented
     * @return
     */
    public void get_default_subscriber_qos(SubscriberQosHolder qos) {
    }

    /**
     * Not Implemented
     * @return
     */
    public int set_default_topic_qos(TopicQos qos) {
        return 0;
    }

    /**
     * Not Implemented
     * @return
     */
    public void get_default_topic_qos(TopicQosHolder qos) {
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
        return RETCODE_UNSUPPORTED.value ;
    }

    /**
     * @return Returns the vector_Publisher.
     */
    public Vector getVector_Publisher() {
        return Vector_Publisher;
    }

    /**
     * @return Returns the vector_Subscriber.
     */
    public Vector getVector_Subscriber() {
        return Vector_Subscriber;
    }

    /**
     * @return Returns the vector_Topic.
     */
    public Vector getVector_Topic() {
        return Vector_Topic;
    }

    /**
     * @return
     */
    public boolean isDeletable(){
        return getVector_Publisher().isEmpty() && getVector_Subscriber().isEmpty()&& getVector_Topic().isEmpty() ;
    }
}
