# Configuration Server

**Referencias**

- [spring-microservices-in-action-2021](https://github.com/magadiflo/spring-microservices-in-action-2021/blob/main/05.configuration-with-spring-cloud-config-server.md)

---

## Dependencias

````xml
<!--Spring Boot 3.3.1-->
<!--Java 21-->
<!--spring-cloud.version 2023.0.2-->
<dependencies>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-config-server</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
````

## Habilita el config server

En la clase principal agregamos la anotación para habilitar esta aplicación como un servidor de configuraciones de
Spring Cloud Config.

````java

@EnableConfigServer //<-- Habilita aplicación como un servidor de configuraciones
@SpringBootApplication
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }

}
````

## Define ubicación de búsqueda de datos de configuración

Agregamos las siguientes configuraciones al `application.yml`. Observemos que hemos definido como perfil activo el
perfil `native`, esto es porque vamos a usar el sistema de archivos para almacenar información de configuración de
la aplicación. En otras palabras, `native` es solo un perfil para `Spring Cloud Config Server`, lo que indica que los
archivos de configuración se recuperarán o leerán desde el `classpath` o el `filesystem`.

````yml
server:
  port: 8888
  error:
    include-message: always

spring:
  application:
    name: config-server
  profiles:
    active: native
  cloud:
    config:
      server:
        native:
          search-locations: classpath:/config
````

Como estamos trabajando con el perfil `native`, los archivos de configuración lo va a buscar, en nuestro caso,
en el `classpath:/config`, eso es equivalente a la siguiente ubicación `src/main/resources/config`. Por lo tanto,
el directorio `/config` debe ser creada en la ruta anteriormente mencionada. Este directorio va a contener todos los
archivos de configuración de los distintos microservicios de nuestra aplicación. Para más información visitar el
siguiente enlace donde se habla más en detalle sobre el config server
[spring-microservices-in-action-2021](https://github.com/magadiflo/spring-microservices-in-action-2021/blob/main/05.configuration-with-spring-cloud-config-server.md)

