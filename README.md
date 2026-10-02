# Proyecto Biometría y Medio Ambiente – Sprint 0

**Autora:** Pelin Kaynak · GTI, EPSG (UPV Campus de Gandia)

## Descripción

Sistema distribuido que lleva una medición ambiental desde un sensor hasta un navegador:

```
Arduino (C++)  --BLE iBeacon-->  Teléfono Android (Java)  --POST REST-->  Servidor (Node.js)
                                                                            |  api  -> logica -> basedatos (SQLite)
Navegador (HTML/JS)  <--GET REST--------------------------------------------'
```

1. **Arduino** (placa SparkFun Pro nRF52840 Mini) genera una medición ficticia (CO2 = 123) y la emite en una trama iBeacon.
2. **Android** detecta la trama, extrae tipo y valor, y la envía por `POST /guardarMedicion`.
3. **Servidor REST** recibe la petición y llama a la **lógica del negocio**, que la guarda en **SQLite**.
4. La **página web** pide `GET /recuperarUltimaMedicion` y muestra la última medición.

**Criterio de aceptación del Sprint 0:** el valor ficticio puesto en la placa Arduino aparece en la página web.

## Estructura del repositorio

| Carpeta | Contenido | Diseño |
|---|---|---|
| `doc/` | Diseños de cada componente (`xxx_design.md`) | – |
| `src/arduino/` | Programa de la placa (C++) | `doc/arduino_design.md` |
| `src/android/` | App Android (Java) | `doc/android_design.md` |
| `src/basedatos/` | Script SQL de la base de datos | `doc/basedatos_design.md` |
| `src/logica/` | Lógica del negocio (Node.js) | `doc/logica_design.md` |
| `src/api/` | Servidor REST (Node.js + Express) | `doc/api_design.md` |
| `src/web/` | Lógica fake y UX del navegador (HTML/CSS/JS) | `doc/web_design.md` |

## Ramas

- `master`: versiones entregadas de cada sprint.
- `develop`: desarrollo diario; se fusiona en `master` al cerrar el sprint.

## Despliegue

> *Se completará a medida que se implementen los componentes.*

### Servidor + web
```bash
npm install            # instala express, sqlite3, mocha...
npm run servidor       # arranca el servidor REST en http://localhost:8080
```
Abrir en el navegador: `http://localhost:8080/` (el servidor también sirve la página web).

### Arduino (SparkFun Pro nRF52840 Mini)
1. Arduino IDE → Preferencias → Gestor de URLs adicionales de tarjetas: `https://adafruit.github.io/arduino-board-index/package_adafruit_index.json`
2. Gestor de placas → instalar **Adafruit nRF52 by Adafruit**.
3. Añadir la definición de la placa SparkFun Pro nRF52840 Mini siguiendo el [tutorial de SparkFun](https://learn.sparkfun.com/tutorials/nrf52840-development-with-arduino-and-circuitpython) (ficheros en `%LOCALAPPDATA%\Arduino15\packages\adafruit\hardware\nrf52\<versión>`).
4. Abrir `src/arduino/HolaMundoIBeacon/HolaMundoIBeacon.ino`, elegir la placa *SparkFun Pro nRF52840 Mini* y el puerto, y subir.
5. Comprobación: Monitor Serie a 115200 baudios y, en el móvil, la app **nRF Connect** debe mostrar el dispositivo `GTI3A-2025` con UUID `EPSG-GTI-PROY-3A`.

### Android
Abrir `src/android/` con Android Studio, poner la IP del servidor en `URL_SERVIDOR` de `LogicaFakeTelefono.java` (emulador: `http://10.0.2.2:8080`; teléfono real: `http://<IPv4 del ordenador>:8080`) y ejecutar en un teléfono real con Bluetooth.

## API REST

| Ruta | Petición | Respuesta |
|---|---|---|
| `POST /guardarMedicion` | `{"UUID": "EPSG-GTI-PROY-3A", "TipoMedicion": 11, "ValorMedicion": 123}` | `201` éxito · `400` / `500` error |
| `GET /recuperarUltimaMedicion` | – | `200` `{"UUID": "EPSG-GTI-PROY-3A", "Timestamp": "2026-09-30T10:15:30.000Z", "TipoMedicion": 11, "ValorMedicion": 123}` · `404` / `500` error |

El `Timestamp` lo genera el servidor al guardar. Detalles en `doc/api_design.md`.

## Tests

```bash
npm test               # tests automáticos de basedatos, logica, api y web (mocha)
```
Tests de Android (JUnit): en `src/android/` ejecutar `./gradlew test`.
