# AuraWars

**AuraWars** es un videojuego web de estrategia táctica por turnos, inspirado en las mecánicas de *Advance Wars*. Es un proyecto académico desarrollado con **Angular y Phaser** para la interfaz y el tablero interactivo, y con **Java y Spring Boot** para las reglas del juego, la autenticación y la persistencia.

El objetivo es administrar un ejército, aprovechar el terreno, producir unidades, conquistar propiedades y superar a los demás jugadores mediante decisiones estratégicas.

> **Estado:** Finalizado.

##  Funcionalidades

- **Cuentas y seguridad:** registro, verificación de correo con OTP, inicio de sesión en dos pasos y recuperación de contraseña.
- **Autenticación con JWT:** protección de las operaciones del backend y gestión de sesiones.
- **Partidas por turnos:** creación y configuración de partidas con **2 a 4 jugadores**, selección de facciones y comandantes.
- **Tablero táctico:** visualización con Phaser, mapa con terrenos, propiedades y unidades.
- **Mecánicas de juego:** movimiento, combate, captura de propiedades, finalización de turnos y condiciones de victoria.
- **Producción de unidades:** compra de tropas según los recursos disponibles y los edificios controlados.
- **Comandantes:** selección y activación de poderes disponibles en el motor del juego.
- **Seguimiento:** historial de acciones y resultados de las partidas.
- **Administración:** panel para consultar usuarios, partidas y datos generales del sistema.
- **Reportes PDF:** exportación de información de partidas desde el backend.
- **Modo demostración:** acceso a una vista de prueba del tablero mediante `/tablero-demo`.

##  Tecnologías

| Capa | Tecnologías |
| --- | --- |
| Frontend | Angular 22, TypeScript, HTML, CSS, RxJS |
| Motor visual | Phaser 3 |
| Backend | Java 21, Spring Boot 4, Spring Security, Spring Data JPA |
| Autenticación | JWT, OTP por correo y BCrypt |
| Documentación de API | Swagger / OpenAPI |
| Reportes | Apache PDFBox |
| Construcción | npm y Maven |
| Despliegue previsto | Netlify (frontend) y Tomcat (backend WAR) |

##  Estructura del repositorio

```text
Warfare-Vanguard/
├── AdvanceWars/                  # Backend Java / Spring Boot
│   ├── src/main/java/
│   │   └── co/edu/unbosque/advancewars/
│   │       ├── configuration/    # Configuración e inicialización
│   │       ├── controller/       # Endpoints REST
│   │       ├── dto/              # Objetos para las peticiones y respuestas
│   │       ├── model/            # Entidades y modelos del juego
│   │       ├── repository/       # Acceso a los datos
│   │       ├── security/         # JWT, filtros y CORS
│   │       └── service/          # Lógica de negocio y reglas del juego
│   ├── src/main/resources/
│   └── pom.xml
├── AdvanceWarsFront/             # Frontend Angular / Phaser
│   ├── public/                   # Archivos estáticos
│   ├── src/app/
│   │   ├── components/           # Pantallas y paneles
│   │   ├── guards/               # Protección de rutas
│   │   ├── models/               # Interfaces TypeScript
│   │   ├── phaser/               # Escena del tablero
│   │   └── services/             # Comunicación con el backend
│   ├── angular.json
│   └── package.json
└── README.md
```

##  Ejecución local

### Requisitos

- **JDK 21** y Maven (el backend incluye Maven Wrapper).
- **Node.js y npm** compatibles con Angular 22.
- Una cuenta de Gmail configurada para  aprobar el registro, el inicio de sesión y los códigos OTP.

### 1. Backend

Desde la carpeta raíz del repositorio:

```bash
cd AdvanceWars
```

Configura `src/main/resources/application.properties` con los valores necesarios para tu entorno (correo, JWT, OTP y CORS). **No publiques contraseñas ni claves privadas en Git.**

Ejecuta con Maven Wrapper:

```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

De forma predeterminada, Spring Boot atiende en `http://localhost:8080`, salvo que se configure otro puerto o contexto.

### 2. Frontend

En otra terminal:

```bash
cd AdvanceWarsFront
npm install
npm run start
```

Abre **http://localhost:4200**.

La dirección del backend está centralizada en:

```text
AdvanceWarsFront/src/app/services/api-config.ts
```

La configuración preparada para el despliegue utiliza:

```typescript
export const API_BASE_URL = 'https://gpcueb.org/AuraWars/api';
```

Para consumir un backend local, ajusta temporalmente esa constante a la URL real de tu API. El backend debe permitir en CORS el origen desde el cual se ejecuta Angular.

##  Despliegue previsto

### Frontend: Netlify

| Parámetro | Valor |
| --- | --- |
| Directorio base | `AdvanceWarsFront` |
| Comando de compilación | `npm run build` |
| Directorio publicado | `dist/advance-wars-front/browser` |
| Dominio previsto | `https://aurawars.netlify.app` |

Para que funcionen las rutas internas de Angular al recargar, incluye el archivo `AdvanceWarsFront/public/_redirects` con este contenido:

```text
/*    /index.html   200
```

### Backend: Apache Tomcat

Genera el archivo WAR desde `AdvanceWars`:

```bash
# Windows
.\mvnw.cmd clean package

# Linux / macOS
./mvnw clean package
```

Renombra el WAR generado en `target/` a **`AuraWars.war`** antes de desplegarlo. Así, si el servidor utiliza el nombre del archivo como contexto, la URL será:

```text
https://gpcueb.org/AuraWars
```

Por tanto, la base prevista para las peticiones del frontend es:

```text
https://gpcueb.org/AuraWars/api
```

El backend debe tener configurado CORS para permitir `https://aurawars.netlify.app` y contar con el servicio de correo necesario en el servidor. **Estas direcciones indican la configuración prevista; no constituyen una confirmación de que el despliegue esté activo.**

> **Nota del repositorio:** verifica que `AdvanceWarsFront/angular.json` esté incluido en Git, pues algunas versiones del `.gitignore` raíz lo excluyen. También se debe proporcionar la configuración privada del backend directamente en el entorno de despliegue.

##  API REST

El backend expone endpoints agrupados por funcionalidad:

| Prefijo | Responsabilidad |
| --- | --- |
| `/api/auth` | Registro, inicio de sesión, OTP y recuperación |
| `/api/users` | Perfil y gestión autorizada de usuarios |
| `/api/sessions` | Estado y cierre de sesiones |
| `/api/catalog` | Facciones, comandantes y unidades |
| `/api/games` | Partidas, tablero, turnos y acciones |
| `/api/admin` | Funciones administrativas |

También incluye generación de reportes PDF y documentación mediante **Swagger/OpenAPI** (sujeta a la configuración de seguridad del servidor).

##  Pruebas

```bash
# Frontend (desde AdvanceWarsFront)
npm run build
npm run test

# Backend (desde AdvanceWars)
./mvnw test
```

En Windows, utiliza `mvnw.cmd test` para el backend. La disponibilidad y el resultado de las pruebas deben verificarse en cada entorno.

##  Sobre el proyecto

AuraWars es un **proyecto académico de la asignatura Desarrollo de Sistemas de Información 3**. Su arquitectura separa las responsabilidades de la interfaz, la representación del tablero, las reglas de negocio y el almacenamiento de información.

*Proyecto no oficial, inspirado en el género de estrategia por turnos y en Advance Wars. No está afiliado a sus titulares.*
