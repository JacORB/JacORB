KerberosClient 
{
	com.sun.security.auth.module.Krb5LoginModule required storeKey=true useTicketCache=true debug=false;
};

KerberosService 
{
	com.sun.security.auth.module.Krb5LoginModule required storeKey=true principal="vicads@OPENROADSCONSULTING.COM" debug=false;
};

