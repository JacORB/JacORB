package demo.corbaloc;

public class GoodDayImpl
    extends GoodDayPOA
{
    private String location;

    public GoodDayImpl( String location )
    {
        this.location = location;
    }

    public String hello_simple()
    {
        return "Hello World, from " + location;
    }
}
