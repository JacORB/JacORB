package org.jacorb.test.orb.policies;

import java.util.*;
import org.omg.CORBA.*;
import org.omg.Messaging.*;
import org.omg.RTCORBA.*;
import org.omg.TimeBase.*;
import junit.framework.*;
import org.jacorb.test.common.*;


/**
 * Tests the various policy classes defined by JacORB.  This only tests the
 * handling of the policy objects, not the actual effect of these policies.
 * 
 * @author Andre Spiegel spiegel@gnu.org
 * @version $Id$
 */
public class PolicyHandlingTest extends JacORBTestCase
{
    private static org.omg.CORBA.ORB orb = null;
    
    public PolicyHandlingTest (String name)
    {
        super (name);
    }
    
    public static Test suite()
    {
        return new TestSuite (PolicyHandlingTest.class);
    }
    
    public void testClientProtocolPolicy()
    {
        Protocol[] protocols = new Protocol[] { };
        Any value = create_any();
        ProtocolListHelper.insert (value, protocols);
        ClientProtocolPolicy p = (ClientProtocolPolicy)create_policy
        (
            CLIENT_PROTOCOL_POLICY_TYPE.value,
            value
        );
        assertEquals (p.policy_type(), CLIENT_PROTOCOL_POLICY_TYPE.value);

        ClientProtocolPolicy p2 = (ClientProtocolPolicy)p.copy();

        // accessing the tag does not work when the protocols list is empty
        // assertEquals (((org.jacorb.orb.policies.ClientProtocolPolicy)p).tag(),
        //              ((org.jacorb.orb.policies.ClientProtocolPolicy)p2).tag());
        assertEquals (p.protocols(), p2.protocols());
        p.destroy();
        p2.destroy();
    }
    
    public void testMaxHopsPolicy()
    {
        Any value = create_any();
        value.insert_ushort((short)17);
        MaxHopsPolicy p = (MaxHopsPolicy)create_policy
        (
            org.omg.Messaging.MAX_HOPS_POLICY_TYPE.value,
            value
        );
        assertEquals (MAX_HOPS_POLICY_TYPE.value, p.policy_type());
        assertEquals (17, p.max_hops());
        
        MaxHopsPolicy p2 = (MaxHopsPolicy)p.copy();
        assertEquals (p.max_hops(), p2.max_hops());
        p.destroy();
        p2.destroy();
    }
    
    public void testQueueOrderPolicy()
    {
        Any value = create_any();
        value.insert_short((short)0xffff);
        QueueOrderPolicy p = (QueueOrderPolicy)create_policy
        (
            org.omg.Messaging.QUEUE_ORDER_POLICY_TYPE.value,
            value
        );
        assertEquals (QUEUE_ORDER_POLICY_TYPE.value, p.policy_type());
        assertEquals ((short)0xffff, p.allowed_orders());
        
        QueueOrderPolicy p2 = (QueueOrderPolicy)p.copy();
        assertEquals (p.allowed_orders(), p2.allowed_orders());
        p.destroy();
        p2.destroy();
    }
    
    public void testRebindPolicy()
    {
        Any value = create_any();
        value.insert_short((short)0xffff);
        RebindPolicy p = (RebindPolicy)create_policy
        (
            org.omg.Messaging.REBIND_POLICY_TYPE.value,
            value
        );
        assertEquals (REBIND_POLICY_TYPE.value, p.policy_type());
        assertEquals ((short)0xffff, p.rebind_mode());
        
        RebindPolicy p2 = (RebindPolicy)p.copy();
        assertEquals (p.rebind_mode(), p2.rebind_mode());
        p.destroy();
        p2.destroy();
    }
    
    public void testRelativeRequestTimeoutPolicy()
    {
        Any value = create_any();
        value.insert_ulonglong(123456789);
        RelativeRequestTimeoutPolicy p = (RelativeRequestTimeoutPolicy)create_policy
        (
            RELATIVE_REQ_TIMEOUT_POLICY_TYPE.value,
            value
        );
        assertEquals (RELATIVE_REQ_TIMEOUT_POLICY_TYPE.value, p.policy_type());
        assertEquals (123456789, p.relative_expiry());
        
        RelativeRequestTimeoutPolicy p2 = (RelativeRequestTimeoutPolicy)p.copy();
        assertEquals (p.relative_expiry(), p2.relative_expiry());
        p.destroy();
        p2.destroy();
    }
    
    public void testRelativeRoundtripTimeoutPolicy()
    {
        Any value = create_any();
        value.insert_ulonglong(123456789);
        RelativeRoundtripTimeoutPolicy p = (RelativeRoundtripTimeoutPolicy)create_policy
        (
            RELATIVE_RT_TIMEOUT_POLICY_TYPE.value,
            value
        );
        assertEquals (RELATIVE_RT_TIMEOUT_POLICY_TYPE.value, p.policy_type());
        assertEquals (123456789, p.relative_expiry());
        
        RelativeRoundtripTimeoutPolicy p2 = (RelativeRoundtripTimeoutPolicy)p.copy();
        assertEquals (p.relative_expiry(), p2.relative_expiry());
        p.destroy();
        p2.destroy();
    }
    
    public void testReplyEndTimePolicy()
    {
        UtcT time = new UtcT(12, 34, (short)56, (short)78);
        Any value = create_any();
        UtcTHelper.insert (value, time);
        ReplyEndTimePolicy p = (ReplyEndTimePolicy)create_policy
        (
            REPLY_END_TIME_POLICY_TYPE.value,
            value
        );
        assertEquals (REPLY_END_TIME_POLICY_TYPE.value, p.policy_type());
        UtcT outTime = p.end_time();
        assertEquals (time.time, outTime.time);
        assertEquals (time.inacchi, outTime.inacchi);
        assertEquals (time.inacclo, outTime.inacclo);
        assertEquals (time.tdf, outTime.tdf);
        
        ReplyEndTimePolicy p2 = (ReplyEndTimePolicy)p.copy();
        UtcT otherTime = p2.end_time();
        assertEquals (otherTime.time, outTime.time);
        assertEquals (otherTime.inacchi, outTime.inacchi);
        assertEquals (otherTime.inacclo, outTime.inacclo);
        assertEquals (otherTime.tdf, outTime.tdf);

        p.destroy();
        p2.destroy();
    }
    
    public void testReplyPriorityPolicy()
    {
        PriorityRange pr = new PriorityRange ((short)10, (short)20);
        Any value = create_any();
        PriorityRangeHelper.insert (value, pr);
        ReplyPriorityPolicy p = (ReplyPriorityPolicy)create_policy
        (
            REPLY_PRIORITY_POLICY_TYPE.value,
            value
        );
        assertEquals (REPLY_PRIORITY_POLICY_TYPE.value, p.policy_type());
        PriorityRange outPR = p.priority_range();
        assertEquals (pr.min, outPR.min);
        assertEquals (pr.max, outPR.max);
        
        ReplyPriorityPolicy p2 = (ReplyPriorityPolicy)p.copy();
        PriorityRange otherPR = p2.priority_range();
        assertEquals (outPR.min, otherPR.min);
        assertEquals (outPR.max, otherPR.max);
        
        p.destroy();
        p2.destroy();
    }
    
    public void testReplyStartTimePolicy()
    {
        UtcT time = new UtcT(12, 34, (short)56, (short)78);
        Any value = create_any();
        UtcTHelper.insert (value, time);
        ReplyStartTimePolicy p = (ReplyStartTimePolicy)create_policy
        (
            REPLY_START_TIME_POLICY_TYPE.value,
            value
        );
        assertEquals (REPLY_START_TIME_POLICY_TYPE.value, p.policy_type());
        UtcT outTime = p.start_time();
        assertEquals (time.time, outTime.time);
        assertEquals (time.inacchi, outTime.inacchi);
        assertEquals (time.inacclo, outTime.inacclo);
        assertEquals (time.tdf, outTime.tdf);
        
        ReplyStartTimePolicy p2 = (ReplyStartTimePolicy)p.copy();
        UtcT otherTime = p2.start_time();
        assertEquals (otherTime.time, outTime.time);
        assertEquals (otherTime.inacchi, outTime.inacchi);
        assertEquals (otherTime.inacclo, outTime.inacclo);
        assertEquals (otherTime.tdf, outTime.tdf);

        p.destroy();
        p2.destroy();
    }
    
    public void testRequestEndTimePolicy()
    {
        UtcT time = new UtcT(12, 34, (short)56, (short)78);
        Any value = create_any();
        UtcTHelper.insert (value, time);
        RequestEndTimePolicy p = (RequestEndTimePolicy)create_policy
        (
            REQUEST_END_TIME_POLICY_TYPE.value,
            value
        );
        assertEquals (REQUEST_END_TIME_POLICY_TYPE.value, p.policy_type());
        UtcT outTime = p.end_time();
        assertEquals (time.time, outTime.time);
        assertEquals (time.inacchi, outTime.inacchi);
        assertEquals (time.inacclo, outTime.inacclo);
        assertEquals (time.tdf, outTime.tdf);
        
        RequestEndTimePolicy p2 = (RequestEndTimePolicy)p.copy();
        UtcT otherTime = p2.end_time();
        assertEquals (otherTime.time, outTime.time);
        assertEquals (otherTime.inacchi, outTime.inacchi);
        assertEquals (otherTime.inacclo, outTime.inacclo);
        assertEquals (otherTime.tdf, outTime.tdf);

        p.destroy();
        p2.destroy();
    }
    
    public void testRequestPriorityPolicy()
    {
        PriorityRange pr = new PriorityRange ((short)10, (short)20);
        Any value = create_any();
        PriorityRangeHelper.insert (value, pr);
        RequestPriorityPolicy p = (RequestPriorityPolicy)create_policy
        (
            REQUEST_PRIORITY_POLICY_TYPE.value,
            value
        );
        assertEquals (REQUEST_PRIORITY_POLICY_TYPE.value, p.policy_type());
        PriorityRange outPR = p.priority_range();
        assertEquals (pr.min, outPR.min);
        assertEquals (pr.max, outPR.max);
        
        RequestPriorityPolicy p2 = (RequestPriorityPolicy)p.copy();
        PriorityRange otherPR = p2.priority_range();
        assertEquals (outPR.min, otherPR.min);
        assertEquals (outPR.max, otherPR.max);
        
        p.destroy();
        p2.destroy();
    }
    
    public void testRequestStartTimePolicy()
    {
        UtcT time = new UtcT(12, 34, (short)56, (short)78);
        Any value = create_any();
        UtcTHelper.insert (value, time);
        RequestStartTimePolicy p = (RequestStartTimePolicy)create_policy
        (
            REQUEST_START_TIME_POLICY_TYPE.value,
            value
        );
        assertEquals (REQUEST_START_TIME_POLICY_TYPE.value, p.policy_type());
        UtcT outTime = p.start_time();
        assertEquals (time.time, outTime.time);
        assertEquals (time.inacchi, outTime.inacchi);
        assertEquals (time.inacclo, outTime.inacclo);
        assertEquals (time.tdf, outTime.tdf);
        
        RequestStartTimePolicy p2 = (RequestStartTimePolicy)p.copy();
        UtcT otherTime = p2.start_time();
        assertEquals (otherTime.time, outTime.time);
        assertEquals (otherTime.inacchi, outTime.inacchi);
        assertEquals (otherTime.inacclo, outTime.inacclo);
        assertEquals (otherTime.tdf, outTime.tdf);

        p.destroy();
        p2.destroy();
    }
    
    public void testRoutingPolicy()
    {
        RoutingTypeRange rtr = new RoutingTypeRange ((short)10, (short)20);
        Any value = create_any();
        RoutingTypeRangeHelper.insert (value, rtr);
        RoutingPolicy p = (RoutingPolicy)create_policy
        (
            ROUTING_POLICY_TYPE.value,
            value
        );
        assertEquals (ROUTING_POLICY_TYPE.value, p.policy_type());
        RoutingTypeRange outRTR = p.routing_range();
        assertEquals (rtr.min, outRTR.min);
        assertEquals (rtr.max, outRTR.max);
        
        RoutingPolicy p2 = (RoutingPolicy)p.copy();
        RoutingTypeRange otherRTR = p2.routing_range();
        assertEquals (outRTR.min, otherRTR.min);
        assertEquals (outRTR.max, otherRTR.max);
        
        p.destroy();
        p2.destroy();
    }
    
    public void testSyncScopePolicy()
    {
        Any value = create_any();
        value.insert_short((short)0xffff);
        SyncScopePolicy p = (SyncScopePolicy)create_policy
        (
            org.omg.Messaging.SYNC_SCOPE_POLICY_TYPE.value,
            value
        );
        assertEquals (SYNC_SCOPE_POLICY_TYPE.value, p.policy_type());
        assertEquals ((short)0xffff, p.synchronization());
        
        SyncScopePolicy p2 = (SyncScopePolicy)p.copy();
        assertEquals (p.synchronization(), p2.synchronization());
        p.destroy();
        p2.destroy();
        
    }

    private org.omg.CORBA.ORB getORB()
    {
        if (orb == null)
        {
            Properties props = new Properties();
            props.put ("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
            props.put ("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");
            orb = ORB.init (new String[]{}, props);
        }
        return orb;
    }
    
    private Policy create_policy (int type, Any value)
    {
        try
        {
            return getORB().create_policy (type, value);
        }
        catch (PolicyError ex)
        {
            fail ("failed to create policy: " + ex);
            return null; // not reached
        }
    }

    private Any create_any()
    {
        return getORB().create_any();
    }
    
    private class DummyTCPProtocolProperties
        extends _TCPProtocolPropertiesLocalBase
    {
        public int send_buffer_size() { return 0; }
        public void send_buffer_size(int arg) {}
        public int recv_buffer_size() { return 0; };
        public void recv_buffer_size(int arg) {}
        public boolean keep_alive() { return false; }
        public void keep_alive(boolean arg) {};
        public boolean dont_route() { return false; }
        public void dont_route(boolean arg) {}
        public boolean no_delay() { return false; }
        public void no_delay(boolean arg) {}
        
    }
}
