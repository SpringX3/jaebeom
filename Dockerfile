# [수정 후] Eclipse Temurin (표준 JDK 빌드) 사용
FROM eclipse-temurin:21-jdk-jammy

# 2. 빌드된 JAR 파일을 도커 컨테이너 안으로 복사
COPY build/libs/*-SNAPSHOT.jar app.jar

# 3. 도커가 실행될 때 실행할 명령어
ENTRYPOINT ["java", "-jar", "/app.jar"]