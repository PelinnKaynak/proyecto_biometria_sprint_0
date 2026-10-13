# Diseño del componente: client_business_logic (cliente Android)

**Autora:** Pelin Kaynak · **Sprint:** 0 · **Implementación:** `src/client_business_logic/` (paquete `com.pelinkaynak.biometria`)

---

## 1. Diseño del componente

Lógica del negocio del cliente Android (lógica fake / proxy). La GUI del teléfono (`android_design.md`, `MainActivity`) solo habla con este componente. Ofrece **la misma interfaz** que la lógica del negocio del backend (`business_logic_design.md`), pero por dentro no accede a la base de datos: llama al componente de comunicación del backend (`communication_design.md`).

Regla del subconjunto: el teléfono solo necesita guardar mediciones, así que este componente ofrece únicamente `guardarMedicion()`.

```text
MainActivity --> LogicaFakeTelefono.guardarMedicion() --> PeticionarioREST --> servidor (communication)
```

`LogicaFakeTelefono` es **nueva** (documento *Diseños y prompts para generar nuevo código*, apartado 5). `PeticionarioREST` es **código proporcionado** que se copia al proyecto.

### Tipos

```text
TipoMedicion = { CO2 = 11, TEMPERATURA = 12, RUIDO = 13 }

RespuestaREST = ( codigo: Z, cuerpo: Text )
```

### Clase LogicaFakeTelefono (NUEVA)

```text
                                    --------- LogicaFakeTelefono ---------------------
                                    |
                                    | URL_SERVIDOR: Text
                                    | CODIGO_EXITO: Z
                                    |
                                    |    uuid: Text, tipoMedicion: N, valorMedicion: Z --> crearJSON() --x
                                    |    Text <--
                                    |    codigo: Z --> esExito() --x
                                    |    B <--
                                    |    codigo: Z, cuerpoRespuesta: Text --> notificarResultado() --x
                                    |
                                    |
  uuid: Text                    --> guardarMedicion() -->
  tipoMedicion: N                   |
  valorMedicion: Z                  |
                                    |
                                    -------------------------------------------------
```

Firma idéntica a la del backend:

| Método | `business_logic_design.md` (LogicaDeNegocio) | Este componente (LogicaFakeTelefono) |
|---|---|---|
| `guardarMedicion()` | `uuid: Text, tipoMedicion: N, valorMedicion: Z --> guardarMedicion() -->` | `uuid: Text, tipoMedicion: N, valorMedicion: Z --> guardarMedicion() -->` |

### Clase PeticionarioREST (proporcionada)

```text
                                    --------- PeticionarioREST -----------------------
                                    |
                                    | elMetodo: Text
                                    | urlDestino: Text
                                    | elCuerpo: Text
                                    | laRespuesta: RespuestaREST   (callback)
                                    | codigoRespuesta: Z
                                    | cuerpoRespuesta: Text
                                    |
                                    |    B <-- doInBackground() -->
                                    |    comoFue: B --> onPostExecute() -->
                                    |
                                    |
                                    PeticionarioREST() -->
                                    |
                                    |
  metodo: Text                  --> hacerPeticionREST() -->
  urlDestino: Text                  |
  cuerpo: Text                      |
  RespuestaREST                 <-- |
                                    |
                                    -------------------------------------------------
```

---

## 2. Aclaraciones del diseño

- **guardarMedicion()** recibe `uuid` (texto), `tipoMedicion` (natural) y `valorMedicion` (entero), crea el JSON `{"UUID": ..., "TipoMedicion": ..., "ValorMedicion": ...}` con `crearJSON()` y lo envía a `POST /guardarMedicion` con un `PeticionarioREST`. **No** incluye el `Timestamp` (lo genera el backend). No accede a la base de datos ni a la lógica del negocio del backend.
- **Resultado (igual que en el backend):** `guardarMedicion()` no devuelve ningún dato. `esExito(codigo)` es `true` solo si `codigo == 201`. Si es así se llama a `guardada()` del callback; cualquier otro código (400, 404, 500, o 0 si no hay conexión) se trata como un error y se llama a `error(mensaje)`, que hace el papel de la excepción del backend. El callback (interfaz `RespuestaGuardarMedicion`) no se dibuja como parámetro, según la notación de la asignatura.
- **notificarResultado():** recibe el código y el cuerpo de la respuesta del servidor y llama a `guardada()` (si `esExito(codigo)`) o a `error(mensaje)` del callback de `guardarMedicion()`.
- **Privados y estáticos:** `crearJSON()`, `esExito()`, `notificarResultado()`, `URL_SERVIDOR` y `CODIGO_EXITO` son privados de la clase; los tres métodos son estáticos (`--x`). En Java tienen visibilidad de paquete para poder probarlos con JUnit.
- **Visibilidad en Java de PeticionarioREST:** `doInBackground()` y `onPostExecute()` son privados en el diseño (los llama `AsyncTask`, no otras clases), aunque en Java se declaran `protected` porque sobrescriben métodos de `AsyncTask`.
- **Nombres:** los parámetros siguen la convención de la asignatura (minúsculas: `uuid`, `tipoMedicion`, `valorMedicion`); las claves del JSON (`UUID`, `TipoMedicion`, `ValorMedicion`) son los nombres de las columnas de la tabla `Mediciones` (`database_design.md`).
- **URL_SERVIDOR:** en el emulador de Android Studio es `http://10.0.2.2:8080` (el ordenador que ejecuta el emulador). En un teléfono real es la IPv4 del ordenador en la misma Wi‑Fi o punto de acceso, por ejemplo `http://172.20.10.2:8080`.
- **PeticionarioREST** es una `AsyncTask`: `hacerPeticionREST()` guarda los datos y llama a `execute()`; `doInBackground()` hace la petición en otro hilo (para no bloquear la GUI) y `onPostExecute()` entrega `( codigo, cuerpo )` por el callback `RespuestaREST`. Si no se puede conectar, `codigoRespuesta` queda en 0.
- **Una petición por objeto:** cada llamada a `guardarMedicion()` crea un `PeticionarioREST` nuevo, porque una `AsyncTask` solo se puede ejecutar una vez.
- **Tráfico HTTP sin cifrar:** desde Android 9 está bloqueado por defecto. En el `AndroidManifest.xml` de `src/android/` se añade `android:usesCleartextTraffic="true"` en `<application>`. El permiso `INTERNET` ya existe.
- **Integración en la app:** este componente está fuera del proyecto Gradle (`src/android/`); `app/build.gradle` lo incluye con `sourceSets` (`main` → `java/`, `test` → `test/java/`).

---

## 3. Reglas generales

- **Lenguaje:** Java 8 para Android (minSdk 28), compilado dentro del módulo `app` de `src/android/`.
- **Cabecera de fichero:** cada fichero empieza con un comentario que indica nombre del fichero, descripción, autor (Pelin Kaynak en los ficheros nuevos; autor original Jordi Bataller i Mascarell + aportación de Pelin Kaynak en los proporcionados), fecha y copyright.
- **Cabeceras de funciones/métodos:** cada función o método incluye su diseño lógico en un bloque de comentarios delimitado por líneas discontinuas (`--------------------`), con la notación de la asignatura, y una línea breve en inglés `What it does:` dentro del mismo bloque.
- **Legibilidad:** el código debe ser lo más claro y autoexplicativo posible (nombres descriptivos en castellano), de forma que casi no necesite comentarios adicionales.
- **Tests automáticos:** JUnit 4 en `src/client_business_logic/test/java/` (se ejecutan con `./gradlew test` desde `src/android/`, sin teléfono). `LogicaFakeTelefonoTest` comprueba la creación del JSON (claves `UUID`, `TipoMedicion` y `ValorMedicion`, sin `Timestamp`, valores negativos) las respuestas de éxito (201) y error (400, 404, 500, 0) del servidor, y que `notificarResultado()` llama a `guardada()` con 201 y a `error()` con cualquier otro código. El envío real se comprueba en la prueba de extremo a extremo (criterio de aceptación en el `README.md`).
- **Fidelidad al diseño:** no añadir métodos públicos que no aparezcan en este diseño, y mantener la firma de `guardarMedicion()` idéntica a la del backend.
