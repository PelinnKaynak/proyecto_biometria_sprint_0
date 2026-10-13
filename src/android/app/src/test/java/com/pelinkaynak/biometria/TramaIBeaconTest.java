// ---------------------------------------------------------------------------
// Fichero:     TramaIBeaconTest.java
// Descripción: Tests JUnit de TramaIBeacon: troceo de una trama iBeacon como
//              la que emite nuestra placa (diseño en doc/android_design.md).
// Autora:      Pelin Kaynak
// Fecha:       13/10/2026
// Copyright:   (c) 2026 Pelin Kaynak - Proyecto Biometría y Medio Ambiente, EPSG-UPV
// ---------------------------------------------------------------------------
package com.pelinkaynak.biometria;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TramaIBeaconTest {

    // ------------------------------------------------------------
    // tramaDeNuestraPlaca() --> [Z]_30
    // What it does: builds the 30 bytes our board sends for CO2 = 123, counter = 5.
    // ------------------------------------------------------------
    static byte[] tramaDeNuestraPlaca() {
        byte[] prefijo = { 0x02, 0x01, 0x06, 0x1A, (byte) 0xFF, 0x4C, 0x00, 0x02, 0x15 };
        byte[] uuid = "EPSG-GTI-PROY-3A".getBytes();
        byte[] resto = { 0x0B, 0x05, 0x00, 0x7B, (byte) 0xCB };

        byte[] trama = new byte[30];
        System.arraycopy(prefijo, 0, trama, 0, 9);
        System.arraycopy(uuid, 0, trama, 9, 16);
        System.arraycopy(resto, 0, trama, 25, 5);
        return trama;
    }

    // ------------------------------------------------------------
    // separaUUIDMajorMinorYTxPower() -->
    // What it does: checks UUID, major, minor and txPower are cut from the right bytes.
    // ------------------------------------------------------------
    @Test
    public void separaUUIDMajorMinorYTxPower() {
        TramaIBeacon trama = new TramaIBeacon(tramaDeNuestraPlaca());

        assertArrayEquals("EPSG-GTI-PROY-3A".getBytes(), trama.getUUID());
        assertArrayEquals(new byte[] { 0x0B, 0x05 }, trama.getMajor());
        assertArrayEquals(new byte[] { 0x00, 0x7B }, trama.getMinor());
        assertEquals((byte) 0xCB, trama.getTxPower());
    }

    // ------------------------------------------------------------
    // separaLasPartesDelPrefijo() -->
    // What it does: checks the 9-byte prefix and its parts (flags, header, company, type, length).
    // ------------------------------------------------------------
    @Test
    public void separaLasPartesDelPrefijo() {
        TramaIBeacon trama = new TramaIBeacon(tramaDeNuestraPlaca());

        assertEquals(9, trama.getPrefijo().length);
        assertArrayEquals(new byte[] { 0x02, 0x01, 0x06 }, trama.getAdvFlags());
        assertArrayEquals(new byte[] { 0x1A, (byte) 0xFF }, trama.getAdvHeader());
        assertArrayEquals(new byte[] { 0x4C, 0x00 }, trama.getCompanyID());
        assertEquals(0x02, trama.getiBeaconType());
        assertEquals(0x15, trama.getiBeaconLength());
    }

    // ------------------------------------------------------------
    // guardaLaTramaCompleta() -->
    // What it does: checks getLosBytes() returns the whole packet.
    // ------------------------------------------------------------
    @Test
    public void guardaLaTramaCompleta() {
        byte[] bytes = tramaDeNuestraPlaca();
        assertArrayEquals(bytes, new TramaIBeacon(bytes).getLosBytes());
    }
}
