package demo.unions;

import java.io.BufferedReader;
import java.io.FileReader;

import org.omg.CORBA.ORB;

//an example for using IDL unions

public class Client
{
    public static void main( String[] args ) throws Exception
    {
        ORB orb = ORB.init(args, null);

        BufferedReader reader = new BufferedReader(new FileReader(args[0]));

        MyServer s = MyServerHelper.narrow(orb.string_to_object(reader.readLine()));

        UnitedColors my_union = new UnitedColors();
        UnitedColorsHolder my_union_holder = new UnitedColorsHolder();

        // set up union and make call

        my_union.s("hallo");
        s.writeUnion(my_union, my_union_holder);

        // get the union that came back in the out holder

        my_union = my_union_holder.value;

        // examine the contents of the union that came back

        switch (my_union.discriminator().value() )
        {
            case colorT._blue :
                System.out.println("Blue: " + my_union.s() );
                break;
            case colorT._red :
                System.out.println("Red: " + my_union.s() );
                break;
            default :
                System.out.println("default: " + my_union.i() );
        }

        String [] strs = {"hello", "world"};

        my_union.strs( strs );
        s.writeUnion(my_union, my_union_holder);

        /* other union type */

        Nums n = new Nums();
        n.l(4711);
        s.write2ndUnion(n);
    }
}
