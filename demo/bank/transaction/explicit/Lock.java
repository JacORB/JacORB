package demo.bank.transaction.explicit;

public class Lock
{
    private boolean locked;

    public Lock()
    {
	locked = false;
    }

    public synchronized void lock()
    {
	while(locked)
	{
	    try
	    {
		wait();
	    }
	    catch(InterruptedException ie)
	    {}
	}
	locked = true;
    }

    public synchronized void unlock()
    {
	locked = false;
	notifyAll();
    }
}
