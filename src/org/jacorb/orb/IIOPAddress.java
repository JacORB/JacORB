package org.jacorb.orb;

/**
 * @author Andre Spiegel
 * @version $Id$
 */
public class IIOPAddress 
{
	private String host;
	private int port;
	
	public IIOPAddress (String host, int port)
	{
		this.host = host;
		this.port = port;
	}

	public String getHost()
	{
		return host;
	}

	public int getPort()
	{
		return port;
	}
	
	public String toString()
	{
		return host + ":" + port;
	}
}
