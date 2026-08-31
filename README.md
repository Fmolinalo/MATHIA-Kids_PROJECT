# MathIA

## Plataforma educativa inteligente para el aprendizaje de matemáticas

MathIA es una aplicación educativa para estudiantes de educación básica
que busca transformar el aprendizaje de las matemáticas mediante la
integración de inteligencia artificial, gamificación y aprendizaje adaptativo.

---

## ¿Qué problema resolvemos?

El aprendizaje de las matemáticas puede presentar dificultades relacionadas
con la comprensión de conceptos, la motivación y la participación de los
estudiantes.

MathIA propone una experiencia educativa interactiva que permite practicar
matemáticas, recibir retroalimentación y avanzar de acuerdo con el desempeño
del estudiante.

---

## ¿Qué es MathIA?

MathIA combina herramientas tecnológicas y estrategias de aprendizaje
para crear una experiencia matemática más dinámica, personalizada y motivadora.

La plataforma busca que el estudiante no solamente resuelva ejercicios,
sino que pueda aprender mediante retos, competencias, recompensas y
seguimiento de su progreso.

---

## Funcionalidades

### Aprendizaje

- Diagnóstico matemático adaptativo
- Banco de preguntas
- Ejercicios de matemáticas
- Retroalimentación
- Seguimiento del progreso

### Gamificación

- Sistema de estrellas
- Niveles de progreso
- Recompensas
- Duelos matemáticos
- Competencias semanales

### Inteligencia Artificial

- Asistencia para el aprendizaje
- Apoyo en la resolución de problemas
- Experiencia educativa personalizada

### Seguimiento

- Perfil del estudiante
- Progreso académico
- Métricas de aprendizaje
- Sistema de notificaciones

---

## Tecnologías

| Tecnología | Uso |
|---|---|
| Kotlin | Lenguaje principal |
| Jetpack Compose | Interfaz de usuario |
| Android | Plataforma móvil |
| Firebase Authentication | Gestión de usuarios |
| Cloud Firestore | Almacenamiento de información |
| Firebase Cloud Messaging | Notificaciones |
| Inteligencia Artificial | Asistencia educativa |

---


## Capturas de pantalla

## Logo del aplicativo
<img width="283" height="284" alt="WhatsApp Image 2026-08-20 at 1 05 49 AM" src="https://github.com/user-attachments/assets/c44759b8-2811-4ae1-8f69-d7262732a1ef" />


### Inicio de sesión
<img width="318" height="697" alt="Captura de pantalla 2026-08-31 122530" src="https://github.com/user-attachments/assets/c80e22af-9994-40ab-8670-370912194333" />


### Seleccion de perfil
<img width="325" height="1156" alt="Seleccion de perfil" src="https://github.com/user-attachments/assets/95f0eae5-0820-41d2-87aa-2110e5107de2" />


### Pantalla principal

<img width="315" height="660" alt="Captura de pantalla 2026-08-31 122646" src="https://github.com/user-attachments/assets/b20bc17c-571f-4eef-9ff7-85fd4487d802" />


---
## Arquitectura

MathIA utiliza una arquitectura orientada a la separación de
responsabilidades, permitiendo organizar la aplicación en diferentes capas.

```text
                  ┌──────────────────┐
                  │      MathIA      │
                  │ Android / Compose│
                  └────────┬─────────┘
                           │
                           ▼
                  ┌──────────────────┐
                  │     Firebase     │
                  └────────┬─────────┘
                           │
             ┌─────────────┼─────────────┐
             ▼             ▼             ▼
       Authentication   Firestore    Notifications
             │             │             │
             └─────────────┼─────────────┘
                           │
                           ▼
                    Servicios de IA

