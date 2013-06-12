#!/bin/bash
bn=${0##*/}
host=$(hostname)
[[ $host == "phil" ]] && host="phil.ociweb.com"
implName=imr_demo
poaName=ImRDemoServerPOA

APP=tao_imr

#export ImplRepoServiceIOR=corbaloc::${host}:44555/ImR
export ImplRepoServiceIOR=file:///tmp/tao_imr_locator.ior
$TAO_ROOT/orbsvcs/ImplRepo_Service/${APP} \
 add JACORB:${implName}/${poaName} \
 -a NORMAL \
 -e ImplRepoServiceIOR=file:///tmp/tao_imr_locator.ior \
 -e JACORB_HOME=${JACORB_HOME} \
 -e ACE_ROOT=${ACE_ROOT} \
 -e TAO_ROOT=${TAO_ROOT} \
 -e LD_LIBRARY_PATH=${ACE_ROOT}/lib \
 -e DYLD_LIBRARY_PATH=${ACE_ROOT}/lib \
 -e PATH=${JACORB_HOME}/bin:${ACE_ROOT}/bin:${TAO_ROOT}/bin:${PATH} \
 -w ${JACORB_HOME}/demo/tao_imr/test \
 -r 3 \
 -l ${host} \
 -c "/usr/bin/java -Djava.endorsed.dirs=${JACORB_HOME}/lib -Djacorb.home=${JACORB_HOME} -Dorg.omg.CORBA.ORBClass=org.jacorb.orb.ORB -Dorg.omg.CORBA.ORBSingletonClass=org.jacorb.orb.ORBSingleton -classpath :${JACORB_HOME}/classes:${JACORB_HOME}/demo/tao_imr/build/classes demo.tao_imr.Server server.ior 60"
