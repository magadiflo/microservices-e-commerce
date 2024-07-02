# Discovery Server

- [spring-microservices-in-action-2021](https://github.com/magadiflo/spring-microservices-in-action-2021/blob/main/06.on-service-discovery.md)

---

## Dependencias

````xml
<!--Spring Boot 3.3.1-->
<!--Java 21-->
<!--spring-cloud.version2023.0.2-->
<dependencies>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-config</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
        <exclusions>
            <exclusion>
                <groupId>commons-logging</groupId>
                <artifactId>commons-logging</artifactId>
            </exclusion>
        </exclusions>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
````

**NOTA**

> En las dependencias anteriores estoy excluyendo el `commons-logging` de `spring-cloud-starter-netflix-eureka-server`,
> esto lo hago porque cuando ejecuté esta aplicación me mostró un mensaje en consola diciendo que elimine dicha
> dependencia para evitar posibles conflictos.
>
> Para más información sobre este error, visitar el enlace de las referencias de `spring-microservices-in-action-2021`.

````bash
Standard Commons Logging discovery in action with spring-jcl: please remove commons-logging.jar from classpath in order to avoid potential conflicts
````

## Habilita servidor de descubrimiento

Para habilitar esta aplicación como un servidor de eureka debemos agregar la anotación `@EnableEurekaServer`:

````java

@EnableEurekaServer
@SpringBootApplication
public class DiscoveryServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DiscoveryServerApplication.class, args);
    }

}
````

## Conecta Discovery Server con Spring Config Server

Las configuraciones que crearemos para `Eureka Server` estarán en el servidor de configuraciones, para poder obtenerlas,
demos agregar la siguiente configuración `spring.config.import` y definir la url donde está corriendo el servidor
de configuraciones.

````yml
spring:
  application:
    name: discovery-server
  config:
    import: optional:configserver:http://localhost:8888
````

**IMPORTANTE**

> Toda aplicación debería tener definido su nombre en la configuración `spring.application.name`, dado que los archivos
> que crearemos en el servidor de configuraciones para cada microservicio tendrán como prefijo precisamente el nombre
> de cada microservicio.

## Agrega configuraciones en el Spring Config Server

En el servidor de configuraciones vamos a agregar un archivo de configuración para nuestro discovery server. El archivo
deberá tener como prefijo el nombre del microservicio correspondiente, en este caso, el nombre del discovery server.

`infrastructure/config-server/src/main/resources/config/discovery-server.yml`

````yml
server:
  port: 8761
  error:
    include-message: always

eureka:
  instance:
    hostname: localhost
  client:
    register-with-eureka: false
    fetch-registry: false
    service-url:
      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
````

Si hasta este punto ejecutamos las aplicaciones en el orden siguiente:

1. Config Server
2. Discovery Server

Debemos observar que todo se ejecuta correctamente, el discovery server se ejecuta en el puerto 8761.