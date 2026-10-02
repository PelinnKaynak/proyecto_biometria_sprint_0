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

   major = tipoMedicion * 256 + contador     (byte alto: tipo, byte bajo: contador)
   minor = valorMedicion
```

### Programa principal (`HolaMundoIBeacon.ino`)

```text
Globales = ( elLED: LED, elPuerto: PuertoSerie, elPublicador: Publicador, elMedidor: Medidor )

tiempo: Z --> esperar() -->

inicializarPlaquita() -->

setup() -->              (espera el puerto serie, enciende la emisora, inicia el medidor)

lucecitas() -->          (secuencia de parpadeos del LED)

loop() -->               (cont := cont + 1; lucecitas(); valor := medirCO2(); publicarCO2(valor, cont, 10000))
```

### Clase LED

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
                              | beaconUUID: UUID_16  = "EPSG-GTI-PROY-3A"
                              |
                              |
  laEmisora: EmisoraBLE   <-- laEmisora        (atributo público: "GTI3A-2025", 0x004c, 4)
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
                                   encenderEmisora() -->
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
                                   activar() -->
                                   |
                                   ---------------------------------------------------------
```

Funciones auxiliares libres (en `ServicioEnEmisora.h`):

```text
texto: Text, tamanyo: N --> stringAUint8AlReves() --> [ N ]

p: [ T ], n: N --> alReves() --> [ T ]          (invierte el array)
```

Además, `ServicioEnEmisora` tiene la conversión `operator BLEService&() --> BLEService` y `EmisoraBLE` tiene una segunda versión de `encenderEmisora()` que también instala los callbacks de conexión.

---

## 2. Aclaraciones del diseño

- **Código proporcionado:** este componente es código del profesor (carpeta `Arduino-NoCambia`). Su lógica **no se modifica**; solo se añaden cabeceras de fichero y de función con el diseño lógico (rúbrica, "Buenas prácticas").
- **Medición ficticia:** según la rúbrica del Sprint 0 ("se introduce una medida ficticia en el código de la placa Arduino"), en este sprint no se lee el sensor de ozono. `Medidor::medirCO2()` devuelve el valor fijo **123** (y `medirTemperatura()` devuelve −12). Es el valor que debe aparecer en la página web en la prueba de funcionamiento. Para la defensa se puede cambiar a otro número reconocible.
- **Codificación de la trama:** `publicarCO2()` pone en `major` el tipo de medición en el byte alto y el contador en el byte bajo (`major = (CO2 << 8) + contador`), y el valor en `minor`. El teléfono tiene que deshacer esta codificación (ver `android_design.md`).
- **Tiempo de emisión (cambio respecto al código original):** en `loop()` se llama a `publicarCO2(valor, cont, 10000)` en lugar de `1000`. Con 1 segundo de anuncio cada ~5 segundos (el resto lo ocupa `lucecitas()`) la placa no aparecía en *nRF Connect*; con 10 segundos se detecta sin problemas. Es el único cambio de lógica en el código proporcionado.
- **UUID del beacon:** `EPSG-GTI-PROY-3A` (16 caracteres ASCII). El teléfono lo usa para reconocer *nuestro* beacon entre los demás.
- **Callbacks:** los métodos `instalarCallbackConexionEstablecida()`, `instalarCallbackConexionTerminada()` e `instalarCallbackCaracteristicaEscrita()` reciben funciones de callback. Según la notación de la asignatura, los callbacks se omiten en el diseño lógico, por eso no aparecen como parámetros.
- **Conexión con otros componentes:** la única salida del componente es la trama iBeacon, que recibe el componente `android`.

---

## 3. Reglas generales

- **Lenguaje:** C++ para Arduino (núcleo *Adafruit nRF52*, biblioteca Bluefruit).
- **Cabecera de fichero:** cada fichero empieza con un comentario que indica nombre del fichero, descripción, autor original (Jordi Bataller i Mascarell), aportación de Pelin Kaynak (diseño y documentación), fecha y copyright.
- **Cabeceras de funciones/métodos:** cada función o método incluye su diseño lógico en un bloque de comentarios delimitado por líneas discontinuas (`--------------------`), con la notación de la asignatura.
- **Legibilidad:** el código debe ser lo más claro y autoexplicativo posible, de forma que casi no necesite comentarios adicionales.
- **Tests (criterio de aceptación reproducible):** en la placa no hay framework de tests automáticos. La prueba es:
  1. Subir el programa y abrir el Monitor Serie (115200 baudios): aparece `---- loop(): empieza N` en cada iteración.
  2. Con la app *nRF Connect* en el teléfono se ve el dispositivo `GTI3A-2025` con UUID `EPSG-GTI-PROY-3A`, `major = 0x0B??` (11 en el byte alto) y `minor = 123`.
- **Fidelidad al diseño:** no añadir clases ni funcionalidades que no aparezcan en este diseño.
