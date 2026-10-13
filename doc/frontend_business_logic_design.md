# Diseño del componente: frontend_business_logic

**Autora:** Pelin Kaynak · **Sprint:** 0 · **Implementación:** `src/frontend_business_logic/`

---

## 1. Diseño del componente

Lógica del negocio del cliente web (lógica fake / proxy). La GUI (`gui_design.md`) solo habla con este componente. Ofrece **la misma interfaz** que la lógica del negocio del backend (`business_logic_design.md`), pero por dentro no accede a la base de datos: llama al componente de comunicación (`communication_design.md`).

Regla del subconjunto: la GUI solo necesita consultar la última medición, así que este componente ofrece únicamente `recuperarUltimaMedicion()`.

### Tipos

```text
TipoMedicion = { CO2 = 11, TEMPERATURA = 12, RUIDO = 13 }

Medicion = ( UUID: Text, Timestamp: Text, TipoMedicion: N, ValorMedicion: Z )
```

### Clase LogicaFakeNavegador

```text
                          --------- LogicaFakeNavegador ---------------------
                          |
                          |
  Medicion | vacío    <-- recuperarUltimaMedicion() <--
                          |
                          ---------------------------------------------------
```

Firma idéntica a la del backend:

| Método | `business_logic_design.md` (LogicaDeNegocio) | Este componente (LogicaFakeNavegador) |
|---|---|---|
| `recuperarUltimaMedicion()` | `recuperarUltimaMedicion() --> Medicion \| vacío` | `recuperarUltimaMedicion() --> Medicion \| vacío` |

---

## 2. Aclaraciones del diseño

- **Implementación proxy:** `recuperarUltimaMedicion()` hace `GET /recuperarUltimaMedicion` (definido en `communication_design.md`) con una URL relativa, es decir, al mismo servidor que sirvió la página. Por eso no hay ninguna IP escrita en el código.
- **Traducción de la respuesta a tipos de dominio:** la respuesta con la medición se devuelve como `Medicion`; la respuesta "todavía no hay mediciones" se devuelve como **vacío** (`null`). Los detalles de transporte quedan dentro de este componente: la GUI no los conoce.
- **Errores:** si no se puede contactar con el servidor o este falla, el método lanza un error con un texto descriptivo (por ejemplo *"No se puede conectar con el servidor"*). En el diseño lógico no se representa, igual que en el backend.
- **Asincronía:** el método devuelve una `Promise` (en el diseño lógico se omite, según la notación de la asignatura).
- **Sin estado:** la clase no tiene atributos.
- **Publicación:** el servidor sirve este fichero en la ruta `/frontend_business_logic/LogicaFakeNavegador.js`, y la GUI lo carga antes que `ux.js`.

---

## 3. Reglas generales

- **Lenguaje:** JavaScript (ES2020) sin frameworks, ejecutado en el navegador. Fichero: `src/frontend_business_logic/LogicaFakeNavegador.js`. Usa `fetch`; también se exporta con `module.exports` (si existe) para poder probarlo en Node.js.
- **Cabecera de fichero:** cada fichero empieza con un comentario que indica nombre del fichero, descripción, autora (Pelin Kaynak), fecha y copyright.
- **Cabeceras de funciones/métodos:** cada función o método incluye su diseño lógico en un bloque de comentarios delimitado por líneas discontinuas (`--------------------`), con la notación de la asignatura, y una línea breve en inglés `What it does:` dentro del mismo bloque.
- **Legibilidad:** el código debe ser lo más claro y autoexplicativo posible (nombres descriptivos en castellano), de forma que casi no necesite comentarios adicionales.
- **Tests automáticos:** con mocha, en `src/frontend_business_logic/test/`, contra el servidor REST real arrancado con una base de datos temporal. Comprueban que `recuperarUltimaMedicion()` llama a `GET /recuperarUltimaMedicion`, que devuelve vacío (`null`) con la tabla vacía, que devuelve la `Medicion` con `UUID`, `Timestamp`, `TipoMedicion` y `ValorMedicion`, y que produce un error si el servidor no está disponible o responde con un error.
- **Fidelidad al diseño:** no añadir métodos públicos que no aparezcan en este diseño, y mantener la firma idéntica a la del backend.
