package demo.mtclient;

class ClientThread 
    extends Thread 
    implements ObserverOperations
{
    private MyServer srv;
    private String msg;
    private int id;
    private int time;

    private Observer me;

    public ClientThread ( MyServer _s, String _x, int _id)
    {
	srv = _s;
	msg = _x;
	id = _id;
	time = 1;
	setDaemon(true);
    }

    public void setMe(Observer obs)
    {
	me = obs;
    }

    public void run()
    {
	System.out.println("ClientThread " + id + " starts");
	int lifeTime = 10;
	try
	{
	    while( lifeTime > 0 )
	    {       
		lifeTime--;
		String a[] = srv.arryfy( msg,5 );
		System.out.println( id + ", " + lifeTime + 
                                    " to go." + srv.writeMessages( a, me ));
		sleep(500);
	    }
	}
	catch( org.omg.CORBA.COMM_FAILURE cf )
	{
	    System.out.println("Communication failure");
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
	System.out.println("thread exits...");
    }

    public void update1( Observer o)
    { 
	System.out.println("Client " + id + " update1");
	o.update2();
    }

    public void update2()
    { 
	System.out.println("Client " + id + " update2");
    }
}


