@echo off
set CLASSPATH=./oci/lib/*;./oci/third-party/lib/*;./vertx/*;./lombok/*;.
set VERBOSE=true
set QUEUENAME=demo-02-02-23
set JSONFMT=true
set QUEUEOCID=ocid1.queue.oc1.iad.aaaabbbbccccddddeeeeffff11112222233334444555566667777ababxxx
set OCICONFIGFILE=oci.properties
set REGION=us-ashburn-1
set QUEUECOMPARTMENTID=ocid1.queue.oc1.iad.aaaabbbbccccddddeeeeffff11112222233334444555566667777ababxxx
REM set MAXGETS=1
REM set DELETEDURATIONSECS=20
set POLLDURATIONSECS=3
set INTERREADELAYSECS=4
set DLQCOUNT=7
set RETENTIONSECONDS=2400
set ALLSTATES=FALSE
set TENANCY=ociobenablement
set USERNAME=joe.blogs@oracle.com
set AUTHTOKEN=

echo %1
if [%1]==[reset] goto :reset
if [%1]==[java] goto :java
if [%1]==[groovy] goto :groovy
if [%1]==[stomp] goto :stomp

:reset
REM reset all the environment variables
set CLASSPATH=
set VERBOSE=
set QUEUENAME=
set JSONFMT=
set QUEUEOCID=
set OCICONFIGFILE=
set REGION=
set QUEUECOMPARTMENTID=
set MAXGETS=
set DELETEDURATIONSECS=
set DLQCOUNT=
set RETENTIONSECONDS=
set TENANCY=
set USERNAME=
set AUTHTOKEN=
goto :eof

:groovy
echo 'run as groovy'
groovy SoloOCIQueueDemoTool.groovy %2
goto :eof

:stomp
echo 'run stomp client'
copy SoloOCIQueueStompDemoTool.groovy SoloOCIQueueStompDemoTool.java
 java SoloOCIQueueStompDemoTool.java %2
 REM groovy SoloOCIQueueStompDemoTool.groovy %2
goto :eof

:java
echo 'run as Java'
copy SoloOCIQueueDemoTool.groovy SoloOCIQueueDemoTool.java
java SoloOCIQueueDemoTool.java %2
del SoloOCIQueueDemoTool.java

:eof