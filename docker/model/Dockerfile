FROM maven:3.9.9-eclipse-temurin-21-jammy AS build

WORKDIR /app

COPY pom.xml ./
COPY lib ./

# Install Gedinline JAR (local dependency)
RUN mvn install:install-file \
  -Dfile=gedinline-4.0.1.jar \
  -DgroupId=com.github.nigel-parker \
  -DartifactId=gedinline \
  -Dversion=4.0.1 \
  -Dpackaging=jar 

# Copy Java source code and the R analysis
COPY src/main/java src/main/java
COPY src/main/resources/valipop/analysis-r/geeglm src/main/resources/valipop/analysis-r/geeglm

RUN mvn clean package -Dmaven.test.skip=true

# Extract version from pom.xml
RUN VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout) && \
    echo "VERSION=$VERSION" && \
    mv target/valipop-${VERSION}-jar-with-dependencies.jar ./valipop.jar

# Create runtime image
FROM valipop-base AS run

COPY --from=build /app/valipop.jar ./valipop.jar
COPY docker/model/entrypoint.sh .

ENTRYPOINT [ "./entrypoint.sh" ]
