package org.jacorb.test.orb.rmi;

public class Zoo implements java.io.Serializable {
    public String id;
    public String name;
    public Zoo inner;
    private transient Object hidden = "hidden";
    public Zoo(String id, String name) {
        this.id = id;
        this.name = name;
	this.inner = null;
    }
    public Zoo(String id, String name, Zoo inner) {
        this.id = id;
        this.name = name;
	this.inner = inner;
    }
    public String toString() {
        return "Zoo(" + id + ", \"" + name + "\"" +
	    ((inner == null) ? "" : ", " + inner.toString()) + ")";
    }
    public boolean equals(Object o) {
	return (o instanceof Zoo)
	    && (((Zoo)o).id.equals(id))
	    && (((Zoo)o).name.equals(name))
	    && ((((Zoo)o).inner == null && inner == null)
		|| (((Zoo)o).inner != null && ((Zoo)o).inner.equals(inner)));
    }
    private synchronized void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException {
	id = id + "!";
        s.defaultWriteObject();
    }
}
