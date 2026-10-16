# Diseño del componente: business_logic

**Autora:** Pelin Kaynak · **Sprint:** 0 · **Implementación:** `src/business_logic/`

---

## 1. Diseño del componente

La lógica del negocio es la única parte del sistema que accede a la base de datos (diseñada en `database_design.md`: tabla `Mediciones` con las columnas `id`, `IdSensor`, `Timestamp`, `TipoMedicion` y `ValorMedicion`). Es independiente de cualquier mecanismo de comunicación: sus métodos solo usan tipos del dominio.

### Tipos

```text
TipoMedicion = { CO2 = 11, TEMPERATURA = 12, RUIDO = 13 }

Medicion = ( IdSensor: N, Timestamp: Text, TipoMedicion: N, ValorMedicion: Z )

vacío = ausencia de valor (en JavaScript, null)

A | B = un valor de tipo A o un valor de tipo B

Tipos externos (opacos): ConexionSQLite (conexión de la biblioteca sqlite3),
                         Promesa (Promise de JavaScript)
```

### Clase LogicaDeNegocio

```text
                          --------- LogicaDeNegocio ------------------------
                          |
                          | laConexion: ConexionSQLite
                          | baseDeDatosAbierta: Promesa
                          | SCRIPT_CREACION: Text
                          | TIPOS_VALIDOS: [ N ]
                          |
                          |    nombreBD: Text --> abrirBaseDeDatos() -->
                          |
                          |
  nombreBD: Text      --> LogicaDeNegocio() -->
                          |
                          |
  idSensor: N         --> guardarMedicion() -->
  tipoMedicion: N         |
  valorMedicion: Z        |
                          |
                          |
  Medicion | vacío    <-- recuperarUltimaMedicion() <--
                          |
                          |
                          cerrar() -->
                          |
                          --------------------------------------------------
```

- `abrirBaseDeDatos()` es **privado** (está dentro de la caja).
- `guardarMedicion()` y `cerrar()` modifican el estado (`-->` a la derecha).
- `recuperarUltimaMedicion()` solo consulta (`<--` a la derecha).

---

## 2. Aclaraciones del diseño

- **Constantes privadas:** en el código son constantes del módulo `LogicaDeNegocio.js` (fuera de la clase, no se exportan), por eso se dibujan como privadas. `SCRIPT_CREACION` es la ruta del script `src/database/crearBaseDeDatos.sql` y `TIPOS_VALIDOS` = `[11, 12, 13]`. Se escriben en mayúsculas porque son constantes (convención de JavaScript).
- **laConexion** y **baseDeDatosAbierta** son privadas (dentro de la caja), igual que `abrirBaseDeDatos()`. `ConexionSQLite` es la conexión de la biblioteca `sqlite3` (tipo externo opaco). `baseDeDatosAbierta` es la promesa de que la conexión está abierta y la tabla creada; los demás métodos la esperan antes de usar la base de datos.
- **Nombres:** los parámetros siguen la convención de la asignatura (minúsculas: `idSensor`, `tipoMedicion`, `valorMedicion`). Los campos de `Medicion` (`IdSensor`, `Timestamp`, `TipoMedicion`, `ValorMedicion`) se llaman exactamente como las columnas de la tabla `Mediciones`.
- **vacío:** indica que no hay ninguna `Medicion` (en JavaScript, `null`).
- **Constructor:** recibe el nombre del fichero de la base de datos y llama a `abrirBaseDeDatos()`. Así, los tests pueden usar un fichero temporal distinto del de producción.
- **abrirBaseDeDatos():** abre la conexión SQLite y ejecuta el script `src/database/crearBaseDeDatos.sql` (usa `CREATE TABLE IF NOT EXISTS`), de modo que la tabla existe siempre antes de usarla.
- **guardarMedicion():** recibe `idSensor`, `tipoMedicion` y `valorMedicion`, genera el `Timestamp` en el momento de guardar (hora actual en ISO 8601, UTC) e inserta una fila en `Mediciones`. Si `idSensor` no es un número natural, si el tipo no es 11, 12 o 13, o si faltan datos, la operación falla con un error.
- **recuperarUltimaMedicion():** devuelve la última medición guardada (la de `Timestamp` más reciente y, si empatan, la de `id` mayor) como `Medicion`, con `IdSensor`, `Timestamp`, `TipoMedicion` y `ValorMedicion` (el `id` no se devuelve). Es la última de **cualquier** sensor; el campo `IdSensor` indica de cuál. Si la tabla está vacía, devuelve **vacío** (`null`), no un error.
- **cerrar():** cierra la conexión con la base de datos. Se usa al apagar el servidor y al terminar los tests.
- **Asincronía:** en Node.js el acceso a SQLite es asíncrono; los métodos devuelven `Promise`. En las firmas de los métodos se omite (según la notación de la asignatura, es un detalle de implementación); el atributo privado `baseDeDatosAbierta` sí se dibuja porque forma parte del estado de la clase.
- **Separación de responsabilidades:** este componente no importa ni llama al componente de comunicación (`communication_design.md`) ni a ningún framework web; es el componente de comunicación el que usa una instancia de esta clase.

---

## 3. Reglas generales

- **Lenguaje:** JavaScript (Node.js ≥ 20.19), módulo CommonJS. Biblioteca `sqlite3` para la base de datos. Fichero principal: `src/business_logic/LogicaDeNegocio.js`, que exporta la clase `LogicaDeNegocio`.
- **Cabecera de fichero:** cada fichero empieza con un comentario que indica nombre del fichero, descripción, autora (Pelin Kaynak), fecha y copyright.
- **Cabeceras de funciones/métodos:** cada función o método incluye su diseño lógico en un bloque de comentarios delimitado por líneas discontinuas (`--------------------`), con la notación de la asignatura (ej. `idSensor: N, tipoMedicion: N, valorMedicion: Z --> guardarMedicion() -->`), y una línea breve en inglés `What it does:` dentro del mismo bloque.
- **Legibilidad:** el código debe ser lo más claro y autoexplicativo posible (nombres descriptivos en castellano), de forma que casi no necesite comentarios adicionales.
- **Tests automáticos:** con mocha, en `src/business_logic/test/`. Usan una base de datos temporal y comprueban: abrir la base de datos, guardar una medición y recuperarla como la última (con `Timestamp` generado), que solo se devuelven los cuatro campos de `Medicion`, que con varias mediciones se devuelve la más reciente, que con la tabla vacía se devuelve `null`, y que un tipo de medición no válido o un `idSensor` que falta o es negativo producen un error.
- **Fidelidad al diseño:** no añadir métodos públicos ni funcionalidades que no aparezcan en este diseño.
