# Étape 1 : Build (Compilation)
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copier le fichier de configuration Maven et les sources
COPY pom.xml .
COPY src ./src

# Compiler le projet et ignorer les tests pour gagner du temps
RUN mvn clean package -DskipTests

# Étape 2 : Runtime (Exécution)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copier uniquement le fichier .jar généré depuis l'étape précédente
COPY --from=build /app/target/*.jar app.jar

# Exposer le port par défaut de Spring Boot
EXPOSE 8080

# Lancer l'application
ENTRYPOINT ["java", "-jar", "app.jar"]