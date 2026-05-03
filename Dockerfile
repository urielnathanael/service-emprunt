# ============================================================
# STAGE 1 : Build avec Maven
# ============================================================
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copier pom.xml séparément pour profiter du cache Docker
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copier les sources et compiler
COPY src ./src
RUN mvn package -DskipTests -B

# ============================================================
# STAGE 2 : Image finale légère (JRE seulement)
# ============================================================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Métadonnées
LABEL maintainer="bibliotheque-team"
LABEL app="service-emprunt"
LABEL version="0.0.1-SNAPSHOT"

# Copier le JAR depuis le stage build
COPY --from=build /app/target/service-emprunt-0.0.1-SNAPSHOT.jar app.jar

# Port exposé
EXPOSE 8083

# Démarrage avec options JVM optimisées pour container
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
