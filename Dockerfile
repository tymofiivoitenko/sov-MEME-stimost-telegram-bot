FROM openjdk:14
RUN mkdir /resources
COPY src/main/resources/memes /resources/memes
ADD target/sovmemstimost-bot.jar sovmemstimost-bot.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "sovmemstimost-bot.jar"]