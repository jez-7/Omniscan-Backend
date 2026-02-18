🚀 Omniscan:
Omniscan es un motor de monitoreo de precios de alta frecuencia diseñado bajo una arquitectura Event-Driven. El sistema escanea productos en tiempo real, analiza fluctuaciones mediante ventanas deslizantes y notifica ofertas críticas directamente a un Bot de Telegram.

🛠️ Stack Tecnológico Evolucionado
- Java 21 & Spring Boot 4: Uso de Virtual Threads (Project Loom) para manejar cientos de procesos de I/O (escaneo y notificaciones) sin bloquear el sistema.

- Apache Kafka: Desacoplamiento total entre el productor de precios y el procesador de ofertas.

- Redis: Implementación de ventanas deslizantes de tiempo para calcular promedios móviles y detectar volatilidad en milisegundos.

- MongoDB: Persistencia NoSQL para un historial flexible de variaciones de precios.

- JUnit 5 & Mockito: Cobertura de tests unitarios asegurando la integridad de la lógica de negocio y el cumplimiento de integraciones.
  

🏗️ Arquitectura y Flujo de Datos

- Ingesta Inteligente: El PriceProducer consulta APIs externas y aplica un algoritmo de simulación de volatilidad para estresar la lógica de detección.

- Streaming & Análisis: Kafka distribuye los eventos al PriceConsumer, quien utiliza Redis (LTRIM/LRANGE) para mantener los últimos 10 precios de cada producto.

- Detección de Ofertas: Se activa una alerta si el precio actual es un 5% menor al promedio reciente.

- Notificación Multi-canal: Integración con la API de Telegram para enviar alertas instantáneas con links directos y thumbnails de los productos.
  

🧪 Calidad de Código y Mantenibilidad

- Testing: Implementación de mocks para dependencias de infraestructura (Redis, Kafka, Repositorios), permitiendo tests aislados y rápidos.

- Clean Architecture: Separación clara de responsabilidades entre servicios de notificación, productores y consumidores.

- Variables de Entorno: Configuración preparada para despliegue en contenedores mediante application.properties dinámicos.

# 🚀 Metricas SonarQube Cloud

![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=jez-7_Omniscan-Backend&metric=alert_status)
![Coverage](https://sonarcloud.io/api/project_badges/measure?project=jez-7_Omniscan-Backend&metric=coverage)
![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=jez-7_Omniscan-Backend&metric=sqale_rating)
