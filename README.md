# Spring Boot, Hazelcast example
A very simple application demonstrating Spring Boot and Hazelcast integration and how to write integration tests.


To start the API server from Maven:
```
mvn spring-boot:run
```

To start the implementation supporting transactional polling:
```
mvn spring-boot:run -Dspring.profiles.active=transactional-polling
```

