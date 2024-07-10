# Gateway Server

**Referencias**

- [spring-microservices-in-action-2021](https://github.com/magadiflo/spring-microservices-in-action-2021/blob/main/08.service-routing-with-spring-cloud-gateway.md)
- [ReactiveLoadBalancerClientFilter](https://docs.spring.io/spring-cloud-gateway/reference/spring-cloud-gateway/global-filters.html#reactive-loadbalancer-client-filter)

---

## Dependencias

````xml
<!--Java 21-->
<!--Spring Boot 3.3.1-->
<!--Spring Cloud Version 2023.0.2-->
<dependencies>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-config</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-gateway</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>io.projectreactor</groupId>
        <artifactId>reactor-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
````

## Agrega propiedades de configuración

En el `application.yml` del servidor Gateway agregamos la siguiente configuración:

````yml
spring:
  application:
    name: gateway-server
  config:
    import: optional:configserver:http://localhost:8888
````

En el servidor de configuraciones agregamos la configuración para el servidor gateway en el
archivo `gateway-server.yml`:

````yml
server:
  port: 8080
  error:
    include-message: always

spring:
  cloud:
    gateway:
      routes:
        - id: customer-service
          uri: lb://customer-service
          predicates:
            - Path=/api/v1/customers/**

        - id: product-service
          uri: lb://product-service
          predicates:
            - Path=/api/v1/products/**

        - id: order-service
          uri: lb://order-service
          predicates:
            - Path=/api/v1/orders/**

        - id: order-line-service
          uri: lb://order-service
          predicates:
            - Path=/api/v1/order-lines/**

        - id: payment-service
          uri: lb://payment-service
          predicates:
            - Path=/api/v1/payments/**

eureka:
  instance:
    prefer-ip-address: true
    instance-id: ${spring.application.name}:${vcap.application.instance_id:${spring.application.instance_id:${random.value}}}
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
````

Si la URL tiene un esquema `lb` (como `lb://customer-service`), utiliza el `Spring Cloud ReactorLoadBalancer` para
resolver el nombre (`customer-service` en este ejemplo) a un host y puerto reales y reemplaza el URI en el mismo
atributo. La URL original no modificada se añade a la lista en el
atributo `ServerWebExchangeUtils.GATEWAY_ORIGINAL_REQUEST_URL_ATTR`.

