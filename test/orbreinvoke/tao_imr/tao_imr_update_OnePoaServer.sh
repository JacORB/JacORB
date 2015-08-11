#!/bin/bash
bn=${0##*/}
host=$(hostname)
[[ $host == "phil" ]] && host="phil.ociweb.com"
implName=$1
poa="POA"
if [[ -z $implName ]] ; then
    echo "Usage: $bn <EchoServer1 or EchoServer2>"
    exit 1
fi
if [[ $implName != "EchoServer1" && $implName != "EchoServer2" ]] ; then
    echo "Usage: $bn <EchoServer1 or EchoServer2>"
    exit 1
fi

poaBaseName="EchoServer-Parent-POA/EchoServer-POA"

endpoint="iiop://${host}:32000"
if [[ $implName == "EchoServer2" ]] ; then
    endpoint="iiop://${host}:32999"
fi

server_name="org.jacorb.test.orbreinvoke.tao_imr.OnePoaServer"
tao_locator_ior="/tmp/tao_imr_locator.ior"
iorfile="/tmp/${server_name}.${implName}.EchoServer.ior"
APP=tao_imr

#export ImplRepoServiceIOR=corbaloc::${host}:44555/ImR
#export ImplRepoServiceIOR=file:///tmp/tao_imr_locator.ior
$TAO_ROOT/orbsvcs/ImplRepo_Service/${APP} \
 update "JACORB:${implName}/${poaBaseName}" \
 -a NORMAL \
 -e ImplRepoServiceIOR=file://${tao_locator_ior} \
 -e JACORB_HOME=${JACORB_HOME} \
 -e ACE_ROOT=${ACE_ROOT} \
 -e TAO_ROOT=${TAO_ROOT} \
 -e LD_LIBRARY_PATH=${ACE_ROOT}/lib \
 -e DYLD_LIBRARY_PATH=${ACE_ROOT}/lib \
 -e PATH=${JACORB_HOME}/bin:${ACE_ROOT}/bin:${TAO_ROOT}/bin:${PATH} \
 -w "${JACORB_HOME}/test/orbreinvoke/tao_imr" \
 -r 3 \
 -l ${host} \
 -ORBInitRef ImplRepoService=file://${tao_locator_ior} \
 -c "/usr/bin/java -Djava.endorsed.dirs=${JACORB_HOME}/lib -Djacorb.home=${JACORB_HOME} -Dorg.omg.CORBA.ORBClass=org.jacorb.orb.ORB -Dorg.omg.CORBA.ORBSingletonClass=org.jacorb.orb.ORBSingleton -classpath ${JACORB_HOME}/classes:`pwd`/target/target-classes ${server_name} -iorfile ${iorfile} -DOAAddress=${endpoint} -Djacorb.use_tao_imr=on -Djacorb.implname=${implName} -ORBInitRef ImplRepoService=file://${tao_locator_ior}"
