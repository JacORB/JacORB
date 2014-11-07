/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2014 Gerald Brose / The JacORB Team.
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.TestUtils;
import org.jacorb.test.idl.AbstractIDLTestcase;
import org.omg.CORBA.Repository;
import org.omg.CORBA.RepositoryHelper;

/**
 * @author Alphonse Bendt
 */
public class IFRServerSetup
{
    protected ClientServerSetup clientServerSetup;

    class InitIDL extends AbstractIDLTestcase
    {
        private final List<String> arguments = new ArrayList<String>();

        public InitIDL (File file, String[] idlArgs) throws Exception
        {
            super (file);

            initLogging();

            if ( idlArgs != null)
            {
                arguments.addAll(Arrays.asList(idlArgs));
            }
        }

        public void init () throws Exception
        {
            arguments.addAll (Arrays.asList(new String[] {"-ir", "-forceOverwrite", "-d", dirGeneration.getAbsolutePath(),
                    idlFile.getAbsolutePath()
            }));

            runJacIDL(false);

            compileGeneratedSources(false);
        }

        public File getDirectory ()
        {
            return dirCompilation;
        }

        @Override
        protected String[] createJacIDLArgs()
        {
            String args[] = new String[arguments.size()];

            for(int x=0; x<arguments.size(); ++x)
            {
                args[x] = arguments.get(x);
            }

            return args;
        }
    };

    private static File getIDLFile(String fileName)
    {
        File result = new File(fileName);

        if ( ! result.isAbsolute())
        {
            result = new File(TestUtils.testHome() + "/src/test/idl/" + fileName);
        }
        System.out.println("using IDL " + (result.isDirectory() ? "dir" : "file") + " " + result);

        TestUtils.getLogger().debug("using IDL " + (result.isDirectory() ? "dir" : "file") + " " + result);

        return result;
    }

    public IFRServerSetup(String idlFile, String[] idlArgs, Properties optionalIRServerProps)
        throws Exception
    {
        Properties additionalProps = new Properties();
        if (optionalIRServerProps != null)
        {
            additionalProps.putAll(optionalIRServerProps);
        }

        InitIDL idlSetup = new InitIDL (getIDLFile(idlFile), idlArgs);
        idlSetup.init();

        File dirGeneration = idlSetup.getDirectory();
        final File iorFile = File.createTempFile("IFR_IOR", ".ior");
        iorFile.deleteOnExit();

        Properties serverProps = new Properties();
        serverProps.setProperty("jacorb.test.ir.classpath", dirGeneration.toString());
        serverProps.setProperty("jacorb.test.ir.iorfile", iorFile.toString());

        serverProps.putAll(additionalProps);

        Properties clientProps = new Properties();
        clientProps.setProperty("ORBInitRef.InterfaceRepository", "file://" + iorFile.toString());

        clientServerSetup = new ClientServerSetup(IRServerRunner.class.getName(), "ignored", clientProps, serverProps);

        TestUtils.getLogger().debug ("Waiting for IFR to start...");
        Thread.sleep (10000);
        TestUtils.getLogger().debug ("Done...");
    }

    public void tearDown() throws Exception
    {
        clientServerSetup.tearDown();
    }

    public Repository getRepository()
    {
        return RepositoryHelper.narrow(clientServerSetup.getServerObject());
    }
}
