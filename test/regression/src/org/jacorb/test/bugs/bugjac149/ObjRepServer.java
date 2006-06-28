package org.jacorb.test.bugs.bugjac149;

import java.rmi.Remote;

import javax.rmi.CORBA.Stub;
import javax.rmi.CORBA.Util;

import org.jacorb.test.common.TestUtils;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Object;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.Servant;

/**
 * Test provided by Cisco
 */
public class ObjRepServer
{
    public static void main(String [] args) throws Exception
    {
        new ObjRepServer(args);
    }

    public ObjRepServer(String [] args) throws Exception
    {
        ORB orb;
        Object objref;
        IPing pinger;
        Stub stub;

        Remote remObj;

        orb = ORB.init(args, null);

        pinger = new PingImpl();

        remObj = new RemoteIPingImpl(pinger);

        POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));

        Servant servant = (Servant) Util.getTie(remObj);

        byte[] objID = rootPOA.activate_object (servant );

        rootPOA.the_POAManager().activate();

        stub = TestUtils.toStub(remObj, rootPOA.id_to_reference(objID), RemoteIPing.class);

        objref = stub._duplicate();

        System.out.println("SERVER IOR: " +  orb.object_to_string(objref));
        System.out.flush();

        orb.run();
    }
}
