// ---------------------------------------------------------------------------
// Fichero:     UtilidadesTest.java
// Descripción: Tests JUnit de Utilidades: conversiones entre bytes, texto,
//              números y UUID (diseño en doc/android_design.md).
// Autora:      Pelin Kaynak
// Fecha:       13/10/2026
// Copyright:   (c) 2026 Pelin Kaynak - Proyecto Biometría y Medio Ambiente, EPSG-UPV
// ---------------------------------------------------------------------------
package com.pelinkaynak.biometria;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.junit.Test;

public class UtilidadesTest {

    // ------------------------------------------------------------
    // bytesToStringConvierteCadaByteEnUnCaracter() -->
    // What it does: checks each byte becomes one character ("EPSG-GTI-PELIN") and null gives "".
    // ------------------------------------------------------------
    @Test
    public void bytesToStringConvierteCadaByteEnUnCaracter() {
        assertEquals("EPSG-GTI-PELIN", Utilidades.bytesToString("EPSG-GTI-PELIN".getBytes()));
        assertEquals("", Utilidades.bytesToString(null));
    }

    // ------------------------------------------------------------
    // bytesToIntLeeElMinorConSigno() -->
    // What it does: checks [00 7B] -> 123 and [FF F4] -> -12.
    // ------------------------------------------------------------
    @Test
    public void bytesToIntLeeElMinorConSigno() {
        assertEquals(123, Utilidades.bytesToInt(new byte[] { 0x00, 0x7B }));
        assertEquals(-12, Utilidades.bytesToInt(new byte[] { (byte) 0xFF, (byte) 0xF4 }));
    }

    // ------------------------------------------------------------
    // bytesToLongJuntaLosBytes() -->
    // What it does: checks [0B 05] -> 0x0B05 as a long.
    // ------------------------------------------------------------
    @Test
    public void bytesToLongJuntaLosBytes() {
        assertEquals(0x0B05L, Utilidades.bytesToLong(new byte[] { 0x0B, 0x05 }));
    }

    // ------------------------------------------------------------
    // bytesToHexStringEscribeCadaByteEnHexadecimal() -->
    // What it does: checks [11, 5] -> "0b:05:" (and "" for null).
    // ------------------------------------------------------------
    @Test
    public void bytesToHexStringEscribeCadaByteEnHexadecimal() {
        assertEquals("0b:05:", Utilidades.bytesToHexString(new byte[] { 0x0B, 0x05 }));
        assertEquals("", Utilidades.bytesToHexString(null));
    }

    // ------------------------------------------------------------
    // stringToBytesYBytesToStringSonInversas() -->
    // What it does: checks "EPSG" -> its 4 ASCII bytes.
    // ------------------------------------------------------------
    @Test
    public void stringToBytesYBytesToStringSonInversas() {
        assertArrayEquals(new byte[] { 'E', 'P', 'S', 'G' }, Utilidades.stringToBytes("EPSG"));
    }

    // ------------------------------------------------------------
    // stringToUUIDYUuidToStringSonInversas() -->
    // What it does: checks text -> UUID -> text gives the same 16 characters.
    // ------------------------------------------------------------
    @Test
    public void stringToUUIDYUuidToStringSonInversas() {
        UUID uuid = Utilidades.stringToUUID("EPSG-GTI-PROY-3A");
        assertEquals("EPSG-GTI-PROY-3A", Utilidades.uuidToString(uuid));
        assertEquals(16 * 3, Utilidades.uuidToHexString(uuid).length());
    }

    // ------------------------------------------------------------
    // dosLongToBytesDevuelve16Bytes() -->
    // What it does: checks two longs become 16 bytes.
    // ------------------------------------------------------------
    @Test
    public void dosLongToBytesDevuelve16Bytes() {
        assertEquals(16, Utilidades.dosLongToBytes(1L, 2L).length);
    }

    // ------------------------------------------------------------
    // stringToUUIDRechazaUnTextoQueNoTiene16Caracteres() -->
    // What it does: checks a text that is not 16 characters long is rejected.
    // ------------------------------------------------------------
    @Test(expected = Error.class)
    public void stringToUUIDRechazaUnTextoQueNoTiene16Caracteres() {
        Utilidades.stringToUUID("corto");
    }
}
