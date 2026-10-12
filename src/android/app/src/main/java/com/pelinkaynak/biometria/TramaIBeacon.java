// ---------------------------------------------------------------------------
// Fichero:     TramaIBeacon.java
// Descripción: Trocea los bytes de una trama iBeacon en sus campos.
//              Diseño en doc/android_design.md.
// Autor:       Jordi Bataller i Mascarell (código original proporcionado)
// Aportación:  Pelin Kaynak - cabeceras de fichero y de función con el
//              diseño lógico (29/09/2026)
// Copyright:   (c) Jordi Bataller i Mascarell - EPSG-UPV
// ---------------------------------------------------------------------------
package com.pelinkaynak.biometria;

import java.util.Arrays;

// -----------------------------------------------------------------------------------
// @author: Jordi Bataller i Mascarell
// -----------------------------------------------------------------------------------
public class TramaIBeacon {
    private byte[] prefijo = null; // 9 bytes
    private byte[] uuid = null; // 16 bytes
    private byte[] major = null; // 2 bytes
    private byte[] minor = null; // 2 bytes
    private byte txPower = 0; // 1 byte

    private byte[] losBytes;

    private byte[] advFlags = null; // 3 bytes
    private byte[] advHeader = null; // 2 bytes
    private byte[] companyID = new byte[2]; // 2 bytes
    private byte iBeaconType = 0 ; // 1 byte
    private byte iBeaconLength = 0 ; // 1 byte

    // ------------------------------------------------------------
    // getPrefijo() --> [Z]_9
    // ------------------------------------------------------------
    public byte[] getPrefijo() {
        return prefijo;
    }

    // ------------------------------------------------------------
    // getUUID() --> [Z]_16
    // ------------------------------------------------------------
    public byte[] getUUID() {
        return uuid;
    }

    // ------------------------------------------------------------
    // getMajor() --> [Z]_2
    // ------------------------------------------------------------
    public byte[] getMajor() {
        return major;
    }

    // ------------------------------------------------------------
    // getMinor() --> [Z]_2
    // ------------------------------------------------------------
    public byte[] getMinor() {
        return minor;
    }

    // ------------------------------------------------------------
    // getTxPower() --> Z
    // ------------------------------------------------------------
    public byte getTxPower() {
        return txPower;
    }

    // ------------------------------------------------------------
    // getLosBytes() --> [Z]
    // ------------------------------------------------------------
    public byte[] getLosBytes() {
        return losBytes;
    }

    // ------------------------------------------------------------
    // getAdvFlags() --> [Z]_3
    // ------------------------------------------------------------
    public byte[] getAdvFlags() {
        return advFlags;
    }

    // ------------------------------------------------------------
    // getAdvHeader() --> [Z]_2
    // ------------------------------------------------------------
    public byte[] getAdvHeader() {
        return advHeader;
    }

    // ------------------------------------------------------------
    // getCompanyID() --> [Z]_2
    // ------------------------------------------------------------
    public byte[] getCompanyID() {
        return companyID;
    }

    // ------------------------------------------------------------
    // getiBeaconType() --> Z
    // ------------------------------------------------------------
    public byte getiBeaconType() {
        return iBeaconType;
    }

    // ------------------------------------------------------------
    // getiBeaconLength() --> Z
    // ------------------------------------------------------------
    public byte getiBeaconLength() {
        return iBeaconLength;
    }

    // ------------------------------------------------------------
    // bytes: [Z] --> TramaIBeacon() -->
    // ------------------------------------------------------------
    public TramaIBeacon(byte[] bytes ) {
        this.losBytes = bytes;

        prefijo = Arrays.copyOfRange(losBytes, 0, 8+1 ); // 9 bytes
        uuid = Arrays.copyOfRange(losBytes, 9, 24+1 ); // 16 bytes
        major = Arrays.copyOfRange(losBytes, 25, 26+1 ); // 2 bytes
        minor = Arrays.copyOfRange(losBytes, 27, 28+1 ); // 2 bytes
        txPower = losBytes[ 29 ]; // 1 byte

        advFlags = Arrays.copyOfRange( prefijo, 0, 2+1 ); // 3 bytes
        advHeader = Arrays.copyOfRange( prefijo, 3, 4+1 ); // 2 bytes
        companyID = Arrays.copyOfRange( prefijo, 5, 6+1 ); // 2 bytes
        iBeaconType = prefijo[ 7 ]; // 1 byte
        iBeaconLength = prefijo[ 8 ]; // 1 byte

    } // ()
} // class
// -----------------------------------------------------------------------------------
// -----------------------------------------------------------------------------------
// -----------------------------------------------------------------------------------
// -----------------------------------------------------------------------------------

