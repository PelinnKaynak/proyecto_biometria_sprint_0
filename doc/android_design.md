# Diseño del componente: android

**Autora del diseño:** Pelin Kaynak · **Sprint:** 0 · **Implementación:** `src/android/` (paquete `com.pelinkaynak.biometria`)

---

## 1. Diseño del componente

App Android que escucha las tramas iBeacon de nuestra placa (`arduino_design.md`), extrae la medición y la envía al servidor REST (`api_design.md`).

```text
Escáner BLE --> MainActivity.procesarTramaDetectada() --> LogicaFakeTelefono.guardarMedicion() --POST /guardarMedicion--> API REST
```

Las clases `MainActivity`, `TramaIBeacon` y `Utilidades` son **código proporcionado** (ingeniería inversa). `PeticionarioREST` también es código proporcionado y se copia al proyecto. La única clase **nueva** es `LogicaFakeTelefono` (documento *Diseños y prompts para generar nuevo código*, apartado 5); en `MainActivity` se **añaden** los elementos marcados con `(NUEVO)` para conectar el escaneo con la lógica fake.

### Tipos

```text
RespuestaREST = ( codigo: Z, cuerpo: Text )
```

### Clase MainActivity (proporcionada + ampliada)

```text
                                    --------- MainActivity ------------------------------------
                                    |
                                    | ETIQUETA_LOG: Text
                                    | CODIGO_PETICION_PERMISOS: Z
                                    | elEscaner: BluetoothLeScanner
                                    | UUID_NUESTRO_BEACON: Text                  (NUEVO)
                                    | laLogica: LogicaFakeTelefono               (NUEVO)
                                    | ultimoContadorEnviado: Z                   (NUEVO)
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

### Clase PeticionarioREST (proporcionada)

```text
                                    --------- PeticionarioREST -----------------------
                                    |
                                    | elMetodo: Text
                                    | urlDestino: Text
                                    | elCuerpo: Text
                                    | codigoRespuesta: Z
                                    | cuerpoRespuesta: Text
                                    |
                                    |    B <-- doInBackground() -->
                                    |    comoFue: B --> onPostExecute() -->
                                    |
                                    |
                                    PeticionarioREST() -->
                                    |
                                    |
  metodo: Text                  --> hacerPeticionREST() -->
  urlDestino: Text                  |
  cuerpo: Text                      |
  RespuestaREST                 <--  |
                                    |
                                    -------------------------------------------------
```

### Clase LogicaFakeTelefono (NUEVA)

```text
                                    --------- LogicaFakeTelefono ---------------------
                                    |
                                    | URL_SERVIDOR: Text
                                    |
                                    |    UUID: Text, TipoMedicion: N, ValorMedicion: Z --> crearJSON() --> Text
                                    |    codigo: Z --> esExito() --> T/F
                                    |
                                    |
  JSON( UUID: Text,             --> guardarMedicion() -->
        TipoMedicion: N,            |
        ValorMedicion: Z )          |
                                    |
                                    -------------------------------------------------
```

---

## 2. Aclaraciones del diseño

- **procesarTramaDetectada()** se llama desde el callback del escaneo de `buscarEsteDispositivoBTLE()`:
  1. crea `TramaIBeacon` con los bytes del `ScanResult`;
  2. `UUID = Utilidades.bytesToString( trama.getUUID() )`; si no es `UUID_NUESTRO_BEACON` (`"EPSG-GTI-PROY-3A"`), no hace nada;
  3. deshace la codificación del Arduino: `TipoMedicion = major[0]` (byte alto; 11 = CO2), `contador = major[1] & 0xFF` (byte bajo, 0..255) y `ValorMedicion = Utilidades.bytesToInt( minor )` (entero con signo, así −12 se lee bien);
  4. si el `contador` es igual a `ultimoContadorEnviado`, no hace nada (el Arduino repite la misma trama muchas veces y así no se guardan duplicados);
  5. si no, llama a `laLogica.guardarMedicion(UUID, TipoMedicion, ValorMedicion)` y actualiza `ultimoContadorEnviado`.
- **ultimoContadorEnviado** empieza en −1, para que la primera trama siempre se envíe.
- **LogicaFakeTelefono.guardarMedicion()** recibe `UUID` (texto), `TipoMedicion` (natural) y `ValorMedicion` (entero), crea el JSON `{"UUID": ..., "TipoMedicion": ..., "ValorMedicion": ...}` y hace `POST /guardarMedicion` al servidor REST con `PeticionarioREST`. **No** incluye el `Timestamp` (se genera en el backend). No accede a la base de datos ni a la lógica del negocio.
  - Controla la respuesta del servidor: `esExito(codigo)` es T solo si `codigo == 201`; cualquier otro código (400, 404, 500, o −1 si no hay conexión) es error.
  - El resultado (T/F) se entrega mediante un callback (interfaz `RespuestaGuardarMedicion`), que según la notación del documento de diseño no se dibuja como parámetro. En este sprint `MainActivity` lo escribe en el Log (`ETIQUETA_LOG`).
  - `crearJSON()` y `esExito()` son privados de la clase (visibilidad de paquete en Java para poder probarlos con JUnit).
  - Cada llamada crea un `PeticionarioREST` nuevo, porque una `AsyncTask` solo se puede ejecutar una vez.
- **URL_SERVIDOR:** constante privada de `LogicaFakeTelefono`. En el emulador de Android Studio es `http://10.0.2.2:8080` (dirección especial del ordenador que ejecuta el emulador). En un teléfono real es la IPv4 del ordenador en la misma Wi‑Fi o punto de acceso, por ejemplo `http://172.20.10.2:8080`.
- **Permisos (Android 12+):** el código original salía de `inicializarBlueTooth()` sin pedir los permisos `BLUETOOTH_SCAN`/`BLUETOOTH_CONNECT`, así que el escáner nunca se creaba. Ahora, si `tienePermisosBluetooth()` es falso, se piden y `onRequestPermissionsResult()` vuelve a llamar a `inicializarBlueTooth()` cuando se conceden. En versiones anteriores a Android 12 `tienePermisosBluetooth()` devuelve `true`.
- **Tráfico HTTP sin cifrar:** desde Android 9 está bloqueado por defecto. Se añade `android:usesCleartextTraffic="true"` en `<application>` del `AndroidManifest.xml`. El permiso `INTERNET` ya existe.
- **Tests de JVM y `org.json`:** en los tests unitarios la clase `org.json.JSONObject` de Android no funciona, así que se añade `testImplementation 'org.json:json:20231013'` en `app/build.gradle`.
- **Servicio en segundo plano:** el código `HolaMundoServicio2021` (ver `doc/referencia/holaMundoServicio.md`) no se usa en este sprint; el escaneo se hace desde `MainActivity`. Se usará en sprints posteriores.

---

## 3. Reglas generales

- **Lenguaje:** Java 8 para Android (minSdk 28), proyecto Gradle de Android Studio.
- **Cabecera de fichero:** cada fichero empieza con un comentario que indica nombre del fichero, descripción, autor (Pelin Kaynak en los ficheros nuevos; autor original Jordi Bataller i Mascarell + aportación de Pelin Kaynak en los proporcionados), fecha y copyright.
- **Cabeceras de funciones/métodos:** cada función o método incluye su diseño lógico en un bloque de comentarios delimitado por líneas discontinuas (`--------------------`), con la notación de la asignatura.
- **Legibilidad:** el código debe ser lo más claro y autoexplicativo posible (nombres descriptivos en castellano), de forma que casi no necesite comentarios adicionales.
- **Tests automáticos:** JUnit 4 en `app/src/test/java/` (se ejecutan con `./gradlew test`, sin teléfono):
  - `LogicaFakeTelefonoTest`: comprueba la creación del JSON (claves `UUID`, `TipoMedicion` y `ValorMedicion`, sin `Timestamp`, valores negativos) y las respuestas de éxito (201) y error (400, 404, 500, −1) del servidor.
  - El envío real al servidor se comprueba en la prueba de funcionamiento de extremo a extremo (criterio de aceptación en el `README.md`).
- **Fidelidad al diseño:** no modificar la lógica del código proporcionado salvo lo marcado como `(NUEVO)`, y no añadir clases ni métodos que no aparezcan en este diseño.
