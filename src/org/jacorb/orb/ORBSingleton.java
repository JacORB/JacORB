package org.jacorb.orb;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2012 Gerald Brose / The JacORB Team.
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Library General Public
 *   License as published by the Free Software Foundation; either
 *   version 2 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this library; if not, write to the Free
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

import java.util.HashSet;
import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;
import org.jacorb.config.JacORBConfiguration;
import org.jacorb.orb.typecode.NullTypeCodeCache;
import org.jacorb.orb.typecode.NullTypeCodeCompactor;
import org.jacorb.orb.typecode.TypeCodeCache;
import org.jacorb.orb.typecode.TypeCodeCompactor;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.BAD_TYPECODE;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.INITIALIZE;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.TypeCode;
import org.slf4j.Logger;

/**
 * @author Gerald Brose, FU Berlin
 */

public class ORBSingleton
    extends org.omg.CORBA_2_5.ORB
{
    private static final String FACTORY_METHODS_MESG = "The Singleton ORB only permits factory methods";

    private boolean doStrictCheckOnTypecodeCreation;

    protected Logger logger;

    protected IBufferManager bufferManager;

    protected TypeCodeCache typeCodeCache;

    protected TypeCodeCompactor typeCodeCompactor;

    /**
     * the configuration object for this ORB instance
     */
    protected org.jacorb.config.Configuration configuration;

    /**
     * in case a singleton orb is created the c'tor will access the JacORB configuration
     * to configure the orb. otherwise configure needs to be called to properly set up
     * the created instance.
     *
     * @param isSingleton determine if a singleton orb is created.
     */
    protected ORBSingleton(boolean isSingleton)
    {
        super();

        try
        {
            if (isSingleton)
            {
                configuration = JacORBConfiguration.getConfiguration(null, null, null, false);

                // Don't call configure method as if this has been called from ORB::ctor
                // class construction order can cause issues.
                logger = configuration.getLogger ("jacorb.orb.singleton");

                doStrictCheckOnTypecodeCreation = configuration.getAttributeAsBoolean
                    ("jacorb.interop.strict_check_on_tc_creation", true);

                final BufferManagerFactory bufferManagerFactory = newBufferManagerFactory(configuration);
                bufferManager = bufferManagerFactory.newSingletonBufferManager(configuration);

                typeCodeCache = NullTypeCodeCache.getInstance();
                typeCodeCompactor = NullTypeCodeCompactor.getInstance();

                logger.info("Created ORBSingleton");

                if (logger.isDebugEnabled())
                {
                    logger.debug("BufferManagerFactory: " + bufferManagerFactory);
                    logger.debug("BufferManager: " + bufferManager);
                    logger.debug("jacorb.interop.strict_check_on_tc_creation set to " + doStrictCheckOnTypecodeCreation);
                    ClassLoader omgcl  = org.omg.CORBA.ORB.class.getClassLoader ();
                    ClassLoader thiscl = this.getClass().getClassLoader();

                    if (omgcl != thiscl)
                    {
                        logger.debug ("OMG.ORB classloader (" + omgcl + ") does not match JacORB ORBSingleton classloader (" + thiscl + "). This may cause problems; see the ProgrammingGuide for further details");
                    }
                }
            }
        }
        catch (ConfigurationException e)
        {
            throw new INTERNAL(e.toString());
        }
    }

    private BufferManagerFactory newBufferManagerFactory(final org.jacorb.config.Configuration configuration) throws ConfigurationException
    {
        return (BufferManagerFactory) configuration.getAttributeAsObject(BufferManagerFactory.PARAM_NAME, DefaultBufferManagerFactory.class.getName());
    }

    public ORBSingleton()
    {
        this(true);
    }

    protected void configure(Configuration config) throws ConfigurationException
    {
        configuration = config;

        logger = configuration.getLogger("jacorb.orb");

        doStrictCheckOnTypecodeCreation = configuration.getAttributeAsBoolean
            ("jacorb.interop.strict_check_on_tc_creation", true);

        BufferManagerFactory bufferManagerFactory = newBufferManagerFactory(configuration);

        bufferManager = bufferManagerFactory.newBufferManager
            (((ORBSingleton)org.omg.CORBA.ORBSingleton.init()).getBufferManager(), configuration);

        if (logger.isDebugEnabled())
        {
            logger.debug("BufferManagerFactory: " + bufferManagerFactory);
            logger.debug("BufferManager: " + bufferManager);
            logger.debug("jacorb.interop.strict_check_on_tc_creation set to " + doStrictCheckOnTypecodeCreation);

            // This could lead to dual Singleton initialisation so only do it as part of debug. A singleton
            // will be initialised in org.omg.CORBA.ORBSingleton [a JacORB class] and in org.omg.CORBA.ORB
            // as well.
            if ( ! ( org.omg.CORBA.ORB.init() instanceof ORBSingleton))
            {
                logger.debug ("Default Singleton ORB is not a JacORB singleton. This is not recommended as it *could* lead to classpath/classloader/stub conflicts.");
            }
        }
    }

    /**
     * Returns the logger of this singleton ORB.  Used for testing.
     */
    public Logger getLogger()
    {
        return logger;
    }

    /* factory methods: */

    public org.omg.CORBA.Any create_any()
    {
        return new org.jacorb.orb.Any(this);
    }

    /**
     * Determine if a character is ok to start an id.
     * (Note that '_' is allowed here - it might have
     * been inserted by the IDL compiler to avoid clashes
     * with reserved Java identifiers )
     * @param character the character in question.
     */

    final protected static boolean legalStartChar(int character)
    {
        return ( character >= 'a' && character <= 'z')
            || ( character == '_')
            || ( character >= 'A' && character <= 'Z');
    }

    /**
     * Some parts of JacORB cannot be elegantly configured from the outside
     * and need access to the ORB's configuration retrieve config settings.
     * This method should only be used in those restricted cases!
     */

    public final org.jacorb.config.Configuration getConfiguration()
    {
        return configuration;
    }

    /**
     * Determine if a character is ok for the middle of an id.
     * @param ch the character in question.
     */
    final protected static boolean legalNameChar(int ch)
    {
        return legalStartChar(ch)
           || (ch == '_')
           || (ch >= '0' && ch <= '9');
    }

    /**
     * code>checkTCName</code> checks that a name is a legal IDL name
     * (CORBA 2.6 4-59).
     * @throws org.omg.CORBA.BAD_PARAM
     */
    private void checkTCName (String name) throws BAD_PARAM
    {
        checkTCName(name, false);
    }


   /**
    * <code>checkTCName</code> checks the name is a legal IDL name and
    * may optionally allow a null string (CORBA 2.6 4-59).
    *
    * @param name a <code>String</code> value
    * @param allowNull a <code>boolean</code> value
    * @exception BAD_PARAM if an error occurs
    */
    private void checkTCName (String name, boolean allowNull)
        throws BAD_PARAM
    {
        if (name == null)
        {
            if (allowNull)
            {
                return;
            }

            throw new BAD_PARAM("Illegal null IDL name",
                                15,
                                CompletionStatus.COMPLETED_NO );
        }

        if( name.length() > 0 )
        {
            // check that name begins with an ASCII char
            if( !legalStartChar( name.charAt(0)) )
            {
                throw new BAD_PARAM
                (
                        "Illegal start character to IDL name: " + name,
                        15,
                        CompletionStatus.COMPLETED_NO
                );
            }
            for( int i = 0; i < name.length(); i++ )
            {
                if( ! legalNameChar( name.charAt(i) ))
                {
                    throw new BAD_PARAM("Illegal IDL name: " + name,
                                        15,
                                        CompletionStatus.COMPLETED_NO );
                }
            }
        }
        else
        {
            throw new BAD_PARAM("Illegal blank IDL name",
                                15,
                                CompletionStatus.COMPLETED_NO );
        }
    }

    /**
     * Check that a repository ID is legal
     * (cf. CORBA 2.4 chapter 10, section 7.3
     * @param repId a <code>String</code> value
     * @exception BAD_PARAM if an error occurs
     */
    private void checkTCRepositoryId( String repId )
        throws BAD_PARAM
    {
        if( repId == null || repId.indexOf( ':' ) < 0 )
        {
            throw new BAD_PARAM("Illegal Repository ID: " + repId,
                                16,
                                CompletionStatus.COMPLETED_NO );
        }
    }

    /**
     * check that a type is a legal member type
     * (cf. CORBA 2.4 chapter 10, section 7.3
     * @throws org.omg.CORBA.BAD_PARAM
     */

    private void checkTCMemberType( TypeCode typeCode )
        throws BAD_TYPECODE
    {
        if( !org.jacorb.orb.TypeCode.isRecursive(typeCode) &&
            (typeCode == null ||
             typeCode.kind().value() == TCKind._tk_null ||
             typeCode.kind().value() == TCKind._tk_void ||
             typeCode.kind().value() == TCKind._tk_except
             )
            )
        {
            throw new BAD_TYPECODE("Illegal member TypeCode",
                                   2,
                                   CompletionStatus.COMPLETED_NO );
        }
    }

    /* TypeCode factory section */

    public TypeCode create_alias_tc( String id,
                                     String name,
                                     TypeCode original_type)
    {
        checkTCRepositoryId( id );
        checkTCName (name, true);
        checkTCMemberType( original_type );
        return new org.jacorb.orb.TypeCode( org.omg.CORBA.TCKind._tk_alias,
                                            id,
                                            name,
                                            original_type);
    }

    public TypeCode create_array_tc( int length, TypeCode element_type)
    {
        checkTCMemberType( element_type );
        return new org.jacorb.orb.TypeCode( org.omg.CORBA.TCKind._tk_array,
                                            length,
                                            element_type);
    }

    public TypeCode create_enum_tc( String id,
                                    String name,
                                    String[] members)
    {
        return create_enum_tc (id, name, members, true);
    }

    /**
     * Allows the possibility of not checking the name when creating this
     * typecode.  This is to cater for compact typecodes where the name
     * may not be set.  Checking of the name will always be true for user
     * driven requests
     * @param id
     * @param name
     * @param members
     * @param checkName
     * @returns TypeCode
     */
    public TypeCode create_enum_tc( String id,
                             String name,
                             String [] members,
                             boolean checkName)
    {
        checkTCRepositoryId( id );
        checkTCName (name, true);

        if (checkName)
        {
            // check that member names are legal and unique
            final HashSet<String> names = new HashSet<String>() ;
            for( int i = 0; i < members.length; i++ )
            {
                boolean fault = false;

                try
                {
                    checkTCName( members[i] );
                }
                catch( BAD_PARAM e )
                {
                    fault = true;
                    logger.debug("Typecode name check failed", e);
                }

                if((members[i] != null && names.contains(members[i])) || fault )
                {
                    throw new BAD_PARAM("Illegal enum member name: " + members[i],
                                        17,
                                        CompletionStatus.COMPLETED_NO );
                }
                names.add(members[i]);
            }
        }

        return new org.jacorb.orb.TypeCode( id, name, members);
    }

    public TypeCode create_exception_tc( String id,
                                         String name,
                                         org.omg.CORBA.StructMember[] members)
    {
        return create_exception_tc(id, name, members, true);
    }

    /**
     * Allows the possibility of not checking the name when creating this
     * typecode.  This is to cater for compact typecodes where the name
     * may not be set.  Checking of the name will always be true for user
     * driven requests
     * @param id
     * @param name
     * @param members
     * @param checkName
     * @returns TypeCode
     */
    public TypeCode create_exception_tc( String id,
                                  String name,
                                  org.omg.CORBA.StructMember[] members,
                                  boolean checkName)
    {
        checkTCRepositoryId( id );
        checkTCName (name, true);

        // check that member names are legal and unique
        final HashSet<String> names = new HashSet<String>() ;
        for( int i = 0; i < members.length; i++ )
        {
            checkTCMemberType( members[i].type );

            if (checkName)
            {
                boolean fault = false;

                try
                {
                    checkTCName( members[i].name );
                }
                catch( BAD_PARAM e )
                {
                    fault = true;
                    logger.debug("Typecode name check failed", e);
                }

                if((members[i].name != null && names.contains( members[i].name )) || fault )
                {
                    throw new BAD_PARAM("Illegal exception member name: " + members[i].name,
                                        17,
                                        CompletionStatus.COMPLETED_NO );
                }
                names.add(members[i].name);
            }
        }

        return new org.jacorb.orb.TypeCode( org.omg.CORBA.TCKind._tk_except,
                                            id,
                                            name,
                                            members);
    }

    public TypeCode create_interface_tc( String id, String name)
    {
        checkTCRepositoryId( id );
        checkTCName (name, true);
        return new org.jacorb.orb.TypeCode( org.omg.CORBA.TCKind._tk_objref,
                                            id,
                                            name);
    }

    public org.omg.CORBA.TypeCode create_fixed_tc( short digits,
                                                   short scale)
    {
        if (digits <= 0 || scale < 0 || scale > digits)
        {
            throw new org.omg.CORBA.BAD_PARAM
               ("Invalid combination of digits and scale factor");
        }
        return new org.jacorb.orb.TypeCode(digits, scale);
    }

    public org.omg.CORBA.TypeCode create_recursive_tc( String id )
    {
        checkTCRepositoryId( id );
        return new org.jacorb.orb.TypeCode( id );
    }

    public org.omg.CORBA.TypeCode create_recursive_sequence_tc (int bound, int offset)
    {
        throw new NO_IMPLEMENT ("create_recursive_sequence_tc - NYI");
    }


    public TypeCode create_sequence_tc( int bound, TypeCode element_type)
    {
        checkTCMemberType( element_type );
        TypeCode typeCode =
            new org.jacorb.orb.TypeCode( org.omg.CORBA.TCKind._tk_sequence,
                                         bound,
                                         element_type);
        return typeCode;
    }

    public TypeCode create_string_tc(int bound)
    {
        return new org.jacorb.orb.TypeCode( org.omg.CORBA.TCKind._tk_string, bound );
    }

    public TypeCode create_wstring_tc(int bound)
    {
        return new org.jacorb.orb.TypeCode( org.omg.CORBA.TCKind._tk_wstring, bound);
    }


    public TypeCode create_struct_tc(String id,
                                     String name,
                                     org.omg.CORBA.StructMember[] members)
    {
        return create_struct_tc (id, name, members, true);
    }

    /**
     * Allows the possibility of not checking the name when creating this
     * typecode.  This is to cater for compact typecodes where the name
     * may not be set.  Checking of the name will always be true for user
     * driven requests
     * @param id
     * @param name
     * @param members
     * @param checkName
     * @returns TypeCode
     */
    public TypeCode create_struct_tc(String id,
                              String name,
                              org.omg.CORBA.StructMember [] members,
                              boolean checkName)
    {
        checkTCRepositoryId( id );
        checkTCName (name, true);

        // check that member names are legal and unique
        final HashSet<String> names = new HashSet<String>();
        for( int i = 0; i < members.length; i++ )
        {
            if (checkName)
            {
                checkTCMemberType( members[i].type );
                boolean fault = false;

                try
                {
                    checkTCName( members[i].name );
                }
                catch( BAD_PARAM e )
                {
                    fault = true;
                    logger.debug("Typecode name check failed", e);
                }

                if((members[i].name != null && names.contains(members[i].name)) || fault )
                {
                    throw new BAD_PARAM("Illegal struct member name: " + members[i].name + (fault? " (Bad PARAM) ": "" ),
                                        17,
                                        CompletionStatus.COMPLETED_NO );
                }
                names.add(members[i].name);
            }
        }

        org.jacorb.orb.TypeCode typeCode =
            new org.jacorb.orb.TypeCode( org.omg.CORBA.TCKind._tk_struct,
                                         id,
                                         name,
                                         members);

        return typeCode;
    }

    public TypeCode create_union_tc( String id,
                                     String name,
                                     TypeCode discriminator_type,
                                     org.omg.CORBA.UnionMember[] members)
    {
        return create_union_tc(id, name, discriminator_type, members, true);
    }

    /**
     * Allows the possibility of not checking the name when creating this
     * typecode.  This is to cater for compact typecodes where the name
     * may not be set.  Checking of the name will always be true for user
     * driven requests
     * @param id
     * @param name
     * @param members
     * @param checkName
     * @returns TypeCode
     */
    public TypeCode create_union_tc( String id,
                              String name,
                              TypeCode discriminator_type,
                              org.omg.CORBA.UnionMember [] members,
                              boolean checkName)
    {
        checkTCRepositoryId( id );
        checkTCName (name, true);

        // check discriminator type

        TypeCode disc_tc =
            org.jacorb.orb.TypeCode.originalType(discriminator_type);

        if (disc_tc == null ||
            !(disc_tc.kind().value() == TCKind._tk_short ||
              disc_tc.kind().value() == TCKind._tk_long ||
              disc_tc.kind().value() == TCKind._tk_longlong ||
              disc_tc.kind().value() == TCKind._tk_ushort ||
              disc_tc.kind().value() == TCKind._tk_ulong  ||
              disc_tc.kind().value() == TCKind._tk_ulonglong ||
              disc_tc.kind().value() == TCKind._tk_char ||
              disc_tc.kind().value() == TCKind._tk_boolean ||
              disc_tc.kind().value() == TCKind._tk_enum
              )
            )
        {
            throw new BAD_PARAM("Illegal union discriminator type",
                                20,
                                CompletionStatus.COMPLETED_NO );
        }

        // check that member names are legal (they do not need to be unique)

        for( int i = 0; i < members.length; i++ )
        {
            checkTCMemberType( members[i].type );

            if (checkName)
            {
                try
                {
                    checkTCName( members[i].name );
                }
                catch( BAD_PARAM e )
                {
                    logger.debug("Typecode name check failed", e);
                    throw new BAD_PARAM("Illegal union member name: " + members[i].name,
                                        17,
                                        CompletionStatus.COMPLETED_NO );
                }
            }

            // check that member type matches discriminator type or is default

            org.omg.CORBA.Any label = members[i].label;
            if (! discriminator_type.equivalent( label.type() ) &&
                ! ( label.type().kind().value() == TCKind._tk_octet &&
                    label.extract_octet() == (byte)0
                  )
                )
            {
                throw new BAD_PARAM("Label type does not match discriminator type",
                                    19,
                                    CompletionStatus.COMPLETED_NO );
            }

            // check that member labels are unique

            for( int j = 0; j < i; j++ )
            {
                if( label.equal( members[j].label ))
                {
                    throw new BAD_PARAM("Duplicate union case label",
                                        18,
                                        CompletionStatus.COMPLETED_NO );
                }
            }
        }

        org.jacorb.orb.TypeCode typeCode =
           new org.jacorb.orb.TypeCode( id,
                                        name,
                                        discriminator_type,
                                        members);

        // resolve any recursive references to this TypeCode in its members

        return typeCode;
    }


    public TypeCode get_primitive_tc(org.omg.CORBA.TCKind tcKind)
    {
        return org.jacorb.orb.TypeCode.get_primitive_tc( tcKind.value() );
    }

    public org.omg.CORBA.TypeCode create_value_tc(String id,
                                                  String name,
                                                  short type_modifier,
                                                  TypeCode concrete_base,
                                                  org.omg.CORBA.ValueMember[] members)
    {
        checkTCRepositoryId( id );

        // The name parameter should be a valid IDL name, but in the case of
        // an RMI valuetype the ORB in jdk1.4 sends a dotted name (such as
        // "some.package.SomeClass") over the wire. For interoperability with
        // Sun's ORB we skip the name check in this case.

        if ( !id.startsWith("RMI:") )
        {
            checkTCName (name, true);
        }
        return new org.jacorb.orb.TypeCode (id,
                                            name,
                                            type_modifier,
                                            concrete_base,
                                            members);
    }

    public org.omg.CORBA.TypeCode create_value_box_tc(String id,
                                                      String name,
                                                      TypeCode boxed_type)
    {
        checkTCRepositoryId( id );
        checkTCName (name, true);
        return new org.jacorb.orb.TypeCode (org.omg.CORBA.TCKind._tk_value_box,
                                            id,
                                            name,
                                            boxed_type);
    }

    public org.omg.CORBA.TypeCode create_abstract_interface_tc(String id,
                                                               String name)
    {
       checkTCRepositoryId( id );

       // strict_check_on_tc_creation is incompatible with Sun's ValueHandler,
       // which calls create_abstract_interface_tc() passing an empty string
       // as the name parameter. checkTCName() then throws
       //`org.omg.CORBA.BAD_PARAM: Illegal blank IDL name'.
       if ( doStrictCheckOnTypecodeCreation )
       {
           checkTCName (name, true);
       }

       return new org.jacorb.orb.TypeCode (org.omg.CORBA.TCKind._tk_abstract_interface,
                                           id,
                                           name);
    }

    public org.omg.CORBA.TypeCode create_local_interface_tc(String id,
                                                            String name)
    {
       checkTCRepositoryId( id );
       checkTCName (name, true);
       return new org.jacorb.orb.TypeCode (org.omg.CORBA.TCKind._tk_local_interface,
                                           id,
                                           name);
    }

    public org.omg.CORBA.TypeCode create_native_tc(String id,
                                                   String name)
    {
       checkTCRepositoryId( id );
       checkTCName (name, true);
       return new org.jacorb.orb.TypeCode (org.omg.CORBA.TCKind._tk_native,
                                           id,
                                           name);
    }

   /* not allowed on the singleton: */

    public org.omg.CORBA.ExceptionList create_exception_list()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT (FACTORY_METHODS_MESG);
    }

    public org.omg.CORBA.NVList create_list (int count)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT (FACTORY_METHODS_MESG);
    }

    public org.omg.CORBA.NamedValue create_named_value
        (String name, org.omg.CORBA.Any value, int flags)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT (FACTORY_METHODS_MESG);
    }

    public org.omg.CORBA.NVList create_operation_list
        (org.omg.CORBA.OperationDef oper)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT (FACTORY_METHODS_MESG);
    }

    public org.omg.CORBA.NVList create_operation_list
        (org.omg.CORBA.Object obj)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT (FACTORY_METHODS_MESG);
    }

    public org.omg.CORBA.Object string_to_object(String str)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT (FACTORY_METHODS_MESG);
    }

    public  org.omg.CORBA.Environment create_environment()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT (FACTORY_METHODS_MESG);
    }

    public  org.omg.CORBA.ContextList create_context_list()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT (FACTORY_METHODS_MESG);
    }

    public org.omg.CORBA.portable.OutputStream create_output_stream()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT (FACTORY_METHODS_MESG);
    }

    public org.omg.CORBA.Current get_current()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT (FACTORY_METHODS_MESG);
    }

    public  org.omg.CORBA.Context get_default_context()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT (FACTORY_METHODS_MESG);
    }

    public org.omg.CORBA.Request get_next_response()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT (FACTORY_METHODS_MESG);
    }

    public String[] list_initial_services()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT (FACTORY_METHODS_MESG);
    }

    public String object_to_string( org.omg.CORBA.Object obj)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT (FACTORY_METHODS_MESG);
    }

    public  boolean poll_next_response()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT (FACTORY_METHODS_MESG);
    }

    public org.omg.CORBA.Object resolve_initial_references(String identifier)
        throws org.omg.CORBA.ORBPackage.InvalidName
    {
        throw new org.omg.CORBA.NO_IMPLEMENT (FACTORY_METHODS_MESG);
    }

    public  void send_multiple_requests_deferred(org.omg.CORBA.Request[] req)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT (FACTORY_METHODS_MESG);
    }

    public  void send_multiple_requests_oneway(org.omg.CORBA.Request[] req)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT (FACTORY_METHODS_MESG);
    }

    protected void set_parameters(String[] args, java.util.Properties props)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT (FACTORY_METHODS_MESG);
    }

    protected void set_parameters(String[] args, java.util.Properties props, String id)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT (FACTORY_METHODS_MESG);
    }

    protected void set_parameters(java.applet.Applet app, java.util.Properties  props)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT (FACTORY_METHODS_MESG);
    }

    public void run()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT (FACTORY_METHODS_MESG);
    }

    public void shutdown(boolean wait_for_completion)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT (FACTORY_METHODS_MESG);
    }

    public boolean work_pending()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT (FACTORY_METHODS_MESG);
    }

    public void perform_work()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT (FACTORY_METHODS_MESG);
    }

    public IBufferManager getBufferManager()
    {
        if (bufferManager == null)
        {
            throw new INITIALIZE ("JacORB ORB Singleton not initialized");
        }
        return bufferManager;
    }

    public TypeCodeCache getTypeCodeCache()
    {
        if (bufferManager == null)
        {
            throw new INITIALIZE ("JacORB ORB Singleton not initialized");
        }
        return typeCodeCache;
    }

    public TypeCodeCompactor getTypeCodeCompactor()
    {
        if (bufferManager == null)
        {
            throw new INITIALIZE ("JacORB ORB Singleton not initialized");
        }
        return typeCodeCompactor;
    }
}
