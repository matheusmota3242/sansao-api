# Build de produção do Simão (multi-stage): compila o jar e roda só o JRE.
# Usado pelo GitHub Actions para publicar a imagem no GHCR.

# --- stage 1: build ---
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /build

# Baixa dependências primeiro (camada cacheável enquanto o pom não muda)
COPY pom.xml .
RUN mvn -q -e -B dependency:go-offline

# Compila e empacota o fat jar
COPY src ./src
RUN mvn -q -B -DskipTests package

# --- stage 2: runtime ---
FROM eclipse-temurin:25-jre
WORKDIR /app
ENV TZ=America/Sao_Paulo

# *.jar pega só o fat jar (o Spring Boot gera .jar.original, que não casa)
COPY --from=build /build/target/*.jar app.jar

EXPOSE 8080

# MaxRAMPercentage respeita o limite de memória do container automaticamente
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
