# Order Microservice

**Referencias**

- [book-kafka-in-action-2022](https://github.com/magadiflo/book-kafka-in-action-2022/blob/main/README.md)

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
    customer:
      url: http://localhost:8081
      path: /api/v1/customers
    product:
      url: http://localhost:8082
      path: /api/v1/products
    payment:
      url: http://localhost:8084
      path: /api/v1/payments

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

## Define clases para el uso de clientes http: FeignClient y RestClient

En este apartado nos vamos a comunicar con los otros dos microservicios creados: `product-service` y `customer-service`.
Para eso vamos a implementar dos maneras de establecer comunicación con los microservicios, la primera es usando el
`FeignClient` y la segunda usando `RestClient` (en el video usa RestTemplate).

````java

@FeignClient(name = "customer-service", url = "${custom.config.customer.url}", path = "${custom.config.customer.path}")
public interface CustomerClient {
    @GetMapping(path = "/{customerId}")
    Optional<CustomerResponse> findCustomer(@PathVariable String customerId);
}
````

````java

@RequiredArgsConstructor
@Component
public class ProductClient {

    @Value("${custom.config.product.url}")
    private String productUrl;

    @Value("${custom.config.product.path}")
    private String productPath;

    private RestClient restClient;
    private final RestClient.Builder restClientBuilder;

    @PostConstruct
    public void init() {
        this.restClient = this.restClientBuilder.baseUrl(this.productUrl + this.productPath).build();
    }

    public List<PurchaseResponse> purchaseProducts(List<PurchaseRequest> requestBody) {
        return this.restClient.post()
                .uri("/purchase")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    InputStream responseBody = response.getBody();
                    String responseBodyAsString = new String(responseBody.readAllBytes(), StandardCharsets.UTF_8);
                    throw new BusinessException("Error al procesar compra de productos :: " + responseBodyAsString);
                })
                .body(new ParameterizedTypeReference<>() {
                });
    }
}
````

Con respecto al uso de `@FeignClient`, necesitamos habilitar el uso de feing en nuestro proyecto, para eso agregamos
la siguiente anotación en la clase principal del proyecto.

````java

@EnableFeignClients //<-- Habilita el uso de FeignClient
@EnableJpaAuditing
@SpringBootApplication
public class OrderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
````

Con respecto al `RestClient`, crearemos un bean que exponga el `RestClient.Builder`. Aunque leí en la documentación que
Spring Boot crea y preconfigura un prototipo de bean `RestClient.Builder` para nosotros
([leer RestClient](https://docs.spring.io/spring-boot/reference/io/rest-client.html#io.rest-client.restclient)),
en mi caso seré explícito y lo crearé, dado que más adelante usaré la anotación `@LoadBalanced` en este bean para el
tema del balanceo de carga.

````java

@Configuration
public class AppConfig {
    @Bean
    RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }
}
````

## Crea dtos

La implementación que realizaremos requiere un conjunto de dtos tanto para enviar información como para recibirlos.
A continuación se muestran los dtos que usaremos en este microservicio.

````java
public record CustomerResponse(String id,
                               String firstName,
                               String lastName,
                               String email,
                               Address address) {
}
````

````java
public record Address(String street,
                      String houseNumber,
                      String zipCode) {
}
````

````java
public record OrderRequest(String reference,

                           @NotNull(message = "El monto no debe ser nulo")
                           @Positive(message = "El monto debe ser positivo")
                           BigDecimal amount,

                           @NotNull(message = "El método de pago no debe ser nulo")
                           PaymentMethod paymentMethod,

                           @NotBlank(message = "El cliente debe estar presente")
                           String customerId,

                           @NotEmpty(message = "Deberías comprar al menos un producto")
                           List<@Valid PurchaseRequest> products) {
}
````

````java
public record PurchaseRequest(@NotNull(message = "El producto es obligatorio")
                              Long productId,

                              @Positive(message = "La cantidad debe ser positiva")
                              @NotNull(message = "La cantidad es obligatorio")
                              Double quantity) {
}
````

````java
public record PurchaseResponse(Long productId,
                               String name,
                               String description,
                               BigDecimal price,
                               double quantity) {
}
````

````java
public record OrderLineRequest(Long orderId, Long productId, Double quantity) {
}
````

Probablemente, nuestra aplicación lance errores que son propios de la lógica de negocio, en ese sentido crearemos una
excepción personalizada para manejarlo.

````java
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
````

## Crea repositorios para Order y OrderLine

````java
public interface OrderRepository extends JpaRepository<Order, Long> {
}
````

````java
public interface OrderLineRepository extends JpaRepository<OrderLine, Long> {
}
````

## Crea Mappers

Como parte de buenas prácticas crearemos clases que nos ayudarán a mapear entidades a dtos y viceversa:

````java

@Component
public class OrderMapper {
    public Order toOrder(OrderRequest request) {
        return Order.builder()
                .reference(request.reference())
                .totalAmount(request.amount())
                .paymentMethod(request.paymentMethod())
                .customerId(request.customerId())
                .build();
    }
}
````

````java

@Component
public class OrderLineMapper {

    public OrderLine toOrderLine(OrderLineRequest orderLineRequest) {
        return OrderLine.builder()
                .productId(orderLineRequest.productId())
                .quantity(orderLineRequest.quantity())
                .order(Order.builder().id(orderLineRequest.orderId()).build())
                .build();
    }

}
````

## Crea Servicios para Order y OrderLine

````java
public interface OrderService {
    Long createdOrder(OrderRequest request);
}
````

````java
public interface OrderLineService {
    Long saveOrderLine(OrderLineRequest orderLineRequest);
}
````

A continuación se muestra parte de la implementación de la clase de servicio `OrderServiceImpl`. Notar que aún no está
finalizada la implementación, nos faltan algunos servicios que iremos construyendo más adelante.

````java

@RequiredArgsConstructor
@Service
public class OrderServiceImpl implements OrderService {

    private final CustomerClient customerClient;
    private final ProductClient productClient;
    private final OrderRepository orderRepository;
    private final OrderLineService orderLineService;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public Long createdOrder(OrderRequest request) {
        CustomerResponse customerResponse = this.customerClient.findCustomer(request.customerId())
                .orElseThrow(() -> new BusinessException(String.format("No se puede crear la orden. El cliente con id %s no existe", request.customerId())));

        List<PurchaseResponse> purchaseResponses = this.productClient.purchaseProducts(request.products());

        Order orderDB = this.orderRepository.save(this.orderMapper.toOrder(request));

        request.products().forEach(pr -> {
            OrderLineRequest orderLineRequest = new OrderLineRequest(orderDB.getId(), pr.productId(), pr.quantity());
            this.orderLineService.saveOrderLine(orderLineRequest);
        });

        // TODO: start payment process

        // send the order confirmation --> notification-ms (kafka)
        return 0L;
    }

}
````

````java

@RequiredArgsConstructor
@Service
public class OrderLineServiceImpl implements OrderLineService {

    private final OrderLineRepository orderLineRepository;
    private final OrderLineMapper orderLineMapper;

    @Override
    public Long saveOrderLine(OrderLineRequest orderLineRequest) {
        OrderLine orderLine = this.orderLineMapper.toOrderLine(orderLineRequest);
        return this.orderLineRepository.save(orderLine).getId();
    }
}
````

## Crea controlador de Order

Para finalizar la implementación de este apartado, crearemos el controlador que usa el servicio creado anteriormente y
llama a su método `createdOrder`.

````java

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Long> createOrder(@Valid @RequestBody OrderRequest request) {
        Long orderId = this.orderService.createdOrder(request);
        URI uriOrder = URI.create("/api/v1/orders/" + orderId);
        return ResponseEntity.created(uriOrder).body(orderId);
    }

}
````

## Finaliza implementación de Order

En la sección anterior habíamos implementado únicamente el método `createOrder()` para el servicio de orden, incluso
no habíamos finalizado dado que necesitábamos agregar `Kafka`. En esta sección vamos a continuar implementando este
servicio, empezando desde el controlador de Order y a medida que avancemos iremos agregando las dependencias adicionales
como `Kafka`.

````java

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<List<OrderResponse>> findAllOrders() {
        return ResponseEntity.ok(this.orderService.findAllOrders());
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> findOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(this.orderService.findOrder(orderId));
    }

    @PostMapping
    public ResponseEntity<Long> createOrder(@Valid @RequestBody OrderRequest request) {
        Long orderId = this.orderService.createdOrder(request);
        URI uriOrder = URI.create("/api/v1/orders/" + orderId);
        return ResponseEntity.created(uriOrder).body(orderId);
    }

}
````

El controlador anterior usa una interfaz `OrderService` donde definimos los siguientes métodos:

````java
public interface OrderService {
    List<OrderResponse> findAllOrders();

    OrderResponse findOrder(Long orderId);

    Long createdOrder(OrderRequest request);
}
````

Ahora necesitamos implementar los métodos agregados al servicio anterior. Es importante notar que en este apartado
tenemos casi finalizada el método `createdOrder()`, lo único que nos falta implementar es el envío de información
hacia el microservicio de pagos que aún no lo tenemos implementado.

````java

@RequiredArgsConstructor
@Service
public class OrderServiceImpl implements OrderService {

    private final CustomerClient customerClient;
    private final ProductClient productClient;
    private final OrderRepository orderRepository;
    private final OrderLineService orderLineService;
    private final OrderMapper orderMapper;
    private final OrderProducer orderProducer;

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> findAllOrders() {
        return this.orderRepository.findAll().stream()
                .map(this.orderMapper::toOrderResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse findOrder(Long orderId) {
        return this.orderRepository.findById(orderId)
                .map(this.orderMapper::toOrderResponse)
                .orElseThrow(() -> new EntityNotFoundException("No existe la orden con el id " + orderId + " proporcionado"));
    }

    @Override
    @Transactional
    public Long createdOrder(OrderRequest request) {
        CustomerResponse customerResponse = this.customerClient.findCustomer(request.customerId())
                .orElseThrow(() -> new BusinessException(String.format("No se puede crear la orden. El cliente con id %s no existe", request.customerId())));

        List<PurchaseResponse> purchaseProducts = this.productClient.purchaseProducts(request.products());

        Order orderDB = this.orderRepository.save(this.orderMapper.toOrder(request));

        request.products().forEach(pr -> {
            OrderLineRequest orderLineRequest = new OrderLineRequest(orderDB.getId(), pr.productId(), pr.quantity());
            this.orderLineService.saveOrderLine(orderLineRequest);
        });

        // TODO: start payment process

        OrderConfirmation orderConfirmation = new OrderConfirmation(request.reference(), request.amount(),
                request.paymentMethod(), customerResponse, purchaseProducts);
        this.orderProducer.sendOrderConfirmation(orderConfirmation);

        return orderDB.getId();
    }

}
````

## Agrega nuevos dtos y conversión en el OrderMapper

Antes de continuar con la implementación, necesitamos crear algunos dtos que estaremos usando y que hemos usado en el
código anterior.

````java
public record OrderConfirmation(String orderReference,
                                BigDecimal totalAmount,
                                PaymentMethod paymentMethod,
                                CustomerResponse customer,
                                List<PurchaseResponse> products) {
}
````

````java

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class OrderResponse {
    private Long id;
    private String reference;
    private BigDecimal totalAmount;
    private PaymentMethod paymentMethod;
    private String customerId;
}
````

Teniendo los dtos anteriores, vamos a crear un método adicional en el `OrderMapper` para realizar la conversión entre
una entidad `Order` y un dto `OrderResponse`:

````java

@Component
public class OrderMapper {
    public Order toOrder(OrderRequest request) {
        return Order.builder()
                .reference(request.reference())
                .totalAmount(request.amount())
                .paymentMethod(request.paymentMethod())
                .customerId(request.customerId())
                .build();
    }

    public OrderResponse toOrderResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .reference(order.getReference())
                .totalAmount(order.getTotalAmount())
                .paymentMethod(order.getPaymentMethod())
                .customerId(order.getCustomerId())
                .build();
    }
}
````

## Agrega dependencia de Kafka

En el `pom.xml` del `order-service` agregamos la dependencia de `Kafka`:

````xml

<dependencies>
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
````

Ahora, a través de una clase de configuración expondremos un `@Bean` que nos permitirá crear un `topic` a donde
enviaremos la confirmación luego de procesar la orden de compra.

````java

@Configuration
public class KafkaOrderTopicConfig {

    public final static String ORDER_TOPIC = "order-topic";

    @Bean
    public NewTopic topic() {
        return TopicBuilder.name(ORDER_TOPIC).build();
    }
}
````

**NOTA 1**

> El bean `NewTopic` hace que se cree el `topic` en el broker; no es necesario si el topic ya existe.

**NOTA 2**
> Si ejecutamos por primera vez el microservicio sin crear el topic `(NewTopic)`, incluso si este topic no existe en
> `kafka`, recibiremos en la consola del IDE un `WARN` similar a este:
>
> `WARN 12040 --- [demo] [order-service-producer-1] org.apache.kafka.clients.NetworkClient:`<br>
> `[Producer clientId=order-service-producer-1] Error while fetching metadata with correlation id 6 : {order-topic=LEADER_NOT_AVAILABLE}`
>
> Precisamente el `WARN` ocurre porque no existe el topic al que se está llamando, por lo tanto,
> `Spring Boot Apache Kafka` lanza la advertencia y procede a crear el topic por nosotros, es por eso que si ejecutamos
> por segunda vez la aplicación, ahora ya no veremos el warn, dado que ya se ha creado.
>
> En mi caso, prefiero definir el `@Bean NewTopic` para crear el topic de antemano y evitar que lance ese warn, aunque
> eso lo hace solo la primera vez.

Vamos a crear una clase que se encargue de enviar los mensajes a Kafka, de esta manera mantenemos mejor organizada la
aplicación:

````java

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderProducer {

    private final KafkaTemplate<String, OrderConfirmation> kafkaTemplate;

    public void sendOrderConfirmation(OrderConfirmation orderConfirmation) {
        log.info("Enviando confirmación de la orden");
        Message<OrderConfirmation> message = MessageBuilder
                .withPayload(orderConfirmation) //Esto lo definimos en la propiedad spring.json.type.mapping del order-service.yml
                .setHeader(KafkaHeaders.TOPIC, KafkaOrderTopicConfig.ORDER_TOPIC)
                .build();

        this.kafkaTemplate.send(message);
    }
}
````

En la clase anterior estamos pasando un objeto por parámetro llamado `orderConfirmation`. Este nombre de objeto será
el que usaremos en la propiedad `spring.json.type.mapping` del `order-service.yml`. A continuación se muestra el
archivo de configuración `order-service.yml` donde se está usando el objeto `orderConfirmation`.

````yml
spring:
  kafka:
    producer:
      bootstrap-servers: localhost:9092
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      properties:
        spring.json.type.mapping: orderConfirmation:dev.magadiflo.ecommerce.app.models.dtos.OrderConfirmation
````

Los mensajes que se envían a `Kafka` están compuestos por:

- Una marca de tiempo,
- Un valor y
- Una clave opcional.

Para la key y el value necesitamos decirle a `Spring Boot Apache Kafka` cómo serializar los mensajes. Para la:

- `key-serializer`, utilizamos el que nos proporciona la dependencia de Apache
  Kafka `org.apache.kafka.common.serialization.StringSerializer`.


- `value-serializer`, utilizamos el que nos proporciona
  springframework `org.springframework.kafka.support.serializer.JsonSerializer`, dado que enviaremos al servidor de
  kafka objetos y no cadenas de texto. Si solo enviáramos cadenas de texto, podríamos usar el mismo serializador que usa
  el key, pero en nuestro caso enviaremos objetos, por eso necesitamos usar un serializador acorde `JsonSerializer`.


- La configuración `spring.kafka.producer.properties.spring.json.type.mapping` se utiliza para mapear tipos de mensajes
  JSON a clases específicas de tu aplicación. Esto es útil cuando estás trabajando con mensajes JSON en Kafka y deseas
  deserializarlos automáticamente a objetos de tu modelo de datos en lugar de manejar JSON manualmente. De esta manera,
  la biblioteca rellenará el encabezado de tipo con el nombre de clase correspondiente.

- La configuración `spring.json.type.mapping`, está definida por un `token:className`, en nuestro caso lo tenemos
  definido así `orderConfirmation:dev.magadiflo.ecommerce.app.models.dtos.OrderConfirmation`, donde `orderConfirmation`
  es el token y lo que sigue después de los dos puntos el `className`.

- Importante el token que definimos en la configuración anterior (`orderConfirmation`), lo tenemos definido como
  parámetro en el método `sendOrderConfirmation()` de la clase `OrderProducer`. En pocas palabras, es lo que estamos
  enviando como `payload`. Es muy importante que el objeto (token) sea compatible con el className colocado.

## Manejo de errores y excepciones

Vamos a crear una clase global para manejar las excepciones:

````java
public record ErrorResponse(Map<String, String> errors) {
}
````

````java

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handle(EntityNotFoundException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(exception.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<String> handle(BusinessException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handle(MethodArgumentNotValidException exception) {
        var errors = new HashMap<String, String>();
        exception.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            String defaultMessage = error.getDefaultMessage();
            errors.computeIfAbsent(field, fieldKey -> defaultMessage);
        });
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(errors));
    }
}
````

## Implementa el OrderLine

Empezamos implementando el controlador del `OrderLine`:

````java

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/order-lines")
public class OrderLineController {

    private final OrderLineService service;

    @GetMapping(path = "/order/{orderId}")
    public ResponseEntity<List<OrderLineResponse>> findOrderLinesByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(this.service.findAllOrderLinesByOrderId(orderId));
    }

}
````

Continuamos definiendo la interfaz y su posterior implementación:

````java
public interface OrderLineService {
    List<OrderLineResponse> findAllOrderLinesByOrderId(Long orderId);

    Long saveOrderLine(OrderLineRequest orderLineRequest);
}
````

````java

@RequiredArgsConstructor
@Service
public class OrderLineServiceImpl implements OrderLineService {

    private final OrderLineRepository orderLineRepository;
    private final OrderLineMapper orderLineMapper;

    @Override
    public List<OrderLineResponse> findAllOrderLinesByOrderId(Long orderId) {
        return this.orderLineRepository.findAllByOrderId(orderId).stream()
                .map(this.orderLineMapper::toOrderLineResponse)
                .toList();
    }

    @Override
    public Long saveOrderLine(OrderLineRequest orderLineRequest) {
        OrderLine orderLine = this.orderLineMapper.toOrderLine(orderLineRequest);
        return this.orderLineRepository.save(orderLine).getId();
    }
}
````

En la clase anterior estamos haciendo una conversión entre la entidad `OrderLine` y un dto. A continuación nos
sumergiremos dentro de la clase `OrderLineMapper` para ver a detalle la conversión:

````java

@Component
public class OrderLineMapper {

    public OrderLine toOrderLine(OrderLineRequest orderLineRequest) {
        return OrderLine.builder()
                .productId(orderLineRequest.productId())
                .quantity(orderLineRequest.quantity())
                .order(Order.builder().id(orderLineRequest.orderId()).build())
                .build();
    }

    public OrderLineResponse toOrderLineResponse(OrderLine orderLine) {
        return OrderLineResponse.builder()
                .id(orderLine.getId())
                .quantity(orderLine.getQuantity())
                .build();
    }
}
````

En la clase anterior se hace uso del dto siguiente:

````java

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class OrderLineResponse {
    private Long id;
    double quantity;
}
````

Finalmente, creamos el repositorio para el `OrderLine` y definimos un método de consulta usando query methods:

````java
public interface OrderLineRepository extends JpaRepository<OrderLine, Long> {
    List<OrderLine> findAllByOrderId(Long orderId);
}
````
