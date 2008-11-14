package org.jacorb.ir;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2004 Gerald Brose.
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

import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;
import org.apache.avalon.framework.logger.Logger;
import org.omg.CORBA.ExceptionDefPOATie;
import org.omg.CORBA.INTF_REPOS;
import org.omg.PortableServer.POA;

public class OperationDef
    extends Contained
    implements org.omg.CORBA.OperationDefOperations
{
    private org.omg.CORBA.TypeCode result = null;
    private org.omg.CORBA.IDLType result_def = null;
    private org.omg.CORBA.ExceptionDef[] exceptions = null;
    private org.omg.CORBA.ParameterDescription[] params = null;

    private String[] contexts = new String[0];
    private org.omg.CORBA.OperationMode mode;

    private Method method;

    /** the extra information on the operation that is provided in the
        IRHelper */
    private String opInfo;
    private String returnTypeName;
    private String[] paramTypeNames = new String[0];

    private boolean defined = false;

    private Logger logger;    
    private ClassLoader loader;
    private POA poa;

    public OperationDef( Method m,
                         Class def_in,
                         Class irHelper,
                         org.omg.CORBA.InterfaceDef i_def,
                         Logger logger,
                         ClassLoader loader,
                         POA poa)
    {
        this.logger = logger;
        this.loader = loader;
        this.poa = poa;

        def_kind = org.omg.CORBA.DefinitionKind.dk_Operation;
        name( m.getName());

        if (def_in == null)
        {
           throw new INTF_REPOS ("Class argument null");
        }
        if (i_def == null)
        {
           throw new INTF_REPOS ("Idef argument null" );
        }

        id( RepositoryID.toRepositoryID( 
                RepositoryID.className( i_def.id(), loader) + "/" + name(), 
                false, 
                loader));

        version(id().substring( id().lastIndexOf(':')));
        defined_in = i_def;
        containing_repository = i_def.containing_repository();
        String className = def_in.getName();
        absolute_name = i_def.absolute_name() + "::" + name;
        method = m;

        if (this.logger.isDebugEnabled())
        {
            this.logger.debug("New OperationDef, name: " + name +
                              " "  + absolute_name);
        }

        Hashtable irInfo = null;
        opInfo = null;
        try
        {
            irInfo = (Hashtable)irHelper.getDeclaredField("irInfo").get(null);
            opInfo = (String)irInfo.get( name()  );
        }
        catch( Exception e )
        {
            logger.error("Caught Exception", e);
        }

        /* parse extra operation information that's in the opInfo string */

        if( opInfo != null )
        {
            if( opInfo.endsWith("-oneway"))
            {
                mode = org.omg.CORBA.OperationMode.OP_ONEWAY;
            }

            if( opInfo.indexOf("(") > 0 )
                returnTypeName = opInfo.substring(0,opInfo.indexOf("("));


            StringTokenizer strtok =
                new StringTokenizer( opInfo.substring( opInfo.indexOf("(") + 1,
                                                       opInfo.lastIndexOf(")")), ",");

            paramTypeNames = new String[strtok.countTokens()];
            for( int i = 0; i < paramTypeNames.length; i++ )
            {
                String token = strtok.nextToken();

                paramTypeNames[i] = ( !token.equals(",") ? token : null );
            }
        }

        if( mode == null )
        {
            mode = org.omg.CORBA.OperationMode.OP_NORMAL;
        }


        contexts = new String[0];
    }

    void define()
    {
        try
        {
            result =
                TypeCodeUtil.getTypeCode( method.getReturnType(),
                                          this.loader,
                                          null,
                                          returnTypeName,
                                          this.logger);

            result_def = org.jacorb.ir.IDLType.create( result, 
                                                       containing_repository,
                                                       this.logger,
                                                       this.poa);
        }
        catch( Exception e )
        {
            logger.error("Caught Exception", e);
        }

        params = getParameterDescriptions();

        Class [] ex_classes = method.getExceptionTypes();
        Class uexc = null;
        try
        {
            uexc = this.loader.loadClass("org.omg.CORBA.UserException");
        }
        catch ( ClassNotFoundException e1)
        {
            throw new INTF_REPOS(ErrorMsg.IR_Definition_Not_Found,
                                               org.omg.CORBA.CompletionStatus.COMPLETED_NO);
        }

        Vector v = new Vector();
        for( int ix = 0; ix < ex_classes.length; ix++ )
        {
            if( uexc.isAssignableFrom(ex_classes[ix]))
            {
                try
                {
                    ExceptionDef ex =  new ExceptionDef( ex_classes[ ix ],
                                                         defined_in,
                                                         containing_repository,
                                                         this.loader, 
                                                         this.poa,
                                                         this.logger);
                    org.omg.CORBA.ExceptionDef exRef =
                        org.omg.CORBA.ExceptionDefHelper.narrow(
                              this.poa.servant_to_reference(
                                   new ExceptionDefPOATie (  ex )
								   )
							  );

					v.addElement( exRef );
					ex.setReference( exRef );
                }
                catch( Exception e )
                {
                    logger.error("Caught Exception", e);
                }
            }
        }

        exceptions = new org.omg.CORBA.ExceptionDef[ v.size() ];
        v.copyInto( exceptions );

        defined = true;
    }


    org.omg.CORBA.ParameterDescription[] getParameterDescriptions()
    {
        org.omg.CORBA.TypeCode tc = null;
        Class m_params[] = method.getParameterTypes();

        org.omg.CORBA.ParameterDescription[] params =
            new org.omg.CORBA.ParameterDescription[m_params.length];

        if( paramTypeNames.length > 0 )
        {
            if (paramTypeNames.length != m_params.length)
            {
                throw new INTF_REPOS ("Different parameter type numbers! " +
                                      paramTypeNames.length + " vs. " + m_params.length +
                                      " inforString: " + opInfo);
            }
        }


        for( int i = 0; i < params.length; i++)
        {
            String name = "arg_" + i;
            String paramInfo = null;
            org.omg.CORBA.ParameterMode mode = null;
            try
            {
                String parameterTypeName = m_params[i].getName();

                if( paramTypeNames.length != 0 )
                    paramInfo = paramTypeNames[i];

                if( ! parameterTypeName.endsWith("Holder")  )
                {
                    mode = org.omg.CORBA.ParameterMode.PARAM_IN;
                    if( paramInfo != null && paramInfo.indexOf(' ') > 0 )
                    {
                        parameterTypeName =
                            paramInfo.substring( paramInfo.indexOf(' ')+1);
                        name =
                            paramInfo.substring( paramInfo.indexOf(':')+1,
                                                 paramInfo.indexOf(' '));
                    }
                }
                else
                {
                    if ( ! (paramInfo != null && (paramInfo.indexOf(' ') > 0)))
                    {
                        throw new INTF_REPOS ("No param info for " + parameterTypeName);
                    }

                    if( paramInfo.substring(0, (paramInfo.indexOf(' ')-1)).startsWith("inout:"))
                        mode = org.omg.CORBA.ParameterMode.PARAM_INOUT;
                    else
                        mode = org.omg.CORBA.ParameterMode.PARAM_OUT;

                    name = paramInfo.substring( paramInfo.indexOf(':')+1,
                                                paramInfo.indexOf(' '));

                    parameterTypeName =
                       parameterTypeName.substring(0, parameterTypeName.indexOf("Holder"));
                }


                if (this.logger.isDebugEnabled())
                {
                    this.logger.debug("Operation " + name() + ", param #"+ i +
                                      "name: " + name +
                                      ", paramTypeName " + parameterTypeName +
                                      paramInfo);
                }

                tc = TypeCodeUtil.getTypeCode( m_params[i],
                                               this.loader,
                                               null,
                                               parameterTypeName,
                                               this.logger);
            }
            catch ( Exception e )
            {
                logger.error("Caught Exception", e);
                throw new INTF_REPOS( ErrorMsg.IR_Definition_Not_Found,
						    org.omg.CORBA.CompletionStatus.COMPLETED_NO);
            }
            org.omg.CORBA.IDLType type_def =
                IDLType.create( tc, containing_repository, 
                                this.logger, this.poa );
            params[i] =
                new org.omg.CORBA.ParameterDescription( name, tc, type_def, mode);
        }
        return params;
    }

    public org.omg.CORBA.IDLType result_def()
    {
        if ( ! defined)
        {
            throw new INTF_REPOS ("OperationDef undefined");
        }
        if (result_def == null)
        {
            throw new INTF_REPOS ("Result def for op " + name () + " null");
        }
        return result_def;
    }

    public void result_def(org.omg.CORBA.IDLType a)
    {
        result_def = a;
    }

    public org.omg.CORBA.OperationMode mode()
    {
        return mode;
    }

    public void mode( org.omg.CORBA.OperationMode a)
    {
        mode = a;
    }

    public org.omg.CORBA.TypeCode result()
    {
        if ( ! defined)
        {
            throw new INTF_REPOS ("OperationDeg undefined");
        }

        return result;
    }

    public org.omg.CORBA.ParameterDescription[] params()
    {
        if( !defined )
            define();
        return params;
    }

    public void params(org.omg.CORBA.ParameterDescription[] a)
    {
        params = a;
    }

    public java.lang.String[] contexts()
    {
        if( !defined )
            define();
        return contexts;
    }

    public void contexts(java.lang.String[] a)
    {
        contexts = a;
    }

    public org.omg.CORBA.ExceptionDef[] exceptions()
    {
        if( !defined )
            define();
        return exceptions;
    }

    public void exceptions(org.omg.CORBA.ExceptionDef[] a)
    {
        exceptions = a;
    }

    public org.omg.CORBA.OperationDescription describe_operation()
    {
        if( !defined )
            define();

        org.omg.CORBA.ExceptionDescription ex_des[] =
            new org.omg.CORBA.ExceptionDescription[exceptions.length];

        for( int i = 0; i < exceptions.length; i++ )
        {
			org.omg.CORBA.ContainedPackage.Description cd = exceptions[i].describe();

            if( cd.kind != org.omg.CORBA.DefinitionKind.dk_Exception )
            {
                throw new INTF_REPOS( ErrorMsg.IR_Unexpected_Definition_Kind,
                                                    org.omg.CORBA.CompletionStatus.COMPLETED_NO);
            }
            ex_des[i] = org.omg.CORBA.ExceptionDescriptionHelper.extract( cd.value );
        }
        return new org.omg.CORBA.OperationDescription(name,
                                                      id,
                                                      org.omg.CORBA.ContainedHelper.narrow(defined_in).id(),
                                                      version,
                                                      result,  mode,  contexts, params, ex_des);
    }

    // from IRObject

    public void destroy()
    {
        throw new INTF_REPOS(
                                           ErrorMsg.IR_Not_Implemented,
                                           org.omg.CORBA.CompletionStatus.COMPLETED_NO);
    }


    // from Contained

    public org.omg.CORBA.ContainedPackage.Description describe()
    {
        if ( ! defined)
        {
            throw new INTF_REPOS ("OperationDeg undefined");
        }


        org.omg.CORBA.Any a = orb.create_any();
        org.omg.CORBA.OperationDescriptionHelper.insert( a, describe_operation() );
        return new org.omg.CORBA.ContainedPackage.Description( org.omg.CORBA.DefinitionKind.dk_Operation, a);
    }


}
