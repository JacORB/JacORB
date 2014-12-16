// -*- C++ -*-

#ifndef BIDIRTESTI_H_
#define BIDIRTESTI_H_

#include "BiDirTestS.h"

#if !defined (ACE_LACKS_PRAGMA_ONCE)
#pragma once
#endif /* ACE_LACKS_PRAGMA_ONCE */

class  ClientCallback_i : public virtual CORBA::servant_traits<test::interop::bidir::ClientCallback>::base_type
{
public:
  ClientCallback_i () = default;

  //Destructor
  virtual ~ClientCallback_i ();

  virtual void hello (const std::string& message) override;
};

class CallbackServer_i : public virtual CORBA::servant_traits<test::interop::bidir::CallbackServer>::base_type
{
public:
  CallbackServer_i () = default;

  //Destructor
  virtual ~CallbackServer_i ();

  virtual void callback_hello (IDL::traits<test::interop::bidir::ClientCallback>::ref_type cc, const std::string& message);
};


#endif /* BIDIRTESTI_H_  */
