# Diseño del componente: web

**Autora:** Pelin Kaynak · **Sprint:** 0 · **Implementación:** `src/web/`

---

## 1. Diseño del componente

La parte web tiene dos capas, siguiendo la estructura `ux/` + `logicaFake/` del esqueleto de la asignatura (ver `doc/referencia/esqueletoPHP.md`):

1. **LogicaFakeNavegador:** ofrece al navegador las mismas funciones que la lógica del negocio, pero por dentro hace peticiones a la API REST (`api_design.md`).
2. **UX (ClienteHTML):** la página que ve el usuario. Solo habla con la lógica fake.

```text
UX (index.html + ux.js)  -->  LogicaFakeNavegador  --GET-->  API REST
```

### Tipos

```text
Medicion = ( uuid: Text, timestamp: Text, tipoMedicion: N, valorMedicion: Z )
```

### LogicaFakeNavegador

```text
                                   --------- LogicaFakeNavegador --------------
                                   |
                                   | urlServidor: Text
                                   |
                                   |
  urlServidor: Text            --> LogicaFakeNavegador() -->
                                   |
                                   |
  Medicion | vacío             <-- recuperarUltimaMedicion() <--
  error: Text                  <--
                                   |
                                   --------------------------------------------
```

### UX del navegador (ClienteHTML)

```text
+--------------------------------------------------+
|  H1  "Última medición"                           |
|                                                  |
|  +-- DIV contenedorMedicion -------------------+ |
|  |  P  UUID:            <uuid: Text>           | |
|  |  P  Timestamp:       <timestamp: Text>      | |
|  |  P  Tipo medición:   <tipoMedicion: N>      | |
|  |  P  Valor medición:  <valorMedicion: Z>     | |
|  +---------------------------------------------+ |
|                                                  |
|  [ BUTTON "Actualizar" ]                         |
|                                                  |
|  P  mensajeError: <Text> (oculto si no hay error)|
+--------------------------------------------------+
```

Funciones de la UX (`ux.js`):

```text
botonActualizarPulsado() -->

medicion: Medicion --> mostrarMedicion() -->

mensaje: Text --> mostrarError() -->
```

---

## 2. Aclaraciones del diseño

- **Nombre del método:** en el documento de Sprint 0 se llamaba `recuperarMedicion()`. Se renombra a `recuperarUltimaMedicion()` para que la lógica fake tenga **exactamente** los mismos nombres que la lógica del negocio, de la que se deriva.
- **recuperarUltimaMedicion():** hace `GET {urlServidor}/recuperarUltimaMedicion`.
  - `200` → devuelve la `Medicion` recibida en el JSON.
  - `404` → devuelve **vacío** (`null`): todavía no hay mediciones.
  - Otro código o fallo de red → produce un error con un texto descriptivo.
- **urlServidor:** por defecto es la cadena vacía (`""`), es decir, el mismo origen que sirvió la página (el servidor REST sirve también la web). Los tests pasan la URL del servidor de pruebas.
- **botonActualizarPulsado():** llama a `recuperarUltimaMedicion()`; si hay medición, llama a `mostrarMedicion()`; si es vacío, muestra el mensaje *"Todavía no hay mediciones"*; si hay error, llama a `mostrarError()`.
- **Al cargar la página** se ejecuta una vez `botonActualizarPulsado()` para que la última medición aparezca sin pulsar nada.
- **mostrarMedicion():** junto al número del tipo se muestra su nombre, por ejemplo `11 (CO2)`, `12 (Temperatura)`, `13 (Ruido)`. Oculta el mensaje de error.
- **Estado de ux.js:** `laLogicaFake: LogicaFakeNavegador` (creada con `urlServidor = ""`) y la tabla constante `NOMBRES_TIPO_MEDICION = { 11: "CO2", 12: "Temperatura", 13: "Ruido" }`.
- **HTML:** cada `<p>` del contenedor tiene la etiqueta fija y un `<span>` con id (`uuid`, `timestamp`, `tipoMedicion`, `valorMedicion`) donde se escribe el dato. El botón tiene id `botonActualizar` y el párrafo de error id `mensajeError` (atributo `hidden` cuando no hay error).
- **Separación de capas:** la UX nunca hace `fetch` directamente ni conoce las rutas REST; eso es trabajo de la lógica fake.
- **Ficheros:** `src/web/index.html`, `src/web/estilos.css`, `src/web/ux.js`, `src/web/logicaFake/LogicaFakeNavegador.js`.

---

## 3. Reglas generales

- **Lenguaje:** HTML5, CSS3 y JavaScript (ES2020) sin frameworks, ejecutado en el navegador. `LogicaFakeNavegador.js` usa `fetch` y también se exporta con `module.exports` (si existe) para poder probarlo en Node.js.
- **Cabecera de fichero:** cada fichero empieza con un comentario que indica nombre del fichero, descripción, autora (Pelin Kaynak), fecha y copyright.
- **Cabeceras de funciones/métodos:** cada función o método incluye su diseño lógico en un bloque de comentarios delimitado por líneas discontinuas (`--------------------`), con la notación de la asignatura.
- **Legibilidad:** el código debe ser lo más claro y autoexplicativo posible (nombres descriptivos en castellano), de forma que casi no necesite comentarios adicionales.
- **Tests automáticos:** con mocha, en `src/web/test/`.
  - Lógica fake: contra el servidor REST real arrancado con una base de datos temporal; comprueban la respuesta `null` con la tabla vacía, que se recibe la medición guardada con todos sus campos, y el error cuando el servidor no está disponible.
  - UX: con `jsdom`, sustituyendo la lógica fake por un doble de prueba; comprueban que pulsar el botón llama a `recuperarUltimaMedicion()`, que los datos se muestran en los `<p>` correctos y que un error se muestra al usuario.
- **Fidelidad al diseño:** no añadir funciones, pantallas ni elementos que no aparezcan en este diseño.
