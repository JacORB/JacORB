package test;

public class T1
{
   public static void main (String[] args)
      throws Exception
   {
      org.omg.CORBA.ORB orb;
      org.omg.CORBA.Object obj;
      org.omg.CORBA.TypeCode tc;
      org.omg.DynamicAny.DynAnyFactory factory;
      java.util.Properties sysProps = System.getProperties ();
      org.omg.DynamicAny.DynSequence dSeq;
      org.omg.DynamicAny.DynAny dAny;

      sysProps.put ("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
      sysProps.put ("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");

      orb = org.omg.CORBA.ORB.init (args, null);

      obj = orb.resolve_initial_references ("DynAnyFactory");
      factory = org.omg.DynamicAny.DynAnyFactoryHelper.narrow (obj);

      tc = orb.create_sequence_tc (4, orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_string));
      //      tc = orb.create_sequence_tc (4, orb.create_string_tc(0) );
      dAny = factory.create_dyn_any_from_type_code (tc);
      dSeq =  org.omg.DynamicAny.DynSequenceHelper.narrow (dAny);
      dSeq.set_length (4);
   }
}
