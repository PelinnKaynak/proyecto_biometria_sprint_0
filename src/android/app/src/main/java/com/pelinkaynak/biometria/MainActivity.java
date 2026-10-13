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
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;

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

    // --------------------------------------------------------------
    // --------------------------------------------------------------
    private BluetoothLeScanner elEscanner;

    private ScanCallback callbackDelEscaneo = null;

    // (NUEVO)
    private LogicaFakeTelefono laLogica;

    // (NUEVO) último contador enviado de cada sensor (IdSensor -> contador):
    // evita enviar varias veces la misma trama repetida
    private final Map<Integer, Integer> ultimoContadorPorSensor = new HashMap<>();

    // ------------------------------------------------------------
    // buscarTodosLosDispositivosBTLE() -->
    // What it does: scans ALL the Bluetooth devices around and only writes them in the log.
    // ------------------------------------------------------------
    private void buscarTodosLosDispositivosBTLE() {
        Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): empieza ");

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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): vuelvo a pedir permiso para escanear por si no lo tuviea de antes");
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): startScan ");
        this.elEscanner.startScan(this.callbackDelEscaneo);

        Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): termina ");
    } // ()

    // ------------------------------------------------------------
    // resultado: ScanResult --> mostrarInformacionDispositivoBTLE() -->
    // What it does: writes in the log all the details of a found packet (name, rssi, UUID, major, minor...).
    // ------------------------------------------------------------
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
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
    // dispositivoBuscado: Text --> buscarEsteDispositivoBTLE() -->
    // What it does: scans only the device with this name ("PelinIBeacon"). For each packet:
    //               writes it in the log and processes it with procesarTramaDetectada().
    // ------------------------------------------------------------
    private void buscarEsteDispositivoBTLE(final String dispositivoBuscado) {
        Log.d(ETIQUETA_LOG, " buscarEsteDispositivoBTLE(): empieza ");

        Log.d(ETIQUETA_LOG, " buscarEsteDispositivoBTLE(): buscando " + dispositivoBuscado);

        Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): instalamos scan callback ");


        // super.onScanResult(ScanSettings.SCAN_MODE_LOW_LATENCY, result); para ahorro de energía

        this.callbackDelEscaneo = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult resultado) {
                super.onScanResult(callbackType, resultado);
                Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): onScanResult() ");

                mostrarInformacionDispositivoBTLE(resultado);
                procesarTramaDetectada(resultado); // (NUEVO)
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

            }
        };

        ScanFilter sf = new ScanFilter.Builder().setDeviceName(dispositivoBuscado).build();

        Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): empezamos a escanear buscando: " + dispositivoBuscado);
        //Log.d(ETIQUETA_LOG, "  buscarEsteDispositivoBTLE(): empezamos a escanear buscando: " + dispositivoBuscado
        //      + " -> " + Utilidades.stringToUUID( dispositivoBuscado ) );

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        Log.d(ETIQUETA_LOG, " buscarEsteDispositivoBTLE(): empieztar scan");
        // (NUEVO) eliminado el startScan() sin filtro que había aquí: con el
        // mismo callback Android rechaza el segundo startScan() y el filtro
        // por nombre no se llegaba a aplicar
        // this.elEscanner.startScan(this.callbackDelEscaneo);

        ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();

        ArrayList<ScanFilter> filtros = new ArrayList<ScanFilter>();
        filtros.add( sf );


        // ANTES this.elEscanner.startScan( this.callbackDelEscaneo);

        // AHORA (2025)
        this.elEscanner.startScan( filtros, settings, this.callbackDelEscaneo);

        Log.d(ETIQUETA_LOG, " buscarEsteDispositivoBTLE(): termina");
    } // ()

    // --------------------------------------------------------------
    // resultado: ScanResult --> procesarTramaDetectada() -->          (NUEVO)
    //
    // Deshace la codificación del Arduino (UUID = prefijo + IdSensor,
    // major = tipo * 256 + contador, minor = valor) con TramaIBeacon y
    // Utilidades y, si la trama es de una placa del proyecto y trae un
    // contador nuevo para ese sensor, envía IdSensor, TipoMedicion y
    // ValorMedicion al servidor con laLogica.guardarMedicion().
    // What it does: if the packet is from one of our boards and has a new counter for that sensor,
    //               gets the sensor id (last 2 UUID bytes), type (major byte 1), counter (major byte 2)
    //               and value (minor) and sends them to the server.
    // --------------------------------------------------------------
    private void procesarTramaDetectada(ScanResult resultado) {
        TramaIBeacon trama = new TramaIBeacon(resultado.getScanRecord().getBytes());

        byte[] uuid = trama.getUUID();
        String prefijo = Utilidades.bytesToString(Arrays.copyOfRange(uuid, 0, 14));
        if (!PREFIJO_UUID_NUESTROS_BEACONS.equals(prefijo)) {
            Log.d(ETIQUETA_LOG, " procesarTramaDetectada(): no es nuestro beacon, UUID = " + Utilidades.bytesToString(uuid));
            return;
        }
        int idSensor = ((uuid[14] & 0xFF) << 8) | (uuid[15] & 0xFF); // 2 últimos bytes del UUID

        byte[] major = trama.getMajor();
        int tipoMedicion = major[0] & 0xFF;                          // byte alto de major
        int contador = major[1] & 0xFF;                              // byte bajo de major
        int valorMedicion = Utilidades.bytesToInt(trama.getMinor()); // minor

        Integer ultimoContador = this.ultimoContadorPorSensor.get(idSensor);
        if (ultimoContador != null && ultimoContador == contador) {
            return; // la misma trama repetida de este sensor: ya se envió
        }
        this.ultimoContadorPorSensor.put(idSensor, contador);

        Log.d(ETIQUETA_LOG, " procesarTramaDetectada(): envío IdSensor = " + idSensor
                + " TipoMedicion = " + tipoMedicion + " ValorMedicion = " + valorMedicion + " contador = " + contador);

        this.laLogica.guardarMedicion(idSensor, tipoMedicion, valorMedicion,
                new LogicaFakeTelefono.RespuestaGuardarMedicion() {
                    @Override
                    public void guardada() {
                        Log.d(ETIQUETA_LOG, " procesarTramaDetectada(): guardada en el servidor = true");
                    }

                    @Override
                    public void error(String mensaje) {
                        Log.d(ETIQUETA_LOG, " procesarTramaDetectada(): guardada en el servidor = false (" + mensaje + ")");
                    }
                });
    } // ()

    // ------------------------------------------------------------
    // detenerBusquedaDispositivosBTLE() -->
    // What it does: stops the current scan.
    // ------------------------------------------------------------
    private void detenerBusquedaDispositivosBTLE() {

        Log.d(ETIQUETA_LOG, " detenerBusquedaDispositivosBTLE(): empieza");
        if (this.callbackDelEscaneo == null) {
            Log.d(ETIQUETA_LOG, " detenerBusquedaDispositivosBTLE(): termina");
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        Log.d(ETIQUETA_LOG, " detenerBusquedaDispositivosBTLE(): parando scan");
        this.elEscanner.stopScan(this.callbackDelEscaneo);
        this.callbackDelEscaneo = null;

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
    // What it does: button 'search our device' -> scans "PelinIBeacon" and sends its measurements.
    // ------------------------------------------------------------
    public void botonBuscarNuestroDispositivoBTLEPulsado(View v) {
        Log.d(ETIQUETA_LOG, " boton nuestro dispositivo BTLE Pulsado");
        //this.buscarEsteDispositivoBTLE( Utilidades.stringToUUID( "EPSG-GTI-PROY-3A" ) );

        //this.buscarEsteDispositivoBTLE("EPSG-GTI-PROY-3A");
        this.buscarEsteDispositivoBTLE("PelinIBeacon" );
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
    // tienePermisosBluetooth() --> B          (NUEVO)
    //
    // true si la app puede escanear y conectar por Bluetooth
    // (permisos BLUETOOTH_SCAN y BLUETOOTH_CONNECT, que existen
    // desde Android 12; en versiones anteriores devuelve true)
    // What it does: returns true if the app has the BLUETOOTH_SCAN and BLUETOOTH_CONNECT permissions
    //               (always true before Android 12).
    // ------------------------------------------------------------
    private boolean tienePermisosBluetooth() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S) {
            return true; // antes de Android 12 estos permisos no existen
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
                    if (this.elEscanner == null) {
                        inicializarBlueTooth(); // (NUEVO) ahora sí se puede obtener el escáner
                    }
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                }  else {

                    Log.d(ETIQUETA_LOG, " onRequestPermissionResult(): Socorro: permisos NO concedidos  !!!!");

                }
                return;
        }
        // Other 'case' lines to check for other
        // permissions this app might request.
    } // ()



} // class