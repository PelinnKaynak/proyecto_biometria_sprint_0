// ---------------------------------------------------------------------------
// Fichero:     MedicionDetectada.java
// Descripción: Medición extraída de una trama iBeacon de nuestra placa:
//              uuid del emisor, tipo de medición, contador y valor.
//              Diseño en doc/android_design.md.
// Autora:      Pelin Kaynak
// Fecha:       29/09/2026
// Copyright:   (c) 2026 Pelin Kaynak - Proyecto Biometría y Medio Ambiente, EPSG-UPV
// ---------------------------------------------------------------------------
package org.jordi.prueba2025;

public class MedicionDetectada {

    private final String uuid;
    private final int tipoMedicion;
    private final int contador;
    private final int valorMedicion;

    // -----------------------------------------------------------------------
    // uuid: Text, tipoMedicion: N, contador: N, valorMedicion: Z --> MedicionDetectada() -->
    // -----------------------------------------------------------------------
    public MedicionDetectada(String uuid, int tipoMedicion, int contador, int valorMedicion) {
        this.uuid = uuid;
        this.tipoMedicion = tipoMedicion;
        this.contador = contador;
        this.valorMedicion = valorMedicion;
    }

    // -----------------------------------------------------------------------
    // getUuid() --> Text
    // -----------------------------------------------------------------------
    public String getUuid() {
        return uuid;
    }

    // -----------------------------------------------------------------------
    // getTipoMedicion() --> N
    // -----------------------------------------------------------------------
    public int getTipoMedicion() {
        return tipoMedicion;
    }

    // -----------------------------------------------------------------------
    // getContador() --> N
    // -----------------------------------------------------------------------
    public int getContador() {
        return contador;
    }

    // -----------------------------------------------------------------------
    // getValorMedicion() --> Z
    // -----------------------------------------------------------------------
    public int getValorMedicion() {
        return valorMedicion;
    }
}
