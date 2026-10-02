# Diseño del componente: logica

**Autora:** Pelin Kaynak · **Sprint:** 0 · **Implementación:** `src/logica/`

---

## 1. Diseño del componente

La lógica del negocio es la única parte del sistema que accede a la base de datos (diseñada en `basedatos_design.md`). No sabe nada de HTTP ni de REST.

### Tipos

```text
TipoMedicion = { CO2 = 11, TEMPERATURA = 12, RUIDO = 13 }

Medicion = ( UUID: Text, Timestamp: Text, TipoMedicion: N, ValorMedicion: Z )
```

### Clase LogicaDeNegocio

```text
                          --------- LogicaDeNegocio ------------------------
                          |
                          | laConexion: ConexionSQLite
                          |
                          |    nombreBD: Text --> abrirBaseDeDatos() -->
                          |
                          |
  nombreBD: Text      --> LogicaDeNegocio() -->
                          |
                          |
  UUID: Text          --> guardarMedicion() -->
  TipoMedicion: N         |
  ValorMedicion: Z        |
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

- **laConexion** es privada (dentro de la caja), igual que `abrirBaseDeDatos()`.
- **Constructor:** recibe el nombre del fichero de la base de datos y llama a `abrirBaseDeDatos()`. Así, los tests pueden usar un fichero temporal distinto del de producción.
- **abrirBaseDeDatos():** abre la conexión SQLite y ejecuta el script `src/basedatos/crearBaseDeDatos.sql` (usa `CREATE TABLE IF NOT EXISTS`), de modo que la tabla existe siempre antes de usarla.
- **guardarMedicion():** recibe `UUID`, `TipoMedicion` y `ValorMedicion`, genera el `Timestamp` en el momento de guardar (hora actual en ISO 8601, UTC) e inserta una fila en `Mediciones`. El `UUID` se guarda tal cual, como TEXT. Si el tipo no es 11, 12 o 13, o faltan datos, la operación falla con un error.
- **recuperarUltimaMedicion():** devuelve la última medición guardada (la de `Timestamp` más reciente) como `Medicion`, con `UUID`, `Timestamp`, `TipoMedicion` y `ValorMedicion`. Si la tabla está vacía, devuelve **vacío** (`null`), no un error.
- **cerrar():** cierra la conexión con la base de datos. Se usa al apagar el servidor y al terminar los tests.
- **Asincronía:** en Node.js el acceso a SQLite es asíncrono; los métodos devuelven `Promise` (en el diseño lógico se omite, según la notación de la asignatura).
- **Separación de responsabilidades:** la comunicación con los clientes la hace el componente `api` (ver `api_design.md`), que usa una instancia de esta clase. Aquí no hay código HTTP.

---

## 3. Reglas generales

- **Lenguaje:** JavaScript (Node.js ≥ 20.19), módulo CommonJS. Biblioteca `sqlite3` para la base de datos. Fichero principal: `src/logica/LogicaDeNegocio.js`, que exporta la clase `LogicaDeNegocio`.
- **Cabecera de fichero:** cada fichero empieza con un comentario que indica nombre del fichero, descripción, autora (Pelin Kaynak), fecha y copyright.
- **Cabeceras de funciones/métodos:** cada función o método incluye su diseño lógico en un bloque de comentarios delimitado por líneas discontinuas (`--------------------`), con la notación de la asignatura (ej. `UUID: Text, TipoMedicion: N, ValorMedicion: Z --> guardarMedicion() -->`).
- **Legibilidad:** el código debe ser lo más claro y autoexplicativo posible (nombres descriptivos en castellano), de forma que casi no necesite comentarios adicionales.
- **Tests automáticos:** con mocha, en `src/logica/test/`. Usan una base de datos temporal y comprueban: abrir la base de datos, guardar una medición y recuperarla como la última (con `Timestamp` generado), que solo se devuelven los cuatro campos de `Medicion`, que con varias mediciones se devuelve la más reciente, que con la tabla vacía se devuelve `null`, y que un tipo de medición no válido produce un error.
- **Fidelidad al diseño:** no añadir métodos públicos ni funcionalidades que no aparezcan en este diseño.
