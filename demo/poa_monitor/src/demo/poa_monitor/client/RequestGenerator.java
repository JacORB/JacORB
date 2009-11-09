package demo.poa_monitor.client;

import demo.poa_monitor.foox.*;

public class RequestGenerator extends Thread {
	boolean active = true;
	private Foo foo;
	private boolean firstObjectContact;
	public RequestGenerator(Foo _foo, boolean _firstObjectContact) {
		foo = _foo;
		firstObjectContact = _firstObjectContact;
	}
	public void run() {
		long startTime = 0;
		long stopTime = 0;
		while (active) {
			try {	
				int costs = (int) (Math.random() * Client.cost);
		    	startTime = System.currentTimeMillis();
				foo.compute(costs);
		    	stopTime = System.currentTimeMillis();
		    	Client.addTime((int)(stopTime-startTime), costs, firstObjectContact);
		    	firstObjectContact = false;
				
			} catch (org.omg.CORBA.SystemException e) {
				System.out.println("system exception received: "+e);
                                e.printStackTrace();
			} catch (Throwable e) {
				System.out.println("exception received: "+e);
				e.printStackTrace();			
			}
			try {
				if (Client.speed != 0) sleep((int) (Math.random() * Client.speed));
			} catch (InterruptedException e) {
			}			
		}
	}
}
