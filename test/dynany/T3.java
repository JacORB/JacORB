package test.dynany;

public class T3
{
   public static void main (String[] args)
      throws Exception
   {
      String names[] = new String [2];
      org.omg.CORBA.Any any;
      org.omg.CORBA.Any[] contents;
      org.omg.CORBA.ORB orb;
      org.omg.CORBA.Object obj;
      org.omg.DynamicAny.DynAnyFactory factory;
      org.omg.CosTrading.LookupPackage.SpecifiedProps union;
      java.util.Properties sysProps = System.getProperties ();
      org.omg.DynamicAny.DynUnion dUnion;
      org.omg.DynamicAny.DynSequence dSeq;
      org.omg.DynamicAny.DynAny dAny;

      sysProps.put ("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
      sysProps.put ("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");

      orb = org.omg.CORBA.ORB.init (args, null);

      obj = orb.resolve_initial_references ("DynAnyFactory");
      factory =  org.omg.DynamicAny.DynAnyFactoryHelper.narrow (obj);

      union = new org.omg.CosTrading.LookupPackage.SpecifiedProps ();

      names[0] = "one";
      names[1] = "two";

      union.prop_names (names);
      any = orb.create_any ();
      org.omg.CosTrading.LookupPackage.SpecifiedPropsHelper.insert (any, union);

      dAny = factory.create_dyn_any (any);
      dUnion = org.omg.DynamicAny.DynUnionHelper.narrow (dAny);

      dAny = dUnion.member ();
      any = dAny.to_any ();

      dAny = factory.create_dyn_any (any);
      dSeq = org.omg.DynamicAny.DynSequenceHelper.narrow (dAny);
      System.out.println ("Sequence length = " + dSeq.get_length ());

      contents = dSeq.get_elements ();

      for (int i = 0; i < contents.length; i++)
      {
          System.out.println ("contents[i].type()" + contents[i].type().kind().value());
         System.out.println ("DynSequence val = " + contents[i].extract_string ());
      }
   }
}

