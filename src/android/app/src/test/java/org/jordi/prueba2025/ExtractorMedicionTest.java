// ---------------------------------------------------------------------------
// Fichero:     ExtractorMedicionTest.java
// Descripción: Tests JUnit de ExtractorMedicion con tramas iBeacon hechas a
//              mano (diseño en doc/android_design.md).
// Autora:      Pelin Kaynak
// Fecha:       29/09/2026
// Copyright:   (c) 2026 Pelin Kaynak - Proyecto Biometría y Medio Ambiente, EPSG-UPV
// ---------------------------------------------------------------------------
package org.jordi.prueba2025;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ExtractorMedicionTest {

    private static final byte[] PREFIJO_IBEACON = {
            0x02, 0x01, 0x06, 0x1A, (byte) 0xFF, 0x4C, 0x00, 0x02, 0x15
    };

    // -----------------------------------------------------------------------
    // uuid: Text, major: [Z]_2, minor: [Z]_2 --> crearTrama() --> TramaIBeacon
    // -----------------------------------------------------------------------
    private static TramaIBeacon crearTrama(String uuid, byte[] major, byte[] minor) {
        byte[] bytes = new byte[30];
        System.arraycopy(PREFIJO_IBEACON, 0, bytes, 0, 9);
        System.arraycopy(uuid.getBytes(), 0, bytes, 9, 16);
        System.arraycopy(major, 0, bytes, 25, 2);
        System.arraycopy(minor, 0, bytes, 27, 2);
        bytes[29] = (byte) 0xC5; // txPower
        return new TramaIBeacon(bytes);
    }

    @Test
    public void extraeUuidTipoContadorYValorDeUnaTramaDeCO2() {
        TramaIBeacon trama = crearTrama("EPSG-GTI-PROY-3A",
                new byte[]{0x0B, 0x05},          // CO2 = 11, contador = 5
                new byte[]{0x00, 0x7B});         // 123

        MedicionDetectada medicion = ExtractorMedicion.extraerMedicion(trama);

        assertEquals("EPSG-GTI-PROY-3A", medicion.getUuid());
        assertEquals(11, medicion.getTipoMedicion());
        assertEquals(5, medicion.getContador());
        assertEquals(123, medicion.getValorMedicion());
    }

    @Test
    public void leeValoresNegativos() {
        TramaIBeacon trama = crearTrama("EPSG-GTI-PROY-3A",
                new byte[]{0x0C, 0x01},                     // TEMPERATURA = 12
                new byte[]{(byte) 0xFF, (byte) 0xF4});      // -12

        MedicionDetectada medicion = ExtractorMedicion.extraerMedicion(trama);

        assertEquals(12, medicion.getTipoMedicion());
        assertEquals(-12, medicion.getValorMedicion());
    }

    @Test
    public void leeElContadorSinSigno() {
        TramaIBeacon trama = crearTrama("EPSG-GTI-PROY-3A",
                new byte[]{0x0B, (byte) 0xC8},   // contador = 200
                new byte[]{0x00, 0x01});

        assertEquals(200, ExtractorMedicion.extraerMedicion(trama).getContador());
    }
}
