# Build de produção do Simão (multi-stage): compila o jar e roda só o JRE.
# Usado pelo GitHub Actions para publicar a imagem no GHCR.
#
# A imagem sai multi-arquitetura (amd64 + arm64) porque a VM Always Free da
# Oracle é Ampere A1, aarch64: uma imagem só amd64 nem baixa lá, o pull morre
# em "no matching manifest for linux/arm64/v8".

# --- stage 1: build ---
# Preso ao $BUILDPLATFORM de propósito: o stage roda SEMPRE na arquitetura do
# runner, nunca emulado. Pode, porque o jar é bytecode e não tem arquitetura —
# o mesmo artefato serve os dois alvos. Sem esse pin, o buildx roda o Maven
# inteiro dentro de QEMU pro alvo arm64 e o CI passa de minutos a dezenas deles.
FROM --platform=$BUILDPLATFORM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /build

# Baixa dependências primeiro (camada cacheável enquanto o pom não muda)
COPY pom.xml .
RUN mvn -q -e -B dependency:go-offline

# Compila e empacota o fat jar
COPY src ./src
RUN mvn -q -B -DskipTests package

# --- stage 2: runtime ---
# Este sim é resolvido por plataforma alvo (o JRE é binário nativo). Só tem
# COPY/ENV/ENTRYPOINT, nenhum RUN, então nada precisa ser executado na arquitetura
# estrangeira — é por isso que o build cruzado dispensa QEMU.
FROM eclipse-temurin:25-jre
WORKDIR /app
ENV TZ=America/Sao_Paulo

# *.jar pega só o fat jar (o Spring Boot gera .jar.original, que não casa)
COPY --from=build /build/target/*.jar app.jar

EXPOSE 8080

# MaxRAMPercentage respeita o limite de memória do container automaticamente
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
