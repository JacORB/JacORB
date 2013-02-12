#!/bin/bash
bn=${0##*/}
host=$(hostname)
[[ $host == "phil" ]] && host="phil.ociweb.com"
test_dir="${JACORB_HOME}/test/orbreinvoke/bug_479"

cmdline="${test_dir}/ExecJavaService.sh SOM GGGGGGGGGGG1 aaax.gl.corba.CorbaService -serviceName GGGGGGGGGGG1 -client -Xms1024m -Xmx1536m -XX:CompileThreshold=8000 -XX:PermSize=128m -XX:MaxPermSize=256m -XX:MaxDirectMemorySize=256m -classpath .:/myvols/top/params:/myvols/top/params/jacorb:/myvols/top/external/cots/commons-logging/commons-logging-api.jar:/myvols/top/external/cots/commons-logging/commons-logging.jar:/myvols/top/external/cots/log4j/log4j.jar:/common/COTS/JacORB_01_17_2013/lib/jacorb.jar:/common/COTS/JacORB_01_17_2013/lib/jacorb-services.jar:/common/COTS/JacORB_01_17_2013/lib/idl.jar:/common/COTS/JacORB_01_17_2013/lib/slf4j-api-1.6.4.jar:/common/COTS/JacORB_01_17_2013/lib/slf4j-jdk14-1.6.4.jar:/myvols/top/params/jacorb:/myvols/top/external/cots/spring/dist/spring.jar:/myvols/top/external/cots/sprin -batch/dist/spring-batch-infrastructure-2.1.5.RELEASE.jar:/myvols/top/repository/jars/SpringUtilities.jar:/myvols/top/repository/jars/SpringConfiguration.jar:/myvols/top/repository/jars/CoreUtilities.jar:/myvols/top/repository/jars/GiasInterfaceCore.jar:/myvols/top/repository/jars/FlyweightIdCurrentClient.jar:/myvols/top/repository/jars/CorbaUtilities.jar:/myvols/top/repository/jars/giasClient.jar:/myvols/top/repository/jars/giasServer.jar:/myvols/top/repository/jar:/ObjectReferenceFactory.jar:/myvols/top/repository/jars/ObjectReferenceFactoryClient.jar:/myvols/top/repository/jars/ObjectReferenceFactoryServer.jar:/myvols/top/repository/jars/GatewayCore.jar:/myvols/top/repository/jars/GGGGGGGGGGG.jar -javaagent:/myvols/top/external/cots/spring/lib/aspectj/aspectjweaver.jar -org.omg.PortableInterceptor.ORBInitializerClass.aaax.gl.corba.FlyweightIdOrbInitializer -DGGGGGGGGGGG.logfilename=GGGGGGGGGGG1 -Dhost=mmmmmmmmm-lnx6-dev -Dlg4j.configuration=file:/myvols/top/params/log4j/giasgateway_log4j.properties -DContainerContextPath=classpath:apps/GGGGGGGGGGG/spring/service-context.xml -DOrbInstanceBeanName=giasGatewayOrb -DGGGGGGGGGGG.poaname=GGGGGGGGGGG1 -DGGGGGGGGGGG.orb.implname=aaa.bbbbbbb_bbbbb.ccc_c.GGGGGGGGGGG1.mmmmmmmmm-lnx6-dev/GGGGGGGGGGG1 -DGGGGGGGGGGG.nameServiceName=GGGGGGGGGGG1 -DGGGGGGGGGGG.libraryServant.iorFile=/common/ngl/nltmp/IOR/mmmmmmmmm/GGGG_GGGG_Library.ior -DGGGGGGGGGGG.pofileMgrServant.iorFile=/common/ngl/nltmp/IOR/mmmmmmmmm/ProfileIOR -DGGGGGGGGGGG.servantsorb.OAAddress=iiop://mmmmmmmmm-lnx6-dev:16139 -DGGGGGGGGGGG.servantsorb.alternate_addresses=mmmmmmmmm-lnx6-dev:16149,mmmmmmmmm-lnx6-dev:16129 -Djacorb.log.default.verbosity=0 -DORBInitRef.ImplementationRepository=file:/common/ngl/orbConfig/mmmmmmmmm/deploy/bbbbbbb_bbbbb.ccc_c/dbs/jacorb_imrlocator_ior -DORBInitRef.NameService=file:/common/ngl/orbConfig/mmmmmmmmm/deploy/unclass_util.ccc_c/dbs/tao_nameservice_ior EOM"

tao_locator_ior="/tmp/tao_imr_locator.ior"
#export ImplRepoServiceIOR=corbaloc::${host}:44555/ImR
#export ImplRepoServiceIOR=file:///tmp/tao_imr_locator.ior
$TAO_ROOT/orbsvcs/ImplRepo_Service/tao_imr \
 update JACORB:aaa.bbbbbbb_bbbbb.ccc_c.GGGGGGGGGGG1.mmmmmmmmm-lnx6-dev/GGGGGGGGGGG1 \
 -a NORMAL \
 -e ImplRepoServiceIOR=file://${tao_locator_ior} \
 -e JACORB_HOME=${JACORB_HOME} \
 -e ACE_ROOT=${ACE_ROOT} \
 -e TAO_ROOT=${ACE_ROOT}/TAO \
 -e LD_LIBRARY_PATH=${ACE_ROOT}/lib \
 -e DYLD_LIBRARY_PATH=${ACE_ROOT}/lib \
 -e PATH=${JACORB_HOME}/bin:${ACE_ROOT}/bin:${TAO_ROOT}/bin:${PATH} \
 -w "${test_dir}" \
 -r 0 \
 -l ${host} \
 -ORBInitRef ImplRepoService=file://${tao_locator_ior} \
 -c "${cmdline}"
exit 0
