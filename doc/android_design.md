# Diseño del componente: android

**Autora del diseño:** Pelin Kaynak · **Sprint:** 0 · **Implementación:** `src/android/` (paquete `com.pelinkaynak.biometria`)

---

## 1. Diseño del componente

App Android (GUI del teléfono) que escucha las tramas iBeacon de nuestra placa (`arduino_design.md`) y extrae la medición. Para guardarla usa **solo** la lógica del negocio del cliente (`client_business_logic_design.md`); este componente no contiene código de comunicación HTTP.

```text
Escáner BLE --> MainActivity.procesarTramaDetectada() --> LogicaFakeTelefono.guardarMedicion()
```

Las clases `MainActivity`, `TramaIBeacon` y `Utilidades` son **código proporcionado** (ingeniería inversa). En `MainActivity` se **añaden** los elementos marcados con `(NUEVO)` para conectar el escaneo con la lógica del cliente.

### Tipos

```text
TipoMedicion = { CO2 = 11, TEMPERATURA = 12, RUIDO = 13 }
```

### Clase MainActivity (proporcionada + ampliada)

```text
                                    --------- MainActivity ------------------------------------
                                    |
                                    | ETIQUETA_LOG: Text
                                    | CODIGO_PETICION_PERMISOS: Z
                                    | elEscanner: BluetoothLeScanner
                                    | callbackDelEscaneo: ScanCallback           (callback)
                                    | PREFIJO_UUID_NUESTROS_BEACONS: Text        (NUEVO)
                                    | laLogica: LogicaFakeTelefono               (NUEVO)
                                    | ultimoContadorPorSensor: [ (idSensor: N, contador: N) ]  (NUEVO)
                                    |
                                    |    buscarTodosLosDispositivosBTLE() -->
                                    |    resultado: ScanResult --> mostrarInformacionDispositivoBTLE() -->
                                    |    dispositivoBuscado: Text --> buscarEsteDispositivoBTLE() -->
                                    |    detenerBusquedaDispositivosBTLE() -->
                                    |    inicializarBlueTooth() -->
                                    |    resultado: ScanResult --> procesarTramaDetectada() -->   (NUEVO)
                                    |    tienePermisosBluetooth() --> B                           (NUEVO)
                                    |
                                    |
  savedInstanceState: Bundle    --> onCreate() -->
                                    |
                                    |
  v: View                       --> botonBuscarDispositivosBTLEPulsado() -->
                                    |
                                    |
  v: View                       --> botonBuscarNuestroDispositivoBTLEPulsado() -->
                                    |
                                    |
  v: View                       --> botonDetenerBusquedaDispositivosBTLEPulsado() -->
                                    |
                                    |
  requestCode: Z                --> onRequestPermissionsResult() -->
  permissions: [ Text ]             |
  grantResults: [ Z ]               |
                                    |
                                    ---------------------------------------------------------
```

### Clase TramaIBeacon (proporcionada)

```text
                          --------- TramaIBeacon ----------------------------
                          |
                          | prefijo: [ Z ]_9      uuid: [ Z ]_16
                          | major: [ Z ]_2        minor: [ Z ]_2
                          | txPower: Z            losBytes: [ Z ]
                          | advFlags: [ Z ]_3     advHeader: [ Z ]_2
                          | companyID: [ Z ]_2    iBeaconType: Z
                          | iBeaconLength: Z
                          |
                          |
  bytes: [ Z ]        --> TramaIBeacon() -->
                          |
                          |
  [ Z ]               <-- getPrefijo() <--
                          |
                          |
  [ Z ]               <-- getUUID() <--
                          |
                          |
  [ Z ]               <-- getMajor() <--
                          |
                          |
  [ Z ]               <-- getMinor() <--
                          |
                          |
  Z                   <-- getTxPower() <--
                          |
                          |
  [ Z ]               <-- getLosBytes() <--
                          |
                          |
  [ Z ]               <-- getAdvFlags() <--
                          |
                          |
  [ Z ]               <-- getAdvHeader() <--
                          |
                          |
  [ Z ]               <-- getCompanyID() <--
                          |
                          |
  Z                   <-- getiBeaconType() <--
                          |
                          |
  Z                   <-- getiBeaconLength() <--
                          |
                          ----------------------------------------------------
```

### Clase Utilidades (proporcionada, todos los métodos estáticos)

```text
                                          --------- Utilidades ---------------------
                                          |
  texto: Text                         --> stringToBytes() --x
  [ Z ]                               <-- |
                                          |
                                          |
  uuid: Text                          --> stringToUUID() --x
  UUID                                <-- |
                                          |
                                          |
  uuid: UUID                          --> uuidToString() --x
  Text                                <-- |
                                          |
                                          |
  uuid: UUID                          --> uuidToHexString() --x
  Text                                <-- |
                                          |
                                          |
  bytes: [ Z ]                        --> bytesToString() --x
  Text                                <-- |
                                          |
                                          |
  masSignificativos: Z                --> dosLongToBytes() --x
  menosSignificativos: Z                  |
  [ Z ]                               <-- |
                                          |
                                          |
  bytes: [ Z ]                        --> bytesToInt() --x
  Z                                   <-- |
                                          |
                                          |
  bytes: [ Z ]                        --> bytesToLong() --x
  Z                                   <-- |
                                          |
                                          |
  bytes: [ Z ]                        --> bytesToIntOK() --x
  Z                                   <-- |
                                          |
                                          |
  bytes: [ Z ]                        --> bytesToHexString() --x
  Text                                <-- |
                                          |
                                          ------------------------------------------
```

---

## 2. Aclaraciones del diseño

- **procesarTramaDetectada()** se llama desde el callback del escaneo de `buscarEsteDispositivoBTLE()`:
  1. crea `TramaIBeacon` con los bytes del `ScanResult`;
  2. si los 14 primeros bytes del UUID (`trama.getUUID()`) no son `PREFIJO_UUID_NUESTROS_BEACONS` (`"EPSG-GTI-PELIN"`), no es una placa de este proyecto (por ejemplo, las placas de otros grupos emiten `"EPSG-GTI-PROY-3A"`) y no hace nada; si lo son, `idSensor` = los 2 últimos bytes del UUID (byte alto, byte bajo, 0..65535);
  3. deshace la codificación del Arduino: `tipoMedicion = major[0] & 0xFF` (byte alto; 11 = CO2), `contador = major[1] & 0xFF` (byte bajo, 0..255) y `valorMedicion = Utilidades.bytesToInt( minor )` (entero con signo, así −12 se lee bien);
  4. si el `contador` es igual al último contador enviado de ese mismo sensor (`ultimoContadorPorSensor`), no hace nada (el Arduino repite la misma trama muchas veces y así no se guardan duplicados);
  5. si no, guarda el contador de ese sensor en `ultimoContadorPorSensor` y llama a `laLogica.guardarMedicion(idSensor, tipoMedicion, valorMedicion)`; el resultado (`guardada()` o `error(mensaje)`) se escribe en el Log.
- **ultimoContadorPorSensor** empieza vacío, para que la primera trama de cada sensor siempre se envíe. Se guarda un contador por sensor porque, cuando haya varias placas, sus contadores van por separado.
- **UUID del beacon:** solo se usa en el teléfono para reconocer las placas de este proyecto y sacar el `IdSensor`; ya no se envía al servidor.
- **Limitación conocida:** `ultimoContadorPorSensor` dura mientras la app está abierta. Si una placa se reinicia (su contador vuelve a 1) y el último contador guardado de ese sensor era justo 1, esa primera trama se toma por repetida; la siguiente (contador 2) ya se envía.
- **Tipos externos (opacos):** `BluetoothLeScanner`, `ScanCallback`, `ScanResult`, `Bundle` y `View` son clases de Android que se usan tal cual; `UUID` (en `Utilidades`) es `java.util.UUID`, equivalente a `[ N ]_16`.
- **Visibilidad en Java:** `onCreate()` está en el borde de la caja porque lo llama Android desde fuera de la clase, aunque en Java se declara `protected` (sobrescribe `AppCompatActivity`).
- **Sin comunicación en la GUI:** `MainActivity` no contiene código HTTP; para guardar usa solo `LogicaFakeTelefono` (`client_business_logic_design.md`).
- **Permisos (Android 12+):** el código original salía de `inicializarBlueTooth()` sin pedir los permisos `BLUETOOTH_SCAN`/`BLUETOOTH_CONNECT`, así que el escáner nunca se creaba. Ahora, si `tienePermisosBluetooth()` es falso, se piden y `onRequestPermissionsResult()` vuelve a llamar a `inicializarBlueTooth()` cuando se conceden. En versiones anteriores a Android 12 `tienePermisosBluetooth()` devuelve `true`.
- **Escaneo con filtro (`buscarEsteDispositivoBTLE`):** el código original llamaba dos veces a `startScan()` con el mismo callback, primero sin filtro y después con el filtro por nombre. Android rechaza la segunda llamada (`onScanFailed`, código 1), así que el filtro no se aplicaba. Se ha comentado la llamada sin filtro `(NUEVO)`; ahora solo se escanea el dispositivo "PelinIBeacon". El nombre de la placa se cambió de "GTI3A-2025" (el mismo en las placas de todos los grupos) a "PelinIBeacon" `(NUEVO)` para reconocerla fácilmente en *nRF Connect*.
- **Servicio en segundo plano:** el código `HolaMundoServicio2021` (ver `referencia/holaMundoServicio.md`) no se usa en este sprint; el escaneo se hace desde `MainActivity`. Se usará en sprints posteriores.

---

## 3. Reglas generales

- **Lenguaje:** Java 8 para Android (minSdk 28), proyecto Gradle de Android Studio (`src/android/`). El módulo `app` incluye también el código de `src/client_business_logic/` mediante `sourceSets` en `app/build.gradle`.
- **Cabecera de fichero:** cada fichero empieza con un comentario que indica nombre del fichero, descripción, autor (Pelin Kaynak en los ficheros nuevos; autor original Jordi Bataller i Mascarell + aportación de Pelin Kaynak en los proporcionados), fecha y copyright.
- **Cabeceras de funciones/métodos:** cada función o método incluye su diseño lógico en un bloque de comentarios delimitado por líneas discontinuas (`--------------------`), con la notación de la asignatura, y una línea breve en inglés `What it does:` dentro del mismo bloque.
- **Legibilidad:** el código debe ser lo más claro y autoexplicativo posible (nombres descriptivos en castellano), de forma que casi no necesite comentarios adicionales.
- **Tests automáticos:** JUnit 4 en `app/src/test/java/` (se ejecutan con `./gradlew test`, sin teléfono):
  - `TramaIBeaconTest`: con una trama como la que emite nuestra placa (IdSensor = 1, CO2 = 123, contador 5), comprueba que se separan bien el prefijo y sus partes, el UUID (prefijo del proyecto + IdSensor), el major, el minor y el txPower.
  - `UtilidadesTest`: comprueba `bytesToString` (UUID), `bytesToInt` con signo (123 y −12), `bytesToLong`, `bytesToHexString`, `stringToBytes`, `dosLongToBytes`, que `stringToUUID` y `uuidToString` son inversas y que `stringToUUID` rechaza textos que no tienen 16 caracteres.
  - El envío real al servidor se comprueba en la prueba de funcionamiento de extremo a extremo (criterio de aceptación en el `README.md`).
- **Fidelidad al diseño:** no modificar la lógica del código proporcionado salvo lo marcado como `(NUEVO)`, y no añadir clases ni métodos que no aparezcan en este diseño.
