package test.POA.local;

public class FooImpl extends FooPOA {
	public FooImpl() {		
	}

	public void compute() {
            System.out.println("-- Foo.compute.");
	}

    public void setFoo( Foo foo )
    {
        System.out.println("** Local call to compute **");
        foo.compute();
    }
}
