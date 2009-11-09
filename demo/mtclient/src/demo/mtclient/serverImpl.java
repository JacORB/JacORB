package demo.mtclient;

public class serverImpl 
    extends MyServerPOA
{
    private static final int _delay = 245;

    private void delay()
    {
	try
	{
	    Thread.currentThread().sleep(_delay);
	}
	catch( InterruptedException i)
	{}
    }

    public String[] arryfy( String s, int i )
    {
	String result [] = new String[i];
	for( int j = 0; j < i; j++ )
	    result[j] = s;
	delay();
	return result;
    }

    public String writeMessage( String s )
    {
	System.out.println("Message from " + s );
	delay();
	return s + " written";
    }

    public  String writeMessages( String[] s, Observer _observer )
    {
	for( int i = 0; i < s.length; i++)
	    System.out.print("Message: " + s[i] + ", ");

	delay();

	_observer.update1(_observer);
	_observer.update2();
	return "ok.";
    }
}


