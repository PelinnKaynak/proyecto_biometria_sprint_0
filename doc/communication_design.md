# Diseño del componente: communication

**Autora:** Pelin Kaynak · **Sprint:** 0 · **Implementación:** `src/communication/`

---

## 1. Diseño del componente

Componente de comunicación del backend: un servidor REST. Es la puerta de entrada al backend. Traduce peticiones HTTP a llamadas a la lógica del negocio (`business_logic_design.md`) y sus resultados a respuestas JSON. Flujo de dependencias: comunicación → lógica del negocio → base de datos (`database_design.md`).

### Tipos

```text
JSON de entrada  = ( UUID: Text, TipoMedicion: N, ValorMedicion: Z )

JSON de salida   = ( UUID: Text, Timestamp: Text, TipoMedicion: N, ValorMedicion: Z )

ServidorHTTP = servidor HTTP de Node.js (tipo externo opaco; permite consultar el puerto y cerrarlo)

EXITO = 201 ( resultado: "ok" )

ERROR = 400 | 404 | 500 ( error: Text )
```

### Servidor REST

```text
                                   --------- API REST (servidorREST) -----------------
                                   |
                                   | logica: LogicaDeNegocio
                                   | elServidorHTTP: ServidorHTTP
                                   |
                                   |    cuerpo: JSON --> esMedicionEntranteValida() --> B
                                   |    error: Error --> responderJSONMalFormado() -->   (JSON mal formado -> 400)
                                   |
                                   |
  JSON( UUID: Text,            --> POST /guardarMedicion -->
        TipoMedicion: N,           |
        ValorMedicion: Z )         |
  EXITO | ERROR                <-- |
                                   |
                                   |
  JSON( UUID: Text,            <-- GET /recuperarUltimaMedicion <--
        Timestamp: Text,           |
        TipoMedicion: N,           |
        ValorMedicion: Z ) | ERROR |
                                   |
                                   |
  puerto: N, nombreBD: Text    --> arrancarServidor() -->
  ServidorHTTP                 <-- |
                                   |
                                   pararServidor() -->
                                   |
                                   ---------------------------------------------------
```

### Contrato HTTP

| Método y ruta | Cuerpo de la petición | Respuesta correcta | Respuestas de error |
|---|---|---|---|
| `POST /guardarMedicion` | `{"UUID": "EPSG-GTI-PROY-3A", "TipoMedicion": 11, "ValorMedicion": 123}` | `201` `{"resultado": "ok"}` | `400` datos que faltan o no válidos · `500` fallo interno |
| `GET /recuperarUltimaMedicion` | – | `200` `{"UUID": "EPSG-GTI-PROY-3A", "Timestamp": "2026-09-29T10:15:30.000Z", "TipoMedicion": 11, "ValorMedicion": 123}` | `404` no hay mediciones · `500` fallo interno |

Todas las respuestas de error tienen la forma `{"error": "<descripción>"}`.

---

## 2. Aclaraciones del diseño

- **logica** es una instancia **privada** de `LogicaDeNegocio`, creada en `arrancarServidor()` con el `nombreBD` recibido. **elServidorHTTP** (privado) guarda el servidor que devuelve `arrancarServidor()`, para que `pararServidor()` pueda cerrarlo.
- **POST /guardarMedicion:** recibe un JSON con `UUID` (texto no vacío), `TipoMedicion` (11, 12 o 13) y `ValorMedicion` (entero) y llama a `guardarMedicion()` de `LogicaDeNegocio`. Si falta alguno o no es válido → `400`; si la lógica falla por otro motivo → `500`. El `Timestamp` **no** se recibe del cliente (si llega, se ignora): lo genera la lógica del negocio.
- **GET /recuperarUltimaMedicion:** llama a `recuperarUltimaMedicion()` de `LogicaDeNegocio` y devuelve el JSON con `UUID`, `Timestamp`, `TipoMedicion` y `ValorMedicion`. Si la lógica devuelve vacío (tabla sin datos) → `404`; si falla → `500`.
- **Página web:** el mismo servidor sirve los ficheros estáticos de la GUI (`src/gui/`, ver `gui_design.md`) en la ruta `/` y la lógica fake del navegador (`src/frontend_business_logic/`, ver `frontend_business_logic_design.md`) en la ruta `/frontend_business_logic/`. Así la página y la API tienen el mismo origen y no hacen falta cabeceras CORS.
- **Configuración:** el puerto por defecto es `8080` y la base de datos por defecto es `mediciones.db`; se pueden cambiar con las variables de entorno `PUERTO` y `NOMBRE_BD`. El servidor escucha en todas las interfaces (`0.0.0.0`) para que el teléfono lo alcance desde la misma red Wi‑Fi.
- **arrancarServidor() / pararServidor():** existen para que los tests puedan levantar y parar el servidor con una base de datos temporal. `pararServidor()` también cierra la lógica.
- **Fichero de arranque:** `src/communication/mainServidorREST.js` tiene solo la función `main()`, que llama a `arrancarServidor()` y, al pulsar Ctrl+C, a `pararServidor()` (se ejecuta con `npm run servidor`).
- **Funciones privadas:** `esMedicionEntranteValida()` hace las comprobaciones del `POST`; `responderJSONMalFormado()` convierte un cuerpo que no es JSON válido en una respuesta `400` con `{"error": ...}` en vez de la página de error por defecto de Express.

---

## 3. Reglas generales

- **Lenguaje:** JavaScript (Node.js ≥ 20.19), módulo CommonJS, con la biblioteca `express`. Fichero principal: `src/communication/servidorREST.js`, que exporta `arrancarServidor` y `pararServidor`.
- **Cabecera de fichero:** cada fichero empieza con un comentario que indica nombre del fichero, descripción, autora (Pelin Kaynak), fecha y copyright.
- **Cabeceras de funciones/métodos:** cada función o ruta incluye su diseño lógico en un bloque de comentarios delimitado por líneas discontinuas (`--------------------`), con la notación de la asignatura, y una línea breve en inglés `What it does:` dentro del mismo bloque.
- **Legibilidad:** el código debe ser lo más claro y autoexplicativo posible (nombres descriptivos en castellano), de forma que casi no necesite comentarios adicionales.
- **Tests automáticos:** con mocha, en `src/communication/test/`. Arrancan el servidor en un puerto libre con una base de datos temporal y hacen peticiones HTTP reales (`fetch`) para comprobar: `POST` correcto → 201; `POST` con datos que faltan o con tipo no válido → 400; `POST` con JSON mal formado → 400; `GET` con la tabla vacía → 404; `GET` después de un `POST` → 200 con los mismos `UUID`, `TipoMedicion` y `ValorMedicion` y un `Timestamp`; un `Timestamp` enviado por el cliente se ignora.
- **Fidelidad al diseño:** no añadir rutas ni funcionalidades que no aparezcan en este diseño.
