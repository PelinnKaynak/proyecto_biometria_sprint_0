// ---------------------------------------------------------------------------
// Fichero:     Utilidades.java
// Descripción: Conversiones entre texto, UUID y bytes (métodos estáticos).
//              Diseño en doc/android_design.md.
// Autor:       Jordi Bataller i Mascarell (código original proporcionado)
// Aportación:  Pelin Kaynak - cabeceras de fichero y de función con el
//              diseño lógico (29/09/2026)
// Copyright:   (c) Jordi Bataller i Mascarell - EPSG-UPV
// ---------------------------------------------------------------------------
package com.pelinkaynak.biometria;


import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.UUID;

// -----------------------------------------------------------------------------------
// @author: Jordi Bataller i Mascarell
// -----------------------------------------------------------------------------------
public class Utilidades {

    // ------------------------------------------------------------
    // texto: Text --> stringToBytes() --> [Z]
    // ------------------------------------------------------------
    public static byte[] stringToBytes ( String texto ) {
        return texto.getBytes();
        // byte[] b = string.getBytes(StandardCharsets.UTF_8); // Ja
    } // ()

    // ------------------------------------------------------------
    // uuid: Text --> stringToUUID() --> UUID
    // ------------------------------------------------------------
    public static UUID stringToUUID( String uuid ) {
        if ( uuid.length() != 16 ) {
            throw new Error( "stringUUID: string no tiene 16 caracteres ");
        }
        byte[] comoBytes = uuid.getBytes();

        String masSignificativo = uuid.substring(0, 8);
        String menosSignificativo = uuid.substring(8, 16);
        UUID res = new UUID( Utilidades.bytesToLong( masSignificativo.getBytes() ), Utilidades.bytesToLong( menosSignificativo.getBytes() ) );

        // Log.d( MainActivity.ETIQUETA_LOG, " \n\n***** stringToUUID *** " + uuid  + "=?=" + Utilidades.uuidToString( res ) );

        // UUID res = UUID.nameUUIDFromBytes( comoBytes ); no va como quiero

        return res;
    } // ()

    // ------------------------------------------------------------
    // uuid: UUID --> uuidToString() --> Text
    // ------------------------------------------------------------
    public static String uuidToString ( UUID uuid ) {
        return bytesToString( dosLongToBytes( uuid.getMostSignificantBits(), uuid.getLeastSignificantBits() ) );
    } // ()

    // ------------------------------------------------------------
    // uuid: UUID --> uuidToHexString() --> Text
    // ------------------------------------------------------------
    public static String uuidToHexString ( UUID uuid ) {
        return bytesToHexString( dosLongToBytes( uuid.getMostSignificantBits(), uuid.getLeastSignificantBits() ) );
    } // ()

    // ------------------------------------------------------------
    // bytes: [Z] --> bytesToString() --> Text
    // ------------------------------------------------------------
    public static String bytesToString( byte[] bytes ) {
        if (bytes == null ) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append( (char) b );
        }
        return sb.toString();
    }

    // ------------------------------------------------------------
    // masSignificativos: Z, menosSignificativos: Z --> dosLongToBytes() --> [Z]
    // ------------------------------------------------------------
    public static byte[] dosLongToBytes( long masSignificativos, long menosSignificativos ) {
        ByteBuffer buffer = ByteBuffer.allocate( 2 * Long.BYTES );
        buffer.putLong( masSignificativos );
        buffer.putLong( menosSignificativos );
        return buffer.array();
    }

    // ------------------------------------------------------------
    // bytes: [Z] --> bytesToInt() --> Z   (con signo)
    // ------------------------------------------------------------
    public static int bytesToInt( byte[] bytes ) {
        return new BigInteger(bytes).intValue();
    }

    // ------------------------------------------------------------
    // bytes: [Z] --> bytesToLong() --> Z
    // ------------------------------------------------------------
    public static long bytesToLong( byte[] bytes ) {
        return new BigInteger(bytes).longValue();
    }

    // ------------------------------------------------------------
    // bytes: [Z] --> bytesToIntOK() --> Z
    // ------------------------------------------------------------
    public static int bytesToIntOK( byte[] bytes ) {
        if (bytes == null ) {
            return 0;
        }

        if ( bytes.length > 4 ) {
            throw new Error( "demasiados bytes para pasar a int ");
        }
        int res = 0;



        for( byte b : bytes ) {
           /*
           Log.d( MainActivity.ETIQUETA_LOG, "bytesToInt(): byte: hex=" + Integer.toHexString( b )
                   + " dec=" + b + " bin=" + Integer.toBinaryString( b ) +
                   " hex=" + Byte.toString( b )
           );
           */
            res =  (res << 8) // * 16
                    + (b & 0xFF); // para quedarse con 1 byte (2 cuartetos) de lo que haya en b
        } // for

        if ( (bytes[ 0 ] & 0x8) != 0 ) {
            // si tiene signo negativo (un 1 a la izquierda del primer byte
            res = -(~(byte)res)-1; // complemento a 2 (~) de res pero como byte, -1
        }
       /*
        Log.d( MainActivity.ETIQUETA_LOG, "bytesToInt(): res = " + res + " ~res=" + (res ^ 0xffff)
                + "~res=" + ~((byte) res)
        );
        */

        return res;
    } // ()

    // ------------------------------------------------------------
    // bytes: [Z] --> bytesToHexString() --> Text
    // ------------------------------------------------------------
    public static String bytesToHexString( byte[] bytes ) {

        if (bytes == null ) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
            sb.append(':');
        }
        return sb.toString();
    } // ()
} // class
// -----------------------------------------------------------------------------------
// -----------------------------------------------------------------------------------
// -----------------------------------------------------------------------------------
// -----------------------------------------------------------------------------------

