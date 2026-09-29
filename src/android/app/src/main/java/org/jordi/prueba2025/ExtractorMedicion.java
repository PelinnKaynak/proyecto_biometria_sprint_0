// ---------------------------------------------------------------------------
// Fichero:     ExtractorMedicion.java
// Descripción: Deshace la codificación que usa el Arduino en la trama iBeacon
//              (major = tipo * 256 + contador, minor = valor) y devuelve la
//              medición. Diseño en doc/android_design.md.
// Autora:      Pelin Kaynak
// Fecha:       29/09/2026
// Copyright:   (c) 2026 Pelin Kaynak - Proyecto Biometría y Medio Ambiente, EPSG-UPV
// ---------------------------------------------------------------------------
package org.jordi.prueba2025;

public class ExtractorMedicion {

    // -----------------------------------------------------------------------
    // trama: TramaIBeacon --> extraerMedicion() --> MedicionDetectada
    //
    // uuid          = texto de los 16 bytes del UUID
    // tipoMedicion  = byte alto de major
    // contador      = byte bajo de major (sin signo, 0..255)
    // valorMedicion = minor como entero con signo de 16 bits
    // -----------------------------------------------------------------------
    public static MedicionDetectada extraerMedicion(TramaIBeacon trama) {
        byte[] major = trama.getMajor();

        String uuid = Utilidades.bytesToString(trama.getUUID());
        int tipoMedicion = major[0] & 0xFF;
        int contador = major[1] & 0xFF;
        int valorMedicion = Utilidades.bytesToInt(trama.getMinor());

        return new MedicionDetectada(uuid, tipoMedicion, contador, valorMedicion);
    }
}
