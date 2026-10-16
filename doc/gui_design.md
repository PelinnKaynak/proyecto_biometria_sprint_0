# Diseño del componente: gui

**Autora:** Pelin Kaynak · **Sprint:** 0 · **Implementación:** `src/gui/`

---

## 1. Diseño del componente

La interfaz gráfica del navegador (UX, *ClienteHTML*): la página que ve el usuario. Muestra la última medición guardada. Solo habla con la lógica del negocio del cliente (`frontend_business_logic_design.md`); nunca hace peticiones HTTP directamente.

Sigue la estructura `ux/` + `logicaFake/` del esqueleto de la asignatura (ver `referencia/esqueletoPHP.md`).

```text
GUI (index.html + ux.js)  -->  LogicaFakeNavegador.recuperarUltimaMedicion()
```

### Tipos

```text
Medicion = ( IdSensor: N, Timestamp: Text, TipoMedicion: N, ValorMedicion: Z )
```

### Pantalla

```text
+--------------------------------------------------+
|  H1  Título: "Última Medición"                   |
|                                                  |
|  +-- DIV Contenedor (contenedorMedicion) ------+ |
|  |  P  IdSensor:       N                       | |
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

### Estado y funciones de `ux.js`

```text
laLogicaFake: LogicaFakeNavegador
NOMBRES_TIPO_MEDICION = { 11: "CO2", 12: "Temperatura", 13: "Ruido" }
FORMATO_FECHA: formato de fecha y hora local (dd/mm/aaaa, hh:mm:ss)

botonActualizarPulsado()

medicion: Medicion --> mostrarMedicion()

mensaje: Text --> mostrarError()
```

---

## 2. Aclaraciones del diseño

- **Nombres:** los campos de `Medicion` empiezan por mayúscula porque son las claves del JSON y las columnas de la tabla `Mediciones` (`database_design.md`). `NOMBRES_TIPO_MEDICION` y `FORMATO_FECHA` son constantes (mayúsculas, convención de JavaScript).
- **Botón:** cuando el usuario lo pulsa, `botonActualizarPulsado()` llama a `recuperarUltimaMedicion()` de la lógica fake; si hay medición, llama a `mostrarMedicion()`; si es vacío, muestra el mensaje *"Todavía no hay mediciones"*; si hay error, llama a `mostrarError()` con su texto.
- **mostrarMedicion():** escribe los cuatro datos en los párrafos del contenedor y oculta el mensaje de error. Para que el usuario los entienda, el `Timestamp` se muestra como fecha y hora local (ej. `06/10/2026, 11:52:54`) y el `TipoMedicion` con su nombre (`11` → `CO2`, `12` → `Temperatura`, `13` → `Ruido`); un tipo desconocido se muestra con su número. Los datos no cambian: solo cambia cómo se muestran.
- **Varios sensores:** la página muestra la última medición de cualquier sensor; la fila `IdSensor` indica de qué sensor es.
- **mostrarError():** escribe el texto en el párrafo de error y lo hace visible.
- **HTML:** cada `<p>` del contenedor tiene la etiqueta fija y un `<span>` con id (`IdSensor`, `Timestamp`, `TipoMedicion`, `ValorMedicion`) donde se escribe el dato. El botón tiene id `botonActualizar` y el párrafo de error id `mensajeError` (atributo `hidden` cuando no hay error).
- **Carga de scripts:** al final del `<body>`, primero `frontend_business_logic/LogicaFakeNavegador.js` y después `ux.js`.
- **Separación de capas:** la GUI nunca usa `fetch` ni conoce las rutas del servidor ni la base de datos.
- **Ficheros:** `src/gui/index.html`, `src/gui/estilos.css`, `src/gui/ux.js`. El servidor los sirve en la ruta `/`.

---

## 3. Reglas generales

- **Lenguaje:** HTML5, CSS3 y JavaScript (ES2020) sin frameworks, ejecutado en el navegador.
- **Cabecera de fichero:** cada fichero empieza con un comentario que indica nombre del fichero, descripción, autora (Pelin Kaynak), fecha y copyright.
- **Cabeceras de funciones/métodos:** cada función o método incluye su diseño lógico en un bloque de comentarios delimitado por líneas discontinuas (`--------------------`), con la notación de la asignatura, y una línea breve en inglés `What it does:` dentro del mismo bloque.
- **Legibilidad:** el código debe ser lo más claro y autoexplicativo posible (nombres descriptivos en castellano), de forma que casi no necesite comentarios adicionales.
- **Tests automáticos:** con mocha y `jsdom`, en `src/gui/test/`, sustituyendo la lógica fake por un doble de prueba. Comprueban la estructura de la página (título, contenedor con 4 párrafos, botón, párrafo de error), que pulsar el botón llama a `recuperarUltimaMedicion()`, que los datos se muestran en los `<p>` correctos (fecha local y nombre del tipo), y que los errores y el caso "sin mediciones" se muestran al usuario.
- **Fidelidad al diseño:** no añadir funciones, pantallas ni elementos que no aparezcan en este diseño.
