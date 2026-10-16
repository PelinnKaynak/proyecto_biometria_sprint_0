# Prompts utilizados con la IA

- **Herramienta:** Claude (asistente de IA de Anthropic)
- **Autora:** Pelin Kaynak
- **Sprint:** 0 – Proyecto Biometría y Medio Ambiente

Aquí están los prompts más importantes que he usado en el sprint, ordenados por tema. Los escribí en turco; aquí están resumidos en español.

---

## 1. Código a partir de mi diseño

> Te paso mi documento de diseño (las clases, los métodos y sus parámetros en la notación de la asignatura) y el código que nos dio el profesor (Arduino y Android). A partir de mi diseño, escribe el código del servidor REST, la lógica del negocio, la base de datos SQLite y la página web. Cada función o método tiene que llevar una cabecera con su diseño lógico entre líneas discontinuas. Usa nombres en castellano y código fácil de leer.

> Conecta la aplicación Android con el servidor: cuando la app detecte una trama iBeacon de mi placa, tiene que sacar la medición (IdSensor, tipo y valor) y enviarla al servidor con una lógica fake del teléfono (`LogicaFakeTelefono`). Esta lógica tiene que tener la misma firma que `guardarMedicion()` del servidor, para que `MainActivity` no tenga código HTTP.

> Haz lo mismo en la web: la página no puede hacer peticiones HTTP directamente. Crea `LogicaFakeNavegador` con `recuperarUltimaMedicion()`, con la misma firma que en la lógica del negocio.

## 2. Tests

> Escribe tests automáticos de los métodos críticos de cada parte:
> - **mocha:** lógica del negocio, servidor REST (códigos 201, 400, 404), base de datos (columnas, CHECK, valores negativos), lógica fake del navegador y la página web (con jsdom).
> - **JUnit:** clases de Android y `LogicaFakeTelefono`.
> - **Placa:** un test en C++ con un doble de prueba de Bluefruit, para probar el código de la placa sin la placa.

## 3. Cambios en la base de datos

> Quiero cambiar la base de datos: en vez de guardar el UUID del beacon, la tabla `Mediciones` tiene que guardar `IdSensor` (un número). El UUID solo se usa en el teléfono para reconocer mi placa (`EPSG-GTI-PELIN` + IdSensor). El `Timestamp` no lo envía el teléfono: lo genera el servidor. Añade `CHECK` para que `TipoMedicion` solo sea 11, 12 o 13 y para que `IdSensor` no sea negativo. Actualiza el diseño de la base de datos, el script SQL, el código y los tests.

> En la base de datos la hora sale con 2 horas de diferencia. Explícame por qué y haz que la web muestre la hora local.

## 4. Corregir lo que no funcionaba

> La app muestra la medición, pero al guardar dice "No se pudo guardar en el servidor: respondió 0". Revisa la IP del servidor (`URL_SERVIDOR`), los timeouts y por qué la petición no llega al servidor.

> En mi teléfono (Android 11) la app se queda en "Buscando nuestra placa" y no la encuentra, pero nRF Connect sí la ve. Revisa los permisos de Bluetooth y de ubicación y cómo se filtran las tramas.

> En la base de datos solo se guarda una medición, aunque la placa sigue enviando. Busca la causa y corrígela. *(La causa era que el teléfono solo avisa una vez de cada dispositivo por búsqueda; ahora la búsqueda se reinicia cada 10 segundos.)*

> El botón "Detener" no para la búsqueda de todos los dispositivos. Corrígelo y quita los contadores que había puesto para depurar.

> Revisa todo el código otra vez y dime si hay algo que no esté bien.

## 5. Cumplir AGENTS.md

> Te paso `AGENTS.md` y las especificaciones (`Software_Engineering_Spec.md` y `Database_Design_Spec.md`). Revisa todo mi repositorio y dime qué no cumple al 100 %. Después corrígelo: estructura de carpetas (`communication`, `business_logic`, `database`, lógica del cliente y del navegador), las tres secciones de cada documento de diseño, la notación v3, las cabeceras de las funciones, las firmas iguales entre cliente y servidor y los tests.

> La lectura de la trama está dentro de `MainActivity` y no se puede probar sin teléfono. Sácala a una clase aparte (`MedicionIBeacon`) y escribe sus tests con JUnit.

> Dime qué tengo que cambiar en mis dibujos del cuaderno para que coincidan con el código y con la notación v3, clase por clase.

## 6. Revisión con el agente

> Crea la carpeta `inputs` con el enlace de mi repositorio de GitHub. Revisa el repositorio como el agente de `AGENTS.md` y escribe el informe en `outputs/2026-10-16/Pelin_Kaynak_review.md`.

> El informe tiene dos fallos de notación (`gui_design.md` y `Publicador` en `arduino_design.md`). Corrígelos y vuelve a revisar el repositorio.

---

## Cómo he usado la IA

- He hecho yo el diseño (los dibujos del cuaderno y el documento de diseño). La IA ha escrito el código y los tests a partir de ese diseño.
- He usado la IA para encontrar y corregir los errores que aparecían al probar el sistema.
- He usado la IA para revisar que todo cumple `AGENTS.md` y las especificaciones.
- He probado yo el sistema completo con la placa, el teléfono, el servidor y la web.
