// -*- C++ -*-

#include "BiDirTest_i.h"

// Implementation skeleton destructor
ClientCallback_i::~ClientCallback_i (void)
{
}

void ClientCallback_i::hello (const std::string& message)
{
  std::cout << "ClientCallback received msg: " << message << std::endl;
}

// Implementation skeleton destructor
CallbackServer_i::~CallbackServer_i ()
{
}

void CallbackServer_i::callback_hello (IDL::traits<test::interop::bidir::ClientCallback>::ref_type cc, const std::string& message)
{
  std::cout << "CallbackServer received msg: " << message << std::endl;
  cc->hello(message);
}
