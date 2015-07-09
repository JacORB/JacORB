package org.jacorb.test.bugs.bug1010;

/**
 * @author Alon Hessing
 */

class ClientThread
    extends Thread
    implements ObserverOperations
{

    private final DummyServant dummyServant;
    private int id;

    private Observer me;

    public ClientThread (DummyServant dummyServant, String _x, int _id)
    {
        id = _id;
        this.dummyServant = dummyServant;
        setDaemon(true);
    }


    @Override
    public void run()
    {
        System.out.println("ClientThread " + id + " starts");
        try
        {
            dummyServant.test();
        }
        catch( org.omg.CORBA.COMM_FAILURE cf )
        {
            System.out.println("Communication failure");
        }
        catch (Exception e)
        {
            System.out.println("Error...");
            e.printStackTrace();
//            System.exit(1);
        }
        System.out.println("thread exits...");
    }

    @Override
    public void update1( Observer o)
    {
        System.out.println("Client " + id + " update1");
        o.update2();
    }

    @Override
    public void update2()
    {
        System.out.println("Client " + id + " update2");
    }
}
