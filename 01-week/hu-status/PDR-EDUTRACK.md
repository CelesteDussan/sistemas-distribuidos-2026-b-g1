# PDR — Diseño y Responsabilidades del Proyecto: EduTrack

**Plataforma distribuida de seguimiento escolar para padres y tutores**

**Proyecto:** Sistemas Distribuidos 2026-B — Grupo G1  
**Complementario al PRD de EduTrack**  
**Fecha:** Agosto 2026  
**Versión:** 1.0 — MVP 1

---

## 1. Propósito del documento

Este documento complementa el PRD de EduTrack y define cómo se organizará el desarrollo del MVP 1.

El objetivo principal es dividir el sistema en módulos pequeños y claros, permitiendo que cada integrante del equipo tenga una responsabilidad específica y pueda trabajar de manera independiente sin perder la integración general del proyecto.

Cada integrante será responsable de un módulo o funcionalidad principal, incluyendo su implementación, pruebas básicas y documentación.

---

## 2. Organización general del sistema

EduTrack estará dividido en cinco módulos principales:

| Módulo | Responsabilidad |
|---|---|
| Identidad y Cuentas | Usuarios, padres, estudiantes y colegios |
| Registros Académicos | Tareas y calificaciones |
| Asistencia | Registro y seguimiento de asistencia |
| Notificaciones | Alertas y notificaciones a padres |
| Comunicación | Mensajes entre padres y profesores |

Cada módulo tendrá una responsabilidad específica y podrá comunicarse con otros módulos mediante APIs REST o eventos.

Esta división permite aplicar conceptos de sistemas distribuidos y facilita que los integrantes trabajen en diferentes partes del proyecto.

---

## 3. División de responsabilidades

### Integrante 1 — Identidad y Cuentas

**Responsabilidad principal:** Gestionar la información básica de los usuarios del sistema.

Funciones:

- Crear usuarios.
- Registrar padres o tutores.
- Registrar estudiantes.
- Registrar colegios.
- Vincular estudiantes con sus padres.
- Permitir que un padre tenga varios hijos asociados.
- Evitar vínculos duplicados.

**Historia relacionada:** HU-003 — Ver progreso de múltiples hijos.

**Entrega mínima:**

- Entidades de usuario, padre y estudiante.
- Endpoint para consultar los hijos asociados a un padre.
- Validación para evitar relaciones duplicadas.
- Pruebas básicas.

---

### Integrante 2 — Registros Académicos

**Responsabilidad principal:** Gestionar las calificaciones y tareas de los estudiantes.

Funciones:

- Registrar calificaciones.
- Consultar calificaciones por estudiante.
- Registrar tareas.
- Consultar tareas pendientes.
- Generar un evento cuando se publique una nueva calificación.

**Historia relacionada:** HU-001 — Ver calificaciones en tiempo casi real.

**Entrega mínima:**

- Entidad de calificación.
- Endpoint para registrar calificaciones.
- Endpoint para consultar calificaciones.
- Evento `GradeCreated`.
- Pruebas básicas.

---

### Integrante 3 — Asistencia

**Responsabilidad principal:** Registrar y consultar la asistencia de los estudiantes.

Funciones:

- Registrar asistencia.
- Registrar ausencias.
- Consultar asistencia por estudiante.
- Evitar registros duplicados.
- Mantener el orden correcto de los eventos de asistencia.

**Historia relacionada:** HU-005 — Registro de asistencia con orden causal.

**Entrega mínima:**

- Entidad de asistencia.
- Endpoint para registrar asistencia.
- Endpoint para consultar asistencia.
- Validación de eventos duplicados.
- Evento `StudentAbsent`.
- Pruebas básicas.

---

### Integrante 4 — Notificaciones

**Responsabilidad principal:** Procesar las alertas generadas por otros módulos.

Funciones:

- Recibir eventos de nuevas calificaciones.
- Recibir eventos de inasistencia.
- Generar notificaciones para los padres.
- Evitar notificaciones duplicadas.
- Implementar reintentos cuando ocurra un error.

**Historias relacionadas:**

- HU-001 — Notificación de nueva calificación.
- HU-002 — Alerta de inasistencia sin duplicados.

**Entrega mínima:**

- Servicio de notificaciones.
- Consumo de eventos.
- Control de duplicados mediante `idempotency key`.
- Mecanismo básico de reintento.
- Pruebas básicas.

---

### Integrante 5 — Comunicación

**Responsabilidad principal:** Gestionar la comunicación entre padres y profesores.

Funciones:

- Enviar mensajes.
- Consultar conversaciones.
- Relacionar mensajes con una materia.
- Identificar al padre y al profesor.
- Mostrar la disponibilidad básica del profesor.

**Historia relacionada:** HU-004 — Comunicación directa con profesores.

**Entrega mínima:**

- Entidad de mensaje.
- Endpoint para enviar mensajes.
- Endpoint para consultar conversaciones.
- Asociación del mensaje con una materia.
- Pruebas básicas.

---

## 4. Comunicación entre módulos

Para mantener el proyecto sencillo, se utilizarán dos tipos principales de comunicación.

### Comunicación síncrona

Se utilizarán APIs REST cuando un módulo necesite obtener una respuesta inmediata.

Ejemplo:

Padre → Dashboard → Registros Académicos → Consultar calificaciones

### Comunicación asíncrona

Se utilizarán eventos cuando una acción pueda ser procesada posteriormente.

Ejemplo:

Registros Académicos → `GradeCreated` → Notificaciones → Padre

También:

Asistencia → `StudentAbsent` → Notificaciones → Padre

Esto permite demostrar comunicación distribuida sin agregar complejidad innecesaria al proyecto.

---

## 5. Flujo principal del MVP

Uno de los principales flujos del sistema será el registro de una calificación.

1. El profesor registra una calificación.
2. Registros Académicos guarda la calificación.
3. Se genera el evento `GradeCreated`.
4. Notificaciones recibe el evento.
5. Se genera una notificación para el padre.
6. El padre puede consultar la nueva calificación desde EduTrack.

Si el servicio de Notificaciones está temporalmente fuera de servicio, la calificación continuará almacenada y podrá ser consultada.

La notificación podrá procesarse posteriormente mediante un mecanismo de reintento.

---

## 6. Flujo de asistencia

Para demostrar el manejo de eventos duplicados se utilizará el registro de inasistencia.

1. El profesor registra que un estudiante faltó.
2. Asistencia guarda la ausencia.
3. Se genera el evento `StudentAbsent`.
4. Notificaciones recibe el evento.
5. Se verifica el identificador del evento.
6. Se genera una notificación para el padre.

Si el mismo evento llega nuevamente:

`StudentAbsent → verificar ID → evento existente → ignorar`

De esta manera se demuestra el concepto de idempotencia dentro del sistema.

---

## 7. Arquitectura simplificada

La estructura general del sistema será:

                     EDUTRACK
                        |
          --------------+--------------
          |                            |
   Identidad y Cuentas        Registros Académicos
          |                            |
          |                       GradeCreated
          |                            |
          |                            v
        Padre                    Notificaciones
          |                            ^
          |                            |
          +---- Comunicación           |
          |                            |
          |                      StudentAbsent
          |                            |
          +-------- Asistencia --------+

Cada módulo mantiene su propia responsabilidad y evita depender directamente de la lógica interna de los demás módulos.

---

## 8. Arquitectura interna

Cada módulo seguirá una arquitectura hexagonal sencilla.

Estructura propuesta:

src/
|
|-- domain/
|   |-- entities/
|   |-- events/
|
|-- application/
|   |-- usecases/
|
|-- ports/
|
|-- adapters/
    |-- api/
    |-- persistence/

### Dominio

Contiene las entidades y reglas principales del negocio.

No debe depender directamente de bases de datos, APIs o servicios externos.

### Aplicación

Contiene los casos de uso y coordina la lógica del dominio.

### Puertos

Definen las interfaces necesarias para comunicarse con elementos externos.

### Adaptadores

Contienen las implementaciones relacionadas con APIs REST, persistencia, mensajería y otras tecnologías externas.

---

## 9. Responsabilidad individual

Cada integrante deberá entregar como mínimo:

- Implementación de su módulo.
- README corto explicando su funcionamiento.
- Pruebas unitarias básicas.
- Evidencia de funcionamiento.
- Commits relacionados con su trabajo.
- Pull Request correspondiente a su historia de usuario.
- Documentación de decisiones importantes cuando sea necesario.

Esto permitirá identificar claramente la contribución individual de cada integrante.

---

## 10. Integración del equipo

Aunque cada integrante tenga una responsabilidad principal, todos los módulos deberán respetar contratos comunes.

Antes de implementar una comunicación entre módulos se debe definir:

- Nombre del evento o endpoint.
- Información que recibe.
- Información que devuelve.
- Identificador del evento.
- Posibles errores.

Ejemplo de evento:

Evento: `GradeCreated`

Datos:

- `eventId`
- `studentId`
- `subjectId`
- `grade`
- `createdAt`

El módulo de Notificaciones no necesita conocer cómo Registros Académicos almacena una calificación. Solamente necesita conocer el contrato del evento.

Esto reduce el acoplamiento entre los módulos.

---

## 11. Estrategia de Git

El desarrollo utilizará ramas separadas para las historias de usuario.

Ejemplo:

main
|
+-- develop
    |
    +-- feature/HU-001-academic-records
    +-- feature/HU-002-notifications
    +-- feature/HU-003-identity
    +-- feature/HU-004-communication
    +-- feature/HU-005-attendance

Cada integrante trabajará principalmente en su propia rama.

Flujo esperado:

`feature → Pull Request → develop → pruebas → main`

Esto ayuda a reducir conflictos y permite mantener evidencia individual del trabajo realizado.

---

## 12. Estrategia de pruebas

Cada responsable deberá probar su módulo antes de integrarlo.

### Pruebas unitarias

Validarán las reglas internas del módulo.

Ejemplo:

Una calificación válida puede ser registrada correctamente.

### Pruebas de integración

Comprobarán la comunicación con bases de datos, mensajería u otros componentes.

### Pruebas de contrato

Comprobarán que los eventos y APIs respeten la estructura acordada por el equipo.

### Pruebas E2E

Validarán los flujos completos del sistema.

Ejemplo:

Profesor registra ausencia → Asistencia → Evento → Notificaciones → Alerta al padre

---

## 13. Responsabilidades compartidas

Algunas tareas serán responsabilidad de todo el equipo:

- Definir contratos entre módulos.
- Revisar Pull Requests.
- Integrar los diferentes módulos.
- Ejecutar pruebas E2E.
- Mantener actualizada la documentación.
- Resolver conflictos de integración.
- Preparar la demostración final.

---

## 14. Alcance del MVP 1

Para evitar que el proyecto tenga una complejidad innecesaria, el MVP 1 se concentrará en:

- Gestión básica de usuarios.
- Relación entre padres y estudiantes.
- Registro y consulta de calificaciones.
- Registro y consulta de asistencia.
- Notificaciones de calificaciones e inasistencias.
- Comunicación básica entre padres y profesores.
- Comunicación mediante eventos.
- Idempotencia.
- Reintentos.
- Outbox para eventos importantes.

Las siguientes funcionalidades quedarán para versiones futuras:

- Inteligencia artificial.
- Predicción de riesgo académico.
- Reportes PDF avanzados.
- Calendario escolar compartido.
- Funcionalidades adicionales que no sean necesarias para demostrar el sistema distribuido.

---

## 15. Criterio de éxito

El MVP se considerará exitoso cuando sea posible demostrar al menos un flujo distribuido completo:

**Profesor registra información → módulo responsable almacena los datos → se genera un evento → otro módulo procesa el evento → el padre recibe o consulta la información.**

Además, el sistema deberá continuar funcionando parcialmente cuando uno de los servicios secundarios presente una falla.

---

## 16. Resultado esperado

Al finalizar el MVP 1, EduTrack tendrá una arquitectura distribuida sencilla en la que cada integrante tendrá una responsabilidad claramente identificable.

La división por módulos permitirá trabajar en paralelo, reducir conflictos en Git y demostrar conceptos de sistemas distribuidos como:

- Comunicación síncrona y asíncrona.
- Consistencia eventual.
- Idempotencia.
- Eventos.
- Reintentos.
- Outbox.
- Tolerancia a fallos.
- Separación de responsabilidades.

Este PDR complementa el PRD de EduTrack al convertir los requisitos generales del producto en una organización técnica y una división clara de responsabilidades para el equipo.