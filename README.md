# Spring Boot Hexagonal Architecture + Kafka Archetype

Este proyecto es un arquetipo (plantilla base) diseñado para construir microservicios robustos en Java utilizando **Arquitectura Hexagonal** (Puertos y Adaptadores) y **Apache Kafka**.

## 🚀 Tecnologías

* **Java 21**
* **Spring Boot 3.3.4**
* **Apache Kafka** (con imágenes oficiales de Confluent)
* **Spring Data JPA** (con H2 Database en memoria para pruebas)
* **Swagger / OpenAPI** (springdoc-openapi)
* **Docker & Docker Compose**

## 🏗️ Arquitectura Hexagonal

El proyecto está estructurado para desacoplar completamente la lógica de negocio (Dominio) de los detalles técnicos (Infraestructura).

### Estructura de Paquetes
```text
src/main/java/com/archetype/kafka/
├── domain/ (El núcleo del negocio)
│   ├── model/           # Entidades del dominio (Transaction)
│   └── port/            # Interfaces de entrada y salida
│       ├── in/          # Casos de uso (API hacia el dominio)
│       └── out/         # Puertos de salida (BBDD, Kafka, APIs externas)
├── application/         # Orquestación de Casos de Uso
│   └── service/         # Implementación de port.in usando port.out
└── infrastructure/      # Adaptadores técnicos (El mundo exterior)
    ├── adapter/
    │   ├── in/          # Entrada a la app (Controladores REST, Listeners Kafka)
    │   └── out/         # Salida de la app (JPA, Productores Kafka, Clientes HTTP)
    └── config/          # Configuración de Spring (Beans, Kafka, Errores)
```

## ⚙️ Características Destacadas

* **Consumidor y Productor Kafka en un solo microservicio**: El servicio expone un endpoint REST para recibir una transacción, la procesa, la guarda en BD, publica un evento en Kafka y **él mismo lo consume** simulando flujos asíncronos completos.
* **Gestión Avanzada de Errores Kafka**: Implementa `DefaultErrorHandler` con política de reintentos locales (`FixedBackOff`).
* **Dead Letter Topic (DLT)**: Los mensajes que fallan todos los reintentos o que sufren errores fatales (ej. `IllegalArgumentException`) son enviados automáticamente a una cola secundaria (`.DLT`) para no bloquear el procesamiento.
* **ACK Manual**: Modo de reconocimiento manual para garantizar cero pérdida de mensajes en caso de caídas.

## 🏁 Inicio Rápido

### 1. Levantar Infraestructura (Kafka + UI)
El proyecto incluye un `docker-compose.yml` preconfigurado con Zookeeper, Kafka y Kafka UI.
```bash
docker-compose up -d
```
* Kafka UI estará disponible en: [http://localhost:8081](http://localhost:8081)

### 2. Arrancar la Aplicación
```bash
mvn spring-boot:run
```
* Swagger UI estará disponible en: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---
> 💡 **Nota**: Para instrucciones detalladas sobre cómo probar los distintos flujos y la gestión de errores, consulta el archivo [PRUEBAS.md](./PRUEBAS.md).
