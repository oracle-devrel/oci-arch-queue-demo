#!/bin/bash
export CLASSPATH= $JAVA_HOME/lib/*.jar;./oci/lib/*;./oci/third-party/lib/*;./*
export VERBOSE=true
export QUEUENAME=test
export JSONFMT=true
export QUEUEOCID=ocid1.queue.oc1.iad.aaaabbbbccccddddeeeeffff11112222233334444555566667777ababxxx
export OCICONFIGFILE=oci.properties
export REGION=us-ashburn-1
export QUEUECOMPARTMENTID=ocid1.compartment.oc1..aaaabbbbccccddddeeeeffff11112222233334444555566667777ababxxx
# export MAXGETS=1
# export DELETEDURATIONSECS=20
# export POLLDURATIONSECS=5
# export DLQCOUNT=0
export RETENTIONSECONDS=2400

echo $1

if [ $1 = "reset" ] 
then
  # reset all the environment variables
  echo 'resetting env vars'
  export CLASSPATH=
  export VERBOSE=
  export QUEUENAME=
  export JSONFMT=
  export QUEUEOCID=
  export OCICONFIGFILE=
  export REGION=
  export QUEUECOMPARTMENTID=
  export MAXGETS=
  export DELETEDURATIONSECS=
  export DLQCOUNT=
  export RETENTIONSECONDS=

elif [ $1 = "groovy" ] 
then
  echo 'run as groovy'
  groovy ./SoloOCIQueueDemoTool.groovy $2


elif [ $1 = "java" ] 
then
  echo 'run as Java'
  cp ./SoloOCIQueueDemoTool.groovy ./SoloOCIQueueDemoTool.java
  java SoloOCIQueueDemoTool.java $2
  rm ./SoloOCIQueueDemoTool.java
fi
