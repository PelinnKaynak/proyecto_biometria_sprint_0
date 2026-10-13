// ---------------------------------------------------------------------------
// Fichero:     LogicaFakeTelefono.java
// Descripción: Lógica fake del teléfono. guardarMedicion() crea un JSON con
//              IdSensor, TipoMedicion y ValorMedicion y lo envía con
//              POST /guardarMedicion al servidor REST. No accede a la base de
//              datos ni a la lógica del negocio. Diseño en doc/client_business_logic_design.md.
// Autora:      Pelin Kaynak
// Fecha:       30/09/2026
// Copyright:   (c) 2026 Pelin Kaynak - Proyecto Biometría y Medio Ambiente, EPSG-UPV
// ---------------------------------------------------------------------------
package com.pelinkaynak.biometria;

import org.json.JSONException;
import org.json.JSONObject;

// LogicaFakeTelefono class: the phone's fake logic. It does not save anything itself:
// it sends the measurement to the server with POST /guardarMedicion.
public class LogicaFakeTelefono {

    // Dirección del servidor REST:
    //   emulador       -> http://10.0.2.2:8080 (el ordenador que ejecuta el emulador)
    //   teléfono real  -> http://<IPv4 del ordenador en la misma Wi-Fi>:8080
    private static final String URL_SERVIDOR = "http://172.20.10.2:8080";

    private static final int CODIGO_EXITO = 201;

    // -----------------------------------------------------------------------
    // Callback de guardarMedicion() (se omite en la notación). Igual que en el
    // backend: si todo va bien no devuelve nada (guardada()); si falla, avisa
    // del error con un texto (error()), como la excepción del backend.
    // -----------------------------------------------------------------------
    public interface RespuestaGuardarMedicion {
        void guardada();
        void error(String mensaje);
    }

    // -----------------------------------------------------------------------
    // idSensor: N, tipoMedicion: N, valorMedicion: Z --> guardarMedicion() -->
    // (misma firma que LogicaDeNegocio.guardarMedicion() del backend)
    //
    // Crea el JSON (sin Timestamp: lo genera el backend) y hace
    // POST /guardarMedicion. Al terminar llama a guardada() o a error().
    // What it does: makes the JSON and sends it with POST to the server.
    //               Calls guardada() if the server answered 201, error() if not.
    // -----------------------------------------------------------------------
    public void guardarMedicion(int idSensor, int tipoMedicion, int valorMedicion,
                                final RespuestaGuardarMedicion respuesta) {
        String cuerpo;
        try {
            cuerpo = crearJSON(idSensor, tipoMedicion, valorMedicion);
        } catch (JSONException error) {
            respuesta.error("no se pudo crear el JSON: " + error.getMessage());
            return;
        }

        new PeticionarioREST().hacerPeticionREST(
                "POST",
                URL_SERVIDOR + "/guardarMedicion",
                cuerpo,
                (codigo, cuerpoRespuesta) -> notificarResultado(codigo, cuerpoRespuesta, respuesta));
    }

    // -----------------------------------------------------------------------
    // codigo: Z, cuerpoRespuesta: Text --> notificarResultado() -->         (privado, estático)
    // (el callback respuesta se omite en la notación)
    // What it does: calls guardada() if the code is 201, or error() with the code and body if not.
    // -----------------------------------------------------------------------
    static void notificarResultado(int codigo, String cuerpoRespuesta,
                                   RespuestaGuardarMedicion respuesta) {
        if (esExito(codigo)) {
            respuesta.guardada();
        } else {
            respuesta.error("el servidor respondió " + codigo + " " + cuerpoRespuesta);
        }
    }

    // -----------------------------------------------------------------------
    // idSensor: N, tipoMedicion: N, valorMedicion: Z --> crearJSON() --> Text     (privado, estático)
    // What it does: builds {"IdSensor":..., "TipoMedicion":..., "ValorMedicion":...} (no Timestamp).
    // -----------------------------------------------------------------------
    static String crearJSON(int idSensor, int tipoMedicion, int valorMedicion)
            throws JSONException {
        JSONObject json = new JSONObject();
        json.put("IdSensor", idSensor);
        json.put("TipoMedicion", tipoMedicion);
        json.put("ValorMedicion", valorMedicion);
        return json.toString();
    }

    // -----------------------------------------------------------------------
    // codigo: Z --> esExito() --> B                                            (privado, estático)
    // What it does: returns true only if the answer code is 201 (saved).
    // -----------------------------------------------------------------------
    static boolean esExito(int codigo) {
        return codigo == CODIGO_EXITO;
    }
}
