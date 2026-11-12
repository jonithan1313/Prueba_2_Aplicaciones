# Plan de trabajo: GitHub + Trello

> Para que la rúbrica (IE 2.3.2) lo valore, los commits y tarjetas deben ser **reales y distribuidos en el tiempo y entre los dos integrantes**.
> Este documento es una guía: ejecútenla a lo largo de las 5 semanas, no el último día. **No hagan commits después de la fecha de entrega** (nota 1.0 automática).

## Ramas
`main` (estable) · `develop` (integración) · `feature/<nombre>` por tarea. Cada feature se une a `develop` con Pull Request revisado por el otro integrante.

## Convención de commits (técnicos, en imperativo)
`feat:` nueva funcionalidad · `fix:` corrección · `refactor:` · `test:` · `docs:` · `chore:` configuración.

## Commits sugeridos (A = integrante 1, B = integrante 2)

| # | Semana | Quién | Commit |
|---|--------|-------|--------|
| 1 | 1 | A | `chore: crear proyecto Compose con Gradle version catalog y dependencias` |
| 2 | 1 | B | `docs: agregar README inicial y .gitignore` |
| 3 | 1 | A | `feat(data): definir entidades Room y DAOs de usuarios, etapas, FAQ, progreso` |
| 4 | 1 | B | `feat(data): agregar content.json y siembra inicial en ContentRepository` |
| 5 | 2 | A | `feat(domain): implementar Validators con reglas de nombre, correo y contraseña` |
| 6 | 2 | B | `test: agregar pruebas unitarias de Validators` |
| 7 | 2 | A | `feat(auth): AuthRepository con hash PBKDF2 y sesión en DataStore` |
| 8 | 2 | B | `feat(ui): tema de alto contraste y componentes AppTextField/StatusChip` |
| 9 | 2 | A | `feat(auth): pantallas de registro e inicio de sesión con validación visual` |
| 10 | 3 | B | `feat(navigation): NavHost, barra inferior y riel adaptable a tablet` |
| 11 | 3 | A | `feat(stages): listado de etapas con estado y progreso animado` |
| 12 | 3 | B | `feat(stages): detalle de etapa con formato, fecha límite y pauta` |
| 13 | 3 | A | `feat(guide): navegación paso a paso con transición animada` |
| 14 | 3 | B | `feat(faq): listado por categoría con respuestas desplegables animadas` |
| 15 | 3 | A | `feat(faq): búsqueda en tiempo real tolerante a tildes y mayúsculas` |
| 16 | 4 | B | `feat(edit): formulario de actualización de estado con validaciones` |
| 17 | 4 | A | `feat(camera): captura de foto de evidencia con FileProvider` |
| 18 | 4 | B | `feat(checklist): lista ordenada por fecha con alerta de vencimiento` |
| 19 | 4 | A | `feat(notifications): ReminderScheduler y ReminderWorker con WorkManager` |
| 20 | 4 | B | `feat(settings): pantalla de recordatorios y solicitud de permiso de notificaciones` |
| 21 | 5 | A | `fix: evitar recordatorios para hitos ya entregados` |
| 22 | 5 | B | `refactor: mover cálculo de ítems de etapa a domain/Models` |
| 23 | 5 | A | `fix(ui): mejorar contraste de chips de estado en modo oscuro` |
| 24 | 5 | B | `docs: completar README con integrantes, funcionalidades y pasos de ejecución` |

## Tablero Trello
**Nombre:** `Tutor Virtual ESUCAR – DSY1105` · **Columnas:** Backlog · Sprint actual · En progreso · En revisión · Hecho  
**Etiquetas:** `UI`, `Datos`, `Lógica`, `Recurso nativo`, `Docs`, `Bug` · Agregar a ambos integrantes y al docente.

| Tarjeta | HU | Responsable |
|---------|----|-------------|
| Configurar proyecto, Gradle y repositorio | — | A |
| Modelo de datos Room + JSON semilla | HU-01/02 | A / B |
| Validators + pruebas unitarias | — | A / B |
| Registro y login local | — | A |
| Tema accesible y componentes de formulario | HU-12 | B |
| Navegación adaptable (teléfono/tablet) | HU-12 | B |
| Listado de etapas | HU-01, HU-06 | A |
| Detalle de etapa y rúbrica | HU-02, HU-10 | B |
| Guía paso a paso | HU-03 | A |
| FAQ por categoría | HU-04 | B |
| Búsqueda de FAQ | HU-05 | A |
| Formulario actualizar estado + persistencia | HU-07 | B |
| Cámara: foto de evidencia (recurso nativo 1) | HU-07 | A |
| Checklist con alertas | HU-08 | B |
| Notificaciones locales (recurso nativo 2) | HU-09 | A |
| Ajustes de recordatorios y permisos | HU-09 | B |
| Pruebas manuales en teléfono y tablet | HU-12 | A y B |
| README, capturas y entrega en AVA | — | A y B |
| (EFT) Actualización de contenidos con conexión | HU-11 | Backlog |

Mueve cada tarjeta entre columnas a medida que avanzan y enlaza el commit/PR en sus comentarios.

## Checklist de entrega
- [ ] Repo **público** en GitHub con acceso al docente y a ambos integrantes
- [ ] README con nombres, funcionalidades y pasos para ejecutar
- [ ] Commits distribuidos entre ambos y con mensajes técnicos
- [ ] Trello con tareas repartidas y actividad real
- [ ] Código subido al **AVA grupal** (un integrante) y enlace **AVA individual** activado por cada uno
- [ ] Ningún cambio en el repo después de la fecha de entrega
