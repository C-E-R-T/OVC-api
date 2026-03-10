# build
FROM gradle:8.5-jdk17 AS build
WORKDIR /app

# 설정 파일 복사
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
RUN gradle dependencies --no-daemon

# 소스 코드 복사 및 실제 빌드
COPY src ./src
RUN gradle bootJar -x test --no-daemon

# runtime stage
FROM eclipse-temurin:17-jre-alpine

RUN apk add --no-cache tzdata
ENV TZ=Asia/Seoul

WORKDIR /app

# 빌드 결과물 복사
COPY --from=build /app/build/libs/*.jar app.jar


EXPOSE 8080


ENTRYPOINT ["java", "-jar", "app.jar"]