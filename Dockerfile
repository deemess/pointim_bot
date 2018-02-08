# Build
FROM maven:3-jdk-8-alpine AS build
LABEL name=paskal/pointim_bot
LABEL maintainer="paskal.07@gmail.com"

WORKDIR /usr/src
RUN apk --no-cache --update add ca-certificates wget && \
	update-ca-certificates && \
	wget https://github.com/deemess/pointim_bot/archive/master.zip && \
	unzip master.zip && \
	cd /usr/src/pointim_bot-master && \
	mvn package

# Run
FROM openjdk:8-jdk-alpine

COPY --from=build /usr/src/pointim_bot-master/target /usr/pointim_bot
# dirty hack for https://github.com/deemess/pointim_bot/issues/10
COPY --from=build /usr/src/pointim_bot-master/log4j.xml /usr/pointim_bot/

WORKDIR /usr/pointim_bot

RUN apk add --update ca-certificates && update-ca-certificates

# $LOGIN and $PASSWORD taken from env inside docker
ENTRYPOINT java -Xms64M -Xmx64M -jar pointim_bot-1.0-SNAPSHOT.jar ${LOGIN}:${PASSWORD}
