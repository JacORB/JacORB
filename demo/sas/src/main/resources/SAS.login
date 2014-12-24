KerberosClient
{
    com.sun.security.auth.module.Krb5LoginModule required debug=true;
};

KerberosService
{
    com.sun.security.auth.module.Krb5LoginModule required storeKey=true principal="${principal}" debug=true;
};
