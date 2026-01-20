🚀 Omniscan-Backend: Sistema de Monitoreo de Precios Event-Driven

Omniscan-Backend es una solución de infraestructura robusta diseñada para el escaneo y monitoreo de precios en tiempo real. El sistema utiliza una arquitectura orientada a eventos (EDA) para procesar flujos de datos de alta velocidad, garantizando eficiencia y escalabilidad mediante el uso de tecnologías modernas de mensajería y persistencia.
+4

🛠️ Stack Tecnológico

Java 21 & Spring Boot 3: Implementación de Virtual Threads (Project Loom) para un manejo de concurrencia de alto rendimiento, similar al aplicado en sistemas de turnos previos.
+1


Apache Kafka: Motor central para el streaming de eventos de precios y desacoplamiento de servicios.

Redis: Capa de caché ultrarrápida para comparaciones de precios en tiempo real y reducción de latencia.


MongoDB: Almacenamiento NoSQL para persistir el historial de variaciones de precios con esquemas flexibles.


Docker & Docker Compose: Orquestación completa de la infraestructura (Kafka, Mongo, Redis) para entornos de desarrollo y producción replicables.
+1

JUnit 5 & Mockito: Suite de testing integral para asegurar la calidad del código y la lógica de negocio.

🏗️ Arquitectura y Flujo de Datos

Ingesta (Omniscan Producer): Simula la captura de datos enviando eventos de precios a un topic de Kafka.

Procesamiento (Stream Processor): Un consumidor de Kafka procesa el evento y utiliza Redis para validar fluctuaciones de precio de forma inmediata.


Persistencia: En caso de detectar cambios significativos, se actualiza el estado en el caché y se registra el histórico en MongoDB.


Consumo: El backend expone una API REST lista para ser consumida por un frontend en Angular.

💡 Valor Técnico
Este proyecto refleja la capacidad de integrar servicios complejos y manejar flujos de datos asincrónicos, demostrando un dominio avanzado en el ecosistema de Java y la gestión de infraestructuras basadas en contenedores
