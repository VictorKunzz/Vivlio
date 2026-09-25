# Ambiente de desenvolvimento do backend.
#
# O JDK e fixado em 17 de proposito. O pom declara <java.version>17</java.version>,
# e o Lombok 1.18.34 que o Spring Boot 3.3.5 resolve nao suporta JDK 23: com um JDK
# mais novo o processador de anotacoes falha em silencio e a compilacao quebra em
# "cannot find symbol getId()". Fixar aqui e no CI e o que impede isso.
#
# O Maven nao e instalado: vem do wrapper versionado no repositorio (3.9.9).

FROM eclipse-temurin:17-jdk

WORKDIR /app

# Dependencias antes do codigo: esta camada so e reconstruida quando o pom muda.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw -B -ntp dependency:go-offline

COPY src/ src/

EXPOSE 8080

CMD ["./mvnw", "-B", "-ntp", "spring-boot:run"]
