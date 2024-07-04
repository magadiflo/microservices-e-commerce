# Order Microservice

---

## Dependencias

````xml
<!--Spring Boot 3.3.1-->
<!--Java 21-->
<!--spring-cloud.version 2023.0.2-->
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-config</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-openfeign</artifactId>
    </dependency>

    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
````

## Configura aplicación

Vamos a agregar las propiedades de configuración al `application.yml` y además crear el archivo `order-service.yml` que
estará ubicado en el directorio `/config` del servidor de configuraciones.

````yml
spring:
  application:
    name: order-service
  config:
    import: optional:configserver:http://localhost:8888
````

Como decía, crearemos el archivo `infrastructure/config-server/src/main/resources/config/order-service.yml` en el
directorio del servidor de configuraciones:

````yml
server:
  port: 8083
  error:
    include-message: always

spring:
  datasource:
    url: jdbc:postgresql://localhost:5435/db_order_service
    username: magadiflo
    password: magadiflo

  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        format_sql: true

eureka:
  instance:
    prefer-ip-address: true
    instance-id: ${spring.application.name}:${vcap.application.instance_id:${spring.application.instance_id:${random.value}}}
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/

custom:
  config:
    customer-url: http://localhost:8081/api/v1/customers
    product-url: http://localhost:8082/api/v1/products
    payment-url: http://localhost:8084/api/v1/payments

logging:
  level:
    org.hibernate.SQL: DEBUG
````

Notar que hemos agregado configuraciones personalizadas (`custom`). Nuestro microservicio de órdenes se va a comunicar
con varios microservicios, por eso definimos la url de cada uno de ellos.

