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

## Crea entidades: Order y OrderLine

Antes de crear las entidades definamos una clase de enumeración para los métodos de pagos que serán usados en la entidad
Order:

````java
public enum PaymentMethod {
    PAYPAL, CREDIT_CARD, VISA_CARD, MASTER_CARD, BITCOIN
}
````

Ahora, vamos a crear las entidades con las que trabajaremos en este microservicio `Order` y `OrderLine`:

````java

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "orders")
@EntityListeners(AuditingEntityListener.class)
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String reference;
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createDate;

    @LastModifiedDate
    @Column(insertable = false)
    private LocalDateTime lastModifiedDate;

    private String customerId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderLine> orderLines;
}
````

**DONDE**

- Relacionamos esta entidad con la entidad `OrderLine` usando una relación bidireccional `(@OneToMany - @ManyToOne)`.
- Vamos a trabajar con auditoría, por lo tanto, usaremos las anotaciones `@CreatedDate` y `@LastModifiedDate`. Para que
  estas anotaciones funcionen debemos agregar la anotación `@EntityListeners(AuditingEntityListener.class)` a nivel de
  clase en la entidad `Order`. Así mismo, debemos agregar la anotación `@EnableJpaAuditing` en la clase principal de
  este microservicio.

````java

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "order_lines")
public class OrderLine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private double quantity;

    private Long productId;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;
}
````
