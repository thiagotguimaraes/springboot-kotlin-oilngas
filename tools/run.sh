#!/bin/bash

./mvnw clean install
./mvnw flyway:repair
./mvnw flyway:migrate
./mvnw spring-boot:run