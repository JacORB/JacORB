package demo.benchmark;

public class benchImpl 
    extends benchPOA
{
    

    public int[] intTransfer(int myInt[]) 
    {
	return myInt;
    }

    public void octetInTransfer( byte mybytes[] ) 
    {
    }

    public byte[] octetOutTransfer() 
    {
	return null;
    }

    public void ping()
    {}

    public Struct[] structTransfer(Struct myStruct[]) 
    {
	return myStruct;
    }
}


