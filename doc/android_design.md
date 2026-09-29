# Diseño del componente: android

**Autora del diseño:** Pelin Kaynak · **Sprint:** 0 · **Implementación:** `src/android/` (paquete `org.jordi.prueba2025`)

---

## 1. Diseño del componente

App Android que escucha las tramas iBeacon de nuestra placa (`arduino_design.md`), extrae la medición y la envía al servidor REST (`api_design.md`).

```text
Escáner BLE --> MainActivity.procesarTramaDetectada() --> ExtractorMedicion.extraerMedicion()
                                                     --> LogicaFakeTelefono.guardarMedicion() --POST--> API REST
```

Las clases `MainActivity`, `TramaIBeacon` y `Utilidades` son **código proporcionado** (ingeniería inversa). `PeticionarioREST` también es código proporcionado y se copia al proyecto. Las clases **nuevas** son `MedicionDetectada`, `ExtractorMedicion` y `LogicaFakeTelefono`; en `MainActivity` se **añaden** los elementos marcados con `(NUEVO)`.

### Tipos

```text
MedicionDetectada = ( uuid: Text, tipoMedicion: N, contador: N, valorMedicion: Z )

RespuestaREST = ( codigo: Z, cuerpo: Text )
```

### Clase MainActivity (proporcionada + ampliada)

```text
                                    --------- MainActivity ------------------------------------
                                    |
                                    | ETIQUETA_LOG: Text
                                    | CODIGO_PETICION_PERMISOS: Z
                                    | elEscaner: BluetoothLeScanner
                                    | URL_SERVIDOR: Text                         (NUEVO)
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

### Clase MedicionDetectada (NUEVA)

```text
                                    --------- MedicionDetectada ----------------------
                                    |
                                    | uuid: Text
                                    | tipoMedicion: N
                                    | contador: N
                                    | valorMedicion: Z
                                    |
                                    |
  uuid: Text, tipoMedicion: N   --> MedicionDetectada() -->
  contador: N, valorMedicion: Z     |
                                    |
                                    |
  Text                          <-- getUuid() <--
                                    |
                                    |
  N                             <-- getTipoMedicion() <--
                                    |
                                    |
  N                             <-- getContador() <--
                                    |
                                    |
  Z                             <-- getValorMedicion() <--
                                    |
                                    -------------------------------------------------
```

### Clase ExtractorMedicion (NUEVA)

```text
                                    --------- ExtractorMedicion ----------------------
                                    |
                                    |
  trama: TramaIBeacon           --> extraerMedicion() --x
  MedicionDetectada             <-- |
                                    |
                                    -------------------------------------------------
```

### Clase LogicaFakeTelefono (NUEVA)

```text
                                    --------- LogicaFakeTelefono ---------------------
                                    |
                                    | urlServidor: Text
                                    |
                                    |
  urlServidor: Text             --> LogicaFakeTelefono() -->
                                    |
                                    |
  uuid: Text                    --> guardarMedicion() <--
  tipoMedicion: N                   |
  valorMedicion: Z                  |
  B                             <-- |
                                    |
                                    |
  uuid: Text                    --> crearCuerpoJSON() --x
  tipoMedicion: N                   |
  valorMedicion: Z                  |
  Text                          <-- |
                                    |
                                    |
  codigo: Z                     --> esRespuestaCorrecta() --x
  B                             <-- |
                                    |
                                    -------------------------------------------------
```

---

## 2. Aclaraciones del diseño

- **extraerMedicion()** deshace la codificación del Arduino:
  - `uuid = Utilidades.bytesToString( trama.getUUID() )` → `"EPSG-GTI-PROY-3A"`.
  - `tipoMedicion = major[0]` (byte alto de `major`; 11 = CO2).
  - `contador = major[1] & 0xFF` (byte bajo, sin signo: 0..255).
  - `valorMedicion = Utilidades.bytesToInt( minor )` (entero **con** signo de 16 bits, así −12 se lee bien).
- **procesarTramaDetectada()** se llama desde el callback del escaneo de `buscarEsteDispositivoBTLE()`:
  1. crea `TramaIBeacon` con los bytes del `ScanResult` y llama a `extraerMedicion()`;
  2. si el `uuid` no es `UUID_NUESTRO_BEACON` (`"EPSG-GTI-PROY-3A"`), no hace nada;
  3. si el `contador` es igual a `ultimoContadorEnviado`, no hace nada (el Arduino repite la misma trama muchas veces y así no se guardan duplicados);
  4. si no, llama a `laLogica.guardarMedicion(uuid, tipoMedicion, valorMedicion)` y actualiza `ultimoContadorEnviado`.
- **ultimoContadorEnviado** empieza en −1, para que la primera trama siempre se envíe.
- **LogicaFakeTelefono** tiene el mismo nombre de método que la lógica del negocio (`guardarMedicion`), igual que la lógica fake del navegador. No accede a la base de datos; solo hace `POST {urlServidor}/guardarMedicion` usando `PeticionarioREST`.
  - `crearCuerpoJSON()` construye `{"uuid": ..., "tipoMedicion": ..., "valorMedicion": ...}` con `org.json.JSONObject`. **No** incluye `timestamp` (lo genera el servidor).
  - `esRespuestaCorrecta(codigo)` es `true` solo si `codigo == 201`.
  - El resultado (B) se entrega de forma asíncrona (callback, omitido en la notación); en este sprint se escribe en el Log (`ETIQUETA_LOG`) si fue bien o mal.
- **URL_SERVIDOR:** constante en `MainActivity`, con la IP del ordenador que ejecuta el servidor en la misma Wi‑Fi, por ejemplo `http://192.168.1.35:8080`. Hay que cambiarla antes de ejecutar.
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
  - `ExtractorMedicionTest`: con bytes de una trama construida a mano (UUID `EPSG-GTI-PROY-3A`, major `0x0B05`, minor `123`) comprueba uuid, tipo 11, contador 5 y valor 123; y con minor `0xFFF4` comprueba el valor −12.
  - `LogicaFakeTelefonoTest`: comprueba que `crearCuerpoJSON()` produce las tres claves correctas y sin `timestamp`, y que `esRespuestaCorrecta()` es `true` para 201 y `false` para 400, 404 y 500.
  - El envío real al servidor se comprueba en la prueba de funcionamiento de extremo a extremo (criterio de aceptación en el `README.md`).
- **Fidelidad al diseño:** no modificar la lógica del código proporcionado salvo lo marcado como `(NUEVO)`, y no añadir clases ni métodos que no aparezcan en este diseño.
