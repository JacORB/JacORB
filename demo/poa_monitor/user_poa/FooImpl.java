package demo.poa_monitor.user_poa;

import demo.poa_monitor.foox.*;

public class FooImpl extends FooPOA {
	private String id;
	public FooImpl(String _id) {		
		id = _id;
	}
	public void compute(int time) {
		try {		
			for (int i=1; i<=time; i=i+100) {
//				System.out.print(time+" ");
				Thread.currentThread().sleep(100);			
			}
		} catch (InterruptedException e) {
		}
	}
}
