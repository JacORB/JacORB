@echo off
rem call java interpreter
java @@@CLASSPATH@@@ -Djacorb.home=@@@JACORB_HOME@@@ -Dorg.omg.CORBA.ORBClass=org.jacorb.orb.ORB -Dorg.omg.CORBA.ORBSingletonClass=org.jacorb.orb.ORBSingleton %*
