NTLogin
{
	com.tagish.auth.win32.NTSystemLogin required returnNames=true returnSIDs=false;
};

FileLogin
{
	com.tagish.auth.FileLogin required debug=true pwdFile="D:\\Works\\Takhini\\TagishAuth\\passwd";
};

DBLogin
{
	com.tagish.auth.DBLogin required debug=true dbDriver="sun.jdbc.odbc.JdbcOdbcDriver" dbURL="jdbc:odbc:DBLogin";
};
