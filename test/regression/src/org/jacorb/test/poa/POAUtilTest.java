package org.jacorb.test.poa;

import java.util.*;
import junit.framework.*;

import org.jacorb.poa.except.*;
import org.jacorb.poa.util.POAUtil;
import org.jacorb.poa.policy.ThreadPolicy;
import org.jacorb.poa.policy.LifespanPolicy;
import org.jacorb.poa.policy.IdUniquenessPolicy;
import org.jacorb.poa.policy.IdAssignmentPolicy;
import org.jacorb.poa.policy.ServantRetentionPolicy;
import org.jacorb.poa.policy.RequestProcessingPolicy;
import org.jacorb.poa.policy.ImplicitActivationPolicy;
import org.jacorb.test.common.*;
import org.omg.PortableServer.*;

/**
 * A unit test of several of the stand-alone methods in org.jacorb.poa.POAUtil.
 * @author Andre Spiegel spiegel@gnu.org
 * @version $Id$
 */
public class POAUtilTest extends JacORBTestCase
{

    public POAUtilTest (String name)
    {
        super (name);
    }
    
    public void test_mask_id_1()
    {
        do_mask_id ("abc", "abc");
    }
  
    public void test_mask_id_2()
    {
        do_mask_id ("a", "a");
    }

    public void test_mask_id_3()
    {
        do_mask_id ("", "");
    }

    public void test_mask_id_4()
    {
        do_mask_id ("a/b", "a&%b");
    }
    
    public void test_mask_id_5()
    {
        do_mask_id ("a//b", "a&%&%b");
    }
    
    public void test_mask_id_6()
    {
        do_mask_id ("a&b", "a&&b");
    }
    
    public void test_mask_id_7()
    {
        do_mask_id ("a&&b", "a&&&&b");
    }
    
    public void test_mask_id_8()
    {
        do_mask_id ("/", "&%");
    }
    
    public void test_mask_id_9()
    {
        do_mask_id ("&", "&&");
    }
    
    public void test_mask_id_10()
    {
        do_mask_id ("&%", "&&%");
    }
    
    public void test_unmask_id_1()
    {
        do_unmask_id ("abc", "abc");
    }
    
    public void test_unmask_id_2()
    {
        do_unmask_id ("a", "a");
    }
    
    public void test_unmask_id_3()
    {
        do_unmask_id ("", "");
    }
    
    public void test_unmask_id_4()
    {
        do_unmask_id ("a&%c", "a/c");
    }
    
    public void test_unmask_id_5()
    {
        do_unmask_id ("a&&c", "a&c");
    }
    
    public void test_unmask_id_6()
    {
        do_unmask_id ("a&%%c", "a/%c");
    }
    
    public void test_unmask_id_7()
    {
        do_unmask_id ("&&", "&");
    }
    
    public void test_unmask_id_8()
    {
        do_unmask_id ("&%", "/");
    }
    
    public void test_unmask_id_9()
    {
        do_unmask_id ("%&&%", "%&%");
    }
    
    public void test_unmask_id_10()
    {
        try
        {
            do_unmask_id ("&", "");
            fail ("illegal oid, should have raised an exception");
        }
        catch (ArrayIndexOutOfBoundsException ex)
        {
            // ok
        }
    }
    
    public void test_unmask_id_11()
    {
        try
        {
            do_unmask_id ("ab&cd", "");
            fail ("illegal oid, should have raised an exception");
        }
        catch (POAInternalError ex)
        {
            // ok
        }
    }
    
    public void test_convert_policy()
    {
        // ThreadPolicy
        assertEquals
        (
            "ORB_CTRL_MODEL",
            POAUtil.convert (null, THREAD_POLICY_ID.value)
        );
        assertEquals
        (
            "ORB_CTRL_MODEL",
            POAUtil.convert
            (
                new ThreadPolicy (ThreadPolicyValue.ORB_CTRL_MODEL),
                THREAD_POLICY_ID.value)
        );
        assertEquals
        (
            "SINGLE_THREAD_MODEL",
            POAUtil.convert
            (
                new ThreadPolicy (ThreadPolicyValue.SINGLE_THREAD_MODEL),
                THREAD_POLICY_ID.value)
        );
        
        // LifespanPolicy
        assertEquals
        (
            "TRANSIENT",
            POAUtil.convert (null, LIFESPAN_POLICY_ID.value)
        );
        assertEquals
        (
            "TRANSIENT",
            POAUtil.convert
            (
                new LifespanPolicy (LifespanPolicyValue.TRANSIENT),
                LIFESPAN_POLICY_ID.value)
        );
        assertEquals
        (
            "PERSISTENT",
            POAUtil.convert
            (
                new LifespanPolicy (LifespanPolicyValue.PERSISTENT),
                LIFESPAN_POLICY_ID.value)
        );
        
        // IdUniquenessPolicy
        assertEquals
        (
            "UNIQUE_ID",
            POAUtil.convert (null, ID_UNIQUENESS_POLICY_ID.value)
        );
        assertEquals
        (
            "UNIQUE_ID",
            POAUtil.convert
            (
                new IdUniquenessPolicy (IdUniquenessPolicyValue.UNIQUE_ID),
                ID_UNIQUENESS_POLICY_ID.value)
        );
        assertEquals
        (
            "MULTIPLE_ID",
            POAUtil.convert
            (
                new IdUniquenessPolicy (IdUniquenessPolicyValue.MULTIPLE_ID),
                ID_UNIQUENESS_POLICY_ID.value)
        );
        
        // IdAssignmentPolicy
        assertEquals
        (
            "SYSTEM_ID",
            POAUtil.convert (null, ID_ASSIGNMENT_POLICY_ID.value)
        );
        assertEquals
        (
            "SYSTEM_ID",
            POAUtil.convert
            (
                new IdAssignmentPolicy (IdAssignmentPolicyValue.SYSTEM_ID),
                ID_ASSIGNMENT_POLICY_ID.value)
        );
        assertEquals
        (
            "USER_ID",
            POAUtil.convert
            (
                new IdAssignmentPolicy (IdAssignmentPolicyValue.USER_ID),
                ID_ASSIGNMENT_POLICY_ID.value)
        );
        
        // ServantRetentionPolicy
        assertEquals
        (
            "RETAIN",
            POAUtil.convert (null, SERVANT_RETENTION_POLICY_ID.value)
        );
        assertEquals
        (
            "RETAIN",
            POAUtil.convert
            (
                new ServantRetentionPolicy (ServantRetentionPolicyValue.RETAIN),
                SERVANT_RETENTION_POLICY_ID.value)
        );
        assertEquals
        (
            "NON_RETAIN",
            POAUtil.convert
            (
                new ServantRetentionPolicy (ServantRetentionPolicyValue.NON_RETAIN),
                SERVANT_RETENTION_POLICY_ID.value)
        );
        
        // RequestProcessingPolicy
        assertEquals
        (
            "USE_ACTIVE_OBJECT_MAP_ONLY",
            POAUtil.convert (null, REQUEST_PROCESSING_POLICY_ID.value)
        );
        assertEquals
        (
            "USE_ACTIVE_OBJECT_MAP_ONLY",
            POAUtil.convert
            (
                new RequestProcessingPolicy (RequestProcessingPolicyValue.USE_ACTIVE_OBJECT_MAP_ONLY),
                REQUEST_PROCESSING_POLICY_ID.value)
        );
        assertEquals
        (
            "USE_SERVANT_MANAGER",
            POAUtil.convert
            (
                new RequestProcessingPolicy (RequestProcessingPolicyValue.USE_SERVANT_MANAGER),
                REQUEST_PROCESSING_POLICY_ID.value)
        );
        assertEquals
        (
            "USE_DEFAULT_SERVANT",
            POAUtil.convert
            (
                new RequestProcessingPolicy (RequestProcessingPolicyValue.USE_DEFAULT_SERVANT),
                REQUEST_PROCESSING_POLICY_ID.value)
        );
        
        // ImplicitActicationPolicy
        assertEquals
        (
            "NO_IMPLICIT_ACTIVATION",
            POAUtil.convert (null, IMPLICIT_ACTIVATION_POLICY_ID.value)
        );
        assertEquals
        (
            "NO_IMPLICIT_ACTIVATION",
            POAUtil.convert
            (
                new ImplicitActivationPolicy (ImplicitActivationPolicyValue.NO_IMPLICIT_ACTIVATION),
                IMPLICIT_ACTIVATION_POLICY_ID.value)
        );
        assertEquals
        (
            "IMPLICIT_ACTIVATION",
            POAUtil.convert
            (
                new ImplicitActivationPolicy (ImplicitActivationPolicyValue.IMPLICIT_ACTIVATION),
                IMPLICIT_ACTIVATION_POLICY_ID.value)
        );
                        
    }
    
    private void do_mask_id (byte[] input, byte[] expected)
    {
        byte[] result = POAUtil.maskId (input);
        assertArraysEqual (expected, result);
    }
    
    private void do_mask_id (String input, String expected)
    {
        do_mask_id (input.getBytes(), expected.getBytes());
    }
    
    private void do_unmask_id (byte[] input, byte[] expected)
    {
        byte[] result = POAUtil.unmaskId (input);
        assertArraysEqual (expected, result);
    }
    
    private void do_unmask_id (String input, String expected)
    {
        do_unmask_id (input.getBytes(), expected.getBytes());
    }
    
    private void assertArraysEqual (byte[] expected, byte[] result)
    {
        if (!Arrays.equals (expected, result))
        {    
            throw new AssertionFailedError
            (
                "expected: <" + byteArrayToString (expected) + ">, but was: <"
                + byteArrayToString (result) + ">"
            );
        }
    }
    
    private String byteArrayToString (byte[] data)
    {
        StringBuffer result = new StringBuffer();
        for (int i=0; i<data.length; i++)
        {
            result.append (Integer.toHexString (data[i]));
            if (i < data.length-1) result.append (' ');
        }
        return result.toString();
    }
}
