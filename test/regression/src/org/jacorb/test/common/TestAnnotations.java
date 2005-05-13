package org.jacorb.test.common;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 2005  Gerald Brose.
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Library General Public
 *   License as published by the Free Software Foundation; either
 *   version 2 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this library; if not, write to the Free
 *   Software Foundation, 51 Franklin Street, Fifth Floor, Boston,
 *   MA 02110-1301, USA.
 */

import junit.framework.*;

/**
 * Represents JacORB-specific javadoc annotations for JUnit tests.
 * 
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
