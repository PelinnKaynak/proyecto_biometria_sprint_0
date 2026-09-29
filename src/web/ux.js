// ---------------------------------------------------------------------------
// Fichero:     ux.js
// Descripción: Comportamiento de la UX del navegador. Solo habla con la
//              lógica fake (nunca con la API REST directamente).
//              Diseño en doc/web_design.md.
// Autora:      Pelin Kaynak
// Fecha:       29/09/2026
// Copyright:   (c) 2026 Pelin Kaynak - Proyecto Biometría y Medio Ambiente, EPSG-UPV
// ---------------------------------------------------------------------------

var laLogicaFake = new LogicaFakeNavegador('')

var NOMBRES_TIPO_MEDICION = { 11: 'CO2', 12: 'Temperatura', 13: 'Ruido' }

// ---------------------------------------------------------------------------
// botonActualizarPulsado() -->
// ---------------------------------------------------------------------------
async function botonActualizarPulsado() {
  try {
    const medicion = await laLogicaFake.recuperarUltimaMedicion()
    if (medicion === null) {
      mostrarError('Todavía no hay mediciones')
      return
    }
    mostrarMedicion(medicion)
  } catch (error) {
    mostrarError(error.message)
  }
}

// ---------------------------------------------------------------------------
// medicion: Medicion --> mostrarMedicion() -->
// ---------------------------------------------------------------------------
function mostrarMedicion(medicion) {
  const nombreTipo = NOMBRES_TIPO_MEDICION[medicion.tipoMedicion] || 'desconocido'

  document.getElementById('uuid').textContent = medicion.uuid
  document.getElementById('timestamp').textContent = medicion.timestamp
  document.getElementById('tipoMedicion').textContent = `${medicion.tipoMedicion} (${nombreTipo})`
  document.getElementById('valorMedicion').textContent = medicion.valorMedicion

  document.getElementById('mensajeError').hidden = true
}

// ---------------------------------------------------------------------------
// mensaje: Text --> mostrarError() -->
// ---------------------------------------------------------------------------
function mostrarError(mensaje) {
  const parrafoError = document.getElementById('mensajeError')
  parrafoError.textContent = mensaje
  parrafoError.hidden = false
}

document.getElementById('botonActualizar').addEventListener('click', botonActualizarPulsado)
botonActualizarPulsado()
