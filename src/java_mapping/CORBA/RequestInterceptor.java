package org.omg.CORBA;

/**
 * Java for PIDL interface RequestInterceptor
 */
public abstract class RequestInterceptor
	extends  org.omg.CORBA.Interceptor

{
	public abstract void client_invoke(org.omg.CORBA.Request request);
	public abstract void target_invoke(org.omg.CORBA.Request request);
}


