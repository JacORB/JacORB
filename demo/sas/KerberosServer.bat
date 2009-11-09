@echo off
set JACORBDEF=-Dorg.omg.CORBA.ORBClass=org.jacorb.orb.ORB -Dorg.omg.CORBA.ORBSingletonClass=org.jacorb.orb.ORBSingleton
set KERBEROSDEF=-Djava.security.auth.login.config=SAS.login -Djava.security.krb5.realm=OPENROADSCONSULTING.COM -Djava.security.krb5.kdc=openroadsconsulting.com
set CP=build/classes
set XCP=../../lib/jacorb.jar;../../lib/slf4j-api-1.5.6.jar;../../lib/slf4j-jdk14-1.5.6.jar;../../lib/antlr-2.7.2.jar;
java -Xbootclasspath/p:%XCP% -cp %CP% %JACORBDEF% %KERBEROSDEF% -Dcustom.props=KerberosServer.properties org.jacorb.demo.sas.KerberosServer sas.ior testPass
