#!/bin/bash
bn=${0##*/}
host=$(hostname)
[[ $host == "phil" ]] && host="phil.ociweb.com"
implName=$1
poaBaseName="EchoServer-POA"
if [[ -z $implName ]] ; then
    echo "$bn: Usage: $bn <EchoServer1 or EchoServer2>"
    exit 1
fi
if [[ $implName != "EchoServer1" && $implName != "EchoServer2" ]] ; then
    echo "$bn: Usage: $bn <EchoServer1 or EchoServer2>"
    exit 1
fi

endpoint="iiop://${host}:42000"
if [[ $implName == "EchoServer2" ]] ; then
    endpoint="iiop://${host}:42999"
fi

server_name="test.orbreinvoke.tao_imr.SimpleServer"
iorfile="/tmp/${server_name}.${implName}.${poaBaseName}.ior"
tao_locator_ior="/tmp/tao_imr_locator.ior"
APP="tao_imr"

#export ImplRepoServiceIOR=corbaloc::${host}:44555/ImR
#export ImplRepoServiceIOR=file:///tmp/tao_imr_locator.ior
$TAO_ROOT/orbsvcs/ImplRepo_Service/${APP} \
 add JACORB:${implName}/${poaBaseName} \
 -a NORMAL \
 -e ImplRepoServiceIOR=file://${tao_locator_ior} \
 -e JACORB_HOME=${JACORB_HOME} \
 -e ACE_ROOT=${ACE_ROOT} \
 -e TAO_ROOT=${ACE_ROOT}/TAO \
 -e LD_LIBRARY_PATH=${ACE_ROOT}/lib \
 -e DYLD_LIBRARY_PATH=${ACE_ROOT}/lib \
 -e PATH=${JACORB_HOME}/bin:${ACE_ROOT}/bin:${TAO_ROOT}/bin:${PATH} \
 -w "${JACORB_HOME}/test/orbreinvoke/tao_imr" \
 -r 3 \
 -l ${host} \
 -ORBInitRef ImplRepoService=file://${tao_locator_ior} \
 -c "/usr/bin/java -Djava.endorsed.dirs=${JACORB_HOME}/lib -Djacorb.home=${JACORB_HOME} -Dorg.omg.CORBA.ORBClass=org.jacorb.orb.ORB -Dorg.omg.CORBA.ORBSingletonClass=org.jacorb.orb.ORBSingleton -classpath :${JACORB_HOME}/classes ${server_name} -iorfile ${iorfile} -DOAAddress=${endpoint} -Djacorb.use_tao_imr=on -Djacorb.implname=${implName} -ORBInitRef ImplRepoService=file://${tao_locator_ior}"
