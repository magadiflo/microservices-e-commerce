# Payment Service

---

## Dependencias

````xml
<!--Spring Boot 3.3.1-->
<!--Java 21-->
<!--spring cloud version 2023.0.2-->
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
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka</artifactId>
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
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
````

## Agrega propiedades de configuración

En el `application.yml` del microservicio `payment-service` agregamos las siguientes propiedades.

````yml
spring:
  application:
    name: payment-service
  config:
    import: optional:configserver:http://localhost:8888
````

Ahora, en el `payment-service.yml` del servidor de configuraciones agregamos las configuraciones finales de este
microservicio, eso incluye las configuraciones de la aplicación como productor hacia kafka.

````yml
server:
  port: 8084
  error:
    include-message: always

spring:
  datasource:
    url: jdbc:postgresql://localhost:5435/db_payment_service
    username: magadiflo
    password: magadiflo

  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        format_sql: true

  kafka:
    producer:
      bootstrap-servers: localhost:9092
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer

eureka:
  instance:
    prefer-ip-address: true
    instance-id: ${spring.application.name}:${vcap.application.instance_id:${spring.application.instance_id:${random.value}}}
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/

logging:
  level:
    org.hibernate.SQL: DEBUG
````

En las configuraciones estamos definiendo la base de datos `db_payment_service`, base de datos que debemos crear antes
de ejecutar la aplicación.

## Crea entidad Payment

Antes de crear la entidad `Payment` debemos crear el enum `PaymentMetod`:

````java
public enum PaymentMethod {
    PAYPAL, CREDIT_CARD, VISA_CARD, MASTER_CARD, BITCOIN
}
````

Ahora, procedemos a crear la entidad `Payment`:

````java

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "payments")
@EntityListeners(AuditingEntityListener.class)
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private Long orderId;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createDate;

    @LastModifiedDate
    @Column(insertable = false)
    private LocalDateTime lastModifiedDate;
}
````

Nuestra entidad `Payment` llevará anotaciones de auditoría `@CreatedDate` y `@LastModifiedDate`. Para que estas
anotaciones funcionen le agregamos la anotación `@EntityListeners(AuditingEntityListener.class)` a nivel de clase de la
entidad. Además, debemos agregarle una última anotación en la clase principal del microservicio `@EnableJpaAuditing`.

````java

@EnableJpaAuditing
@SpringBootApplication
public class PaymentServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}
````

## Crea dtos

Los dtos usados en este microservicio serán las siguientes:

````java
public record PaymentNotification(String orderReference,
                                  BigDecimal amount,
                                  PaymentMethod paymentMethod,
                                  String customerFirstName,
                                  String customerLastName,
                                  String customerEmail) {
}
````

````java
public record Customer(@NotBlank(message = "El nombre es requerido")
                       String firstName,

                       @NotBlank(message = "El apellido es requerido")
                       String lastName,

                       @NotBlank(message = "El correo es requerido")
                       @Email(message = "El correo no tiene un formato válido")
                       String email) {
}
````

````java
public record PaymentRequest(@NotNull(message = "El monto es requerido")
                             @Positive(message = "El monto debe ser positivo")
                             BigDecimal amount,

                             @NotNull(message = "El método de pago es requerido")
                             PaymentMethod paymentMethod,

                             @NotNull(message = "La id de la orden es requerido")
                             Long orderId,

                             String orderReference,

                             @Valid
                             @NotNull(message = "Debe ingresar datos del cliente")
                             Customer customer) {
}
````
