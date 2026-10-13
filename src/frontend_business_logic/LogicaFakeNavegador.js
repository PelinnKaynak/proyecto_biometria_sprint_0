// ---------------------------------------------------------------------------
// Fichero:     LogicaFakeNavegador.js
// Descripción: Lógica fake del navegador. Obtiene la última medición con
//              GET /recuperarUltimaMedicion. No accede a la base de datos ni
//              a la lógica del negocio. Diseño en doc/frontend_business_logic_design.md.
// Autora:      Pelin Kaynak
// Fecha:       30/09/2026
// Copyright:   (c) 2026 Pelin Kaynak - Proyecto Biometría y Medio Ambiente, EPSG-UPV
// ---------------------------------------------------------------------------

// LogicaFakeNavegador class: the browser's fake logic. It does not read the database itself:
// it asks the server with GET /recuperarUltimaMedicion.

class LogicaFakeNavegador {
  // -------------------------------------------------------------------------
  // recuperarUltimaMedicion() --> Medicion | vacío
  //
  // Medicion = ( UUID: Text, Timestamp: Text, TipoMedicion: N, ValorMedicion: Z )
  // (misma firma que LogicaDeNegocio.recuperarUltimaMedicion() del backend)
  // Hace GET /recuperarUltimaMedicion al mismo servidor que sirvió la página.
  // 200 -> la medición · 404 -> null (todavía no hay mediciones)
  // otro código o fallo de red -> Error
  // What it does: asks the server for the last measurement. Returns the measurement (200),
  //               null if there is none yet (404), or throws an error (other code or no connection).
  // -------------------------------------------------------------------------
  async recuperarUltimaMedicion() {
    let respuesta
    try {
      respuesta = await fetch('/recuperarUltimaMedicion')
    } catch (errorDeRed) {
      throw new Error('No se puede conectar con el servidor')
    }

    if (respuesta.status === 200) {
      return await respuesta.json()
    }
    if (respuesta.status === 404) {
      return null
    }
    throw new Error(`El servidor respondió con el código ${respuesta.status}`)
  }
}

if (typeof module !== 'undefined' && module.exports) {
  module.exports = { LogicaFakeNavegador }
}
