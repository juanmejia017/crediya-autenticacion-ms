# CrediYa — Microservicio de Autenticación (`crediya-autenticacion-ms`)

Microservicio **imperativo (Spring MVC + JPA)** construido con Clean Architecture (scaffold de Bancolombia, `reactive=false`).
Es un repositorio independiente con su propio build Gradle, base de datos y `docker-compose.yml`.
El microservicio de solicitudes vive en su propio repositorio:
[`crediya-solicitudes-ms`](https://github.com/juanmejia017/crediya-solicitudes-ms).

## Proyecto Base Implementando Clean Architecture

## Antes de Iniciar

Empezaremos por explicar los diferentes componentes del proyectos y partiremos de los componentes externos, continuando con los componentes core de negocio (dominio) y por último el inicio y configuración de la aplicación.

Lee el artículo [Clean Architecture — Aislando los detalles](https://medium.com/bancolombia-tech/clean-architecture-aislando-los-detalles-4f9530f35d7a)

# Arquitectura

![Clean Architecture](https://miro.medium.com/max/1400/1*ZdlHz8B0-qu9Y-QO3AXR_w.png)

## Domain

Es el módulo más interno de la arquitectura, pertenece a la capa del dominio y encapsula la lógica y reglas del negocio mediante modelos y entidades del dominio.

## Usecases

Este módulo gradle perteneciente a la capa del dominio, implementa los casos de uso del sistema, define lógica de aplicación y reacciona a las invocaciones desde el módulo de entry points, orquestando los flujos hacia el módulo de entities.

## Infrastructure

### Helpers

En el apartado de helpers tendremos utilidades generales para los Driven Adapters y Entry Points.

Estas utilidades no están arraigadas a objetos concretos, se realiza el uso de generics para modelar comportamientos
genéricos de los diferentes objetos de persistencia que puedan existir, este tipo de implementaciones se realizan
basadas en el patrón de diseño [Unit of Work y Repository](https://medium.com/@krzychukosobudzki/repository-design-pattern-bc490b256006)

Estas clases no puede existir solas y debe heredarse su compartimiento en los **Driven Adapters**

### Driven Adapters

Los driven adapter representan implementaciones externas a nuestro sistema, como lo son conexiones a servicios rest,
soap, bases de datos, lectura de archivos planos, y en concreto cualquier origen y fuente de datos con la que debamos
interactuar.

### Entry Points

Los entry points representan los puntos de entrada de la aplicación o el inicio de los flujos de negocio.

## Application

Este módulo es el más externo de la arquitectura, es el encargado de ensamblar los distintos módulos, resolver las dependencias y crear los beans de los casos de use (UseCases) de forma automática, inyectando en éstos instancias concretas de las dependencias declaradas. Además inicia la aplicación (es el único módulo del proyecto donde encontraremos la función “public static void main(String[] args)”.

**Los beans de los casos de uso se disponibilizan automaticamente gracias a un '@ComponentScan' ubicado en esta capa.**

# Cómo ejecutar el microservicio de autenticación

## Requisitos

- Java 25
- Docker Desktop (para la base de datos PostgreSQL)

## Variables de entorno

| Variable      | Descripción                     | Valor por defecto                              |
|---------------|---------------------------------|------------------------------------------------|
| `DB_URL`      | URL JDBC de PostgreSQL          | `jdbc:postgresql://localhost:5432/crediya_db`  |
| `DB_USERNAME` | Usuario de la base de datos     | *(obligatoria)*                                |
| `DB_PASSWORD` | Contraseña de la base de datos  | *(obligatoria)*                                |
| `JWT_SECRET`  | Secreto para firmar los tokens (mínimo 32 caracteres). **Debe ser el mismo en `crediya-solicitudes-ms`** | *(obligatoria)* |
| `JWT_EXPIRACION_MINUTOS` | Minutos de vida del token | `60` |
| `ADMIN_CORREO` | Correo del administrador inicial | `admin@crediya.com` |
| `ADMIN_CLAVE` | Clave del administrador inicial (8 a 64 caracteres) | *(obligatoria)* |

## Pasos (PowerShell)

```powershell
$env:DB_USERNAME="crediya"
$env:DB_PASSWORD="crediya"
$env:JWT_SECRET="cambia-este-secreto-por-uno-largo-de-32-o-mas"
$env:ADMIN_CLAVE="Admin123*"

docker compose up -d
.\gradlew.bat clean build
.\gradlew.bat :app-service:bootRun
```

## Endpoints

### `POST /api/v1/login` — HU 3: Iniciar sesión

Recibe `correoElectronico` y `clave`. Si son correctos devuelve un token JWT que se envía en las demás peticiones
con el header `Authorization: Bearer <token>`. No hay límite de intentos y no existe recuperación de contraseña
en esta versión.

```json
{ "correoElectronico": "admin@crediya.com", "clave": "Admin123*" }
```

Respuesta `200`:

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tipo": "Bearer",
  "expiraEnSegundos": 3600,
  "idUsuario": 1,
  "correoElectronico": "admin@crediya.com",
  "rol": "ADMIN"
}
```

| Código | Cuándo                                                        |
|--------|---------------------------------------------------------------|
| 200    | Sesión iniciada: devuelve el token                            |
| 400    | Falta el correo o la clave, o JSON mal formado                |
| 401    | Correo o clave incorrectos (`CREDENCIALES_INVALIDAS`)         |
| 500    | Error inesperado                                              |

El token (HS256) lleva el id, el correo, el documento y el rol del usuario, y vence a los `JWT_EXPIRACION_MINUTOS`.
Las claves se guardan encriptadas con PBKDF2-HMAC-SHA256 y una sal aleatoria por usuario; nunca en texto plano y
nunca se devuelven en las respuestas.

**Administrador inicial:** al arrancar, si no existe, se crea el usuario `ADMIN_CORREO` / `ADMIN_CLAVE` con rol
administrador. Es el que inicia sesión primero para registrar asesores y clientes.

### Organización del código

- **ApiRest / LoginApiRest:** solo reciben la petición HTTP, la delegan y arman la respuesta (sin logs ni validaciones).
- **ValidadorPeticion** (`api/validacion`): lee el header `Authorization: Bearer`, hace validar el token, registra quién
  hizo la petición y revisa que el cuerpo no venga vacío.
- **Validadores del dominio:** `ValidadorUsuario` (permiso, datos, correo y documento libres), `ValidadorCredenciales`
  (correo y clave, usuario registrado, clave correcta) y `ValidadorToken` (token presente y vigente).
- **Casos de uso:** `registrarUsuario`, `crearAdministradorInicial`, `iniciarSesion`, `validarToken`. Solo orquestan:
  no tienen `if` ni `throw`; llaman a los validadores, a los gateways y guardan.
- **Adapters:** solo persisten; la conversión dominio ↔ entidad está en `jpa/mapper` (`UserEntityMapper`, `RolEntityMapper`)
  y la traducción de errores de la base de datos en `jpa/error/TraductorErroresIntegridad`.
- **Nombres de métodos en español.** Se mantienen en inglés solo los que exige el framework: los métodos derivados de
  Spring Data (`existsBy…`, `findBy…`, `save`), los `@Override` de Spring (`run`, `doFilter`) y los beans
  `entityManagerFactory` y `transactionManager`.

### Roles y permisos

| idRol | Rol      | Registrar usuarios (`POST /api/v1/usuarios`) | Crear solicitudes (`POST /api/v1/solicitud`, en solicitudes-ms) |
|-------|----------|:--------------------------------------------:|:---------------------------------------------------------------:|
| 1     | ADMIN    | Sí                                           | No                                                              |
| 2     | ASESOR   | Sí                                           | No                                                              |
| 3     | CLIENTE  | No                                           | Sí, solo para sí mismo                                          |

Los roles se guardan en la tabla `roles` al arrancar.

### Flujo del inicio de sesión

```
LoginApiRest → ValidadorPeticion.validarCuerpo → AutenticacionUseCase.iniciarSesion
            → UserRepository.buscarPorCorreoElectronico → UserRepositoryAdapter (SELECT, UserEntityMapper)
            → PasswordEncoderGateway.coincide → Pbkdf2PasswordEncoderAdapter
            → TokenGateway.generar → JwtTokenAdapter → LoginApiRest (200 + token)
```

### `POST /api/v1/usuarios` — HU 1: Registrar usuarios (protegido desde la HU 3)

Requiere el header `Authorization: Bearer <token>` de un usuario **ADMIN o ASESOR**.

```
ApiRest.registrarUsuario → ValidadorPeticion.validarPeticionAutenticada (saca el token del header Bearer,
          AutenticacionUseCase.validarToken, cuerpo no vacío)
        → UserUseCase.registrarUsuario (permiso → validar → correo/documento libres → encriptar clave → guardar)
        → UserRepositoryAdapter (INSERT, UserEntityMapper) → ApiRest (201)
```

Campos obligatorios: `nombres`, `apellidos`, `correoElectronico`, `salarioBase` (entre 0 y 15.000.000),
`clave` (8 a 64 caracteres) e `idRol` (1, 2 o 3).
Campos opcionales validados si se envían: `fechaNacimiento` (pasada), `telefono` (7–15 dígitos), `documentoIdentidad` (5–20 alfanuméricos).

| Código | Cuándo                                                        |
|--------|---------------------------------------------------------------|
| 201    | Usuario registrado (header `Location` con la URL del recurso) |
| 400    | Datos inválidos o JSON mal formado                            |
| 401    | Sin token, token inválido o vencido (`NO_AUTENTICADO`)        |
| 403    | El usuario no es administrador ni asesor (`ACCESO_DENEGADO`)  |
| 409    | Correo o documento de identidad ya registrados                |
| 415    | El `Content-Type` no es `application/json`                    |
| 500    | Error inesperado (mensaje genérico, detalle solo en el log)   |

Todas las respuestas incluyen el header `X-Trace-Id` (si lo envías, se reutiliza) y los errores lo devuelven en el campo
`traceId`. Ese mismo id aparece al inicio de cada línea de log de la petición.
