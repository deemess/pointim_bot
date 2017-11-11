FROM maven:3-jdk-8-alpine
LABEL name=paskal/pointim_bot
LABEL maintainer="paskal.07@gmail.com"

WORKDIR /usr/src
RUN apk --no-cache --update add ca-certificates wget && \
	update-ca-certificates && \
	wget https://github.com/deemess/pointim_bot/archive/master.zip && \
	unzip master.zip && \
	cd /usr/src/pointim_bot-master && \
	mvn package
WORKDIR /usr/src/pointim_bot-master/target

ENTRYPOINT java -Xms64M -Xmx64M -jar pointim_bot-1.0-SNAPSHOT.jar ${LOGIN}:${PASSWORD}
