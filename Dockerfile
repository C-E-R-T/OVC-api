# build
FROM gradle:8.5-jdk17 AS build
WORKDIR /app

# 설정 파일 복사
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
RUN gradle clean build -x test --no-daemon > /dev/null 2>&1 || true

# 소스 코드 복사 및 실제 빌드
COPY src ./src
RUN gradle bootJar -x test --no-daemon

# runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 빌드 결과물 복사
COPY --from=build /app/build/libs/*-SNAPSHOT.jar app.jar


EXPOSE 8080


ENTRYPOINT ["java", "-jar", "app.jar"]