package Map;
/**
 * <p>
 * <ul>
 * <li> <b>Java Class</b> ConnectedPointImpl
 * <li> <b>Source File</b> ConnectedPointImpl.java
 * <li> <b>IDL Source File</b> cpoint.idl
 * <li> <b>IDL Absolute Name</b> ::ConnectedPoint
 * <li> <b>Repository Identifier</b> IDL:ConnectedPoint:1.0
 * </ul>
 * <b>IDL definition:</b>
 * <pre>
 * valuetype ConnectedPoint : truncatable ::Point {
 * ...
 * };
 * </pre>
 * </p>
 */
public class ConnectedPointImpl 
    extends ConnectedPoint 
{
    public ConnectedPointImpl() {}

    public ConnectedPointImpl(int a_x, int a_y, String a_label, Point[] a_connected_points) {
        x = a_x;
        y = a_y;
        label = a_label;
        connected_points = a_connected_points;
    }
 
    /**
     * <p>
     * Operation: <b>::Point::print</b>.
     * <pre>
     * void print ();
     * </pre>
     * </p>
     */
    public void print () {
        System.out.println("Derived Point is [" + label + ": (" + x + ", " + y + ")]");
        if (connected_points.length > 0) {
            System.out.println("Connected to:");
            for (int i = 0; i < connected_points.length; i++) {
                System.out.print("\t");
                connected_points[i].print();
            }
        }
    }

    /**
     * <p>
     * Operation: <b>::ConnectedPoint::add_connection</b>.
     * <pre>
     * void add_connection (in ::Point p);
     * </pre>
     * </p>
     */
    public void add_connection (
                                Point p
                                ) {
        Point[] connected = new Point[connected_points.length + 1];
        if (connected_points.length > 0) {
            System.arraycopy(connected_points, 0, connected, 0, connected_points.length);
        }

        connected[connected_points.length] = p;
        connected_points = connected;
    }

}
