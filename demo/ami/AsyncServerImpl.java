package demo.ami;

public class AsyncServerImpl extends AsyncServerPOA 
{
    public int operation (int a, int b) 
    {
        try
        {
            Thread.currentThread().sleep( 2000 );
        }
        catch (InterruptedException e)
        {
        }
        return a + b;
    }
}
            
