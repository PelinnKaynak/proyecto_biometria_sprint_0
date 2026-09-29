# Diseño del componente: api

**Autora:** Pelin Kaynak · **Sprint:** 0 · **Implementación:** `src/api/`

---

## 1. Diseño del componente

El servidor REST es la puerta de entrada al backend. Traduce peticiones HTTP a llamadas a la lógica del negocio (`logica_design.md`) y sus resultados a respuestas JSON.

### Tipos

```text
MedicionEntrante = ( uuid: Text, tipoMedicion: N, valorMedicion: Z )

Medicion = ( uuid: Text, timestamp: Text, tipoMedicion: N, valorMedicion: Z )

RespuestaError = ( error: Text )
```

### Servidor REST

```text
                             --------- API REST (servidorREST) -----------------
                             |
                             | laLogica: LogicaDeNegocio
                             |
                             |    cuerpo: MedicionEntrante --> esMedicionEntranteValida() --> B
                             |    error: Error --> responderJSONMalFormado() -->   (JSON mal formado -> 400)
                             |
                             |
  MedicionEntrante (JSON) --> POST /guardarMedicion -->
  201 ( resultado: "ok" )  <--
  400 | 500 RespuestaError <--
                             |
                             |
  200 Medicion (JSON)      <-- GET /recuperarUltimaMedicion <--
  404 | 500 RespuestaError <--
                             |
                             |
  puerto: N, nombreBD: Text --> arrancarServidor() -->
                             |
                             |
                             pararServidor() -->
                             |
                             ---------------------------------------------------
```

### Contrato HTTP

| Método y ruta | Cuerpo de la petición | Respuesta correcta | Respuestas de error |
|---|---|---|---|
| `POST /guardarMedicion` | `{"uuid": "EPSG-GTI-PROY-3A", "tipoMedicion": 11, "valorMedicion": 123}` | `201` `{"resultado": "ok"}` | `400` datos que faltan o no válidos · `500` fallo interno |
| `GET /recuperarUltimaMedicion` | – | `200` `{"uuid": "...", "timestamp": "2026-09-29T10:15:30.000Z", "tipoMedicion": 11, "valorMedicion": 123}` | `404` no hay mediciones · `500` fallo interno |

Todas las respuestas de error tienen la forma `{"error": "<descripción>"}`.

---

## 2. Aclaraciones del diseño

- **laLogica** es una instancia **privada** de `LogicaDeNegocio`, creada en `arrancarServidor()` con el `nombreBD` recibido.
- **POST /guardarMedicion:** comprueba que llegan `uuid` (texto no vacío), `tipoMedicion` (entero 11, 12 o 13) y `valorMedicion` (entero). Si falta alguno o no es válido → `400`; si la lógica falla por otro motivo → `500`. El `timestamp` **no** se recibe del cliente: lo genera la lógica del negocio.
- **GET /recuperarUltimaMedicion:** si la lógica devuelve vacío (tabla sin datos) → `404`.
- **Página web:** el mismo servidor sirve los ficheros estáticos de `src/web/` en la ruta `/`. Así la página y la API tienen el mismo origen y no hacen falta cabeceras CORS.
- **Configuración:** el puerto por defecto es `8080` y la base de datos por defecto es `mediciones.db`; se pueden cambiar con las variables de entorno `PUERTO` y `NOMBRE_BD`. El servidor escucha en todas las interfaces (`0.0.0.0`) para que el teléfono lo alcance desde la misma red Wi‑Fi.
- **arrancarServidor() / pararServidor():** existen para que los tests puedan levantar y parar el servidor con una base de datos temporal. `pararServidor()` también cierra la lógica.
- **Fichero de arranque:** `src/api/mainServidorREST.js` tiene solo la función `main() -->`, que llama a `arrancarServidor()` y, al pulsar Ctrl+C, a `pararServidor()` (se ejecuta con `npm run servidor`).
- **Funciones privadas:** `esMedicionEntranteValida()` hace las comprobaciones del `POST`; `responderJSONMalFormado()` convierte un cuerpo que no es JSON válido en una respuesta `400` con `{"error": ...}` en vez de la página de error por defecto de Express.

---

## 3. Reglas generales

- **Lenguaje:** JavaScript (Node.js ≥ 18), módulo CommonJS, con la biblioteca `express`. Fichero principal: `src/api/servidorREST.js`, que exporta `arrancarServidor` y `pararServidor`.
- **Cabecera de fichero:** cada fichero empieza con un comentario que indica nombre del fichero, descripción, autora (Pelin Kaynak), fecha y copyright.
- **Cabeceras de funciones/métodos:** cada función o ruta incluye su diseño lógico en un bloque de comentarios delimitado por líneas discontinuas (`--------------------`), con la notación de la asignatura.
- **Legibilidad:** el código debe ser lo más claro y autoexplicativo posible (nombres descriptivos en castellano), de forma que casi no necesite comentarios adicionales.
- **Tests automáticos:** con mocha, en `src/api/test/`. Arrancan el servidor en un puerto libre con una base de datos temporal y hacen peticiones HTTP reales (`fetch`) para comprobar: `POST` correcto → 201; `POST` con datos que faltan o con tipo no válido → 400; `GET` con la tabla vacía → 404; `GET` después de un `POST` → 200 con los mismos `uuid`, `tipoMedicion` y `valorMedicion` y un `timestamp`.
- **Fidelidad al diseño:** no añadir rutas ni funcionalidades que no aparezcan en este diseño.
