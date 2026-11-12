# Tutor Virtual ESUCAR — Trabajo de Título

Aplicación móvil Android (Kotlin + Jetpack Compose) que funciona como **tutor virtual** para la asignatura
Trabajo de Título I, II y III de la Escuela de Suboficiales de Carabineros (caso DSY1105, Duoc UC).
Guía paso a paso la construcción de la tesis, centraliza pautas y FAQ, y permite hacer seguimiento de entregables con recordatorios locales.

**Integrantes:** Jonathan Millao. Joshua Lopez. Vicente Pedreros
**Equipo :** Ambrositos  
**Asignatura:** DSY1105 – Desarrollo de Aplicaciones Móviles · Evaluación Parcial 2

## Funcionalidades

| HU | Funcionalidad | Estado |
|----|---------------|--------|
| HU-01 | Listado de etapas con estado, descripción y fecha límite (offline) | ✅ |
| HU-02 | Detalle de etapa: descripción, formato, fecha límite y pauta | ✅ |
| HU-03 | Guía paso a paso con "Anterior/Siguiente" y progreso ("Paso 2 de 5") | ✅ |
| HU-04 | FAQ por categoría con respuestas desplegables | ✅ |
| HU-05 | Búsqueda en tiempo real (sin distinguir mayúsculas ni tildes) | ✅ |
| HU-06 | Estado visual de cada entregable (color + ícono + etiqueta), persistente | ✅ |
| HU-07 | Actualizar estado, fecha de entrega, notas y foto de evidencia | ✅ |
| HU-08 | Checklist ordenado por fecha, con alerta ≤ 3 días | ✅ |
| HU-09 | Recordatorios locales por hito (WorkManager, sin internet) | ✅ |
| HU-10 | Pauta/rúbrica simplificada por etapa | ✅ |
| HU-11 | Actualización de contenidos con conexión | ⏳ Pendiente (el caso no incluye servidor en el MVP; se aborda en la EFT) |
| HU-12 | Interfaz accesible, alto contraste, adaptable a teléfono y tablet | ✅ |

Además: registro e inicio de sesión local (contraseña con PBKDF2 + sal) para separar el avance de cada estudiante.

### Perfil profesor guía (supervisor)

El caso (sección 3.1) define dos perfiles: el estudiante y el **profesor guía**, que *define la estructura, aprueba hitos y evalúa los contenidos*.
Al registrarse se elige el tipo de cuenta; la cuenta de profesor requiere un **código docente**. Según el rol, la app abre un área distinta.

| ID | Funcionalidad del profesor | Estado |
|----|----------------------------|--------|
| PR-01 | Registro/inicio de sesión como profesor (código docente) y navegación propia | ✅ |
| PR-02 | Panel de estudiantes: avance de cada uno y cantidad de entregas **por revisar** | ✅ |
| PR-03 | Detalle por estudiante: estado de cada etapa y fechas límite | ✅ |
| PR-04 | **Aprobar** un hito o **devolverlo con observaciones** (obligatorias al devolver), viendo notas, fecha y foto de evidencia contra la pauta | ✅ |
| PR-05 | **Definir la estructura**: crear, editar, eliminar y reordenar etapas (título, descripción, formato, fecha límite, pauta) | ✅ |
| PR-06 | Administrar los **pasos de la guía** de cada etapa (crear, editar, eliminar, reordenar) | ✅ |
| PR-07 | Administrar el banco de **preguntas frecuentes** (categoría, pregunta, respuesta) | ✅ |
| PR-08 | El estudiante ve las observaciones del profesor; **"Aprobado" solo lo asigna el profesor** (el estudiante ya no puede autoaprobarse) y un hito aprobado queda bloqueado | ✅ |

Detalles de diseño: al cambiar la fecha límite de una etapa se reprograman los recordatorios ya activados por los estudiantes; al eliminar una etapa
se cancelan sus recordatorios y se borran sus fotos de evidencia. La base de datos pasó a la **versión 2** con una migración (no borra datos existentes).

## Arquitectura (MVVM)

```
UI (Compose)  →  ViewModel (StateFlow)  →  Repository  →  Room / DataStore / assets
                        ↓
                 domain/Validators  (validaciones puras, sin Android)
```

```
app/src/main/java/cl/esucar/tutorvirtual/
├── di/             AppContainer (inyección manual) + fábrica de ViewModels
├── data/local/     Entidades Room, DAOs y AppDatabase
├── data/prefs/     DataStore (sesión)
├── data/repository/ Auth, Content (siembra desde JSON), Progress, Teacher (perfil profesor)
├── domain/         Validators, modelos, fechas, hash de contraseñas
├── notifications/  ReminderScheduler + ReminderWorker (WorkManager)
└── ui/
    ├── theme/      Colores de alto contraste (WCAG AA), tipografía
    ├── components/ Campos con ícono y mensaje de error, chips, barra superior
    ├── navigation/ NavHost, barra inferior (teléfono) / riel lateral (tablet)
    ├── auth/  stages/  faq/  checklist/  settings/   (perfil estudiante: pantalla + ViewModel)
    └── teacher/    Estudiantes, revisión de hitos, estructura (etapas y pasos), FAQ y perfil del profesor
```

Contenido académico en `app/src/main/assets/content.json` (etapas, pasos, rúbricas, FAQ y fechas).
**Es contenido de ejemplo**: reemplazar por el material oficial entregado por la contraparte ESUCAR.
Las fechas límite son absolutas (ISO `yyyy-MM-dd`); ajústalas al calendario académico vigente.

## Requisitos de la evaluación

| Indicador | Dónde está |
|-----------|-----------|
| Interfaz y navegación | `ui/navigation`, `ui/components`, pantallas por módulo |
| Formularios validados con íconos y mensajes | Registro, Login, Actualizar entregable, Días de recordatorio (`AppTextField`) |
| Validaciones desde la lógica | `domain/Validators.kt` + ViewModels; pruebas en `app/src/test` |
| Animaciones | FAQ desplegable, color del estado, barras de progreso, transición entre pasos, navegación |
| Modularidad y persistencia local | MVVM por paquetes; Room + DataStore |
| GitHub + Trello | Ver `docs/PLAN_COMMITS_Y_TRELLO.md` |
| Dos recursos nativos | **Notificaciones locales** (permiso `POST_NOTIFICATIONS`) y **cámara** (intent del sistema + FileProvider) |

## Cómo ejecutar

1. Abrir la carpeta con **Android Studio** (Ladybug 2024.2 o superior) y esperar el *Gradle Sync*.
   Usar **JDK 17** (Settings → Build Tools → Gradle → Gradle JDK).
2. Crear un emulador (API 26+; recomendado API 34/35 con cámara virtual) o conectar un teléfono.
3. Ejecutar la configuración `app` (▶).
4. Crear una cuenta en la pantalla de registro e ingresar (tipo **Estudiante**).
5. Pruebas unitarias: `./gradlew test` (o clic derecho en `ValidatorsTest` → Run).

> Si falta `gradlew`, ejecutar `gradle wrapper --gradle-version 8.9` o dejar que Android Studio use la versión de `gradle-wrapper.properties`.

**Probar recordatorios:** en *Ajustes* activa el interruptor de un hito (acepta el permiso). Los avisos se programan
a las 09:00 de cada día de anticipación configurado. Para una demo rápida, edita una fecha en `content.json`
(borra los datos de la app o desinstala para que se vuelva a sembrar).

## Cómo probar el perfil profesor

1. Registra primero una cuenta de **Estudiante**, entra a una etapa → *Actualizar estado y evidencia*, márcala como **Enviado** (con notas y, si quieres, foto) y cierra sesión (*Ajustes*).
2. En *Crear cuenta* elige **Profesor guía** e ingresa el código docente `ESUCAR-DOCENTE` (constante `UserRole.TEACHER_ACCESS_CODE`).
3. En **Estudiantes** verás la entrega marcada como *por revisar*: ábrela y **aprueba** o **devuelve con observaciones**.
4. En **Etapas** crea o edita una etapa (fecha, pauta, pasos de la guía); en **Preguntas** agrega una FAQ.
5. Vuelve a entrar como estudiante: verás las observaciones, el estado *Aprobado* (bloqueado) y los cambios de estructura.

> Como el MVP no usa servidor (caso, sección 3.4), estudiantes y profesor comparten la base de datos **local del mismo dispositivo**:
> el profesor ve a los estudiantes registrados en ese teléfono. La sincronización entre dispositivos y la validación del código docente
> en un backend quedan para la EFT, junto con HU-11.

## Seguridad y privacidad

- Sin datos personales sensibles; todo se guarda localmente (`allowBackup=false`).
- Contraseñas con PBKDF2-HMAC-SHA256 + sal aleatoria; no hay credenciales en el código.
- Permisos solo en el momento de uso; la cámara usa el intent del sistema, por lo que la app no requiere el permiso `CAMERA`.
- Fotos de evidencia en almacenamiento privado de la app, compartidas con la cámara solo vía `FileProvider`.
- El código docente es una constante local pensada para la demo del MVP (no protege datos reales); en producción debe validarse en el servidor.
