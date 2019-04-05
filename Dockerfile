FROM maven:3.6.0-jdk-11-slim as compiler
WORKDIR /usr/src/app
COPY . .
ENV MAVEN_OPTS="-Xmx512m -XX:MaxPermSize=256m"
RUN mvn -B clean package

FROM openjdk:11.0.2-jdk
COPY --from=compiler /usr/src/app/target/*.jar /usr/src/application.jar
ENTRYPOINT ["java", "-jar", "/usr/src/application.jar", "-server -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -XX:MaxRAMFraction=1 -XshowSettings:vm -Xms512M -Xmx512M"]

