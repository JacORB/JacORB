package demo.poa_monitor.root_poa;

import demo.poa_monitor.foox.*;

public class FooFactoryImpl extends FooFactoryPOA {
	public Foo createFoo(String id) {
		Foo result = new FooImpl("0")._this(_orb());
		System.out.println("[ Foo created id: "+id+" ]");
		return result;
	}
	public String getServerDescription() {
		System.out.println("[ description requested: "+Server.description+" ]");		
		return Server.description;
	}
}
