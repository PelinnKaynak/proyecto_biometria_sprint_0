# Diseño del componente: database

**Autora:** Pelin Kaynak · **Sprint:** 0 · **Implementación:** `src/database/`

---

## 1. Diseño del componente

La base de datos guarda las mediciones que llegan al servidor. Tiene una única tabla.

```text
====================================================================================
TABLE: Mediciones

DESCRIPTION: Guarda cada medición ambiental recibida desde un teléfono.

COLUMNS:

+ id            | INTEGER   | NOT NULL | Auto-Increment
+ UUID          | TEXT      | NOT NULL
+ Timestamp     | TIMESTAMP | NOT NULL
+ TipoMedicion  | INTEGER   | NOT NULL
+ ValorMedicion | INTEGER   | NOT NULL

PRIMARY KEY: id

FOREIGN KEYS: ninguna (solo hay una tabla)

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

- La base de datos contiene una única tabla, `Mediciones`, con la clave primaria `id` y los cuatro datos de una medición (`UUID`, `Timestamp`, `TipoMedicion`, `ValorMedicion`), con los mismos nombres en todo el sistema. No se añaden más campos ni tablas.
- `id` es un número entero que SQLite asigna automáticamente (1, 2, 3…) al insertar cada fila. Es interno de la base de datos: no forma parte del tipo `Medicion` que se devuelve a los clientes.
- `UUID` es **TEXT** porque llega al servidor como texto dentro del JSON (identifica el dispositivo emisor, por ejemplo `EPSG-GTI-PROY-3A`).
- `Timestamp` se declara como **TIMESTAMP** y guarda la fecha en formato ISO 8601 (`2026-09-29T10:15:30.000Z`), que se ordena y compara correctamente. Lo genera la lógica del negocio, no el cliente.
- `TipoMedicion` es INTEGER con los mismos códigos que usa el Arduino (`Publicador::MedicionesID`): 11 CO2, 12 temperatura, 13 ruido. La restricción `CHECK` impide guardar tipos desconocidos (no es un campo nuevo).
- `ValorMedicion` es INTEGER con signo (la temperatura puede ser negativa, por ejemplo −12).
- La base de datos permite guardar nuevas mediciones y recuperarlas. La última medición es la de `Timestamp` más reciente; si dos tienen el mismo `Timestamp` se desempata con el `id` mayor (la insertada después).

---

## 3. Reglas generales

- **Lenguaje:** SQL para SQLite 3. El script de creación estará en `src/database/crearBaseDeDatos.sql`, y los datos ficticios de prueba en `src/database/datosDePrueba.sql`.
- **Cabecera de fichero:** cada fichero empieza con un comentario que indica nombre del fichero, descripción, autora (Pelin Kaynak), fecha y copyright.
- **Cabeceras de funciones/métodos:** los scripts SQL no tienen funciones; cada función de los tests incluye su diseño lógico en un bloque de comentarios delimitado por líneas discontinuas (`--------------------`), con la notación de la asignatura, y una línea breve en inglés `What it does:` dentro del mismo bloque.
- **Legibilidad:** el código debe ser lo más claro y autoexplicativo posible (nombres descriptivos en castellano), de forma que casi no necesite comentarios adicionales.
- **Tests automáticos:** se generan tests (Node.js + mocha, en `src/database/test/`) que crean la base de datos en un fichero temporal con el script SQL y comprueban: que la tabla existe con sus cinco columnas, tipos y NOT NULL, que `id` es la clave primaria y se asigna automáticamente, que se pueden guardar y recuperar las mediciones de prueba, que se guardan valores negativos y que un `TipoMedicion` no válido se rechaza.
- **Fidelidad al diseño:** no añadir tablas, columnas ni funcionalidades que no aparezcan en este diseño.
