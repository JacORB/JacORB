package Map;


/**
 * <ul>
 * <li> <b>IDL Source</b>    "map.idl"
 * <li> <b>IDL Name</b>      ::Map::Point
 * <li> <b>Repository Id</b> IDL:Map/Point:1.0
 * </ul>
 * <b>IDL definition:</b>
 * <pre>
 * valuetype Point  {
  ...
};
 * </pre>
 */
public class PointDefaultFactory
    implements org.omg.CORBA.portable.ValueFactory 
{
    public java.io.Serializable read_value (org.omg.CORBA_2_3.portable.InputStream is) 
    {
        int x = is.read_long();
        int y = is.read_long();
        String label = is.read_string();

        java.io.Serializable val = new PointImpl(x,y,label);


        // create and initialize value
        //        val = is.read_value( val );
        return val;
    }
}
