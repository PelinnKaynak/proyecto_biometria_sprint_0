# Diseño del componente: web

**Autora:** Pelin Kaynak · **Sprint:** 0 · **Implementación:** `src/web/`

---

## 1. Diseño del componente

La parte web tiene dos capas, siguiendo la estructura `ux/` + `logicaFake/` del esqueleto de la asignatura (ver `doc/referencia/esqueletoPHP.md`):

1. **LogicaFakeNavegador:** obtiene la última medición mediante el servidor REST (`api_design.md`). No accede a la base de datos ni a la lógica del negocio.
2. **UX (ClienteHTML):** la página que ve el usuario. Solo habla con la lógica fake.

```text
UX (index.html + ux.js)  -->  LogicaFakeNavegador  --GET /recuperarUltimaMedicion-->  API REST
```

### LogicaFakeNavegador

```text
            --------- LogicaFakeNavegador ------------
            |
            |
          <-- recuperarMedicion() --> JSON( UUID: Text,
            |                               Timestamp: Text,
            |                               TipoMedicion: N,
            |                               ValorMedicion: Z )
            |                         | vacío | error: Text
            |
            ------------------------------------------
```

### UX del navegador (ClienteHTML)

```text
+--------------------------------------------------+
|  H1  Título: "Última Medición"                   |
|                                                  |
|  +-- DIV Contenedor (contenedorMedicion) ------+ |
|  |  P  UUID:           Text                    | |
|  |  P  Timestamp:      Text                    | |
|  |  P  TipoMedicion:   N                       | |
|  |  P  ValorMedicion:  Z                       | |
|  +---------------------------------------------+ |
|                                                  |
|  [ Button "Actualizar" ]                         |
|                                                  |
|  P  Error: Text  (oculto si no hay error)        |
+--------------------------------------------------+
```

Estado y funciones de la UX (`ux.js`):

```text
laLogicaFake: LogicaFakeNavegador
NOMBRES_TIPO_MEDICION = { 11: "CO2", 12: "Temperatura", 13: "Ruido" }
FORMATO_FECHA: formato de fecha y hora local (dd/mm/aaaa, hh:mm:ss)

botonActualizarPulsado() -->

medicion: JSON( UUID: Text, Timestamp: Text, TipoMedicion: N, ValorMedicion: Z ) --> mostrarMedicion() -->

mensaje: Text --> mostrarError() -->
```

---

## 2. Aclaraciones del diseño

- **recuperarMedicion():** no recibe ningún dato de entrada y hace `GET /recuperarUltimaMedicion` al servidor REST (URL relativa: el mismo servidor que sirvió la página, porque el servidor REST sirve también la web).
  - `200` → devuelve el JSON recibido con `UUID`, `Timestamp`, `TipoMedicion` y `ValorMedicion`.
  - `404` → devuelve **vacío** (`null`): todavía no hay mediciones.
  - Otro código o fallo de red → produce un error con un texto descriptivo.
- **Botón:** cuando el usuario lo pulsa, `botonActualizarPulsado()` llama a `recuperarMedicion()` de la lógica fake; si hay medición, llama a `mostrarMedicion()`; si es vacío, muestra el mensaje *"Todavía no hay mediciones"*; si hay error, llama a `mostrarError()`.
- **mostrarMedicion():** escribe los cuatro datos en los párrafos del contenedor y oculta el mensaje de error. Para que el usuario los entienda, el `Timestamp` se muestra como fecha y hora local (ej. `06/10/2026, 11:52:54`) y el `TipoMedicion` con su nombre (`11` → `CO2`, `12` → `Temperatura`, `13` → `Ruido`); un tipo desconocido se muestra con su número. Los datos que llegan del servidor no cambian: solo cambia cómo se muestran.
- **Estado de ux.js:** `laLogicaFake: LogicaFakeNavegador`, la tabla constante `NOMBRES_TIPO_MEDICION` y el formato de fecha `FORMATO_FECHA`.
- **HTML:** cada `<p>` del contenedor tiene la etiqueta fija y un `<span>` con id (`UUID`, `Timestamp`, `TipoMedicion`, `ValorMedicion`) donde se escribe el dato. El botón tiene id `botonActualizar` y el párrafo de error id `mensajeError` (atributo `hidden` cuando no hay error).
- **Separación de capas:** la UX nunca hace `fetch` directamente ni conoce las rutas REST ni la base de datos; la comunicación se hace mediante la lógica fake.
- **Ficheros:** `src/web/index.html`, `src/web/estilos.css`, `src/web/ux.js`, `src/web/logicaFake/LogicaFakeNavegador.js`.

---

## 3. Reglas generales

- **Lenguaje:** HTML5, CSS3 y JavaScript (ES2020) sin frameworks, ejecutado en el navegador. `LogicaFakeNavegador.js` usa `fetch` y también se exporta con `module.exports` (si existe) para poder probarlo en Node.js.
- **Cabecera de fichero:** cada fichero empieza con un comentario que indica nombre del fichero, descripción, autora (Pelin Kaynak), fecha y copyright.
- **Cabeceras de funciones/métodos:** cada función o método incluye su diseño lógico en un bloque de comentarios delimitado por líneas discontinuas (`--------------------`), con la notación de la asignatura.
- **Legibilidad:** el código debe ser lo más claro y autoexplicativo posible (nombres descriptivos en castellano), de forma que casi no necesite comentarios adicionales.
- **Tests automáticos:** con mocha, en `src/web/test/`.
  - Lógica fake: contra el servidor REST real arrancado con una base de datos temporal; comprueban que `recuperarMedicion()` hace `GET /recuperarUltimaMedicion`, la respuesta `null` con la tabla vacía, que se recibe el JSON con `UUID`, `Timestamp`, `TipoMedicion` y `ValorMedicion`, y los errores (servidor no disponible, código 500).
  - UX: con `jsdom`, sustituyendo la lógica fake por un doble de prueba; comprueban la estructura de la página (título, contenedor con 4 párrafos, botón, párrafo de error), que pulsar el botón llama a `recuperarMedicion()`, que los datos se muestran en los `<p>` correctos (fecha local y nombre del tipo), y que los errores se muestran al usuario.
- **Fidelidad al diseño:** no añadir funciones, pantallas ni elementos que no aparezcan en este diseño.
