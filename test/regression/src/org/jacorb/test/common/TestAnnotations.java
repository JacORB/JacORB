package org.jacorb.test.common;

import junit.framework.*;

/**
 * @author Andre Spiegel spiegel@gnu.org
 * @version $Id$
 */
public class TestAnnotations
{
    private static JacORBVersionComparator jacorbVersionComparator =
        new JacORBVersionComparator();
    
    private String clientSince = null;
    private String serverSince = null;
    
    public TestAnnotations (String clientSince, String serverSince)
    {
        this.clientSince = clientSince;
        this.serverSince = serverSince;
    }
    
    public String getClientSince()
    {
        return clientSince;
    }
    
    public String getServerSince()
    {
        return serverSince;
    }

    public static TestAnnotations forTestCase (TestCase t)
    {
        TestAnnotationsParser p = TestAnnotationsParser.getInstance(t.getClass());
        String methodName = t.getName().replaceAll("\\(.+\\)", "");
        return p.getMethodAnnotations (methodName);
    }
    
    public static TestAnnotations forTestSuite (TestSuite t)
    {
        return TestAnnotationsParser.getInstance(t.getClass())
                                    .getClassAnnotations();
    }
    
    public static TestAnnotations forClass (Class c)
    {
        return TestAnnotationsParser.getInstance(c)
                                    .getClassAnnotations();
    }
    
    /**
     * Returns true if the entity annotated by these TestAnnotations
     * is applicable to the given client and server version.
     */
    public boolean isApplicableTo (String clientVersion, String serverVersion)
    {
        int clientCompare = jacorbVersionComparator.compare (clientVersion, 
                                                             clientSince);
        int serverCompare = jacorbVersionComparator.compare (serverVersion,
                                                             serverSince);
        return clientCompare >= 0 && serverCompare >= 0;
    }
    
}
