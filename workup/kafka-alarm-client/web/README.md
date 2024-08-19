# Alarm List Viewer Kafka application

This is a very simple web application which uses the kafka client to view a list of alarms

## building
in parent project build using maven

```
mvn clean install
```

## running

you can run the built executable war directly in the IDE or on the command line ( in web/target directory)

```
java -jar .\kafka-web-0.0.1-SNAPSHOT-exec.war
```
or you can run using the maven spring boot plugin in the web module

```
mvn spring-boot:run
```

The ui is at http://localhost:8081/viewAlarms


# testing

You can send alarm raising and alarm clearing traps to the OpenNMS horizon instance



Using a tool such as  ireasoning mibbrowser (https://www.ireasoning.com/mibbrowser.shtml) you can send traps to localhost:10162

On docker desktop on windows, these traps will appear to opennms horizon  to have come from the host at 172.23.0.1

# testing using docker netsnmp

Alternatively using netsnmp in a  simple docker container (https://hub.docker.com/r/elcolio/net-snmp) you can  send the trap directly to the horizon container

to get the internal address of the horizon instance use

```
 docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' horizon
```

But you can actually use the 'horizon' name in the following commands and docker dns will resolve the address for you.

The following examples show how to use the normal netsnmp snmptrap command with this image. 
 

```
docker run --rm --network=minimal-minion-kafka_default elcolio/net-snmp snmptrap -v 2c -c public horizon:1162 0000 ....
```

The critical docker command options are

```
--rm removes the docker container at end of command (stops filling up your disk with dead images)

--network=minimal-minion-kafka_default makes the detached container join the network used in docker compose

horizon:1162 specifies to send the trap to port 1162 on the horizon instance within the docker compose network

0000 is simply the system up time - the normal alternative '' doesn't work in this docker image.
```

Examples below

#### panMotor raise

```
docker run --rm=true --network=minimal-minion-kafka_default elcolio/net-snmp snmptrap -v 2c -c public horizon:1162 0000 .1.3.6.1.4.1.52330.6.2.0.1        .1.3.6.1.4.1.52330.6.2.1.0  s xxxx   .1.3.6.1.4.1.52330.6.2.1.0 i 0  .1.3.6.1.4.1.52330.6.2.5.0 i 1
```

#### panMotor clear

```
docker run --rm=true --network=minimal-minion-kafka_default elcolio/net-snmp snmptrap -v 2c -c public horizon:1162  0000 .1.3.6.1.4.1.52330.6.2.0.1        .1.3.6.1.4.1.52330.6.2.1.0  s xxxx   .1.3.6.1.4.1.52330.6.2.1.0 i 0  .1.3.6.1.4.1.52330.6.2.5.0 i 0
```


# notes

OpenNMS protobuf file is here 

https://github.com/OpenNMS/opennms/blob/opennms-29.0.10-1/features/kafka/producer/src/main/proto/opennms-kafka-producer.proto

However this is not directly used in the OpenNMS build but hard coded into the model here

https://github.com/OpenNMS/opennms/blob/opennms-29.0.10-1/features/kafka/producer/src/main/java/org/opennms/features/kafka/producer/model/OpennmsModelProtos.java


see https://www.baeldung.com/spring-boot-jsp for web app explaination