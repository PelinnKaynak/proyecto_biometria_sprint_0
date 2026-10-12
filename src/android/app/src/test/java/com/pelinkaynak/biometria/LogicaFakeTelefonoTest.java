// ---------------------------------------------------------------------------
// Fichero:     LogicaFakeTelefonoTest.java
// Descripción: Tests JUnit de LogicaFakeTelefono: creación del JSON enviado
//              y respuestas de éxito y error del servidor
//              (diseño en doc/android_design.md).
// Autora:      Pelin Kaynak
// Fecha:       30/09/2026
// Copyright:   (c) 2026 Pelin Kaynak - Proyecto Biometría y Medio Ambiente, EPSG-UPV
// ---------------------------------------------------------------------------
package com.pelinkaynak.biometria;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.json.JSONObject;
import org.junit.Test;

public class LogicaFakeTelefonoTest {

    @Test
    public void elJSONTieneUUIDTipoMedicionYValorMedicionSinTimestamp() throws Exception {
        JSONObject json = new JSONObject(LogicaFakeTelefono.crearJSON("EPSG-GTI-PROY-3A", 11, 123));

        assertEquals("EPSG-GTI-PROY-3A", json.getString("UUID"));
        assertEquals(11, json.getInt("TipoMedicion"));
        assertEquals(123, json.getInt("ValorMedicion"));
        assertEquals(3, json.length());
        assertFalse(json.has("Timestamp"));
    }

    @Test
    public void elJSONAdmiteValoresNegativos() throws Exception {
        JSONObject json = new JSONObject(LogicaFakeTelefono.crearJSON("EPSG-GTI-PROY-3A", 12, -12));
        assertEquals(-12, json.getInt("ValorMedicion"));
    }

    @Test
    public void el201EsExitoYLosDemasCodigosSonError() {
        assertTrue(LogicaFakeTelefono.esExito(201));
        assertFalse(LogicaFakeTelefono.esExito(400));
        assertFalse(LogicaFakeTelefono.esExito(404));
        assertFalse(LogicaFakeTelefono.esExito(500));
        assertFalse(LogicaFakeTelefono.esExito(-1)); // sin conexión
    }
}
