
// Copyright (C) 1998-1999
// Object Oriented Concepts, Inc.

// **********************************************************************
//
// Copyright (c) 1997
// Mark Spruiell (mark@intellisoft.com)
//
// See the COPYING file for more information
//
// **********************************************************************

package org.jacorb.trading.db.simple.offers;


import java.io.*;
import org.omg.CORBA.*;
import org.omg.CosTradingDynamic.*;
// GB:  import jtport.ORBLayer;


public class DynPropValue implements Serializable
{
  private String m_evalInterface;
  private TypeCodeValue m_returnedType;
  private AnyValue m_extraInfo;

  private transient org.omg.CORBA.ORB orb;

  static final long serialVersionUID = -7792164971850696279L;


  private DynPropValue()
  {
  }


  public DynPropValue(org.omg.CORBA.ORB orb, Any any)
  {
      this.orb = orb;
    setValue(any);
  }


  public Any getValue()
  {
    Any result = orb.create_any();

    DynamicProp p = new DynamicProp();
    org.omg.CORBA.Object obj = orb.string_to_object(m_evalInterface);
    p.eval_if = DynamicPropEvalHelper.narrow(obj);
    p.returned_type = m_returnedType.getValue();
    p.extra_info = m_extraInfo.getValue();
    DynamicPropHelper.insert(result, p);

    return result;
  }


  protected void setValue(Any val)
  {
    DynamicProp p = DynamicPropHelper.extract(val);
    m_evalInterface = orb.object_to_string(p.eval_if);
    m_returnedType = new TypeCodeValue(p.returned_type);
    m_extraInfo = new AnyValue(orb,p.extra_info);
  }
}










