package test.recursiveTC.case2;

import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;

import org.opengis.sfg.*;
import org.omg.CosPropertyService.*;

// Aufruf mit java  -Dorg.omg.CORBA.ORBClass=org.jacorb.orb.ORB -Dorg.omg.CORBA.ORBSingletonClass=org.jacorb.orb.ORBSingleton JacORBTest3

public class Test4 {
	public static void main(String args[]) {
		int n = 1;
		String[] xargs = null;
		org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(xargs, null);

		Any any = orb.create_any();
		WKSGeometry geom = new WKSGeometry();
		// Geometrie als geschlossenes Polygon ohne Inseln
		WKSPoint[] pts = {new WKSPoint(10,10), new WKSPoint(20,20), new WKSPoint(30,10), new WKSPoint(10,10)};
		WKSPoint[][] inbounds = new WKSPoint[0][];
		geom.linear_polygon(new WKSLinearPolygon(pts, inbounds));
		WKSGeometryHelper.insert(any, geom);
		org.omg.CosPropertyService.Property property = new org.omg.CosPropertyService.Property("name", any);
		// property einmal "verpacken" in neuem property
		any = orb.create_any();
		PropertyHelper.insert(any, property);
		property = new org.omg.CosPropertyService.Property("name2", any);

		// Sequence aufbauen (der Einfachheit halber immer mit demselben Property)
		org.omg.CosPropertyService.Property[] properties = new org.omg.CosPropertyService.Property[n];
		for (int i = 0; i < n; i++) properties[i] = property;

		any = orb.create_any();
		PropertiesHelper.insert(any, properties);
		properties = PropertiesHelper.extract(any);

		System.out.println("*** geschafft ***");
		System.exit(0);
	}
}


