package test.recursiveTC.case2;

import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;

import org.opengis.sfg.*;
import org.omg.CosPropertyService.*;

// Aufruf mit java  -Dorg.omg.CORBA.ORBClass=org.jacorb.orb.ORB -Dorg.omg.CORBA.ORBSingletonClass=org.jacorb.orb.ORBSingleton JacORBTest3

public class Test3 {
	public static void main(String args[]) {
		int n = 2; // <<<==== ab n > 1 tritt bereits beim Schreiben auf Any ein recursive typecode-Fehler auf
		           // damit sollte sich fuer beliebig viele Rekursionsstufen ein Test erstellen lassen
		String[] xargs = null;
		org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(xargs, null);

		Any any = orb.create_any();
		WKSGeometry geom = new WKSGeometry();
		// Geometrie als geschlossenes Polygon ohne Inseln
		WKSPoint[] pts = {new WKSPoint(10,10), new WKSPoint(20,20), new WKSPoint(30,10), new WKSPoint(10,10)};
		WKSPoint[][] inbounds = new WKSPoint[0][];
		geom.linear_polygon(new WKSLinearPolygon(pts, inbounds));
		WKSGeometryHelper.insert(any, geom);
		org.omg.CosPropertyService.Property property;

		for (int i = 0; i < n; i++) {
			System.out.println("i="+i);
			property = new org.omg.CosPropertyService.Property("name", any);
			any = orb.create_any();
			PropertyHelper.insert(any, property);
		}

		System.out.println("*** geschafft ***");
		System.exit(0);
	}
}



