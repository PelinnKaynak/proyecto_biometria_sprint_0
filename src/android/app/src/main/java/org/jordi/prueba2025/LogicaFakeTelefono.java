// ---------------------------------------------------------------------------
// Fichero:     LogicaFakeTelefono.java
// Descripción: Lógica fake del teléfono. Ofrece guardarMedicion() con el
//              mismo nombre que la lógica del negocio, pero por dentro hace
//              POST /guardarMedicion al servidor REST. No accede a la base de
//              datos. Diseño en doc/android_design.md.
// Autora:      Pelin Kaynak
// Fecha:       29/09/2026
// Copyright:   (c) 2026 Pelin Kaynak - Proyecto Biometría y Medio Ambiente, EPSG-UPV
// ---------------------------------------------------------------------------
package org.jordi.prueba2025;

import org.json.JSONException;
import org.json.JSONObject;

public class LogicaFakeTelefono {

    private static final int CODIGO_CREADO = 201;

    // -----------------------------------------------------------------------
    // Resultado asíncrono de guardarMedicion(): B (true si se guardó)
    // -----------------------------------------------------------------------
    public interface ResultadoGuardarMedicion {
        void callback(boolean seHaGuardado);
    }

    private final String urlServidor;

    // -----------------------------------------------------------------------
    // urlServidor: Text --> LogicaFakeTelefono() -->
    // -----------------------------------------------------------------------
    public LogicaFakeTelefono(String urlServidor) {
        this.urlServidor = urlServidor;
    }

    // -----------------------------------------------------------------------
    // uuid: Text, tipoMedicion: N, valorMedicion: Z --> guardarMedicion() --> B
    //
    // Envía POST {urlServidor}/guardarMedicion. El resultado (B) llega por
    // el callback cuando responde el servidor.
    // -----------------------------------------------------------------------
    public void guardarMedicion(String uuid, int tipoMedicion, int valorMedicion,
                                final ResultadoGuardarMedicion resultado) {
        String cuerpo;
        try {
            cuerpo = crearCuerpoJSON(uuid, tipoMedicion, valorMedicion);
        } catch (JSONException error) {
            resultado.callback(false);
            return;
        }

        new PeticionarioREST().hacerPeticionREST(
                "POST",
                urlServidor + "/guardarMedicion",
                cuerpo,
                (codigo, cuerpoRespuesta) -> resultado.callback(esRespuestaCorrecta(codigo)));
    }

    // -----------------------------------------------------------------------
    // uuid: Text, tipoMedicion: N, valorMedicion: Z --> crearCuerpoJSON() --> Text
    //
    // No incluye timestamp: lo genera el servidor.
    // -----------------------------------------------------------------------
    public static String crearCuerpoJSON(String uuid, int tipoMedicion, int valorMedicion)
            throws JSONException {
        JSONObject json = new JSONObject();
        json.put("uuid", uuid);
        json.put("tipoMedicion", tipoMedicion);
        json.put("valorMedicion", valorMedicion);
        return json.toString();
    }

    // -----------------------------------------------------------------------
    // codigo: Z --> esRespuestaCorrecta() --> B
    // -----------------------------------------------------------------------
    public static boolean esRespuestaCorrecta(int codigo) {
        return codigo == CODIGO_CREADO;
    }
}
