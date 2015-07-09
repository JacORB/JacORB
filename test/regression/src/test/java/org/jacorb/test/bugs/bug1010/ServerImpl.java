package org.jacorb.test.bugs.bug1010;


import org.jacorb.config.Configurable;
import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;
import org.omg.PortableServer.POA;

/**
 * @author Alon Hessing
 */

public class ServerImpl
    extends MyServerPOA
    implements Configurable

{

    private static final int _delay = 1;
    private POA poa;

    private boolean shutdown;

    private Configuration config = null;

    public void configure (Configuration c)
    {
        config = c;

        poa = config.getORB().getRootPOA();
    }

    private void delay() {
        try {
            Thread.currentThread().sleep(_delay);
        } catch (InterruptedException i) {
        }
    }

    @Override
    public String[] arryfy(String s, int i) {
        String result[] = new String[i];
        for (int j = 0; j < i; j++) { result[j] = s; }
        delay();
        return result;
    }

    @Override
    public DummyServant createDummyServant() {
        try {
            DummyServantImpl dummyServant = new DummyServantImpl(poa);

            DummyServantPOATie servant = new DummyServantPOATie(dummyServant);

            byte[] oid =  poa.servant_to_id(servant);

            dummyServant.setOID(oid);

            return DummyServantHelper.narrow(poa.servant_to_reference(servant));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String writeMessage(String s) {
        System.out.println("Message from " + s);
        delay();
        return s + " written";
    }

    @Override
    public String writeMessages(String[] s, Observer _observer) {
        for (int i = 0; i < s.length; i++) { System.out.print("Message: " + s[i] + ", "); }

        delay();

        _observer.update1(_observer);
        _observer.update2();
        return "ok.";
    }

    @Override
    public void shutdown() {
        shutdown = true;
    }

    public boolean getShutdown() {
        return shutdown;
    }
}