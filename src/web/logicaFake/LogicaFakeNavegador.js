// ---------------------------------------------------------------------------
// Fichero:     LogicaFakeNavegador.js
// Descripción: Lógica fake del navegador. Ofrece a la UX los mismos nombres
//              de métodos que la lógica del negocio, pero por dentro hace
//              peticiones a la API REST. Diseño en doc/web_design.md.
// Autora:      Pelin Kaynak
// Fecha:       29/09/2026
// Copyright:   (c) 2026 Pelin Kaynak - Proyecto Biometría y Medio Ambiente, EPSG-UPV
// ---------------------------------------------------------------------------

class LogicaFakeNavegador {
  // -------------------------------------------------------------------------
  // urlServidor: Text --> LogicaFakeNavegador() -->
  //
  // urlServidor = "" significa el mismo origen que sirvió la página.
  // -------------------------------------------------------------------------
  constructor(urlServidor = '') {
    this.urlServidor = urlServidor
  }

  // -------------------------------------------------------------------------
  // recuperarUltimaMedicion() --> Medicion | vacío
  //                           --> error: Text
  //
  // Medicion = ( uuid: Text, timestamp: Text, tipoMedicion: N, valorMedicion: Z )
  // 200 -> Medicion · 404 -> null · otro código o fallo de red -> Error
  // -------------------------------------------------------------------------
  async recuperarUltimaMedicion() {
    let respuesta
    try {
      respuesta = await fetch(`${this.urlServidor}/recuperarUltimaMedicion`)
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
