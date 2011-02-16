//
// $Id: Hello_Impl.h,v 1.1 2011-01-17 16:54:20 vz Exp $
//

#ifndef _HELLOS_IMPL_H_
#define _HELLOS_IMPL_H_

#include "ace/Hash_Map_Manager_T.h"
#include "HelloS.h"

class UIPMC_Object_Impl : public virtual POA_test::interop::miop_tao_interop::UIPMC_Object
{
public:
  UIPMC_Object_Impl (CORBA::ULong payload, CORBA::ULong clients, CORBA::ULong calls);

  ~UIPMC_Object_Impl (void);

  // The skeleton methods
  virtual void process (test::interop::miop_tao_interop::Octets const &payload);

private:
  CORBA::ULong payload_;

  CORBA::ULong clients_;

  CORBA::ULong calls_;

  typedef ACE_Hash_Map_Manager<CORBA::Octet,
                               CORBA::ULong,
                               TAO_SYNCH_MUTEX> Client_Count_Map;
  Client_Count_Map received_;
};


class Hello_Impl : public virtual POA_test::interop::miop_tao_interop::Hello
{
public:
  // Constructor
  Hello_Impl (CORBA::ORB_ptr orb, test::interop::miop_tao_interop::UIPMC_Object_ptr obj);

  // The skeleton methods
  virtual test::interop::miop_tao_interop::UIPMC_Object_ptr get_object (void);

  virtual void shutdown (void);

private:
  CORBA::ORB_var orb_;

  test::interop::miop_tao_interop::UIPMC_Object_var obj_;
};

#endif // _HELLOS_IMPL_H_
