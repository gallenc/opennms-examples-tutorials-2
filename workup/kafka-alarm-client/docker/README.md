## kafka-web-docker profile

This project uses the [io.fabric8 docker-maven-plugin](https://github.com/fabric8io/docker-maven-plugin/) to build docker containers for the web client and simple message client.
You need docker (or docker desktop) installed for this to work.

The built images need a properties file injected to /tmp/kafkaclient.properties  but will use a default properties if this isnt provided.

In the parent project you can use the profile command to run this module

```
mvn clean install -P docker
```
Alternatively, you can build and run the images directly in docker using the following commands.
(Note that the run -t -i options allow you to use Ctrl-c to terminate the images)

```
docker build -t opennms:kafka-client -f .\Dockerfile-client .

docker run -t -i opennms:kafka-client

```

```
docker build -t opennms:kafka-web -f .\Dockerfile-web .

docker run -t -i opennms:kafka-web

```

if working the kafka-web container will provide a simple web ui at 

http://localhost:8081

or 

http://[::1]:8081
