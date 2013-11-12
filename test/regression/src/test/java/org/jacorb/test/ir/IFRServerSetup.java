/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2012 Gerald Brose / The JacORB Team.
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
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package org.jacorb.test.ir;

import java.io.File;
import java.util.Properties;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.CommonSetup;
import org.omg.CORBA.Repository;
import org.omg.CORBA.RepositoryHelper;

/**
 * @author Alphonse Bendt
 */
public class IFRServerSetup
{
    protected static IDLTestSetup idlSetup;
    protected ClientServerSetup clientServerSetup;


    public IFRServerSetup(String idlFile, String[] idlArgs, Properties optionalIRServerProps)
        throws Exception
    {
        Properties additionalProps = new Properties();
        if (optionalIRServerProps != null)
        {
            additionalProps.putAll(optionalIRServerProps);
        }

        idlSetup = new IDLTestSetup(idlFile, idlArgs);

        File dirGeneration = idlSetup.getDirectory();
        final File iorFile = File.createTempFile("IFR_IOR", ".ior");
        iorFile.deleteOnExit();

        Properties serverProps = new Properties();
        serverProps.setProperty(CommonSetup.JACORB_REGRESSION_DISABLE_IMR, "true");
        serverProps.setProperty("jacorb.test.ir.classpath", dirGeneration.toString());
        serverProps.setProperty("jacorb.test.ir.iorfile", iorFile.toString());

        serverProps.putAll(additionalProps);

        Properties clientProps = new Properties();
        clientProps.setProperty("ORBInitRef.InterfaceRepository", "file://" + iorFile.toString());
        clientProps.setProperty(CommonSetup.JACORB_REGRESSION_DISABLE_IMR, "true");

        clientServerSetup = new ClientServerSetup(IRServerRunner.class.getName(), "ignored", clientProps, serverProps);

        System.out.println ("Waiting for IFR to start...");
        Thread.sleep (10000);
        System.out.println ("Done...");
    }

    public void tearDown() throws Exception
    {
        clientServerSetup.tearDown();
        idlSetup.tearDown();
    }

    public Repository getRepository()
    {
        return RepositoryHelper.narrow(clientServerSetup.getServerObject());
    }
}
