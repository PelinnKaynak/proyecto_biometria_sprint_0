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

+ UUID          | TEXT      | NOT NULL
+ Timestamp     | TIMESTAMP | NOT NULL
+ TipoMedicion  | INTEGER   | NOT NULL
+ ValorMedicion | INTEGER   | NOT NULL

PRIMARY KEY: (ninguna)

FOREIGN KEYS:

+ (ninguna)

CONSTRAINTS:

+ CHECK (TipoMedicion IN (11, 12, 13))
====================================================================================
```

Tipo lógico de una fila:

```text
TipoMedicion = { CO2 = 11, TEMPERATURA = 12, RUIDO = 13 }

Medicion = ( UUID: Text, Timestamp: Text, TipoMedicion: N, ValorMedicion: Z )
```

---

## 2. Aclaraciones del diseño

- La base de datos contiene una única tabla, `Mediciones`, con exactamente los cuatro campos del diseño (`UUID`, `Timestamp`, `TipoMedicion`, `ValorMedicion`) y con los mismos nombres. No se añaden campos ni tablas.
- `UUID` es **TEXT** porque llega al servidor como texto dentro del JSON (identifica el dispositivo emisor, por ejemplo `EPSG-GTI-PROY-3A`).
- `Timestamp` se declara como **TIMESTAMP** y guarda la fecha en formato ISO 8601 (`2026-09-29T10:15:30.000Z`), que se ordena y compara correctamente. Lo genera la lógica del negocio, no el cliente.
- `TipoMedicion` es INTEGER con los mismos códigos que usa el Arduino (`Publicador::MedicionesID`): 11 CO2, 12 temperatura, 13 ruido. La restricción `CHECK` impide guardar tipos desconocidos (no es un campo nuevo).
- `ValorMedicion` es INTEGER con signo (la temperatura puede ser negativa, por ejemplo −12).
- La base de datos permite guardar nuevas mediciones y recuperarlas. La última medición es la de `Timestamp` más reciente; si dos tienen el mismo `Timestamp` se desempata con el `rowid` interno de SQLite (no es un campo de la tabla).

---

## 3. Reglas generales

- **Lenguaje:** SQL para SQLite 3. El script de creación estará en `src/basedatos/crearBaseDeDatos.sql`, y los datos ficticios de prueba en `src/basedatos/datosDePrueba.sql`.
- **Cabecera de fichero:** cada fichero empieza con un comentario que indica nombre del fichero, descripción, autora (Pelin Kaynak), fecha y copyright.
- **Cabeceras de funciones/métodos:** cada función o método incluye su diseño lógico en un bloque de comentarios delimitado por líneas discontinuas (`--------------------`), con la notación de la asignatura (ej. `UUID: Text, TipoMedicion: N, ValorMedicion: Z --> guardarMedicion() -->`).
- **Legibilidad:** el código debe ser lo más claro y autoexplicativo posible (nombres descriptivos en castellano), de forma que casi no necesite comentarios adicionales.
- **Tests automáticos:** se generan tests (Node.js + mocha, en `src/basedatos/test/`) que crean la base de datos en un fichero temporal con el script SQL y comprueban: que la tabla existe con sus cuatro columnas y tipos, que se pueden guardar y recuperar las mediciones de prueba, que se guardan valores negativos y que un `TipoMedicion` no válido se rechaza.
- **Fidelidad al diseño:** no añadir tablas, columnas ni funcionalidades que no aparezcan en este diseño.
