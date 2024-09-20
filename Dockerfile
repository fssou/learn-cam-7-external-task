FROM amazoncorretto:17-alpine as build
RUN mkdir /app
WORKDIR /app
COPY . .
RUN chmod +x gradlew
RUN ./gradlew build

FROM amazoncorretto:17-alpine
RUN mkdir /app
WORKDIR /app

COPY --from=build /app/build/libs/*all.jar app.jar

CMD ["java", "-jar", "app.jar"]