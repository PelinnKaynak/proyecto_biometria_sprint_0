// ---------------------------------------------------------------------------
// Fichero:     LogicaFakeTelefonoTest.java
// Descripción: Tests JUnit de las partes de LogicaFakeTelefono que no
//              necesitan red: el JSON enviado y la interpretación del código
//              de respuesta (diseño en doc/android_design.md).
// Autora:      Pelin Kaynak
// Fecha:       29/09/2026
// Copyright:   (c) 2026 Pelin Kaynak - Proyecto Biometría y Medio Ambiente, EPSG-UPV
// ---------------------------------------------------------------------------
package org.jordi.prueba2025;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.json.JSONObject;
import org.junit.Test;

public class LogicaFakeTelefonoTest {

    @Test
    public void elCuerpoJSONTieneLasTresClavesYNoTieneTimestamp() throws Exception {
        String cuerpo = LogicaFakeTelefono.crearCuerpoJSON("EPSG-GTI-PROY-3A", 11, 123);
        JSONObject json = new JSONObject(cuerpo);

        assertEquals("EPSG-GTI-PROY-3A", json.getString("uuid"));
        assertEquals(11, json.getInt("tipoMedicion"));
        assertEquals(123, json.getInt("valorMedicion"));
        assertEquals(3, json.length());
        assertFalse(json.has("timestamp"));
    }

    @Test
    public void soloEl201EsRespuestaCorrecta() {
        assertTrue(LogicaFakeTelefono.esRespuestaCorrecta(201));
        assertFalse(LogicaFakeTelefono.esRespuestaCorrecta(400));
        assertFalse(LogicaFakeTelefono.esRespuestaCorrecta(404));
        assertFalse(LogicaFakeTelefono.esRespuestaCorrecta(500));
    }
}
