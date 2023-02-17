# oci-arch-queue-demo

[![License: UPL](https://img.shields.io/badge/license-UPL-green)](https://img.shields.io/badge/license-UPL-green) [![Quality gate](https://sonarcloud.io/api/project_badges/quality_gate?project=oracle-devrel_oci-arch-queue-demo)](https://sonarcloud.io/dashboard?id=oracle-devrel_oci-arch-queue-demo)

## Introduction
This repository has been built to provide an illustration of using OCI Queue.

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
- Accommodate the possibility of future improvements so that OCI queue clients in other languages can be built and included. such as Python or Go for example.



## Getting Started

To run the application as a single file Java application (the simplest deployment model). The [prerequisites](#Prerequisites) described below are needed. Other than suitable user credentials and privileges, nothing else is needed.  The application can be run from the command line with the command (once the groovy file has its extension changed to .java).

`java -source XYZ.java`

To make execution really simple, we have provided a shell script (.bat and .sh for Windows and Linux, respectively) which sets up the environment variables. By default, this will execute as a message generator (send). But the other operations can be requested by adding the action name at the end of the command.   The available actions are:

| Parameter   | Behavior                                                     |
| ----------- | ------------------------------------------------------------ |
| send        | This gets the application to run as a message provider pushing messages onto a queue. If the OCID is provided, then that is used otherwise, a new queue is created. |
| send-new    | Irrespective of whether an OCID for a queue is provided, a new queue is created. |
| consume     | This starts the application as a message consumer reading and removing the messages. Each consumed message is displayed on the console and, subject to configuration, will return the receipt. |
| delete-ocid | The application will attempt to delete the queue OCID provided in the configuration file. An additional parameter can be accepted, overriding the OCID provided in the environment variable settings. |
| delete      | The application will attempt to delete the first queue it finds using the Queue name provided in the configuration file. |
| list        | This will get the application to list all the queues within the compartment specified. |
| info        | This will provide information for the specific queue         |

Using the provided script means we can run with Groovy  or Java using the command:

`standalone-queue.bat groovy consume`

or

`standalone-queue.bat java consume`

As the script passes the configuration values using environment variables - the script also accepts a single parameter of `reset`, which will trigger the script to clear all the environment variables.

### Prerequisites

The following resources are required to run this application:

- Java 11 or later (to run as a single file Java application), Java 8 or later to run as Groovy (solution - as intended for [LogGenerator](https://github.com/mp3monster/LogGenerator)).
- Java  OCI SDK needs to be downloaded - we have assumed that the [Full SDK download](https://github.com/oracle/oci-java-sdk/releases/download/v2.51.0/oci-java-sdk-2.51.0.zip) is unzipped into a subfolder called `oci`  (therefore, the dependent jars should be in `./oci/lib`). If you want to use a different location, the script's classpath statement needs to be amended.
- An `OCI.properties` file configured with credentials allowing the user to manage queues in a compartment.
- Environment variables are set up to direct the app's behavior. Parameters are detailed below.
- For Linux deployments, the shell script needs to be made executable (`chmod a+x *.sh`).

#### Environment Variables - Configuration

| Variable Name      | Variable Description                                         | Value                                 |
| ------------------ | ------------------------------------------------------------ | ------------------------------------- |
| CLASSPATH          | This needs to include the location of the SDK jars, including the 3rd party jars it depends upon. If the OCI SDK download has been unpacked into the current directory, this will look like the value provided | ./oci/lib/*;./oci/third-party/lib/*;. |
| QUEUENAME          | The display name to use with the Queue when created. Creation only happens if no QueueOCID is provided.  When running as a consumer, if this is provided without the OCID, then the 1st occurrence of a queue with this name will be used. | myTest                                |
| QUEUEOCID          | The OCID for an existing Queue to be used. In the send mode, if this is provided, then the queue isn't created. We simply connect to this queue. | ocid1.queue.oc1.iad.aaaa.....bbbbb    |
| OCICONFIGFILE      | The location of the properties file that can be used by the SDK to authenticate with OCI. | oci.properties                        |
| QUEUECOMPARTMENTID | The OCID for the compartment in which the queue operates.    | ocid1.queue.oc1.iad.aaaa.....bbbbb    |
| REGION             | The name of the region being used for the querues. Needs to be set to ensure we talk to the correct region | us-ashburn-1 |
| VERBOSE            | This controls how much information is displayed on the console - if you're using the application to help understand how things work, then we recommend having this set to true. any other value will switch off the console logging. | true                                  |
| JSONFMT            | Tells the application to generate its messages using a JSON format. If not set to true, then the message is simply plaintext | true                                  |
| MAXGETS                    | The number of times the application will loop through and try to consume messages. If not set, then the loop will run infinitely | 10 |
|POLLDURATIONSECS | The queue read can operate with long polling to retrieve messages. This value defines how long the poll session can wait for in seconds before returning. If unset then the API will call retrieve what is immediately available, and return. | 10 |
|INTERREADELAYSECS | This imposes a delay between message read API calls. If unset, then the read logic will immediately poll OCI Queue again. | 5 |
|DELETEDURATIONSECS | To demonstrate the ability to change the visibility control of a message. This value, when set, will be used for messages being read. If set, the reading process will also pause this long before invoking the deletion command as well. If not set, then the visibility setting is not amended on a message. | 20 |
|DLQCOUNT | Provides a value for the queue creation for the Dead Letter queue size. If not set, then no DLQ will be set. | 100 |
|RETENTIONSECONDS | The time in seconds that will be used to tell OCI Queue how long a message should be retained before deleting the message. A default value is applied if this isn't set | 2400 |
| BATCHSIZE                       | This sets the number of events to send to OCI at a time. If unset, then each log event is individually sent to OCI | 1 |





## Notes/Issues

None

## URLs
* [OCI Queue Product Page](https://www.oracle.com/cloud/queue/)
* [Java 8](https://www.oracle.com/uk/java/technologies/javase/javase8-archive-downloads.html) , [Java 11](https://www.oracle.com/uk/java/technologies/javase/jdk11-archive-downloads.html)
* [Apache Groovy](https://groovy-lang.org/)
* [OCI Java SDK](https://docs.oracle.com/en-us/iaas/Content/API/SDKDocs/javasdk.htm) guidance and guidance for JavaSDK on the [OCI Cloud Shell](https://docs.oracle.com/en-us/iaas/Content/API/Concepts/cloudshellquickstart_java.htm#Cloud_Shell_Quick_Start_SDK_for_Java) - for example [this download](https://github.com/oracle/oci-java-sdk/releases/download/v2.51.0/oci-java-sdk-2.51.0.zip).
* [LogSimulator](https://github.com/mp3monster/LogGenerator) aka [LogGenerator](https://github.com/mp3monster/LogGenerator)

## Contributing
This project is open source.  Please submit your contributions by forking this repository and submitting a pull request!  Oracle appreciates any contributions that are made by the open-source community.

## License

Copyright (c) 2022 Oracle and/or its affiliates.

Licensed under the Universal Permissive License (UPL), Version 1.0.

See [LICENSE](LICENSE) for more details.

ORACLE AND ITS AFFILIATES DO NOT PROVIDE ANY WARRANTY WHATSOEVER, EXPRESS OR IMPLIED, FOR ANY SOFTWARE, MATERIAL OR CONTENT OF ANY KIND CONTAINED OR PRODUCED WITHIN THIS REPOSITORY, AND IN PARTICULAR SPECIFICALLY DISCLAIM ANY AND ALL IMPLIED WARRANTIES OF TITLE, NON-INFRINGEMENT, MERCHANTABILITY, AND FITNESS FOR A PARTICULAR PURPOSE.  FURTHERMORE, ORACLE AND ITS AFFILIATES DO NOT REPRESENT THAT ANY CUSTOMARY SECURITY REVIEW HAS BEEN PERFORMED WITH RESPECT TO ANY SOFTWARE, MATERIAL OR CONTENT CONTAINED OR PRODUCED WITHIN THIS REPOSITORY. IN ADDITION, AND WITHOUT LIMITING THE FOREGOING, THIRD PARTIES MAY HAVE POSTED SOFTWARE, MATERIAL OR CONTENT TO THIS REPOSITORY WITHOUT ANY REVIEW. USE AT YOUR OWN RISK. 