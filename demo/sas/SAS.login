KerberosClient 
{
	com.sun.security.auth.module.Krb5LoginModule required storeKey=true useTicketCache=true debug=true;
};

KerberosService 
{
	com.sun.security.auth.module.Krb5LoginModule required storeKey=true principal="testService@OPENROADSCONSULTING.COM" debug=true;
};

