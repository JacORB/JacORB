package org.jacorb.test.bugs.bug923;

import org.omg.PortableServer.POA;

public class DayFactoryImpl
    extends DayFactoryPOA
{
    POA poa;

    public DayFactoryImpl(POA poa)
    {
        this.poa = poa;
    }

    public Base getDay()
    {
       Base b = null;
       try
       {
          byte [] oid = GoodDayHelper.id().getBytes();
          String typeId = GoodDayHelper.id();

          System.out.println("create_reference_with_id");
          org.omg.CORBA.Object obj = poa.create_reference_with_id(oid, typeId);

          System.out.println("getDay, calling BaseHelper.narrow");
          b = BaseHelper.narrow(obj);
          System.out.println("getDay, narrow complete");

       } catch (Throwable e) {
         System.out.println("getDay, caught exception=" + e);
       }
       return b;
    }

    public void deleteDay(Base b) {
       System.out.println("deleteDay");
       System.out.println("narrowing to a GoodDay");
       GoodDay g = GoodDayHelper.narrow(b);
       System.out.println("fine narrowing to a GoodDay");
    }

}
