package test.dynany;

public class T4
{
   public static void main (String[] args)
      throws Exception
   {
      org.omg.CORBA.Any any;
      org.omg.CORBA.ORB orb;
      org.omg.CORBA.Object obj;
      org.omg.DynamicAny.DynAnyFactory factory;
      org.omg.CosTrading.LookupPackage.SpecifiedProps union;
      java.util.Properties sysProps = System.getProperties ();
      org.omg.DynamicAny.DynUnion dUnion;
      org.omg.DynamicAny.DynAny dAny;

      sysProps.put ("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
      sysProps.put ("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");

      orb = org.omg.CORBA.ORB.init (args, null);

      obj = orb.resolve_initial_references ("DynAnyFactory");
      factory =  org.omg.DynamicAny.DynAnyFactoryHelper.narrow (obj);

      union = new org.omg.CosTrading.LookupPackage.SpecifiedProps ();
      union.__default (org.omg.CosTrading.LookupPackage.HowManyProps.all);

      any = orb.create_any ();
      org.omg.CosTrading.LookupPackage.SpecifiedPropsHelper.insert (any, union);

      dAny = factory.create_dyn_any (any);
      dUnion = org.omg.DynamicAny.DynUnionHelper.narrow (dAny);

      System.out.println ("No active member? " +
                         dUnion.has_no_active_member());

      dAny = dUnion.member ();

      System.out.println ("Should not see this!");
   }
}
