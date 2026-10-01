# Guía de Pruebas del Arquetipo

Este documento explica paso a paso cómo probar el flujo completo de la aplicación, interactuando con la API REST y visualizando el comportamiento asíncrono en Kafka.

## 🛠️ Preparación

Antes de empezar, asegúrate de tener todo funcionando:

1. Levanta Docker Compose: `docker-compose up -d`
2. Arranca Spring Boot: `mvn spring-boot:run`
3. Abre la interfaz de Kafka UI: [http://localhost:8081](http://localhost:8081)
4. Abre Swagger UI: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

## 🧪 Escenarios de Prueba

Vamos a utilizar el endpoint `POST /api/v1/transactions` desde Swagger para lanzar tres escenarios distintos.

### Escenario 1: Flujo Feliz (Procesamiento Exitoso)

Este escenario demuestra el funcionamiento normal donde todo va bien.

**Payload a enviar (Swagger):**
```json
{
  "accountId": "USER-12345",
  "amount": 100.50
}
```

**Comportamiento esperado:**
1. Recibirás un HTTP 201 Created con el estado `PENDING`.
2. En la terminal de Spring Boot verás:
   * Llamada al servicio externo mock (`Calling external REST service...`).
   * Producción del evento Kafka (`Publishing transaction event to Kafka...`).
   * El Consumer atrapa su propio evento (`Consumed transaction event from Kafka!`).
   * Reconocimiento manual exitoso (`Message processed and acknowledged successfully.`).
3. En **Kafka UI** -> Topics -> `transaction.events.v1` -> Messages, podrás ver el JSON del evento si eres rápido (o filtrando).

---

### Escenario 2: Error Transitorio (Reintentos)

Simularemos un fallo temporal en el servidor (ej. base de datos caída por unos segundos). Hemos programado el código para que si el `accountId` empieza por `ERROR`, lance un `RuntimeException`.

**Payload a enviar (Swagger):**
```json
{
  "accountId": "ERROR-USER",
  "amount": 50.00
}
```

**Comportamiento esperado:**
1. La API te responderá HTTP 201 porque la creación (y publicación a Kafka) fue un éxito. *El error ocurrirá de forma asíncrona en el consumidor.*
2. En la terminal de Spring Boot verás que el consumidor falla (`Simulating a processing error for account ERROR...`).
3. Verás que el consumidor **espera 1 segundo y vuelve a intentarlo**. Hará esto hasta 3 veces (nuestro `FixedBackOff`).
4. Tras fallar los 3 reintentos, el `DefaultErrorHandler` se rinde.
5. El mensaje se envía a la cola de muertos, e inmediatamente verás en rojo: `================= DLT ALARM ======================`
6. En **Kafka UI**, si vas a Topics, verás un nuevo topic llamado `transaction.events.v1.DLT`. Entra en él y verás el mensaje que falló, listo para ser revisado manualmente por un operador.

---

### Escenario 3: Error Fatal (Directo al DLT sin reintentos)

Hay errores que sabemos que nunca se van a resolver por mucho que reintentemos (ej. un formato de datos inválido). Hemos configurado en `KafkaConfig.java` que `IllegalArgumentException` es un error **no-reintentable**.

**Payload a enviar (Swagger):**
```json
{
  "accountId": "FATAL-USER",
  "amount": 10.00
}
```

**Comportamiento esperado:**
1. La API responde 201.
2. El consumidor lo recibe, detecta que empieza por `FATAL` y lanza un `IllegalArgumentException`.
3. Spring Kafka intercepta el error, detecta que está en la lista de exclusiones (lista negra) de reintentos.
4. **Sin esperar ni reintentar**, lo envía directamente al DLT. Verás la alerta roja del DLT inmediatamente.
5. En **Kafka UI**, el topic `.DLT` tendrá ahora un nuevo mensaje (este evento fatal).

---

## 🔍 Verificando en Kafka UI

Para confirmar lo que pasa por debajo de la arquitectura:
1. Ve a `http://localhost:8081`
2. En el menú izquierdo, haz clic en **Topics**.
3. Selecciona tu topic (`transaction.events.v1` o el `.DLT`).
4. Pestaña **Messages** -> Aquí puedes ver en tiempo real el contenido en JSON de todos los eventos.
5. Pestaña **Consumers** -> Aquí verás los grupos de consumo de Spring Boot, el estado de los *Offsets* y confirmar que el `lag` vuelve a cero (lo que significa que tu aplicación procesó todo sin quedarse atascada).
