# Diseño del componente: arduino

**Autora del diseño:** Pelin Kaynak (ingeniería inversa del código proporcionado por el profesor) · **Sprint:** 0 · **Implementación:** `src/arduino/HolaMundoIBeacon/`

---

## 1. Diseño del componente

Programa para la placa **SparkFun Pro nRF52840 Mini** (chip nRF52840, programada con el núcleo *Adafruit nRF52*). Obtiene una medición (ficticia en este sprint) y la emite periódicamente en una trama **iBeacon** por Bluetooth Low Energy.

### Tipos

```text
MedicionesID = { CO2 = 11, TEMPERATURA = 12, RUIDO = 13 }

UUID_16 = [ N ]_16                       (16 bytes)

TramaIBeacon = ( beaconUUID: UUID_16, major: N, minor: Z, txPower: Z )

   beaconUUID = "EPSG-GTI-PELIN" (14 bytes, identifica las placas de este proyecto) + idSensor (2 bytes: alto, bajo)

   major = tipoMedicion * 256 + contador     (byte alto: tipo, byte bajo: contador)
   minor = valorMedicion
```

### Programa principal (`HolaMundoIBeacon.ino`)

```text
Globales = ( elLED: LED, elPuerto: PuertoSerie, elPublicador: Publicador, elMedidor: Medidor )

Loop = ( cont: N )        (contador de vueltas de loop(), de 0 a 255)

inicializarPlaquita()

setup()              (espera el puerto serie, enciende la emisora, inicia el medidor)

lucecitas()          (secuencia de parpadeos del LED)

loop()               (cont := cont + 1; lucecitas(); valor := medirCO2(); publicarCO2(valor, cont, 10000))
```

### Clase LED (`LED.h`)

Función libre definida en `LED.h` (la usan todas las clases):

```text
tiempo: Z --> esperar()          (espera tiempo milisegundos)
```

```text
                     --------- LED ------------------------------
                     |
                     | numeroLED: Z
                     | encendido: B
                     |
                     |
  numero: Z      --> LED() -->
                     |
                     |
                     encender() -->
                     |
                     |
                     apagar() -->
                     |
                     |
                     alternar() -->
                     |
                     |
  tiempo: Z      --> brillar() -->
                     |
                     --------------------------------------------
```

### Clase Medidor

```text
                     --------- Medidor --------------------------
                     |
                     |
                     Medidor() -->
                     |
                     |
                     iniciarMedidor() -->
                     |
                     |
             Z  <--  medirCO2() <--
                     |
                     |
             Z  <--  medirTemperatura() <--
                     |
                     --------------------------------------------
```

### Clase Publicador

```text
                              --------- Publicador --------------------------------
                              |
                              | ID_SENSOR: N          = 1                        (NUEVO)
                              | beaconUUID: UUID_16   = "EPSG-GTI-PELIN" + ID_SENSOR
                              |
                              |
  laEmisora: EmisoraBLE   <-- laEmisora        (atributo público: "PelinIBeacon", 0x004c, 4)
  RSSI: Z                 <-- RSSI             (constante pública = -53)
                              |
                              |
                              Publicador() -->
                              |
                              |
                              encenderEmisora() -->
                              |
                              |
  valorCO2: Z             --> publicarCO2() -->
  contador: N                 |
  tiempoEspera: Z             |
                              |
                              |
  valorTemperatura: Z     --> publicarTemperatura() -->
  contador: N                 |
  tiempoEspera: Z             |
                              |
                              -----------------------------------------------------
```

### Clase PuertoSerie

```text
                     --------- PuertoSerie ----------------------
                     |
                     |
  baudios: Z     --> PuertoSerie() -->
                     |
                     |
                     esperarDisponible() -->
                     |
                     |
  mensaje: T     --> escribir() -->          (T: cualquier tipo imprimible)
                     |
                     --------------------------------------------
```

### Clase EmisoraBLE

```text
                                   --------- EmisoraBLE ----------------------------------
                                   |
                                   | nombreEmisora: Text
                                   | fabricanteID: N
                                   | txPower: Z
                                   |
                                   |
  nombreEmisora: Text          --> EmisoraBLE() -->
  fabricanteID: N                  |
  txPower: Z                       |
                                   |
                                   |
                                   encenderEmisora() -->      (2 versiones; ver Aclaraciones)
                                   |
                                   |
                                   instalarCallbackConexionEstablecida() -->
                                   |
                                   |
                                   instalarCallbackConexionTerminada() -->
                                   |
                                   |
                                   detenerAnuncio() -->
                                   |
                                   |
                        B     <--  estaAnunciando() <--
                                   |
                                   |
  beaconUUID: UUID_16          --> emitirAnuncioIBeacon() -->
  major: Z                         |
  minor: Z                         |
  rssi: N                          |
                                   |
                                   |
  carga: Text                  --> emitirAnuncioIBeaconLibre() -->
  tamanyoCarga: N                  |
                                   |
                                   |
  servicio: ServicioEnEmisora  --> anyadirServicio() -->
                        B     <--  |
                                   |
                                   |
  servicio: ServicioEnEmisora  --> anyadirServicioConSusCaracteristicas() -->
                        B     <--  |      (1ª versión: sin características)
                                   |
                                   |
  servicio: ServicioEnEmisora  --> anyadirServicioConSusCaracteristicas() -->
  caracteristicas: [ Caracteristica ]
                        B     <--  |
                                   |
                                   |
  servicio: ServicioEnEmisora  --> anyadirServicioConSusCaracteristicasYActivar() -->
  caracteristicas: [ Caracteristica ]
                        B     <--  |
                                   |
                                   |
  connHandle: N                --> getConexion() <--
  BLEConnection               <--  |
                                   |
                                   ---------------------------------------------------------
```

### Clase ServicioEnEmisora (con la clase interna Caracteristica)

```text
                                   --------- ServicioEnEmisora ----------------------------
                                   |
                                   | uuidServicio: UUID_16
                                   | elServicio: BLEService
                                   | lasCaracteristicas: [ Caracteristica ]
                                   |
                                   |
  nombreServicio: Text         --> ServicioEnEmisora() -->
                                   |
                                   |
                                   escribeUUID() <--
                                   |
                                   |
  car: Caracteristica          --> anyadirCaracteristica() -->
                                   |
                                   |
                                   activarServicio() -->
                                   |
                                   |
               BLEService     <--  operator BLEService() <--
                                   |
                                   ---------------------------------------------------------

                                   --------- Caracteristica -------------------------------
                                   |
                                   | uuidCaracteristica: UUID_16
                                   | laCaracteristica: BLECharacteristic
                                   |
                                   |    props: N --> asignarPropiedades() -->
                                   |    permisoRead: BleSecurityMode, permisoWrite: BleSecurityMode
                                   |               --> asignarPermisos() -->
                                   |    tam: N --> asignarTamanyoDatos() -->
                                   |
                                   |
  nombreCaracteristica: Text   --> Caracteristica() -->
                                   |
                                   |
  nombreCaracteristica: Text   --> Caracteristica() -->
  props: N                         |
  permisoRead: BleSecurityMode     |
  permisoWrite: BleSecurityMode    |
  tam: N                           |
                                   |
                                   |
  props: N                     --> asignarPropiedadesPermisosYTamanyoDatos() -->
  permisoRead: BleSecurityMode     |
  permisoWrite: BleSecurityMode    |
  tam: N                           |
                                   |
                                   |
  str: Text                    --> escribirDatos() -->
  N                           <--  |
                                   |
                                   |
  str: Text                    --> notificarDatos() -->
  N                           <--  |
                                   |
                                   |
                                   instalarCallbackCaracteristicaEscrita() -->
                                   |
                                   |
                                   activar() -->
                                   |
                                   ---------------------------------------------------------
```

Funciones auxiliares libres (en `ServicioEnEmisora.h`):

```text
pString: Text, pUint: [ N ], tamMax: N --> stringAUint8AlReves() --> [ N ]     (copia el texto al revés en pUint)

p: [ T ], n: N --> alReves() --> [ T ]          (invierte el array)
```

---

## 2. Aclaraciones del diseño

- **operator BLEService() (ServicioEnEmisora):** es una conversión de tipo de C++ (`operator BLEService&()`): permite usar un `ServicioEnEmisora` donde Bluefruit espera un `BLEService` (por ejemplo en `anyadirServicio()`), y devuelve `elServicio`. En C++ su nombre es literalmente `operator BLEService`; el `&` (referencia) se quita según la notación.
- **encenderEmisora() (2 versiones):** en el código hay dos `encenderEmisora()`: una sin parámetros y otra que además recibe los callbacks de conexión establecida y terminada. Como la notación omite los callbacks, las dos tienen el mismo diseño lógico y se dibujan una sola vez. Por la misma razón, `instalarCallbackConexionEstablecida()` e `instalarCallbackConexionTerminada()` se dibujan sin parámetros.
- **Código proporcionado:** este componente es código del profesor (carpeta `Arduino-NoCambia`). Su lógica **no se modifica**; solo se añaden cabeceras de fichero y de función con el diseño lógico (rúbrica, "Buenas prácticas").
- **Medición ficticia:** según la rúbrica del Sprint 0 ("se introduce una medida ficticia en el código de la placa Arduino"), en este sprint no se lee el sensor de ozono. `Medidor::medirCO2()` devuelve el valor fijo **123** (y `medirTemperatura()` devuelve −12). Es el valor que debe aparecer en la página web en la prueba de funcionamiento. Para la defensa se puede cambiar a otro número reconocible.
- **Codificación de la trama:** `publicarCO2()` pone en `major` el tipo de medición en el byte alto y el contador en el byte bajo (`major = (CO2 << 8) + contador`), y el valor en `minor`. El teléfono tiene que deshacer esta codificación (ver `android_design.md`).
- **Tiempo de emisión (cambio respecto al código original):** en `loop()` se llama a `publicarCO2(valor, cont, 10000)` en lugar de `1000`. Con 1 segundo de anuncio cada ~5 segundos (el resto lo ocupa `lucecitas()`) la placa no aparecía en *nRF Connect*; con 10 segundos se detecta sin problemas. Junto con el IdSensor en el UUID, son los únicos cambios de lógica en el código proporcionado.
- **UUID del beacon e IdSensor (cambio respecto al código original, `(NUEVO)`):** los 14 primeros bytes son `EPSG-GTI-PELIN` (antes `EPSG-GTI-PROY-`, que es igual en las placas de todos los grupos; ahora son propios y el teléfono los usa para reconocer *nuestras* placas) y los 2 últimos son `ID_SENSOR`, el número del sensor de la placa (antes eran `'3'`, `'A'`). `ID_SENSOR` es una constante privada de `Publicador` (1 en esta placa): cada placa nueva debe tener un número distinto. Así el sistema distingue varios sensores sin cambiar `major` ni `minor`.
- **Nombre de la emisora (`(NUEVO)`):** `PelinIBeacon` en lugar de `GTI3A-2025`, que es igual en las placas de todos los grupos, para reconocer la placa fácilmente en *nRF Connect*. Va en la respuesta de escaneo; el teléfono no lo usa, reconoce la placa por el UUID (`android_design.md`).
- **Datos del anuncio en cada emisión (`(NUEVO)`):** `emitirAnuncioIBeacon()` llama a `Bluefruit.Advertising.clearData()` y `Bluefruit.ScanResponse.clearData()` antes de poner el beacon. Sin esto, en cada `loop()` los datos nuevos se añadían detrás de los anteriores, no cabían en el anuncio (31 bytes) y la placa seguía emitiendo la primera trama: el teléfono solo recibía un contador y guardaba una única medición.
- **Callbacks:** los métodos `instalarCallbackConexionEstablecida()`, `instalarCallbackConexionTerminada()`, `instalarCallbackCaracteristicaEscrita()` y la 2ª versión de `encenderEmisora()` reciben funciones de callback. Los métodos sí aparecen en las cajas, pero, según la notación de la asignatura, el callback se omite, por eso no tienen parámetros dibujados.
- **Tipos externos (opacos):** `BLEService`, `BLECharacteristic`, `BLEConnection` y `BleSecurityMode` son tipos de la biblioteca Bluefruit (`BleSecurityMode` es un alias de `SecureMode_t`). `T` en `alReves()` significa "cualquier tipo" (función plantilla de C++).
- **Funciones libres sin datos de salida:** `setup()`, `loop()`, `lucecitas()`, `inicializarPlaquita()` y `esperar()` no devuelven nada, por eso se escriben sin flecha de salida (sección 4 de la notación). En los métodos de las clases, la flecha de la derecha indica si cambian el estado (`-->`) o solo lo leen (`<--`).
- **Conexión con otros componentes:** la única salida del componente es la trama iBeacon, que recibe el componente `android`.

---

## 3. Reglas generales

- **Lenguaje:** C++ para Arduino (núcleo *Adafruit nRF52*, biblioteca Bluefruit).
- **Cabecera de fichero:** cada fichero empieza con un comentario que indica nombre del fichero, descripción, autor original (Jordi Bataller i Mascarell), aportación de Pelin Kaynak (diseño y documentación), fecha y copyright.
- **Cabeceras de funciones/métodos:** cada función o método incluye su diseño lógico en un bloque de comentarios delimitado por líneas discontinuas (`--------------------`), con la notación de la asignatura, y una línea breve en inglés `What it does:` dentro del mismo bloque.
- **Legibilidad:** el código debe ser lo más claro y autoexplicativo posible, de forma que casi no necesite comentarios adicionales.
- **Tests automáticos:** en `src/arduino/test/`. El programa real de la placa (`HolaMundoIBeacon.ino` con todas sus clases) se compila en el ordenador con g++, sustituyendo Arduino y Bluefruit por un doble de prueba (`stubs/bluefruit.h`) que no emite nada y apunta lo que se le pide. `pruebasHolaMundoIBeacon.cpp` comprueba: `Medidor` (123 y −12), `LED` (encender, apagar, alternar, brillar), `PuertoSerie.escribir()`, `EmisoraBLE` (encender, nombre `PelinIBeacon`, txPower 4, fabricante 0x004C, intervalo, emitir y detener anuncio), `Publicador` (UUID = prefijo + IdSensor, `major = (tipo << 8) + contador`, `minor = valor`, duración del anuncio y parada), `alReves()`, `stringAUint8AlReves()`, y `setup()`/`loop()` (contador, valor publicado y duración de una vuelta). Se ejecutan con `npm test` (`arduino.test.js`; se omiten si el ordenador no tiene g++).
- **Prueba en la placa (criterio de aceptación):**
  1. Subir el programa y abrir el Monitor Serie (115200 baudios): aparece `---- loop(): empieza N` en cada iteración.
  2. Con la app *nRF Connect* en el teléfono se ve el dispositivo `PelinIBeacon` con un UUID que empieza por `EPSG-GTI-PELIN` y termina en `00 01` (IdSensor = 1), `major = 0x0B??` (11 en el byte alto) y `minor = 123`.
- **Fidelidad al diseño:** no añadir clases ni funcionalidades que no aparezcan en este diseño.
