// ---------------------------------------------------------------------------
// Fichero:     MedicionIBeacon.java
// Descripción: Medición que envía una placa del proyecto dentro de una trama
//              iBeacon (IdSensor, tipo, contador y valor). Saca estos datos de
//              los datos del fabricante del anuncio BLE.
//              Diseño en doc/android_design.md.
// Autora:      Pelin Kaynak
// Fecha:       16/10/2026
// Copyright:   (c) 2026 Pelin Kaynak - Proyecto Biometría y Medio Ambiente, EPSG-UPV
// ---------------------------------------------------------------------------
package com.pelinkaynak.biometria;

import java.util.Arrays;

// MedicionIBeacon class: the measurement that one of our boards sends inside an iBeacon packet.
public class MedicionIBeacon {

    private static final int LONGITUD_MINIMA = 22; // 02 15 + UUID (16) + major (2) + minor (2)
    private static final int LONGITUD_PREFIJO_UUID = 14;

    private final int idSensor;
    private final int tipoMedicion;
    private final int contador;
    private final int valorMedicion;

    // ------------------------------------------------------------
    // idSensor: N, tipoMedicion: N, contador: N, valorMedicion: Z --> MedicionIBeacon() -->   (privado)
    // What it does: creates the measurement (only desdeDatosFabricante() creates them).
    // ------------------------------------------------------------
    private MedicionIBeacon(int idSensor, int tipoMedicion, int contador, int valorMedicion) {
        this.idSensor = idSensor;
        this.tipoMedicion = tipoMedicion;
        this.contador = contador;
        this.valorMedicion = valorMedicion;
    } // ()

    // ------------------------------------------------------------
    // datos: [Z], prefijoUUID: Text --> desdeDatosFabricante() --> MedicionIBeacon | vacío   (estático)
    //
    // datos = datos del fabricante Apple del anuncio:
    //   02 15 + UUID (16 bytes) + major (2) + minor (2) + txPower (1)
    // UUID = prefijo (14 bytes) + IdSensor (2 bytes), major = tipo * 256 + contador,
    // minor = valor (con signo). Devuelve vacío (null) si no es una trama iBeacon
    // o si el UUID no empieza por prefijoUUID (no es una placa del proyecto).
    // What it does: reads IdSensor, type, counter and value from the iBeacon bytes,
    //               or returns null if the packet is not from one of our boards.
    // ------------------------------------------------------------
    public static MedicionIBeacon desdeDatosFabricante(byte[] datos, String prefijoUUID) {
        if (datos == null || datos.length < LONGITUD_MINIMA || datos[0] != 0x02 || datos[1] != 0x15) {
            return null; // no es una trama iBeacon
        }

        byte[] uuid = Arrays.copyOfRange(datos, 2, 18);   // 16 bytes
        byte[] major = Arrays.copyOfRange(datos, 18, 20); // 2 bytes
        byte[] minor = Arrays.copyOfRange(datos, 20, 22); // 2 bytes

        String prefijo = Utilidades.bytesToString(Arrays.copyOfRange(uuid, 0, LONGITUD_PREFIJO_UUID));
        if (!prefijo.equals(prefijoUUID)) {
            return null; // no es una placa del proyecto
        }

        int idSensor = ((uuid[14] & 0xFF) << 8) | (uuid[15] & 0xFF); // 2 últimos bytes del UUID
        int tipoMedicion = major[0] & 0xFF;                          // byte alto de major
        int contador = major[1] & 0xFF;                              // byte bajo de major
        int valorMedicion = Utilidades.bytesToInt(minor);            // minor, con signo

        return new MedicionIBeacon(idSensor, tipoMedicion, contador, valorMedicion);
    } // ()

    // ------------------------------------------------------------
    // getIdSensor() --> N
    // What it does: returns the number of the sensor (last 2 bytes of the UUID).
    // ------------------------------------------------------------
    public int getIdSensor() {
        return idSensor;
    } // ()

    // ------------------------------------------------------------
    // getTipoMedicion() --> N
    // What it does: returns the measurement type (11 = CO2, 12 = temperature, 13 = noise).
    // ------------------------------------------------------------
    public int getTipoMedicion() {
        return tipoMedicion;
    } // ()

    // ------------------------------------------------------------
    // getContador() --> N
    // What it does: returns the counter of the board (it changes with each new measurement).
    // ------------------------------------------------------------
    public int getContador() {
        return contador;
    } // ()

    // ------------------------------------------------------------
    // getValorMedicion() --> Z
    // What it does: returns the measured value.
    // ------------------------------------------------------------
    public int getValorMedicion() {
        return valorMedicion;
    } // ()

} // class
