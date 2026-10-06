// ---------------------------------------------------------------------------
// Fichero:     LogicaFakeTelefono.java
// Descripción: Lógica fake del teléfono. guardarMedicion() crea un JSON con
//              UUID, TipoMedicion y ValorMedicion y lo envía con
//              POST /guardarMedicion al servidor REST. No accede a la base de
//              datos ni a la lógica del negocio. Diseño en doc/android_design.md.
// Autora:      Pelin Kaynak
// Fecha:       30/09/2026
// Copyright:   (c) 2026 Pelin Kaynak - Proyecto Biometría y Medio Ambiente, EPSG-UPV
// ---------------------------------------------------------------------------
package org.jordi.prueba2025;

import org.json.JSONException;
import org.json.JSONObject;

public class LogicaFakeTelefono {

    // Dirección del servidor REST:
    //   emulador       -> http://10.0.2.2:8080 (el ordenador que ejecuta el emulador)
    //   teléfono real  -> http://<IPv4 del ordenador en la misma Wi-Fi>:8080
    private static final String URL_SERVIDOR = "http://172.20.10.2:8080";

    private static final int CODIGO_EXITO = 201;

    // -----------------------------------------------------------------------
    // Callback con el resultado de guardarMedicion(): T (éxito) / F (error)
    // -----------------------------------------------------------------------
    public interface RespuestaGuardarMedicion {
        void callback(boolean exito);
    }

    // -----------------------------------------------------------------------
    // JSON( UUID: Text, TipoMedicion: N, ValorMedicion: Z ) --> guardarMedicion() -->
    //
    // Crea el JSON (sin Timestamp: lo genera el backend) y hace
    // POST /guardarMedicion. La respuesta del servidor (éxito o error) llega
    // por el callback.
    // -----------------------------------------------------------------------
    public void guardarMedicion(String UUID, int TipoMedicion, int ValorMedicion,
                                final RespuestaGuardarMedicion respuesta) {
        String cuerpo;
        try {
            cuerpo = crearJSON(UUID, TipoMedicion, ValorMedicion);
        } catch (JSONException error) {
            respuesta.callback(false);
            return;
        }

        new PeticionarioREST().hacerPeticionREST(
                "POST",
                URL_SERVIDOR + "/guardarMedicion",
                cuerpo,
                (codigo, cuerpoRespuesta) -> respuesta.callback(esExito(codigo)));
    }

    // -----------------------------------------------------------------------
    // UUID: Text, TipoMedicion: N, ValorMedicion: Z --> crearJSON() --> Text     (privado)
    // -----------------------------------------------------------------------
    static String crearJSON(String UUID, int TipoMedicion, int ValorMedicion)
            throws JSONException {
        JSONObject json = new JSONObject();
        json.put("UUID", UUID);
        json.put("TipoMedicion", TipoMedicion);
        json.put("ValorMedicion", ValorMedicion);
        return json.toString();
    }

    // -----------------------------------------------------------------------
    // codigo: Z --> esExito() --> T/F                                          (privado)
    // -----------------------------------------------------------------------
    static boolean esExito(int codigo) {
        return codigo == CODIGO_EXITO;
    }
}
