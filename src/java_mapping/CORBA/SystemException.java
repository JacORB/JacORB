package org.omg.CORBA;

/**
 *  p. A-58/59 in orbos/98-01-06,
 * 
 */
abstract public class SystemException extends java.lang.RuntimeException {

	public int minor;
	public CompletionStatus completed;

	SystemException(String reason, int minor, CompletionStatus completed)
	{
		super( reason );
		this.minor = minor;
		this.completed = completed;
	}

	public String toString()
	{
		String result = getClass().getName() + "[";
		if( minor != 0)
		{
			result += "minor=" + minor + ", ";
		}
		String[] comp = {"YES","NO","MAYBE"};
		result += "completed=" + comp[completed.value()];
		if( getMessage() != null )
		{
			result += ", reason=" + getMessage();
		}
		return result + "]";
	}

}


