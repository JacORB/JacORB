package org.jacorb.test.bugs.bug923;

import org.omg.PortableServer.POA;
import org.omg.CORBA.INTERNAL;

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

          org.omg.CORBA.Object obj = poa.create_reference_with_id(oid, typeId);

          b = BaseHelper.narrow(obj);

       }
       catch (Exception e)
       {
           throw new INTERNAL ("Caught " + e);
       }
       return b;
    }

    public void deleteDay(Base b) {
       GoodDayHelper.narrow(b);
    }

}
