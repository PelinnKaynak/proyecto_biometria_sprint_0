// ---------------------------------------------------------------------------
// Fichero:     LogicaFakeTelefonoTest.java
// Descripción: Tests JUnit de LogicaFakeTelefono: creación del JSON enviado
//              y respuestas de éxito y error del servidor
//              (diseño en doc/client_business_logic_design.md).
// Autora:      Pelin Kaynak
// Fecha:       30/09/2026
// Copyright:   (c) 2026 Pelin Kaynak - Proyecto Biometría y Medio Ambiente, EPSG-UPV
// ---------------------------------------------------------------------------
package com.pelinkaynak.biometria;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.json.JSONObject;
import org.junit.Test;

public class LogicaFakeTelefonoTest {

    // ------------------------------------------------------------
    // elJSONTieneUUIDTipoMedicionYValorMedicionSinTimestamp() -->
    // What it does: checks the JSON has UUID, TipoMedicion and ValorMedicion and no Timestamp.
    // ------------------------------------------------------------
    @Test
    public void elJSONTieneUUIDTipoMedicionYValorMedicionSinTimestamp() throws Exception {
        JSONObject json = new JSONObject(LogicaFakeTelefono.crearJSON("EPSG-GTI-PROY-3A", 11, 123));

        assertEquals("EPSG-GTI-PROY-3A", json.getString("UUID"));
        assertEquals(11, json.getInt("TipoMedicion"));
        assertEquals(123, json.getInt("ValorMedicion"));
        assertEquals(3, json.length());
        assertFalse(json.has("Timestamp"));
    }

    // ------------------------------------------------------------
    // elJSONAdmiteValoresNegativos() -->
    // What it does: checks a negative value (-12) is kept in the JSON.
    // ------------------------------------------------------------
    @Test
    public void elJSONAdmiteValoresNegativos() throws Exception {
        JSONObject json = new JSONObject(LogicaFakeTelefono.crearJSON("EPSG-GTI-PROY-3A", 12, -12));
        assertEquals(-12, json.getInt("ValorMedicion"));
    }

    // ------------------------------------------------------------
    // el201EsExitoYLosDemasCodigosSonError() -->
    // What it does: checks only 201 counts as success (400, 404, 500 and 0 are errors).
    // ------------------------------------------------------------
    @Test
    public void el201EsExitoYLosDemasCodigosSonError() {
        assertTrue(LogicaFakeTelefono.esExito(201));
        assertFalse(LogicaFakeTelefono.esExito(400));
        assertFalse(LogicaFakeTelefono.esExito(404));
        assertFalse(LogicaFakeTelefono.esExito(500));
        assertFalse(LogicaFakeTelefono.esExito(0)); // sin conexión (codigoRespuesta no se rellena)
    }

    // ------------------------------------------------------------
    // ResultadoApuntado: doble de RespuestaGuardarMedicion que apunta qué se llamó.
    // ------------------------------------------------------------
    static class ResultadoApuntado implements LogicaFakeTelefono.RespuestaGuardarMedicion {
        boolean guardada = false;
        String mensajeError = null;

        // ------------------------------------------------------------
        // guardada() -->
        // What it does: remembers that guardada() was called.
        // ------------------------------------------------------------
        @Override
        public void guardada() {
            guardada = true;
        }

        // ------------------------------------------------------------
        // mensaje: Text --> error() -->
        // What it does: remembers the error message.
        // ------------------------------------------------------------
        @Override
        public void error(String mensaje) {
            mensajeError = mensaje;
        }
    }

    // ------------------------------------------------------------
    // con201SeLlamaAGuardada() -->
    // What it does: checks a 201 answer calls guardada() and not error().
    // ------------------------------------------------------------
    @Test
    public void con201SeLlamaAGuardada() {
        ResultadoApuntado resultado = new ResultadoApuntado();
        LogicaFakeTelefono.notificarResultado(201, "{\"resultado\":\"ok\"}", resultado);
        assertTrue(resultado.guardada);
        assertNull(resultado.mensajeError);
    }

    // ------------------------------------------------------------
    // conOtroCodigoSeLlamaAError() -->
    // What it does: checks 400 and 0 (no connection) call error() with the code, and not guardada().
    // ------------------------------------------------------------
    @Test
    public void conOtroCodigoSeLlamaAError() {
        ResultadoApuntado resultado = new ResultadoApuntado();
        LogicaFakeTelefono.notificarResultado(400, "{\"error\":\"datos\"}", resultado);
        assertFalse(resultado.guardada);
        assertTrue(resultado.mensajeError.contains("400"));

        ResultadoApuntado sinConexion = new ResultadoApuntado();
        LogicaFakeTelefono.notificarResultado(0, "", sinConexion);
        assertFalse(sinConexion.guardada);
        assertTrue(sinConexion.mensajeError.contains("0"));
    }
}
