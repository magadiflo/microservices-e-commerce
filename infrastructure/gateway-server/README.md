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

Hasta este punto hemos finalizado la implementación del `Gateway Server`, es decir, podríamos levantar toda la
aplicación y utilizar el servidor gateway como único punto de entrada para las peticiones y debería funcionar
correctamente.

---

# Configurando microservicios para aplicar Load Balancer

---


Para que nuestros microservicios puedan escalar horizontalmente, es decir, se puedan crear múltiples instancias de
ellos, necesitamos trabajar con balanceo de carga. Observar que en el archivo `gateway-server.yml`, nuestro servidor
`Gateway Server` está trabajando con `Load Balancer` a través del esquema `lb`.

Ahora, si revisamos el diagrama general de nuestra aplicación ubicada en el `README.md` principal veremos que el
microservicio `order-service` hace llamadas internas hacia los microservicios `customer-service`, `product-service` y
`payment-service`. Para que esas llamadas puedan realizarse utilizando `Load Balancer` debemos hacer una pequeña
modificación en la generación del bean del `RestClient`, así como modificar los puertos de los microservicios.

## Order-service: Agrega anotación @LoadBalanced

````java

@Configuration
public class AppConfig {
    @Bean
    @LoadBalanced
    RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }
}
````

El `@LoadBalanced` es una anotación para marcar `RestTemplate`, `RestClient.Builder` o `WebClient`. El Builder bean se
configurará para utilizar un `LoadBalancerClient`.

## Order-service: Modifica FeignClient

En nuestro microservicio `order-service` estamos haciendo uso de dos clientes http `FeingClient` y `RestClient`. Con
respecto a este último, vimos en el apartado anterior la modificación realizada.

Con respecto al uso de `@FeignClient`, dejaremos únicamente los dos atributos `name` y `path`. En este caso, como
estamos trabajando con `Spring Cloud Load Balancer`, la anotación `@FeignClient` utilizará el load balance a las
solicitudes el backend. El load balanced se puede configurar utilizando el mismo nombre, en nuestro caso estamos
usando el nombre del microservicio al que queremos llamar `customer-service`. Este nombre está definido en la
configuración `custom.config.customer.name` en el archivo de configuración `order-service.yml`.

````java

@FeignClient(name = "${custom.config.customer.name}", path = "${custom.config.customer.path}")
public interface CustomerClient {
    @GetMapping(path = "/{customerId}")
    Optional<CustomerResponse> findCustomer(@PathVariable String customerId);
}
````

## Config-server: Define puertos dinámicos en archivos de configuración

Recordemos que en el microservicio `config-server` tenemos definidos los archivos de configuración de todos nuestros
microservicios. Vamos a modificar los siguientes microservicios con el puerto en cero (0), eso indica que el puerto
se deberá generar de manera aleatoria.

Los archivos de configuración de los microservicios a modificar son: `customer-service.yml`, `notification-service.yml`,
`order-service.yml`, `payment-service.yml` y `product-service.yml`. Cada uno de esos archivos deberá modificar el
puerto con valor en cero, de la siguiente manera:

````yml
server:
  port: 0
````

Con respecto al archivo de configuración del microservicio de order `order-service.yml`, debemos no solo agregar el
puerto en cero, sino que modificar las rutas de los microservicios a llamar:

````yml
server:
  port: 0

custom:
  config:
    customer:
      name: customer-service
      path: /api/v1/customers
    product:
      url: http://product-service
      path: /api/v1/products
    payment:
      url: http://payment-service
      path: /api/v1/payments
````

Notar que en la configuración `custom.config.customer.name` estamos usando el nombre del microservicio a consumir. Aquí
hemos definido a criterio propio el `name`, dado que el valor de esta configuración será usada por el cliente
`FeignClient`. Mientras que las configuraciones de `product` y `payment` los dejamos con `url`, también a criterio
propio, porque estas configuraciones están siendo usadas por `RestClient`.

Notar que ya no usamos `localhost` con el puerto, dado que estmos trabajando con un servidor de descubrimiento como
`Eureka Server`, aquí únicamente usamos el nombre de los microservicios con quienes nos vamos a comunicar.

**Importante**
> Un punto importante en este apartado es que en las configuraciones de `url` que están siendo usadas por el cliente
> `RestClient` debemos usar el `http://` como prefijo, dado que si no lo usamos, es decir, colocamos únicamente el
> nombre del microservicio a consumir, al momento de hacer la llamada nos va a mostrar un mensaje de error, que
> no encuentra la ruta especificada.

