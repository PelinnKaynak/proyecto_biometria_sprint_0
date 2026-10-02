// ---------------------------------------------------------------------------
// Fichero:     ux.js
// Descripción: Comportamiento de la UX del navegador. Solo habla con la
//              lógica fake (nunca con la API REST directamente).
//              Diseño en doc/web_design.md.
// Autora:      Pelin Kaynak
// Fecha:       30/09/2026
// Copyright:   (c) 2026 Pelin Kaynak - Proyecto Biometría y Medio Ambiente, EPSG-UPV
// ---------------------------------------------------------------------------

var laLogicaFake = new LogicaFakeNavegador()

// ---------------------------------------------------------------------------
// botonActualizarPulsado() -->
//
// Pide la última medición a la lógica fake y la muestra (o muestra el error).
// ---------------------------------------------------------------------------
async function botonActualizarPulsado() {
  try {
    const medicion = await laLogicaFake.recuperarMedicion()
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
// medicion: JSON( UUID: Text, Timestamp: Text, TipoMedicion: N, ValorMedicion: Z )
//   --> mostrarMedicion() -->
// ---------------------------------------------------------------------------
function mostrarMedicion(medicion) {
  document.getElementById('UUID').textContent = medicion.UUID
  document.getElementById('Timestamp').textContent = medicion.Timestamp
  document.getElementById('TipoMedicion').textContent = medicion.TipoMedicion
  document.getElementById('ValorMedicion').textContent = medicion.ValorMedicion

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
