# oci-arch-queue-demo

[![License: UPL](https://img.shields.io/badge/license-UPL-green)](https://img.shields.io/badge/license-UPL-green) [![Quality gate](https://sonarcloud.io/api/project_badges/quality_gate?project=oracle-devrel_oci-arch-queue-demo)](https://sonarcloud.io/dashboard?id=oracle-devrel_oci-arch-queue-demo)

## Introduction

This repository has been built to provide code illustrating the use of OCI Queue. To do this we have included two single-file applications which can be controlled from a single script. They are:

* *SoloOCIQueueDemoTool* - this makes use of the Java SDK for OCI Java (which ultimately uses REST APIs)
* *SoloOCIQueueStompDemoTool* - this doesn't use the OCI Java SDK at all but relies upon the Vertx implementation of the STOMP protocol (STOMP being a specialized use of Web Sockets). As a result, this application is simpler in what it can do.

Both examples have been configured to work in a similar manner using the same environment properties where there is a commonality in setup. For the details for configuring and controlling the utilities, we have indicated in tables whether it applies to Queue (SoloOCIQueueDemoTool), Stomp (SoloOCIQueueStompDemoTool), or Both (SoloOCIQueueDemoTool and SoloOCIQueueStompDemoTool).

Both utilities have a lot in common in terms of implementation, although the SoloOCIQueueStompDemoTool has not been proven to also be usable with the open-source [LogGenerator](https://github.com/mp3monster/LogGenerator) project to date.

## Example Documentation

### SoloOCIQueueDemoTool

The single file app provides code to support the following OCI Queue operations through the OCI APIs & SDK:

- Creating an OCI Queue
- Send messages to an OCI Queue.
- Read messages from an OCI Queue.
- Delete OCI Queues.
- List queues in a compartment and inspect a queue's size.

It has been built with the following points in mind -

- Run as a single file Java application - thus not requiring the build tooling, but code can be borrowed/refactored into another Java application.
- Run as a Groovy solution (single file app or as part of the custom plugins to provide a richer set of options for sending messages using the open-source [LogGenerator](https://github.com/mp3monster/LogGenerator) project, which can use message log files to define message payloads (that can be modified during (re)play). The tool can be used to play messages in any text-based format. This does impose certain methods to be created.
- Leverages the OCI Java SDK
- Accommodate the possibility of future improvements so that OCI queue clients in other languages can be built and included, such as Python or Go for example.

#### Executing the demo

Rename the template script file and adjust the parameters as necessary (see below for more detail). Using the copied script means we can run with Groovy  or Java using the command:

`standalone-queue.bat groovy consume`

or

`standalone-queue.bat java consume`

As the script passes the configuration values using environment variables - the script also accepts a single parameter of `reset`, which will trigger the script to clear all the environment variables.

### SoloOCIQueueStompDemoTool

This demonstrates the use of OCI Queue using the [STOMP](https://stomp.github.io/) protocol. in this example, and we're using the [Vertx implementation](https://vertx.io/docs/vertx-stomp/java/). OCI documentation for Queue support for STOMP can be found [here](https://docs.oracle.com/en-us/iaas/Content/queue/messages-stomp.htm).

The Stomp implementation supports running the utility as both sender and consumer for a single message or can be run as the sender or as the consumer.  The Stomp protocol doesn't cover operations for creating and deleting Queues, so these options are not provided with the Stomp option.

#### Executing the demo

Rename the template script file and adjust the parameters as necessary (see below for more detail). Using the renamed script we can start the utility with the command:

`standalone-queue.sh stomp consume`

The script executes the Stomp utility using Java - but this could be modified to be run in its Groovy form.

## Getting Started

To run the application as a single file Java application (the simplest deployment model). The [prerequisites](#Prerequisites) described below are needed. Other than suitable user credentials and privileges, nothing else is needed.  The application can be run from the command line with the command (once the groovy files have their extensions changed to .java).

`java -source XYZ.java`

To make execution really simple, we have provided a shell script (.bat and .sh for Windows and Linux, respectively) which sets up the environment variables. By default, this will execute as a message generator (send). But the other operations can be requested by adding the action name at the end of the command.   The available actions are:

| Parameter   | Behavior                                                     | Applies to |
| ----------- | ------------------------------------------------------------ | ---------- |
| send        | This gets the application to run as a message provider pushing messages onto a queue. If the OCID is provided, then that is used; otherwise, a new queue is created. | Both       |
| send-new    | Irrespective of whether an OCID for a queue is provided, a new queue is created. | Queue      |
| consume     | This starts the application as a message consumer reading and removing the messages. Each consumed message is displayed on the console and, subject to configuration, will return the receipt. | Both       |
| delete-ocid | The application will attempt to delete the queue OCID provided in the configuration file. An additional parameter can be accepted, overriding the OCID provided in the environment variable settings. | Queue      |
| delete      | The application will attempt to delete the first queue it finds using the Queue name provided in the configuration file. | Queue      |
| list        | This will get the application to list all the queues within the compartment specified. | Queue      |
| info        | This will provide information for the specific queue         | Queue      |
| test        | Chains a sequence of connect, subscribe send, consume, unsubscribe calls | Stomp      |
| reset       | Clears down the environment variables that are set.          | Both       |

### Prerequisites

Both utilities require the following:

- Environment variables are set up to direct the app's behavior. The values for these parameters are set up and cleared using the provided script (standalone-queue.[bat|sh].template). Parameters are detailed below. Remove the .template extension and modify the properties as desired.
- For Linux deployments, the shell script must be made executable (`chmod a+x *.sh`).

#### SoloOCIQueueDemoTool

The following resources are required to run the Queue demo:

- Java 11 or later (to run as a single file Java application), Java 8 or later to run as Groovy (solution - as intended for [LogGenerator](https://github.com/mp3monster/LogGenerator)).
- Java  OCI SDK needs to be downloaded - we have assumed that the [Full SDK download](https://github.com/oracle/oci-java-sdk/releases/download/v2.51.0/oci-java-sdk-2.51.0.zip) is unzipped into a subfolder called `oci`  (therefore, the dependent jars should be in `./oci/lib`). If you want to use a different location, the script's classpath statement needs to be amended.
- An `OCI.properties` file configured with credentials allowing the user to manage queues in a compartment.

#### SoloOCIQueueStompDemoTool

This has currently only been proven with Java11, although in theory, it should also work with Java8 and as a Groovy solution.

* The Vertx Stomp client needs to be downloaded into the folder ./vertx this can be done with the command *wget https://repo1.maven.org/maven2/io/vertx/vertx-stomp/3.2.1/vertx-stomp-3.2.1.jar*



#### Environment Variables - Configuration

| Variable Name      | Variable Description                                         | Value                                 | Applies to                       |
| ------------------ | ------------------------------------------------------------ | ------------------------------------- | ------------------ |
| CLASSPATH          | This needs to include the location of the SDK jars, including the 3rd party jars it depends upon. If the OCI SDK download has been unpacked into the current directory, this will look like the value provided | ./oci/lib/*;./oci/third-party/lib/*;. | Both |
| QUEUENAME          | The display name to use with the Queue when created. Creation only happens if no QueueOCID is provided.  When running as a consumer, if this is provided without the OCID, then the 1st occurrence of a queue with this name will be used. | myTest                                | Queue                           |
| QUEUEOCID          | The OCID for an existing Queue to be used. In the send mode, if this is provided, then the queue isn't created. We simply connect to this queue. | ocid1.queue.oc1.iad.aaaa.....bbbbb    | Both |
| OCICONFIGFILE      | The location of the properties file that can be used by the SDK to authenticate with OCI. | oci.properties                        | Queue                   |
| QUEUECOMPARTMENTID | The OCID for the compartment in which the queue operates.    | ocid1.queue.oc1.iad.aaaa.....bbbbb    | Queue |
| REGION             | The name of the region being used for the querues. Needs to be set to ensure we talk to the correct region | us-ashburn-1 | Both |
| VERBOSE            | This controls how much information is displayed on the console - if you're using the application to help understand how things work, then we recommend having this set to true. any other value will switch off the console logging. | true                                  | Both                              |
| JSONFMT            | Tells the application to generate its messages using a JSON format. If not set to true, then the message is simply plaintext | true                                  | Queue                             |
| MAXGETS                    | The number of times the application will loop through and try to consume messages. If not set, then the loop will run infinitely | 10 | Queue |
|POLLDURATIONSECS | The queue read can operate with long polling to retrieve messages. This value defines how long the poll session can wait for in seconds before returning. If unset then the API will call retrieve what is immediately available, and return. | 10 | Queue |
|POSTSENDDELAYSECS | Controls how long the process will wait between sending messages - expressed in seconds. | 5 | Both |
|INTERREADELAYSECS | This imposes a delay between message read API calls. If unset, then the read logic will immediately poll OCI Queue again. | 5 | Queue |
|DELETEDURATIONSECS | To demonstrate the ability to change the visibility control of a message. This value, when set, will be used for messages being read. If set, the reading process will also pause this long before invoking the deletion command as well. If not set, then the visibility setting is not amended on a message. | 20 | Queue |
|DLQCOUNT | Provides a value for the queue creation for the Dead Letter queue size. If not set, then no DLQ will be set. | 100 | Queue |
|RETENTIONSECONDS | The time in seconds that will be used to tell OCI Queue how long a message should be retained before deleting the message. A default value is applied if this isn't set | 2400 | Queue |
| BATCHSIZE                       | This sets the number of events to send to OCI at a time. If unset, then each log event is individually sent to OCI | 1 | Queue |
| USERNAME | The OCI username to connect with Queue. We need to provide this as a config value as we're not using the SDK configuration file format | joe.blogs@oracle.com | Stomp |
| AUTHTOKEN | The user token for the user identified. As with the username - it needs to be supplied as an environment variable, as we're not using the SDK configuration file format. As auth tokens can involve characters, that can be an issue for environment variables. If the environment variable is not set, then the code will try to retrieve the token from a file called authtoken.txt which needs to be in the folder where the app is run from | axcljvhcv!3r | Stomp |
| TENANCY | The name of the tenancy | IamATenant | Stomp |
| TOTALSEND | Number of messages that can be sent. It also controls how many iterations of connecting and pulling messages from the Queue can be performed. | 5 | Stomp |

Examples of running the script - run the simple Stomp test on Windows with *standalone-queue.bat stomp test* run the Java version of the Queue to send on Linux is *./standalone-queue.sh java send*



## Notes/Issues

* Stomp implementation has not been tested as part of the LogGenerator.
* With Stomp there is a known issue with authentication which means IDCS-based authentication doesn't currently work. Until this issue is addressed, the identity provided needs to be an IAM credential. Using an IDCS credential will need the prefix *identitycloudservice/* prefix once the issue is resolved. The problem manifests itself by failing authentication.

## URLs
* [OCI Queue Product Page](https://www.oracle.com/cloud/queue/)
* [Java 8](https://www.oracle.com/uk/java/technologies/javase/javase8-archive-downloads.html) , [Java 11](https://www.oracle.com/uk/java/technologies/javase/jdk11-archive-downloads.html)
* [Apache Groovy](https://groovy-lang.org/)
* [OCI Java SDK](https://docs.oracle.com/en-us/iaas/Content/API/SDKDocs/javasdk.htm) guidance and guidance for JavaSDK on the [OCI Cloud Shell](https://docs.oracle.com/en-us/iaas/Content/API/Concepts/cloudshellquickstart_java.htm#Cloud_Shell_Quick_Start_SDK_for_Java) - for example, [this download](https://github.com/oracle/oci-java-sdk/releases/download/v2.51.0/oci-java-sdk-2.51.0.zip).
* [LogSimulator](https://github.com/mp3monster/LogGenerator) aka [LogGenerator](https://github.com/mp3monster/LogGenerator)
* Vertx Stomp [website](https://vertx.io/docs/vertx-stomp/java/), [GitHub repository](https://github.com/vert-x3/vertx-stomp) Maven central [JAR](https://repo1.maven.org/maven2/io/vertx/vertx-stomp/3.2.1/vertx-stomp-3.2.1.jar)
* [OCI Queue documentation for STOMP.](https://docs.oracle.com/en-us/iaas/Content/queue/messages-stomp.htm)

## Contributing

This project is open source.  Please submit your contributions by forking this repository and submitting a pull request!  Oracle appreciates any contributions that are made by the open-source community.

## License

Copyright (c) 2022 Oracle and/or its affiliates.

Licensed under the Universal Permissive License (UPL), Version 1.0.

See [LICENSE](LICENSE) for more details.

ORACLE AND ITS AFFILIATES DO NOT PROVIDE ANY WARRANTY WHATSOEVER, EXPRESS OR IMPLIED, FOR ANY SOFTWARE, MATERIAL OR CONTENT OF ANY KIND CONTAINED OR PRODUCED WITHIN THIS REPOSITORY, AND IN PARTICULAR SPECIFICALLY DISCLAIM ANY AND ALL IMPLIED WARRANTIES OF TITLE, NON-INFRINGEMENT, MERCHANTABILITY, AND FITNESS FOR A PARTICULAR PURPOSE.  FURTHERMORE, ORACLE AND ITS AFFILIATES DO NOT REPRESENT THAT ANY CUSTOMARY SECURITY REVIEW HAS BEEN PERFORMED WITH RESPECT TO ANY SOFTWARE, MATERIAL OR CONTENT CONTAINED OR PRODUCED WITHIN THIS REPOSITORY. IN ADDITION, AND WITHOUT LIMITING THE FOREGOING, THIRD PARTIES MAY HAVE POSTED SOFTWARE, MATERIAL OR CONTENT TO THIS REPOSITORY WITHOUT ANY REVIEW. USE AT YOUR OWN RISK. 