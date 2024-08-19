# Experimental kafka clients for OpenNMS

## Overview

## building

To build the project without docker use

```
 mvn clean install
```

To build the project and package the clients as docker images use the docker profile.
This will work with a simple docker instlalation or with docker desktop on windows.

```
 mvn clean install -P docker
```

## running

The simplest way to run the example is to run the full docker compose script with the kafka-client profile  

```
docker compose  --profile kafka-client up -d
```
OpenNMS should appear at
http://[::1]:8980
or
http://localhost:8980

The web-client which connects to the kafka broker will be at
http://[::1]:8081
or
http://localhost:8081

A simple log client which shows all of the messages will be visable if you use

```
docker compose  --profile kafka-client logs -f kafka-client
```



## kafka client

This project contains a simple kafka client example with some tests which will work if you run the OpenNMS kafka docker compose

OpenNMS protobuf file is here 
https://github.com/OpenNMS/opennms/blob/opennms-29.0.10-1/features/kafka/producer/src/main/proto/opennms-kafka-producer.proto

However this is not directly used in the OpenNMS build but hard coded into the model here
https://github.com/OpenNMS/opennms/blob/opennms-29.0.10-1/features/kafka/producer/src/main/java/org/opennms/features/kafka/producer/model/OpennmsModelProtos.java

## simple web app

The simple web application uses the client to display a simple web page corresponding to the state of the alarm list in OpenNMS.
See instructions for use in the web folder .
