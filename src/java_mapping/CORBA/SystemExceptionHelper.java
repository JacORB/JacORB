package org.omg.CORBA;


/**
 * <code>SystemExceptionHelper</code> has been implemented as there is no
 * org.omg.CORBA.SystemExceptionHelper provided by OMG -
 * see http://www.omg.org/issues/issue3750.txt
 */
public final class SystemExceptionHelper
{
   private static org.omg.CORBA.TypeCode _type = org.omg.CORBA.ORB.init().create_exception_tc( org.omg.CORBA.SystemExceptionHelper.id(),"SystemException",new org.omg.CORBA.StructMember[]{new org.omg.CORBA.StructMember("minor",org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.from_int(5)),null),new org.omg.CORBA.StructMember("completed",org.omg.CORBA.ORB.init().create_enum_tc(org.omg.CORBA.CompletionStatusHelper.id(),"CompletionStatus",new String[]{"COMPLETED_YES","COMPLETED_NO","COMPLETED_MAYBE"}),null)});

   public static void insert(org.omg.CORBA.Any any, org.omg.CORBA.SystemException s)
   {
      any.type(getTypeCode(s));
      write(any.create_output_stream (), s);
   }

   public static org.omg.CORBA.SystemException extract(org.omg.CORBA.Any any)
   {
      return read(any.create_input_stream());
   }

   public static org.omg.CORBA.TypeCode type()
   {
      return _type;
   }

   public static String id()
   {
      return "IDL:omg.org/CORBA/SystemException:1.0";
   }

   public static org.omg.CORBA.SystemException read(org.omg.CORBA.portable.InputStream in)
   {
      String id=in.read_string();
      org.omg.CORBA.SystemException result=getSystemException(id);
      result.minor=in.read_ulong();
      result.completed=org.omg.CORBA.CompletionStatusHelper.read(in);
      return result;
   }

   public static void write(org.omg.CORBA.portable.OutputStream out, org.omg.CORBA.SystemException s)
   {
      out.write_string(getId(s));
      out.write_ulong(s.minor);
      org.omg.CORBA.CompletionStatusHelper.write(out,s.completed);
   }


   private static org.omg.CORBA.TypeCode getTypeCode
      (org.omg.CORBA.SystemException s)
   {
      if (s instanceof org.omg.CORBA.BAD_CONTEXT)
      {
         return org.omg.CORBA.BAD_CONTEXTHelper.type ();
      }
      else if (s instanceof org.omg.CORBA.BAD_INV_ORDER)
      {
         return org.omg.CORBA.BAD_INV_ORDERHelper.type ();
      }
      else if (s instanceof org.omg.CORBA.BAD_OPERATION)
      {
         return org.omg.CORBA.BAD_OPERATIONHelper.type ();
      }
      else if (s instanceof org.omg.CORBA.BAD_PARAM)
      {
         return org.omg.CORBA.BAD_PARAMHelper.type ();
      }
      else if (s instanceof org.omg.CORBA.BAD_QOS)
      {
         return org.omg.CORBA.BAD_QOSHelper.type ();
      }
      else if (s instanceof org.omg.CORBA.BAD_TYPECODE)
      {
         return org.omg.CORBA.BAD_TYPECODEHelper.type ();
      }
      else if (s instanceof org.omg.CORBA.CODESET_INCOMPATIBLE)
      {
         return org.omg.CORBA.CODESET_INCOMPATIBLEHelper.type ();
      }
      else if (s instanceof org.omg.CORBA.COMM_FAILURE)
      {
         return org.omg.CORBA.COMM_FAILUREHelper.type ();
      }
      else if (s instanceof org.omg.CORBA.DATA_CONVERSION)
      {
         return org.omg.CORBA.DATA_CONVERSIONHelper.type ();
      }
      else if (s instanceof org.omg.CORBA.FREE_MEM)
      {
         return org.omg.CORBA.FREE_MEMHelper.type ();
      }
      else if (s instanceof org.omg.CORBA.IMP_LIMIT)
      {
         return org.omg.CORBA.IMP_LIMITHelper.type ();
      }
      else if (s instanceof org.omg.CORBA.INITIALIZE)
      {
         return org.omg.CORBA.INITIALIZEHelper.type ();
      }
      else if (s instanceof org.omg.CORBA.INTERNAL)
      {
         return org.omg.CORBA.INTERNALHelper.type ();
      }
      else if (s instanceof org.omg.CORBA.INTF_REPOS)
      {
         return org.omg.CORBA.INTF_REPOSHelper.type ();
      }
      else if (s instanceof org.omg.CORBA.INVALID_TRANSACTION)
      {
         return org.omg.CORBA.INVALID_TRANSACTIONHelper.type ();
      }
      else if (s instanceof org.omg.CORBA.INV_FLAG)
      {
         return org.omg.CORBA.INV_FLAGHelper.type ();
      }
      else if (s instanceof org.omg.CORBA.INV_IDENT)
      {
         return org.omg.CORBA.INV_IDENTHelper.type ();
      }
      else if (s instanceof org.omg.CORBA.INV_OBJREF)
      {
         return org.omg.CORBA.INV_OBJREFHelper.type ();
      }
      else if (s instanceof org.omg.CORBA.INV_POLICY)
      {
         return org.omg.CORBA.INV_POLICYHelper.type ();
      }
      else if (s instanceof org.omg.CORBA.MARSHAL)
      {
         return org.omg.CORBA.MARSHALHelper.type ();
      }
      else if (s instanceof org.omg.CORBA.NO_IMPLEMENT)
      {
         return org.omg.CORBA.NO_IMPLEMENTHelper.type ();
      }
      else if (s instanceof org.omg.CORBA.NO_MEMORY)
      {
         return org.omg.CORBA.NO_MEMORYHelper.type ();
      }
      else if (s instanceof org.omg.CORBA.NO_PERMISSION)
      {
         return org.omg.CORBA.NO_PERMISSIONHelper.type ();
      }
      else if (s instanceof org.omg.CORBA.NO_RESOURCES)
      {
         return org.omg.CORBA.NO_RESOURCESHelper.type ();
      }
      else if (s instanceof org.omg.CORBA.NO_RESPONSE)
      {
         return org.omg.CORBA.NO_RESPONSEHelper.type ();
      }
      else if (s instanceof org.omg.CORBA.OBJECT_NOT_EXIST)
      {
         return org.omg.CORBA.OBJECT_NOT_EXISTHelper.type ();
      }
      else if (s instanceof org.omg.CORBA.OBJ_ADAPTER)
      {
         return org.omg.CORBA.OBJ_ADAPTERHelper.type ();
      }
      else if (s instanceof org.omg.CORBA.PERSIST_STORE)
      {
         return org.omg.CORBA.PERSIST_STOREHelper.type ();
      }
      else if (s instanceof org.omg.CORBA.REBIND)
      {
         return org.omg.CORBA.REBINDHelper.type ();
      }
      else if (s instanceof org.omg.CORBA.TIMEOUT)
      {
         return org.omg.CORBA.TIMEOUTHelper.type ();
      }
      else if (s instanceof org.omg.CORBA.TRANSACTION_MODE)
      {
         return org.omg.CORBA.TRANSACTION_MODEHelper.type ();
      }
      else if (s instanceof org.omg.CORBA.TRANSACTION_REQUIRED)
      {
         return org.omg.CORBA.TRANSACTION_REQUIREDHelper.type ();
      }
      else if (s instanceof org.omg.CORBA.TRANSACTION_ROLLEDBACK)
      {
         return org.omg.CORBA.TRANSACTION_ROLLEDBACKHelper.type ();
      }
      else if (s instanceof org.omg.CORBA.TRANSACTION_UNAVAILABLE)
      {
         return org.omg.CORBA.TRANSACTION_UNAVAILABLEHelper.type ();
      }
      else if (s instanceof org.omg.CORBA.TRANSIENT)
      {
         return org.omg.CORBA.TRANSIENTHelper.type ();
      }
      else if (s instanceof org.omg.CORBA.UNKNOWN)
      {
         return org.omg.CORBA.UNKNOWNHelper.type ();
      }
      else
      {
         throw new org.omg.CORBA.BAD_PARAM ("unrecognized system exception: " + s.getClass ().getName ());
      }
   }


   private static String getId (org.omg.CORBA.SystemException s)
   {
      if (s instanceof org.omg.CORBA.BAD_CONTEXT)
      {
         return org.omg.CORBA.BAD_CONTEXTHelper.id ();
      }
      else if (s instanceof org.omg.CORBA.BAD_INV_ORDER)
      {
         return org.omg.CORBA.BAD_INV_ORDERHelper.id ();
      }
      else if (s instanceof org.omg.CORBA.BAD_OPERATION)
      {
         return org.omg.CORBA.BAD_OPERATIONHelper.id ();
      }
      else if (s instanceof org.omg.CORBA.BAD_PARAM)
      {
         return org.omg.CORBA.BAD_PARAMHelper.id ();
      }
      else if (s instanceof org.omg.CORBA.BAD_QOS)
      {
         return org.omg.CORBA.BAD_QOSHelper.id ();
      }
      else if (s instanceof org.omg.CORBA.BAD_TYPECODE)
      {
         return org.omg.CORBA.BAD_TYPECODEHelper.id ();
      }
      else if (s instanceof org.omg.CORBA.CODESET_INCOMPATIBLE)
      {
         return org.omg.CORBA.CODESET_INCOMPATIBLEHelper.id ();
      }
      else if (s instanceof org.omg.CORBA.COMM_FAILURE)
      {
         return org.omg.CORBA.COMM_FAILUREHelper.id ();
      }
      else if (s instanceof org.omg.CORBA.DATA_CONVERSION)
      {
         return org.omg.CORBA.DATA_CONVERSIONHelper.id ();
      }
      else if (s instanceof org.omg.CORBA.FREE_MEM)
      {
         return org.omg.CORBA.FREE_MEMHelper.id ();
      }
      else if (s instanceof org.omg.CORBA.IMP_LIMIT)
      {
         return org.omg.CORBA.IMP_LIMITHelper.id ();
      }
      else if (s instanceof org.omg.CORBA.INITIALIZE)
      {
         return org.omg.CORBA.INITIALIZEHelper.id ();
      }
      else if (s instanceof org.omg.CORBA.INTERNAL)
      {
         return org.omg.CORBA.INTERNALHelper.id ();
      }
      else if (s instanceof org.omg.CORBA.INTF_REPOS)
      {
         return org.omg.CORBA.INTF_REPOSHelper.id ();
      }
      else if (s instanceof org.omg.CORBA.INVALID_TRANSACTION)
      {
         return org.omg.CORBA.INVALID_TRANSACTIONHelper.id ();
      }
      else if (s instanceof org.omg.CORBA.INV_FLAG)
      {
         return org.omg.CORBA.INV_FLAGHelper.id ();
      }
      else if (s instanceof org.omg.CORBA.INV_IDENT)
      {
         return org.omg.CORBA.INV_IDENTHelper.id ();
      }
      else if (s instanceof org.omg.CORBA.INV_OBJREF)
      {
         return org.omg.CORBA.INV_OBJREFHelper.id ();
      }
      else if (s instanceof org.omg.CORBA.INV_POLICY)
      {
         return org.omg.CORBA.INV_POLICYHelper.id ();
      }
      else if (s instanceof org.omg.CORBA.MARSHAL)
      {
         return org.omg.CORBA.MARSHALHelper.id ();
      }
      else if (s instanceof org.omg.CORBA.NO_IMPLEMENT)
      {
         return org.omg.CORBA.NO_IMPLEMENTHelper.id ();
      }
      else if (s instanceof org.omg.CORBA.NO_MEMORY)
      {
         return org.omg.CORBA.NO_MEMORYHelper.id ();
      }
      else if (s instanceof org.omg.CORBA.NO_PERMISSION)
      {
         return org.omg.CORBA.NO_PERMISSIONHelper.id ();
      }
      else if (s instanceof org.omg.CORBA.NO_RESOURCES)
      {
         return org.omg.CORBA.NO_RESOURCESHelper.id ();
      }
      else if (s instanceof org.omg.CORBA.NO_RESPONSE)
      {
         return org.omg.CORBA.NO_RESPONSEHelper.id ();
      }
      else if (s instanceof org.omg.CORBA.OBJECT_NOT_EXIST)
      {
         return org.omg.CORBA.OBJECT_NOT_EXISTHelper.id ();
      }
      else if (s instanceof org.omg.CORBA.OBJ_ADAPTER)
      {
         return org.omg.CORBA.OBJ_ADAPTERHelper.id ();
      }
      else if (s instanceof org.omg.CORBA.PERSIST_STORE)
      {
         return org.omg.CORBA.PERSIST_STOREHelper.id ();
      }
      else if (s instanceof org.omg.CORBA.REBIND)
      {
        return org.omg.CORBA.REBINDHelper.id ();
      }
      else if (s instanceof org.omg.CORBA.TIMEOUT)
      {
        return org.omg.CORBA.TIMEOUTHelper.id ();
      }
      else if (s instanceof org.omg.CORBA.TRANSACTION_MODE)
      {
         return org.omg.CORBA.TRANSACTION_MODEHelper.id ();
      }
      else if (s instanceof org.omg.CORBA.TRANSACTION_REQUIRED)
      {
         return org.omg.CORBA.TRANSACTION_REQUIREDHelper.id ();
      }
      else if (s instanceof org.omg.CORBA.TRANSACTION_ROLLEDBACK)
      {
         return org.omg.CORBA.TRANSACTION_ROLLEDBACKHelper.id ();
      }
      else if (s instanceof org.omg.CORBA.TRANSACTION_UNAVAILABLE)
      {
         return org.omg.CORBA.TRANSACTION_UNAVAILABLEHelper.id ();
      }
      else if (s instanceof org.omg.CORBA.TRANSIENT)
      {
         return org.omg.CORBA.TRANSIENTHelper.id ();
      }
      else if (s instanceof org.omg.CORBA.UNKNOWN)
      {
         return org.omg.CORBA.UNKNOWNHelper.id ();
      }
      else
      {
         throw new org.omg.CORBA.BAD_PARAM ("unrecognized system exception: " + s.getClass ().getName ());
      }
   }


   private static org.omg.CORBA.SystemException getSystemException (String id)
   {
      if (id.equals (org.omg.CORBA.BAD_CONTEXTHelper.id ()))
      {
         return new org.omg.CORBA.BAD_CONTEXT ();
      }
      else if (id.equals (org.omg.CORBA.BAD_INV_ORDERHelper.id ()))
      {
         return new org.omg.CORBA.BAD_INV_ORDER ();
      }
      else if (id.equals (org.omg.CORBA.BAD_OPERATIONHelper.id ()))
      {
         return new org.omg.CORBA.BAD_OPERATION ();
      }
      else if (id.equals (org.omg.CORBA.BAD_PARAMHelper.id ()))
      {
         return new org.omg.CORBA.BAD_PARAM ();
      }
      else if (id.equals (org.omg.CORBA.BAD_QOSHelper.id ()))
      {
         return new org.omg.CORBA.BAD_QOS ();
      }
      else if (id.equals (org.omg.CORBA.BAD_TYPECODEHelper.id ()))
      {
         return new org.omg.CORBA.BAD_TYPECODE ();
      }
      else if (id.equals (org.omg.CORBA.CODESET_INCOMPATIBLEHelper.id ()))
      {
         return new org.omg.CORBA.CODESET_INCOMPATIBLE ();
      }
      else if (id.equals (org.omg.CORBA.COMM_FAILUREHelper.id ()))
      {
         return new org.omg.CORBA.COMM_FAILURE ();
      }
      else if (id.equals (org.omg.CORBA.DATA_CONVERSIONHelper.id ()))
      {
         return new org.omg.CORBA.DATA_CONVERSION ();
      }
      else if (id.equals (org.omg.CORBA.FREE_MEMHelper.id ()))
      {
         return new org.omg.CORBA.FREE_MEM ();
      }
      else if (id.equals (org.omg.CORBA.IMP_LIMITHelper.id ()))
      {
         return new org.omg.CORBA.IMP_LIMIT ();
      }
      else if (id.equals (org.omg.CORBA.INITIALIZEHelper.id ()))
      {
         return new org.omg.CORBA.INITIALIZE ();
      }
      else if (id.equals (org.omg.CORBA.INTERNALHelper.id ()))
      {
         return new org.omg.CORBA.INTERNAL ();
      }
      else if (id.equals (org.omg.CORBA.INTF_REPOSHelper.id ()))
      {
         return new org.omg.CORBA.INTF_REPOS ();
      }
      else if (id.equals (org.omg.CORBA.INVALID_TRANSACTIONHelper.id ()))
      {
         return new org.omg.CORBA.INVALID_TRANSACTION ();
      }
      else if (id.equals (org.omg.CORBA.INV_FLAGHelper.id ()))
      {
         return new org.omg.CORBA.INV_FLAG ();
      }
      else if (id.equals (org.omg.CORBA.INV_IDENTHelper.id ()))
      {
         return new org.omg.CORBA.INV_IDENT ();
      }
      else if (id.equals (org.omg.CORBA.INV_OBJREFHelper.id ()))
      {
         return new org.omg.CORBA.INV_OBJREF ();
      }
      else if (id.equals (org.omg.CORBA.INV_POLICYHelper.id ()))
      {
         return new org.omg.CORBA.INV_POLICY ();
      }
      else if (id.equals (org.omg.CORBA.MARSHALHelper.id ()))
      {
         return new org.omg.CORBA.MARSHAL ();
      }
      else if (id.equals (org.omg.CORBA.NO_IMPLEMENTHelper.id ()))
      {
         return new org.omg.CORBA.NO_IMPLEMENT ();
      }
      else if (id.equals (org.omg.CORBA.NO_MEMORYHelper.id ()))
      {
         return new org.omg.CORBA.NO_MEMORY ();
      }
      else if (id.equals (org.omg.CORBA.NO_PERMISSIONHelper.id ()))
      {
         return new org.omg.CORBA.NO_PERMISSION ();
      }
      else if (id.equals (org.omg.CORBA.NO_RESOURCESHelper.id ()))
      {
         return new org.omg.CORBA.NO_RESOURCES ();
      }
      else if (id.equals (org.omg.CORBA.NO_RESPONSEHelper.id ()))
      {
         return new org.omg.CORBA.NO_RESPONSE ();
      }
      else if (id.equals (org.omg.CORBA.OBJECT_NOT_EXISTHelper.id ()))
      {
         return new org.omg.CORBA.OBJECT_NOT_EXIST ();
      }
      else if (id.equals (org.omg.CORBA.OBJ_ADAPTERHelper.id ()))
      {
         return new org.omg.CORBA.OBJ_ADAPTER ();
      }
      else if (id.equals (org.omg.CORBA.PERSIST_STOREHelper.id ()))
      {
         return new org.omg.CORBA.PERSIST_STORE ();
      }
      else if (id.equals (org.omg.CORBA.REBINDHelper.id ()))
      {
         return new org.omg.CORBA.REBIND ();
      }
      else if (id.equals (org.omg.CORBA.TIMEOUTHelper.id ()))
      {
         return new org.omg.CORBA.TIMEOUT ();
      }
      else if (id.equals (org.omg.CORBA.TRANSACTION_MODEHelper.id ()))
      {
         return new org.omg.CORBA.TRANSACTION_MODE ();
      }
      else if (id.equals (org.omg.CORBA.TRANSACTION_REQUIREDHelper.id ()))
      {
         return new org.omg.CORBA.TRANSACTION_REQUIRED ();
      }
      else if (id.equals (org.omg.CORBA.TRANSACTION_ROLLEDBACKHelper.id ()))
      {
         return new org.omg.CORBA.TRANSACTION_ROLLEDBACK ();
      }
      else if (id.equals (org.omg.CORBA.TRANSACTION_UNAVAILABLEHelper.id ()))
      {
         return new org.omg.CORBA.TRANSACTION_UNAVAILABLE ();
      }
      else if (id.equals (org.omg.CORBA.TRANSIENTHelper.id ()))
      {
         return new org.omg.CORBA.TRANSIENT ();
      }
      else if (id.equals (org.omg.CORBA.UNKNOWNHelper.id ()))
      {
         return new org.omg.CORBA.UNKNOWN ();
      }
      else
      {
         throw new org.omg.CORBA.MARSHAL("wrong or unrecognized id: " + id);
      }
   }
}
