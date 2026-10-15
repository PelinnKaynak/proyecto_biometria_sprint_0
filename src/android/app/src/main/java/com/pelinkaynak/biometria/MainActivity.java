// ---------------------------------------------------------------------------
// Fichero:     MainActivity.java
// Descripción: Actividad principal: escanea los beacons BLE y envía las mediciones de nuestra placa al servidor REST mediante LogicaFakeTelefono.
//              Diseño en doc/android_design.md.
// Autor:       Jordi Bataller i Mascarell (código original proporcionado)
// Aportación:  Pelin Kaynak (30/09/2026) - procesarTramaDetectada(): extrae la
//              medición del beacon y la envía al servidor con LogicaFakeTelefono;
//              petición de permisos Bluetooth en Android 12+; cabeceras de
//              fichero y de función con el diseño lógico
// Copyright:   (c) Jordi Bataller i Mascarell - EPSG-UPV
// ---------------------------------------------------------------------------
package com.pelinkaynak.biometria;

import android.annotation.SuppressLint;
import android.bluetooth.le.ScanSettings;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.content.ContextCompat;
import android.support.v4.app.ActivityCompat;

import android.os.Bundle;
import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


// MainActivity class: the main screen. It scans Bluetooth, reads the packets of our board
// and sends each new measurement to the server with LogicaFakeTelefono.
public class MainActivity extends AppCompatActivity {


    // --------------------------------------------------------------
    // --------------------------------------------------------------
    private static final String ETIQUETA_LOG = ">>>>";

    private static final int CODIGO_PETICION_PERMISOS = 11223344;

    // (NUEVO) los 14 primeros bytes del UUID de las placas de este proyecto
    // (propios, para no confundirlas con las de otros grupos, que emiten
    // "EPSG-GTI-PROY-3A"); los 2 últimos bytes son el número del sensor (IdSensor)
    private static final String PREFIJO_UUID_NUESTROS_BEACONS = "EPSG-GTI-PELIN";

    // (NUEVO) código del fabricante (Apple) bajo el que va la trama iBeacon en el anuncio
    private static final int ID_FABRICANTE_APPLE = 0x004C;

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    private BluetoothLeScanner elEscanner;

    private ScanCallback callbackDelEscaneo = null;

    // (NUEVO)
    private LogicaFakeTelefono laLogica;

    // (NUEVO) último contador enviado de cada sensor (IdSensor -> contador):
    // evita enviar varias veces la misma trama repetida
    private final Map<Integer, Integer> ultimoContadorPorSensor = new HashMap<>();

    // (NUEVO) textos de la pantalla: última medición recibida y estado (qué está pasando)
    private TextView textoUltimaMedicion;
    private TextView textoEstado;

    // (NUEVO) true mientras se esperan los permisos pedidos al empezar (para no pedirlos en bucle)
    private boolean esperandoPermisos = false;

    // (NUEVO) anuncios BLE recibidos desde que se pulsó el botón (para ver en pantalla si el escaneo funciona)
    private int anunciosRecibidos = 0;
    // (NUEVO) de esos anuncios, cuántos son tramas iBeacon (de cualquier placa)
    private int iBeaconsRecibidos = 0;
    // (NUEVO) mediciones guardadas en el servidor y resultado del último envío (se ven en textoEstado)
    private int medicionesGuardadas = 0;
    private String ultimoResultado = "";
    // (NUEVO) tramas de nuestras placas recibidas (aunque sean repetidas) y principio del UUID del último iBeacon recibido
    private int tramasNuestras = 0;
    private String ultimoIBeacon = "";

    // (NUEVO) cada INTERVALO_REINICIO_ESCANEO ms se para y se vuelve a empezar el escaneo:
    // muchos teléfonos solo avisan UNA vez de cada dispositivo en cada escaneo, así que
    // sin reiniciar solo llegaba la primera trama de la placa (una sola medición)
    private static final long INTERVALO_REINICIO_ESCANEO = 10000;
    private final Handler elTemporizador = new Handler(Looper.getMainLooper());
    private final Runnable reinicioDelEscaneo = new Runnable() {
        @Override
        public void run() {
            reiniciarEscaneo();
        }
    };

    // ------------------------------------------------------------
    // buscarTodosLosDispositivosBTLE() -->
    // What it does: scans ALL the Bluetooth devices around and only writes them in the log.
    // ------------------------------------------------------------
    @SuppressLint("MissingPermission") // (NUEVO) los permisos se comprueban con tienePermisosBluetooth()
    private void buscarTodosLosDispositivosBTLE() {
        Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): empieza ");

        if (this.elEscanner == null) { // (NUEVO)
            mostrarEstado("Bluetooth no disponible: enciende el Bluetooth y vuelve a abrir la app");
            return;
        }

        Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): instalamos scan callback ");

        this.callbackDelEscaneo = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult resultado) {
                super.onScanResult(callbackType, resultado);
                Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): onScanResult() ");

                mostrarInformacionDispositivoBTLE(resultado);
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
                Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): onBatchScanResults() ");

            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): onScanFailed() ");

            }
        };

        Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): empezamos a escanear ");

        if (!tienePermisosBluetooth()) { // (NUEVO) antes: checkSelfPermission(BLUETOOTH_SCAN), que no existe antes de Android 12
            Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): vuelvo a pedir permiso para escanear por si no lo tuviea de antes");
            mostrarEstado("Faltan permisos de Bluetooth / ubicación: dalos en Ajustes > Aplicaciones");
            return;
        }
        Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): startScan ");
        this.elEscanner.startScan(this.callbackDelEscaneo);
        mostrarEstado("Buscando todos los dispositivos (los resultados solo salen en Logcat)"); // (NUEVO)

        Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): termina ");
    } // ()

    // ------------------------------------------------------------
    // resultado: ScanResult --> mostrarInformacionDispositivoBTLE() -->
    // What it does: writes in the log all the details of a found packet (name, rssi, UUID, major, minor...).
    // ------------------------------------------------------------
    @SuppressLint("MissingPermission") // (NUEVO) los permisos se comprueban con tienePermisosBluetooth()
    private void mostrarInformacionDispositivoBTLE(ScanResult resultado) {

        Log.d(ETIQUETA_LOG, " mostrarInformacionDispositivoBTLLE(): empieza ");

        BluetoothDevice bluetoothDevice = resultado.getDevice();
        byte[] bytes = resultado.getScanRecord().getBytes();
        int rssi = resultado.getRssi();

        Log.d(ETIQUETA_LOG, "                                                     ");
        Log.d(ETIQUETA_LOG, "                                                     ");
        Log.d(ETIQUETA_LOG, " ****************************************************");
        Log.d(ETIQUETA_LOG, " ****** DISPOSITIVO DETECTADO BTLE ****************** ");
        Log.d(ETIQUETA_LOG, " ****************************************************");
        if (!tienePermisosBluetooth()) { // (NUEVO) antes: checkSelfPermission(BLUETOOTH_CONNECT), que no existe antes de Android 12
            Log.d(ETIQUETA_LOG, " mostrarInformacionDispositivoBTLLE(): no tengo el permiso BLUETOOTH_CONNECT ");
            return;
        }
        Log.d(ETIQUETA_LOG, " nombre = " + bluetoothDevice.getName());
        Log.d(ETIQUETA_LOG, " toString = " + bluetoothDevice.toString());

        /*
        ParcelUuid[] puuids = bluetoothDevice.getUuids();
        if ( puuids.length >= 1 ) {
            //Log.d(ETIQUETA_LOG, " uuid = " + puuids[0].getUuid());
           // Log.d(ETIQUETA_LOG, " uuid = " + puuids[0].toString());
        }*/

        Log.d(ETIQUETA_LOG, " dirección = " + bluetoothDevice.getAddress());
        Log.d(ETIQUETA_LOG, " rssi = " + rssi);

        Log.d(ETIQUETA_LOG, " bytes = " + new String(bytes));
        Log.d(ETIQUETA_LOG, " bytes (" + bytes.length + ") = " + Utilidades.bytesToHexString(bytes));

        TramaIBeacon tib = new TramaIBeacon(bytes);

        Log.d(ETIQUETA_LOG, " ----------------------------------------------------");
        Log.d(ETIQUETA_LOG, " prefijo  = " + Utilidades.bytesToHexString(tib.getPrefijo()));
        Log.d(ETIQUETA_LOG, "          advFlags = " + Utilidades.bytesToHexString(tib.getAdvFlags()));
        Log.d(ETIQUETA_LOG, "          advHeader = " + Utilidades.bytesToHexString(tib.getAdvHeader()));
        Log.d(ETIQUETA_LOG, "          companyID = " + Utilidades.bytesToHexString(tib.getCompanyID()));
        Log.d(ETIQUETA_LOG, "          iBeacon type = " + Integer.toHexString(tib.getiBeaconType()));
        Log.d(ETIQUETA_LOG, "          iBeacon length 0x = " + Integer.toHexString(tib.getiBeaconLength()) + " ( "
                + tib.getiBeaconLength() + " ) ");
        Log.d(ETIQUETA_LOG, " uuid  = " + Utilidades.bytesToHexString(tib.getUUID()));
        Log.d(ETIQUETA_LOG, " uuid  = " + Utilidades.bytesToString(tib.getUUID()));
        Log.d(ETIQUETA_LOG, " major  = " + Utilidades.bytesToHexString(tib.getMajor()) + "( "
                + Utilidades.bytesToInt(tib.getMajor()) + " ) ");
        Log.d(ETIQUETA_LOG, " minor  = " + Utilidades.bytesToHexString(tib.getMinor()) + "( "
                + Utilidades.bytesToInt(tib.getMinor()) + " ) ");
        Log.d(ETIQUETA_LOG, " txPower  = " + Integer.toHexString(tib.getTxPower()) + " ( " + tib.getTxPower() + " )");
        Log.d(ETIQUETA_LOG, " ****************************************************");

    } // ()

    // ------------------------------------------------------------
    // prefijoUUID: Text --> buscarEsteDispositivoBTLE() -->
    //
    // (NUEVO) El escaneo se hace SIN filtro de Android y el filtro se hace en
    // la app: procesarTramaDetectada() solo acepta las tramas cuyo UUID empieza
    // por prefijoUUID. El filtro por nombre no funcionaba (el nombre va en la
    // respuesta de escaneo) y el filtro por datos del fabricante no lo aplican
    // igual todos los teléfonos (en algunos no llega ninguna trama).
    // What it does: scans all BLE packets and processes each one with procesarTramaDetectada(),
    //               which keeps only iBeacons whose UUID starts with prefijoUUID ("EPSG-GTI-PELIN").
    // ------------------------------------------------------------
    @SuppressLint("MissingPermission") // (NUEVO) los permisos se comprueban con tienePermisosBluetooth()
    private void buscarEsteDispositivoBTLE(final String prefijoUUID) {
        Log.d(ETIQUETA_LOG, " buscarEsteDispositivoBTLE(): empieza ");

        if (this.elEscanner == null) { // (NUEVO)
            mostrarEstado("Bluetooth no disponible: enciende el Bluetooth y vuelve a abrir la app");
            return;
        }

        Log.d(ETIQUETA_LOG, " buscarEsteDispositivoBTLE(): buscando " + prefijoUUID);

        // (NUEVO) si ya había una búsqueda en marcha (botón pulsado otra vez), se para
        // antes de empezar otra: si no, se acumulan escaneos y Android deja de dar resultados
        this.elTemporizador.removeCallbacks(this.reinicioDelEscaneo);
        if (this.callbackDelEscaneo != null && tienePermisosBluetooth()) {
            this.elEscanner.stopScan(this.callbackDelEscaneo);
            this.callbackDelEscaneo = null;
        }

        Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): instalamos scan callback ");


        // super.onScanResult(ScanSettings.SCAN_MODE_LOW_LATENCY, result); para ahorro de energía

        this.callbackDelEscaneo = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult resultado) {
                super.onScanResult(callbackType, resultado);
                Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): onScanResult() ");
                // (NUEVO) ya no se llama a mostrarInformacionDispositivoBTLE(): sin filtro llenaría el Log

                // (NUEVO) cuenta los anuncios y lo muestra en pantalla
                anunciosRecibidos++;
                if (anunciosRecibidos % 10 == 1) { // cada 10, para no saturar la pantalla
                    mostrarEstadoBusqueda();
                }
                try {
                    procesarTramaDetectada(resultado); // (NUEVO)
                } catch (Exception e) { // (NUEVO) una trama de otro dispositivo con otro formato no debe cerrar la app
                    Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): trama no válida: " + e);
                }
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
                Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): onBatchScanResults() ");

            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): onScanFailed() ");
                mostrarEstado("Error al empezar a buscar (código " + errorCode + ")"); // (NUEVO)

            }
        };

        // (NUEVO) sin ScanFilter: antes se filtraba por el nombre (setDeviceName)
        // y después por los datos del fabricante; ahora filtra procesarTramaDetectada()
        this.anunciosRecibidos = 0;
        this.iBeaconsRecibidos = 0;
        this.medicionesGuardadas = 0;
        this.ultimoResultado = "";
        this.tramasNuestras = 0;
        this.ultimoIBeacon = "";

        Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): empezamos a escanear buscando: " + prefijoUUID);
        //Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): empezamos a escanear buscando: " + prefijoUUID
        //      + " -> " + Utilidades.stringToUUID( prefijoUUID ) );

        if (!tienePermisosBluetooth()) { // (NUEVO) antes: checkSelfPermission(BLUETOOTH_SCAN), que no existe antes de Android 12
            mostrarEstado("Faltan permisos de Bluetooth / ubicación: dalos en Ajustes > Aplicaciones");
            return;
        }

        Log.d(ETIQUETA_LOG, " buscarEsteDispositivoBTLE(): empieztar scan");
        // (NUEVO) eliminado el startScan() sin filtro que había aquí: con el
        // mismo callback Android rechaza el segundo startScan() y el filtro
        // por nombre no se llegaba a aplicar
        // this.elEscanner.startScan(this.callbackDelEscaneo);

        ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();

        ArrayList<ScanFilter> filtros = new ArrayList<ScanFilter>(); // (NUEVO) vacía: sin filtro de Android


        // ANTES this.elEscanner.startScan( this.callbackDelEscaneo);

        // AHORA (2025)
        this.elEscanner.startScan( filtros, settings, this.callbackDelEscaneo);
        mostrarEstadoBusqueda(); // (NUEVO)
        this.elTemporizador.postDelayed(this.reinicioDelEscaneo, INTERVALO_REINICIO_ESCANEO); // (NUEVO)

        Log.d(ETIQUETA_LOG, " buscarEsteDispositivoBTLE(): termina");
    } // ()

    // --------------------------------------------------------------
    // resultado: ScanResult --> procesarTramaDetectada() -->          (NUEVO)
    //
    // Saca la trama iBeacon de los datos del fabricante Apple (0x004C) del
    // anuncio: 02 15 + UUID (16) + major (2) + minor (2) + txPower (1). Así no
    // depende de en qué posición del anuncio la pone cada teléfono (TramaIBeacon
    // supone posiciones fijas y en algunos teléfonos no coinciden). Deshace la
    // codificación del Arduino (UUID = prefijo + IdSensor,
    // major = tipo * 256 + contador, minor = valor) y, si la trama es de una placa del proyecto y trae un
    // contador nuevo para ese sensor, envía IdSensor, TipoMedicion y
    // ValorMedicion al servidor con laLogica.guardarMedicion().
    // What it does: if the packet is from one of our boards and has a new counter for that sensor,
    //               gets the sensor id (last 2 UUID bytes), type (major byte 1), counter (major byte 2)
    //               and value (minor) and sends them to the server.
    // --------------------------------------------------------------
    private void procesarTramaDetectada(ScanResult resultado) {
        if (resultado.getScanRecord() == null) {
            return;
        }
        byte[] datos = resultado.getScanRecord().getManufacturerSpecificData(ID_FABRICANTE_APPLE);
        if (datos == null || datos.length < 22 || datos[0] != 0x02 || datos[1] != 0x15) {
            return; // no es una trama iBeacon
        }
        this.iBeaconsRecibidos++;

        byte[] uuid = Arrays.copyOfRange(datos, 2, 18);   // 16 bytes
        byte[] major = Arrays.copyOfRange(datos, 18, 20); // 2 bytes
        byte[] minor = Arrays.copyOfRange(datos, 20, 22); // 2 bytes
        String prefijo = Utilidades.bytesToString(Arrays.copyOfRange(uuid, 0, 14));
        this.ultimoIBeacon = textoLegible(prefijo);
        if (!PREFIJO_UUID_NUESTROS_BEACONS.equals(prefijo)) {
            Log.d(ETIQUETA_LOG, " procesarTramaDetectada(): no es nuestro beacon, UUID = " + Utilidades.bytesToString(uuid));
            return;
        }
        this.tramasNuestras++;
        int idSensor = ((uuid[14] & 0xFF) << 8) | (uuid[15] & 0xFF); // 2 últimos bytes del UUID

        int tipoMedicion = major[0] & 0xFF;                          // byte alto de major
        int contador = major[1] & 0xFF;                              // byte bajo de major
        int valorMedicion = Utilidades.bytesToInt(minor);                 // minor

        Integer ultimoContador = this.ultimoContadorPorSensor.get(idSensor);
        if (ultimoContador != null && ultimoContador == contador) {
            return; // la misma trama repetida de este sensor: ya se envió
        }
        this.ultimoContadorPorSensor.put(idSensor, contador);

        Log.d(ETIQUETA_LOG, " procesarTramaDetectada(): envío IdSensor = " + idSensor
                + " TipoMedicion = " + tipoMedicion + " ValorMedicion = " + valorMedicion + " contador = " + contador);
        mostrarMedicion(idSensor, tipoMedicion, valorMedicion, contador);
        this.ultimoResultado = "Enviando la medición (contador " + contador + ")...";
        mostrarEstadoBusqueda();

        this.laLogica.guardarMedicion(idSensor, tipoMedicion, valorMedicion,
                new LogicaFakeTelefono.RespuestaGuardarMedicion() {
                    @Override
                    public void guardada() {
                        Log.d(ETIQUETA_LOG, " procesarTramaDetectada(): guardada en el servidor = true");
                        medicionesGuardadas++;
                        ultimoResultado = "Guardada en el servidor ✓ (" + medicionesGuardadas + " en total)";
                        mostrarEstadoBusqueda();
                    }

                    @Override
                    public void error(String mensaje) {
                        Log.d(ETIQUETA_LOG, " procesarTramaDetectada(): guardada en el servidor = false (" + mensaje + ")");
                        ultimoResultado = "No se pudo guardar en el servidor: " + mensaje;
                        mostrarEstadoBusqueda();
                    }
                });
    } // ()

    // ------------------------------------------------------------
    // detenerBusquedaDispositivosBTLE() -->
    // What it does: stops the current scan.
    // ------------------------------------------------------------
    @SuppressLint("MissingPermission") // (NUEVO) los permisos se comprueban con tienePermisosBluetooth()
    private void detenerBusquedaDispositivosBTLE() {

        Log.d(ETIQUETA_LOG, " detenerBusquedaDispositivosBTLE(): empieza");
        if (this.callbackDelEscaneo == null) {
            Log.d(ETIQUETA_LOG, " detenerBusquedaDispositivosBTLE(): termina");
            return;
        }

        if (!tienePermisosBluetooth() || this.elEscanner == null) { // (NUEVO) antes: checkSelfPermission(BLUETOOTH_SCAN)
            return;
        }

        Log.d(ETIQUETA_LOG, " detenerBusquedaDispositivosBTLE(): parando scan");
        this.elTemporizador.removeCallbacks(this.reinicioDelEscaneo); // (NUEVO)
        this.elEscanner.stopScan(this.callbackDelEscaneo);
        this.callbackDelEscaneo = null;
        mostrarEstado("Búsqueda detenida"); // (NUEVO)

        Log.d(ETIQUETA_LOG, " detenerBusquedaDispositivosBTLE(): termina");
    } // ()

    // ------------------------------------------------------------
    // v: View --> botonBuscarDispositivosBTLEPulsado() -->
    // What it does: button 'search all devices' -> buscarTodosLosDispositivosBTLE().
    // ------------------------------------------------------------
    public void botonBuscarDispositivosBTLEPulsado(View v) {
        Log.d(ETIQUETA_LOG, " boton buscar dispositivos BTLE Pulsado");
        this.buscarTodosLosDispositivosBTLE();
    } // ()

    // ------------------------------------------------------------
    // v: View --> botonBuscarNuestroDispositivoBTLEPulsado() -->
    // What it does: button 'search our device' -> scans our iBeacons (UUID starting with "EPSG-GTI-PELIN") and sends their measurements.
    // ------------------------------------------------------------
    public void botonBuscarNuestroDispositivoBTLEPulsado(View v) {
        Log.d(ETIQUETA_LOG, " boton nuestro dispositivo BTLE Pulsado");
        //this.buscarEsteDispositivoBTLE( Utilidades.stringToUUID( "EPSG-GTI-PROY-3A" ) );

        //this.buscarEsteDispositivoBTLE("EPSG-GTI-PROY-3A");
        this.buscarEsteDispositivoBTLE(PREFIJO_UUID_NUESTROS_BEACONS); // (NUEVO) antes: "GTI3A-2025"
        //this.buscarEsteDispositivoBTLE("GTI");
        //this.buscarEsteDispositivoBTLE( "fistro" );

    } // ()

    // ------------------------------------------------------------
    // v: View --> botonDetenerBusquedaDispositivosBTLEPulsado() -->
    // What it does: button 'stop' -> stops the scan.
    // ------------------------------------------------------------
    public void botonDetenerBusquedaDispositivosBTLEPulsado(View v) {
        Log.d(ETIQUETA_LOG, " boton detener busqueda dispositivos BTLE Pulsado");
        this.detenerBusquedaDispositivosBTLE();
    } // ()

    // ------------------------------------------------------------
    // inicializarBlueTooth() -->
    // What it does: asks for the Bluetooth permissions if they are missing; when it has them,
    //               turns on Bluetooth and gets the scanner.
    // ------------------------------------------------------------
    @SuppressLint("MissingPermission") // (NUEVO) los permisos se comprueban con tienePermisosBluetooth()
    private void inicializarBlueTooth() {
        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): empieza ");
        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): obtenemos adaptador BT ");

        BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): habilitamos adaptador BT ");

        // (NUEVO) sin permisos no se puede seguir: se piden y, cuando el
        // usuario los concede, onRequestPermissionsResult() vuelve a llamar
        // a inicializarBlueTooth()
        if (!tienePermisosBluetooth()) {
            Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): no tengo permisos, los pido ");
            mostrarEstado("Pidiendo permisos de Bluetooth y ubicación...");
            this.esperandoPermisos = true;
            requestPermissions(
                    new String[]{Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    CODIGO_PETICION_PERMISOS);
            return;
        }
        bta.enable();

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): habilitado =  " + bta.isEnabled() );

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): estado =  " + bta.getState() );

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): obtenemos escaner btle ");

        this.elEscanner = bta.getBluetoothLeScanner();

        if ( this.elEscanner == null ) {
            Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): Socorro: NO hemos obtenido escaner btle  !!!!");
            mostrarEstado("Bluetooth apagado: enciéndelo y vuelve a abrir la app");
        } else {
            mostrarEstado("Listo: pulsa «Buscar nuestro dispositivo BTLE»");

        }

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): voy a perdir permisos (si no los tuviera) !!!!");

        if ( ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ) {
            Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): NO tengo permiso de bluetooth " );
        } else {
            Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): SI tengo permiso de bluetooth ");
        }

        if ( ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ) {
            Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): NO tengo permiso de bluetooth admin" );
        } else {
            Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): SI tengo permiso de bluetooth admin");
        }

        if ( ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): NO tengo permiso de coarse location" );
        } else {
            Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): SI tengo permiso de coarse location");
        }

        if ( ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ) {
            Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): NO tengo permiso de bluetooth scan" );
        } else {
            Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): SI tengo permiso de bluetooth scan ");
        }

        if ( ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ) {
            Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): NO tengo permiso de bluetooth connect" );
        } else {
            Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): SI tengo permiso de bluetooth connect ");
        }




        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): pidiendo permisos " );

            /*
        if (
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED
                        //|| ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        )
        {


            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[]{Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.BLUETOOTH_SCAN,
                            //Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    CODIGO_PETICION_PERMISOS);

             */


            // PERMISOS 2025
            requestPermissions(
                    new String[]{Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    CODIGO_PETICION_PERMISOS);




            Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): permisos pedidos" );
            /*
        }
        else {
            Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): parece que YA tengo los permisos necesarios !!!!");

        }
             */








        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): TERMINA " );
    } // ()


    // --------------------------------------------------------------
    // --------------------------------------------------------------
    // ------------------------------------------------------------
    // texto: Text --> mostrarEstado() -->          (NUEVO)
    // What it does: writes the text in the log and shows it on the screen (what is happening now).
    // ------------------------------------------------------------
    private void mostrarEstado(final String texto) {
        Log.d(ETIQUETA_LOG, " estado: " + texto);
        runOnUiThread(() -> {
            if (this.textoEstado != null) {
                this.textoEstado.setText(texto);
            }
        });
    } // ()

    // ------------------------------------------------------------
    // reiniciarEscaneo() -->          (NUEVO)
    // What it does: stops and starts the scan again with the same callback (the phone then reports
    //               our board again, with its new counter) and schedules the next restart.
    // ------------------------------------------------------------
    @SuppressLint("MissingPermission") // los permisos se comprueban con tienePermisosBluetooth()
    private void reiniciarEscaneo() {
        if (this.callbackDelEscaneo == null || this.elEscanner == null || !tienePermisosBluetooth()) {
            return;
        }
        Log.d(ETIQUETA_LOG, " reiniciarEscaneo()");
        this.elEscanner.stopScan(this.callbackDelEscaneo);
        ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
        this.elEscanner.startScan(new ArrayList<ScanFilter>(), settings, this.callbackDelEscaneo);
        this.elTemporizador.postDelayed(this.reinicioDelEscaneo, INTERVALO_REINICIO_ESCANEO);
    } // ()

    // ------------------------------------------------------------
    // texto: Text --> textoLegible() --> Text          (NUEVO)
    // What it does: returns the text with the non printable characters changed to '?'.
    // ------------------------------------------------------------
    private static String textoLegible(String texto) {
        StringBuilder resultado = new StringBuilder();
        for (char c : texto.toCharArray()) {
            resultado.append(c >= 32 && c <= 126 ? c : '?');
        }
        return resultado.toString();
    } // ()

    // ------------------------------------------------------------
    // mostrarEstadoBusqueda() -->          (NUEVO)
    // What it does: shows on the screen how the search is going: BLE packets and iBeacons received
    //               and the result of the last measurement sent to the server.
    // ------------------------------------------------------------
    private void mostrarEstadoBusqueda() {
        String texto = "";
        if (!this.ultimoResultado.isEmpty()) {
            texto = this.ultimoResultado + "\n\n"; // (el resultado primero, para que siempre se vea)
        }
        texto = texto + "Buscando nuestra placa... (" + this.anunciosRecibidos + " anuncios BLE, "
                + this.iBeaconsRecibidos + " iBeacon, " + this.tramasNuestras + " de nuestra placa)";
        if (!this.ultimoIBeacon.isEmpty()) {
            texto = texto + "\nÚltimo iBeacon: " + this.ultimoIBeacon;
        }
        mostrarEstado(texto);
    } // ()

    // ------------------------------------------------------------
    // idSensor: N, tipoMedicion: N, valorMedicion: Z, contador: N --> mostrarMedicion() -->          (NUEVO)
    // What it does: shows on the screen the last measurement received from the board (type as a name, 11 -> CO2).
    // ------------------------------------------------------------
    private void mostrarMedicion(int idSensor, int tipoMedicion, int valorMedicion, int contador) {
        final String nombreTipo = tipoMedicion == 11 ? "CO2"
                : tipoMedicion == 12 ? "Temperatura"
                : tipoMedicion == 13 ? "Ruido"
                : String.valueOf(tipoMedicion);
        final String texto = "IdSensor " + idSensor + "  ·  " + nombreTipo + "  ·  " + valorMedicion
                + "\n(contador " + contador + ")";
        runOnUiThread(() -> {
            if (this.textoUltimaMedicion != null) {
                this.textoUltimaMedicion.setText(texto);
            }
        });
    } // ()

    // ------------------------------------------------------------
    // tienePermisosBluetooth() --> B          (NUEVO)
    //
    // true si la app puede escanear por Bluetooth: desde Android 12,
    // permisos BLUETOOTH_SCAN y BLUETOOTH_CONNECT; antes de Android 12
    // esos permisos no existen y lo que hace falta es ACCESS_FINE_LOCATION
    // What it does: returns true if the app has the permissions it needs to scan:
    //               BLUETOOTH_SCAN and BLUETOOTH_CONNECT (Android 12+) or ACCESS_FINE_LOCATION (older)
    // ------------------------------------------------------------
    private boolean tienePermisosBluetooth() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S) {
            // antes de Android 12 BLUETOOTH_SCAN no existe: hace falta la ubicación
            return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
        return ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
    } // ()

    // ------------------------------------------------------------
    // savedInstanceState: Bundle --> onCreate() -->
    // What it does: runs when the app opens: shows the screen, creates the logic and prepares Bluetooth.
    // ------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.textoUltimaMedicion = findViewById(R.id.textoUltimaMedicion); // (NUEVO)
        this.textoEstado = findViewById(R.id.textoEstado);                 // (NUEVO)

        Log.d(ETIQUETA_LOG, " onCreate(): empieza ");

        this.laLogica = new LogicaFakeTelefono(); // (NUEVO)

        inicializarBlueTooth();

        Log.d(ETIQUETA_LOG, " onCreate(): termina ");

    } // onCreate()

    // ------------------------------------------------------------
    // requestCode: Z, permissions: [Text], grantResults: [Z] --> onRequestPermissionsResult() -->
    // What it does: Android calls it with the user's answer to the permissions;
    //               if they were given, prepares Bluetooth again to get the scanner.
    // ------------------------------------------------------------
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult( requestCode, permissions, grantResults);

        Log.d(ETIQUETA_LOG, " onRequestPermissionResult(): EMPIEZA " );

        switch (requestCode) {
            case CODIGO_PETICION_PERMISOS:
                // If request is cancelled, the result arrays are empty.
                if (tienePermisosBluetooth()) { // (NUEVO) antes: grantResults[0]

                    Log.d(ETIQUETA_LOG, " onRequestPermissionResult(): permisos concedidos  !!!!");
                    if (this.esperandoPermisos) {
                        this.esperandoPermisos = false;
                        inicializarBlueTooth(); // (NUEVO) ahora sí se puede obtener el escáner
                    }
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                }  else {

                    Log.d(ETIQUETA_LOG, " onRequestPermissionResult(): Socorro: permisos NO concedidos  !!!!");
                    mostrarEstado("Sin permisos de Bluetooth / ubicación: dalos en Ajustes > Aplicaciones");

                }
                return;
        }
        // Other 'case' lines to check for other
        // permissions this app might request.
    } // ()



} // class