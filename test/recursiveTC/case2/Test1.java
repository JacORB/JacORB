package test.recursiveTC.case2;

import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;

import org.opengis.sfg.*;
import org.omg.CosPropertyService.*;


public class Test1 {
	public static void main(String args[]) {
//		String[] xargs = {"xxx", "yyy"};
		String[] xargs = null;
		org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(xargs, null);

		Any any = orb.create_any();
		WKSGeometry geom = new WKSGeometry();
		// Geometrie als geschlossenes Polygon ohne Inseln
		WKSPoint[] pts = 
                {new WKSPoint(10,10), new WKSPoint(20,20), new WKSPoint(30,10), new WKSPoint(10,10)};

		WKSPoint[][] inbound = new WKSPoint[0][];
		geom.linear_polygon(new WKSLinearPolygon(pts, inbound));
		WKSGeometryHelper.insert(any, geom);

		org.omg.CosPropertyService.Property property = 
                    new org.omg.CosPropertyService.Property("name", any);

		// Property wiederum in Any verpacken
		any = orb.create_any();
		PropertyHelper.insert(any, property);

		//jetzt der Rueckweg
		property = PropertyHelper.extract(any);
		System.out.println("*** geschafft ***");
		System.exit(0);
	}
}


