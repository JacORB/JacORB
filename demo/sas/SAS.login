KerberosClient 
{
	com.sun.security.auth.module.Krb5LoginModule required debug=true;
};

KerberosService 
{
	com.sun.security.auth.module.Krb5LoginModule required storeKey=true principal="testService@OPENROADSCONSULTING.COM" debug=true;
};

