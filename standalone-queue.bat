@echo off
set CLASSPATH=./oci/lib/*;./oci/third-party/lib/*;.
set VERBOSE=true
set QUEUENAME=test
set JSONFMT=true
set QUEUEOCID=ocid1.queue.oc1.iad.aaaabbbbccccddddeeeeffff11112222233334444555566667777ababxxx
set OCICONFIGFILE=oci.properties
set REGION=us-ashburn-1
set QUEUECOMPARTMENTID=ocid1.queue.oc1.iad.aaaabbbbccccddddeeeeffff11112222233334444555566667777ababxxx
REM set MAXGETS=1
REM set DELETEDURATIONSECS=20
REM set POLLDURATIONSECS=5
REM set DLQCOUNT=0
set RETENTIONSECONDS=2400

echo %1
if [%1]==[reset] goto :reset
if [%1]==[java] goto :java
if [%1]==[groovy] goto :groovy

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
goto :eof

:groovy
echo 'run as groovy'
groovy SoloOCIQueueDemoTool.groovy %2
goto :eof

:java
echo 'run as Java'
copy SoloOCIQueueDemoTool.groovy SoloOCIQueueDemoTool.java
java SoloOCIQueueDemoTool.java %2
del SoloOCIQueueDemoTool.java

:eof