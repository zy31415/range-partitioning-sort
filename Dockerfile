FROM hseeberger/scala-sbt:8u222_1.3.5_2.13.1 AS build

WORKDIR /app

COPY ./ ./
RUN sbt assembly

FROM openjdk:8-jre-alpine

RUN mkdir -p /opt/range-partitioning-sort
WORKDIR /opt/range-partitioning-sort

COPY --from=build /app/target/scala-2.13/range-partitioning-sort-assembly-*.jar range-partitioning-sort.jar

CMD ["java", "-cp", "range-partitioning-sort.jar", "Server"]