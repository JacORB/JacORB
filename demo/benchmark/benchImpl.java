package demo.benchmark;

public class benchImpl 
    extends benchPOA
{
    

    public int[] intTransfer(int myInt[]) 
    {
	return myInt;
    }

    public byte[] octetTransfer( byte mybytes[] ) 
    {
        return mybytes;
    }

    public void ping()
    {}

    public Struct[] structTransfer(Struct myStruct[]) 
    {
	return myStruct;
    }

    public String[] stringTransfer(String myString[]) 
    {
	return myString;
    }


}


