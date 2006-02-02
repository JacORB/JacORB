package test.idl.OBV.Map;

/**
 * <p>
 * <ul>
 * <li> <b>Java Class</b> PointImpl
 * <li> <b>Source File</b> PointImpl.java
 * <li> <b>IDL Source File</b> point.idl
 * <li> <b>IDL Absolute Name</b> ::Point
 * <li> <b>Repository Identifier</b> IDL:Point:1.0
 * </ul>
 * <b>IDL definition:</b>
 * <pre>
 * valuetype Point  {
 * ...
 * };
 * </pre>
 * </p>
 */

public class PointImpl
    extends Point
{
    public PointImpl() {}

    public PointImpl(int a_x, int a_y, String a_label) {
        x = a_x;
        y = a_y;
        label = a_label;
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
        System.out.println("Point is [" + label + ": (" + x + ", " + y + ")]");
    }

}
