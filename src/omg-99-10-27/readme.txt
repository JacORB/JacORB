This archive contains the Java definitions that are specified and
required by the IDL Java Language mapping as specified in CORBA 2.3.

A complete Java ORB implementation will need to provide more code,
either because it is generated from standard IDL (e.g. dynamic any) or
because it contains vendor-specific generated code (e.g stubs, helpers,
etc.)

This archive contains more files than are strictly necessary because it
is a goal that the whole package be compilable. Files, for which, dummy
implementations are provided are designated as such in the comment
header.  (Examples include the helper classes that needed in order to
allow Interface Repository routines to be compiled.)

Files which are not so marked shall be provided by conformant products
"as is".  Vendors may not add or subtract functionality from them
(though of course things such as comments, formal parameter names, etc.
which do not change functionality may be modified.)

The binary portability requirement implies that a user can take the
generated stub and associated files (helpers, holders, etc.) that were
generated using a particular vendor's IDL compiler, and use them on
another vendor's runtime. Hence the above limitations restricting an
implementor's freedom to modify the required classes.


			modified 27-10-1999 Jeff Mischkinsky
					    jeff_mischkinsky@omg.org
