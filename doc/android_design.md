# Diseño del componente: android

**Autora del diseño:** Pelin Kaynak · **Sprint:** 0 · **Implementación:** `src/android/` (paquete `com.pelinkaynak.biometria`)

---

## 1. Diseño del componente

App Android (GUI del teléfono) que escucha las tramas iBeacon de nuestra placa (`arduino_design.md`) y extrae la medición. Para guardarla usa **solo** la lógica del negocio del cliente (`client_business_logic_design.md`); este componente no contiene código de comunicación HTTP.

```text
Escáner BLE --> MainActivity.procesarTramaDetectada() --> LogicaFakeTelefono.guardarMedicion()
```

Las clases `MainActivity`, `TramaIBeacon` y `Utilidades` son **código proporcionado** (ingeniería inversa). En `MainActivity` se **añaden** los elementos marcados con `(NUEVO)` para conectar el escaneo con la lógica del cliente. La clase `MedicionIBeacon` es **nueva**: lee la medición de los bytes de la trama.

### Tipos

```text
TipoMedicion = { CO2 = 11, TEMPERATURA = 12, RUIDO = 13 }

vacío = ausencia de valor (en Java, null)

A | B = un valor de tipo A o un valor de tipo B

Tipos externos (opacos, clases de Android): BluetoothLeScanner, ScanCallback, ScanResult,
    Bundle, View, TextView, Handler, Runnable
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
                                    | ID_FABRICANTE_APPLE: N                     (NUEVO)
                                    | laLogica: LogicaFakeTelefono               (NUEVO)
                                    | ultimoContadorPorSensor: [ (idSensor: N, contador: N) ]  (NUEVO)
                                    | textoUltimaMedicion: TextView              (NUEVO)
                                    | textoEstado: TextView                      (NUEVO)
                                    | esperandoPermisos: B                       (NUEVO)
                                    | medicionesGuardadas: N                     (NUEVO)
                                    | ultimoResultado: Text                      (NUEVO)
                                    | INTERVALO_REINICIO_ESCANEO: N  = 10000 ms  (NUEVO)
                                    | elTemporizador: Handler                    (NUEVO)
                                    | reinicioDelEscaneo: Runnable               (NUEVO)
                                    |
                                    |
                                    |    buscarTodosLosDispositivosBTLE() -->
                                    |
                                    |
                                    |    resultado: ScanResult --> mostrarInformacionDispositivoBTLE() -->
                                    |
                                    |
                                    |    prefijoUUID: Text --> buscarEsteDispositivoBTLE() -->
                                    |
                                    |
                                    |    detenerBusquedaDispositivosBTLE() -->
                                    |
                                    |
                                    |    inicializarBlueTooth() -->
                                    |
                                    |
                                    |    resultado: ScanResult --> procesarTramaDetectada() -->        (NUEVO)
                                    |
                                    |
                                    |    B <-- tienePermisosBluetooth() <--                            (NUEVO)
                                    |
                                    |
                                    |    texto: Text --> mostrarEstado() -->                           (NUEVO)
                                    |
                                    |
                                    |    mostrarEstadoBusqueda() -->                                   (NUEVO)
                                    |
                                    |
                                    |    reiniciarEscaneo() -->                                        (NUEVO)
                                    |
                                    |
                                    |    idSensor: N, tipoMedicion: N, valorMedicion: Z,
                                    |      contador: N --> mostrarMedicion() -->                  (NUEVO)
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

### Clase MedicionIBeacon (NUEVA)

```text
                                    --------- MedicionIBeacon -----------------------------------
                                    |
                                    | idSensor: N
                                    | tipoMedicion: N
                                    | contador: N
                                    | valorMedicion: Z
                                    | LONGITUD_MINIMA: N
                                    | LONGITUD_PREFIJO_UUID: N
                                    |
                                    |
                                    |    idSensor: N, tipoMedicion: N, contador: N,
                                    |    valorMedicion: Z --> MedicionIBeacon() -->
                                    |
                                    |
  datos: [ Z ]                  --> desdeDatosFabricante() --x
  prefijoUUID: Text                 |
  MedicionIBeacon | vacío       <-- |
                                    |
                                    |
                     N          <-- getIdSensor() <--
                                    |
                                    |
                     N          <-- getTipoMedicion() <--
                                    |
                                    |
                     N          <-- getContador() <--
                                    |
                                    |
                     Z          <-- getValorMedicion() <--
                                    |
                                    -------------------------------------------------------------
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

- **Nombres en mayúsculas:** `ETIQUETA_LOG`, `CODIGO_PETICION_PERMISOS`, `PREFIJO_UUID_NUESTROS_BEACONS`, `ID_FABRICANTE_APPLE`, `INTERVALO_REINICIO_ESCANEO`, `LONGITUD_MINIMA` y `LONGITUD_PREFIJO_UUID` son constantes (`static final`); en Java las constantes se escriben en mayúsculas.
- **Atributos que guardan un callback:** `callbackDelEscaneo` (`ScanCallback`) y `reinicioDelEscaneo` (`Runnable`) guardan objetos que Android llama más tarde. La notación omite los callbacks como **parámetros**, pero estos son **atributos** de la clase que existen en el código, así que se dibujan dentro de la caja.
- **procesarTramaDetectada()** se llama desde el callback del escaneo de `buscarEsteDispositivoBTLE()`:
  1. toma los datos del fabricante Apple del anuncio (`getScanRecord().getManufacturerSpecificData(ID_FABRICANTE_APPLE)`, `ID_FABRICANTE_APPLE` = 0x004C); si no existen o no empiezan por `02 15` (tipo y longitud iBeacon), no es una trama iBeacon y no hace nada. Si lo es, separa `uuid` (bytes 2–17), `major` (18–19) y `minor` (20–21). No se usa `TramaIBeacon`, que supone que la trama empieza en una posición fija del anuncio: en las pruebas, con algunos teléfonos esa posición no coincidía y no se reconocía ninguna trama;
  2. si los 14 primeros bytes del `uuid` no son `PREFIJO_UUID_NUESTROS_BEACONS` (`"EPSG-GTI-PELIN"`), no es una placa de este proyecto (por ejemplo, las placas de otros grupos emiten `"EPSG-GTI-PROY-3A"`) y no hace nada; si lo son, `idSensor` = los 2 últimos bytes del UUID (byte alto, byte bajo, 0..65535);
  3. deshace la codificación del Arduino: `tipoMedicion = major[0] & 0xFF` (byte alto; 11 = CO2), `contador = major[1] & 0xFF` (byte bajo, 0..255) y `valorMedicion = Utilidades.bytesToInt( minor )` (entero con signo, así −12 se lee bien);
  4. si el `contador` es igual al último contador enviado de ese mismo sensor (`ultimoContadorPorSensor`), no hace nada (el Arduino repite la misma trama muchas veces y así no se guardan duplicados);
  5. si no, guarda el contador de ese sensor en `ultimoContadorPorSensor` y llama a `laLogica.guardarMedicion(idSensor, tipoMedicion, valorMedicion)`; antes, muestra la medición en pantalla con `mostrarMedicion()`; el resultado (`guardada()` o `error(mensaje)`) se escribe en el Log y en la pantalla con `mostrarEstado()`.
- **ultimoContadorPorSensor** empieza vacío, para que la primera trama de cada sensor siempre se envíe. Se guarda un contador por sensor porque, cuando haya varias placas, sus contadores van por separado.
- **UUID del beacon:** solo se usa en el teléfono para reconocer las placas de este proyecto y sacar el `IdSensor`; ya no se envía al servidor.
- **Limitación conocida:** `ultimoContadorPorSensor` dura mientras la app está abierta. Si una placa se reinicia (su contador vuelve a 1) y el último contador guardado de ese sensor era justo 1, esa primera trama se toma por repetida; la siguiente (contador 2) ya se envía.
- **Pantalla (`(NUEVO)`):** debajo de los botones hay una tarjeta con dos textos: `textoUltimaMedicion` (la última medición recibida de la placa, p. ej. `IdSensor 1 · CO2 · 123 (contador 5)`) y `textoEstado` (qué está pasando: *Listo*, *Buscando nuestra placa...*, *Enviando la medición al servidor...*, *Guardada en el servidor ✓*, *No se pudo guardar en el servidor: …*, *Faltan permisos…*, *Bluetooth apagado…*, *Error al empezar a buscar (código N)*, *Búsqueda detenida*). Así se ve el recorrido placa → teléfono → servidor sin Logcat. La pantalla está dentro de un `ScrollView` para que un texto largo no quede cortado, y `mostrarEstadoBusqueda()` pone el resultado del último envío en la primera línea. `mostrarEstado()` y `mostrarMedicion()` usan `runOnUiThread()` porque los callbacks del escaneo y de la petición REST pueden llegar desde otro hilo.
- **MedicionIBeacon (`(NUEVO)`):** `desdeDatosFabricante(datos, prefijoUUID)` hace los pasos 1–3 de `procesarTramaDetectada()`: comprueba que los datos empiezan por `02 15` y que el UUID empieza por `prefijoUUID`, y lee `idSensor` (2 últimos bytes del UUID), `tipoMedicion` (byte alto de major), `contador` (byte bajo de major) y `valorMedicion` (minor, con signo). Si los datos no son de una placa del proyecto devuelve **vacío** (`null`). Es estático y no depende de Android, así se puede probar con JUnit. El constructor es privado: solo `desdeDatosFabricante()` crea objetos. `procesarTramaDetectada()` lo usa y después hace el paso 4 (contador repetido) y el envío.
- **Tipos externos (opacos):** `BluetoothLeScanner`, `ScanCallback`, `ScanResult`, `Bundle`, `View`, `TextView`, `Handler` y `Runnable` son clases de Android que se usan tal cual; `UUID` (en `Utilidades`) es `java.util.UUID`, equivalente a `[ N ]_16`.
- **Visibilidad en Java:** `onCreate()` está en el borde de la caja porque lo llama Android desde fuera de la clase, aunque en Java se declara `protected` (sobrescribe `AppCompatActivity`).
- **Sin comunicación en la GUI:** `MainActivity` no contiene código HTTP; para guardar usa solo `LogicaFakeTelefono` (`client_business_logic_design.md`).
- **Permisos (Android 12+):** el código original salía de `inicializarBlueTooth()` sin pedir los permisos `BLUETOOTH_SCAN`/`BLUETOOTH_CONNECT`, así que el escáner nunca se creaba. Ahora, si `tienePermisosBluetooth()` es falso, se piden y `onRequestPermissionsResult()` vuelve a llamar a `inicializarBlueTooth()` cuando se conceden. En versiones anteriores a Android 12 los permisos `BLUETOOTH_SCAN`/`BLUETOOTH_CONNECT` no existen (siempre salen "no concedidos"), así que `tienePermisosBluetooth()` comprueba `ACCESS_FINE_LOCATION`, que es lo que Android necesita para escanear BLE. Por la misma razón, los métodos que escanean ya no usan `checkSelfPermission(BLUETOOTH_SCAN)` (que en Android 11 hacía salir sin escanear) sino `tienePermisosBluetooth()`. `esperandoPermisos` evita pedir los permisos en bucle: `onRequestPermissionsResult()` solo vuelve a llamar a `inicializarBlueTooth()` si los pidió ella. En Android 11 o anterior, además, la **Ubicación** del teléfono debe estar activada para recibir tramas BLE.
- **Escaneo y filtro (`buscarEsteDispositivoBTLE`, `(NUEVO)`):** el código original llamaba dos veces a `startScan()` con el mismo callback (sin filtro y con filtro por nombre); Android rechaza la segunda llamada (`onScanFailed`, código 1). Se ha comentado la llamada sin filtro. Además, el filtro de Android ya no se usa: el filtro por nombre no funcionaba (la placa envía su nombre `PelinIBeacon` en la respuesta de escaneo, no en el anuncio) y el filtro por datos del fabricante (Apple `0x004C` + `02 15` + prefijo) no lo aplican igual todos los teléfonos (en las pruebas, a veces no llegaba ninguna trama aunque nRF Connect veía la placa). Ahora `startScan()` recibe una lista de filtros vacía y el filtro se hace en la app: `procesarTramaDetectada()` ignora todo lo que no tenga un UUID que empiece por `PREFIJO_UUID_NUESTROS_BEACONS`. Por eso el callback ya no llama a `mostrarInformacionDispositivoBTLE()` (llenaría el Log con todos los dispositivos) y protege `procesarTramaDetectada()` con `try/catch` (una trama de otro dispositivo con otro formato no debe cerrar la app). `mostrarEstadoBusqueda()` muestra en `textoEstado` el resultado del último envío (`ultimoResultado`: *Enviando la medición (contador N)...*, *Guardada en el servidor ✓ (K en total)* con `medicionesGuardadas`, o el error) y debajo *Buscando nuestra placa...*. **Reinicio periódico del escaneo:** en las pruebas solo llegaba la primera trama de la placa: muchos teléfonos solo avisan una vez de cada dispositivo en cada escaneo, aunque cambien sus datos. Por eso, mientras se busca, `elTemporizador` llama a `reiniciarEscaneo()` cada `INTERVALO_REINICIO_ESCANEO` (10 s, lo mismo que tarda la placa en cambiar de contador): para el escaneo y lo vuelve a empezar con el mismo callback, y así llega la trama nueva. Son 3 inicios cada 30 s, por debajo del límite de Android (5). `detenerBusquedaDispositivosBTLE()` cancela el temporizador. Si se pulsa un botón de búsqueda (cualquiera de los dos) con una búsqueda en marcha, primero se para la anterior (`stopScan`; si no, se perdería su callback y `detenerBusquedaDispositivosBTLE()` ya no podría pararla), porque Android deja de dar resultados si se acumulan escaneos o se empiezan más de 5 en 30 segundos.
- **Servicio en segundo plano:** el código `HolaMundoServicio2021` (ver `referencia/holaMundoServicio.md`) no se usa en este sprint; el escaneo se hace desde `MainActivity`. Se usará en sprints posteriores.

---

## 3. Reglas generales

- **Lenguaje:** Java 8 para Android (minSdk 28), proyecto Gradle de Android Studio (`src/android/`). El módulo `app` incluye también el código de `src/client_business_logic/` mediante `sourceSets` en `app/build.gradle`.
- **Cabecera de fichero:** cada fichero empieza con un comentario que indica nombre del fichero, descripción, autor (Pelin Kaynak en los ficheros nuevos; autor original Jordi Bataller i Mascarell + aportación de Pelin Kaynak en los proporcionados), fecha y copyright.
- **Cabeceras de funciones/métodos:** cada función o método incluye su diseño lógico en un bloque de comentarios delimitado por líneas discontinuas (`--------------------`), con la notación de la asignatura, y una línea breve en inglés `What it does:` dentro del mismo bloque.
- **Legibilidad:** el código debe ser lo más claro y autoexplicativo posible (nombres descriptivos en castellano), de forma que casi no necesite comentarios adicionales.
- **Tests automáticos:** JUnit 4 en `app/src/test/java/` (se ejecutan con `./gradlew test`, sin teléfono):
  - `MedicionIBeaconTest`: comprueba `desdeDatosFabricante()` con una trama de nuestra placa (IdSensor 1, CO2, contador 5, valor 123), un valor negativo (−12) y un contador mayor que 127, un IdSensor de 2 bytes (258), una trama de otro grupo (prefijo distinto → vacío) y datos que no son iBeacon (→ vacío).
  - `TramaIBeaconTest`: con una trama como la que emite nuestra placa (IdSensor = 1, CO2 = 123, contador 5), comprueba que se separan bien el prefijo y sus partes, el UUID (prefijo del proyecto + IdSensor), el major, el minor y el txPower.
  - `UtilidadesTest`: comprueba `bytesToString` (UUID), `bytesToInt` con signo (123 y −12), `bytesToLong`, `bytesToHexString`, `stringToBytes`, `dosLongToBytes`, que `stringToUUID` y `uuidToString` son inversas y que `stringToUUID` rechaza textos que no tienen 16 caracteres.
  - El envío real al servidor se comprueba en la prueba de funcionamiento de extremo a extremo (criterio de aceptación en el `README.md`).
- **Fidelidad al diseño:** no modificar la lógica del código proporcionado salvo lo marcado como `(NUEVO)`, y no añadir clases ni métodos que no aparezcan en este diseño.
