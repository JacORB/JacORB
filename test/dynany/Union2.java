package test.dynany;

public class Union2
{
   public static void main (String[] args)
      throws Exception
   {
      String names[] = new String [2];
      org.omg.CORBA.Any any;
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
System.out.println ("Creating dynamic union ");

      dAny = factory.create_dyn_any (any);
System.out.println ("Created dynamic union ");
      dUnion = org.omg.DynamicAny.DynUnionHelper.narrow (dAny);

System.out.println ("Narrowed dynamic union");

      dAny = dUnion.member ();

System.out.println ("Got dynamic union member");
      any = dAny.to_any ();
System.out.println ("Got dynamic union member any");

      dAny = factory.create_dyn_any (any);
System.out.println ("Narrowing dynamic sequence");
      dSeq =  org.omg.DynamicAny.DynSequenceHelper.narrow (dAny);
System.out.println ("Narrowed dynamic sequence");
      System.out.println ("Sequence length = " + dSeq.get_length ());
   }
}
