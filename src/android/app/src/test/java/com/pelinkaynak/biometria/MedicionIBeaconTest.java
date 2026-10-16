// ---------------------------------------------------------------------------
// Fichero:     MedicionIBeaconTest.java
// Descripción: Tests de MedicionIBeacon.desdeDatosFabricante(): lectura de una
//              trama de nuestra placa, valores negativos, IdSensor de 2 bytes y
//              tramas que no son nuestras o no son iBeacon.
// Autora:      Pelin Kaynak
// Fecha:       16/10/2026
// Copyright:   (c) 2026 Pelin Kaynak - Proyecto Biometría y Medio Ambiente, EPSG-UPV
// ---------------------------------------------------------------------------
package com.pelinkaynak.biometria;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class MedicionIBeaconTest {

    private static final String PREFIJO = "EPSG-GTI-PELIN";

    // ------------------------------------------------------------
    // prefijo: Text, idSensor: N, major: [Z]_2, minor: [Z]_2 --> datosFabricante() --> [Z]   (estático)
    // What it does: builds the manufacturer data of an iBeacon packet like the one of our board.
    // ------------------------------------------------------------
    private static byte[] datosFabricante(String prefijo, int idSensor, byte[] major, byte[] minor) {
        byte[] datos = new byte[23];
        datos[0] = 0x02;
        datos[1] = 0x15;
        byte[] bytesPrefijo = prefijo.getBytes();
        System.arraycopy(bytesPrefijo, 0, datos, 2, 14);
        datos[16] = (byte) (idSensor >> 8);
        datos[17] = (byte) (idSensor & 0xFF);
        datos[18] = major[0];
        datos[19] = major[1];
        datos[20] = minor[0];
        datos[21] = minor[1];
        datos[22] = (byte) 0xC5; // txPower
        return datos;
    } // ()

    // ------------------------------------------------------------
    // leeLaTramaDeNuestraPlaca() -->
    // What it does: reads IdSensor 1, CO2, counter 5 and value 123 from a packet of our board.
    // ------------------------------------------------------------
    @Test
    public void leeLaTramaDeNuestraPlaca() {
        // IdSensor 1, CO2 (11), contador 5, valor 123
        byte[] datos = datosFabricante(PREFIJO, 1, new byte[] { 0x0B, 0x05 }, new byte[] { 0x00, 0x7B });
        MedicionIBeacon medicion = MedicionIBeacon.desdeDatosFabricante(datos, PREFIJO);
        assertNotNull(medicion);
        assertEquals(1, medicion.getIdSensor());
        assertEquals(11, medicion.getTipoMedicion());
        assertEquals(5, medicion.getContador());
        assertEquals(123, medicion.getValorMedicion());
    }

    // ------------------------------------------------------------
    // leeValoresNegativosYContadoresAltos() -->
    // What it does: checks a negative value (-12) and a counter above 127 (200) are read right.
    // ------------------------------------------------------------
    @Test
    public void leeValoresNegativosYContadoresAltos() {
        // valor -12 (FF F4) y contador 200 (byte 0xC8)
        byte[] datos = datosFabricante(PREFIJO, 1, new byte[] { 0x0B, (byte) 0xC8 },
                new byte[] { (byte) 0xFF, (byte) 0xF4 });
        MedicionIBeacon medicion = MedicionIBeacon.desdeDatosFabricante(datos, PREFIJO);
        assertNotNull(medicion);
        assertEquals(200, medicion.getContador());
        assertEquals(-12, medicion.getValorMedicion());
    }

    // ------------------------------------------------------------
    // leeIdSensorDeDosBytes() -->
    // What it does: checks the IdSensor uses the last 2 UUID bytes (0x01 0x02 = 258).
    // ------------------------------------------------------------
    @Test
    public void leeIdSensorDeDosBytes() {
        byte[] datos = datosFabricante(PREFIJO, 258, new byte[] { 0x0C, 0x01 }, new byte[] { 0x00, 0x15 });
        MedicionIBeacon medicion = MedicionIBeacon.desdeDatosFabricante(datos, PREFIJO);
        assertNotNull(medicion);
        assertEquals(258, medicion.getIdSensor()); // 0x01 0x02
        assertEquals(12, medicion.getTipoMedicion());
    }

    // ------------------------------------------------------------
    // ignoraLasPlacasDeOtrosGrupos() -->
    // What it does: checks a packet with another UUID prefix gives null (not our board).
    // ------------------------------------------------------------
    @Test
    public void ignoraLasPlacasDeOtrosGrupos() {
        byte[] datos = datosFabricante("EPSG-GTI-PROY-", 13121, new byte[] { 0x0B, 0x05 }, new byte[] { 0x00, 0x7B });
        assertNull(MedicionIBeacon.desdeDatosFabricante(datos, PREFIJO));
    }

    // ------------------------------------------------------------
    // ignoraLoQueNoEsIBeacon() -->
    // What it does: checks data that is not an iBeacon (wrong type, null or too short) gives null.
    // ------------------------------------------------------------
    @Test
    public void ignoraLoQueNoEsIBeacon() {
        byte[] datos = datosFabricante(PREFIJO, 1, new byte[] { 0x0B, 0x05 }, new byte[] { 0x00, 0x7B });
        datos[1] = 0x16; // no es 02 15
        assertNull(MedicionIBeacon.desdeDatosFabricante(datos, PREFIJO));
        assertNull(MedicionIBeacon.desdeDatosFabricante(null, PREFIJO));
        assertNull(MedicionIBeacon.desdeDatosFabricante(new byte[] { 0x02, 0x15, 0x45 }, PREFIJO));
    }
}
