package demo.unions;

public class ServerImpl
    extends MyServerPOA
{
    public void writeUnion(UnitedColors union, UnitedColorsHolder unionh)
    {
        switch ( union.discriminator().value() )
        {
            case colorT._blue :
                System.out.println("Blue: " + union.s() );
                break;
            case colorT._red :
                System.out.println("Red: " + union.s() );
                break;
            case colorT._black :
                System.out.println("Black: ");
                String [] strs = union.strs();
                for( int i = 0; i < strs.length; i++ )
                    System.out.println(strs[i]);
                break;
            default :
                System.out.println("default: " + union.i() );
        }
        UnitedColors new_union = new UnitedColors();

        // change color and write s.th. back

        new_union.s( colorT.blue, "This gets back");
        unionh.value =  new_union;
    }


    public void write2ndUnion(Nums union)
    {
        switch ( union.discriminator() )
        {
            case 'l' :
                System.out.println("Long " );
                break;
            case 'f' :
                System.out.println("Float " );
                break;
            default :
                System.out.println("default: " );
        }
    }
}


