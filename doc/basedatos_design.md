# Diseño del componente: basedatos

**Autora:** Pelin Kaynak · **Sprint:** 0 · **Implementación:** `src/basedatos/`

---

## 1. Diseño del componente

La base de datos guarda las mediciones que llegan al servidor. Tiene una única tabla.

```text
====================================================================================
TABLE: Mediciones

DESCRIPTION: Guarda cada medición ambiental recibida desde un teléfono.

COLUMNS:

+ id            | INTEGER | NOT NULL | Auto-Increment
+ uuid          | TEXT    | NOT NULL
+ timestamp     | TEXT    | NOT NULL
+ tipoMedicion  | INTEGER | NOT NULL
+ valorMedicion | INTEGER | NOT NULL

PRIMARY KEY: id

FOREIGN KEYS:

+ (ninguna)

CONSTRAINTS:

+ CHECK (tipoMedicion IN (11, 12, 13))
====================================================================================
```

Tipo lógico de una fila:

```text
TipoMedicion = { CO2 = 11, TEMPERATURA = 12, RUIDO = 13 }

Medicion = ( uuid: Text, timestamp: Text, tipoMedicion: TipoMedicion, valorMedicion: Z )
```

---

## 2. Aclaraciones del diseño

- Se añade la columna `id` (clave primaria autoincremental) al diseño del documento de Sprint 0. Sirve para identificar cada fila y para obtener la **última** medición con `ORDER BY id DESC LIMIT 1`, sin depender de que dos mediciones tengan el mismo `timestamp`.
- `uuid` es **TEXT** porque llega del teléfono como texto dentro del JSON (identifica el dispositivo emisor, por ejemplo `EPSG-GTI-PROY-3A`).
- `timestamp` se guarda como **TEXT** en formato ISO 8601 (`2026-09-29T10:15:30.000Z`). SQLite no tiene un tipo TIMESTAMP propio; el texto ISO 8601 se ordena y compara correctamente. Lo genera la lógica del negocio, no el cliente.
- `tipoMedicion` es INTEGER con los mismos códigos que usa el Arduino (`Publicador::MedicionesID`): 11 CO2, 12 temperatura, 13 ruido. La restricción `CHECK` impide guardar tipos desconocidos.
- `valorMedicion` es INTEGER con signo (la temperatura puede ser negativa, por ejemplo −12).
- Los nombres de columnas se escriben en *lowerCamelCase* (en el documento de Sprint 0 empezaban por mayúscula) para seguir la convención de nombres de variables y coincidir con las claves del JSON de la API.
- No se añaden más tablas ni campos en este sprint.

---

## 3. Reglas generales

- **Lenguaje:** SQL para SQLite 3. El script de creación estará en `src/basedatos/crearBaseDeDatos.sql`, y los datos ficticios de prueba en `src/basedatos/datosDePrueba.sql`.
- **Cabecera de fichero:** cada fichero empieza con un comentario que indica nombre del fichero, descripción, autora (Pelin Kaynak), fecha y copyright.
- **Cabeceras de funciones/métodos:** cada función o método incluye su diseño lógico en un bloque de comentarios delimitado por líneas discontinuas (`--------------------`), con la notación de la asignatura (ej. `uuid: Text, tipo: N, valor: Z --> guardarMedicion() -->`).
- **Legibilidad:** el código debe ser lo más claro y autoexplicativo posible (nombres descriptivos en castellano), de forma que casi no necesite comentarios adicionales.
- **Tests automáticos:** se generan tests (Node.js + mocha, en `src/basedatos/test/`) que crean la base de datos en un fichero temporal con el script SQL y comprueban: que la tabla existe con sus columnas, que se puede insertar y leer una medición, que `id` se autoincrementa y que un `tipoMedicion` no válido se rechaza.
- **Fidelidad al diseño:** no añadir tablas, columnas ni funcionalidades que no aparezcan en este diseño.
