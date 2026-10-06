// ---------------------------------------------------------------------------
// Fichero:     ux.js
// Descripción: Comportamiento de la UX del navegador. Solo habla con la
//              lógica fake (nunca con la API REST directamente).
//              Diseño en doc/web_design.md.
// Autora:      Pelin Kaynak
// Fecha:       06/10/2026
// Copyright:   (c) 2026 Pelin Kaynak - Proyecto Biometría y Medio Ambiente, EPSG-UPV
// ---------------------------------------------------------------------------

var laLogicaFake = new LogicaFakeNavegador()

// Nombre que ve el usuario para cada TipoMedicion (códigos del Arduino)
var NOMBRES_TIPO_MEDICION = { 11: 'CO2', 12: 'Temperatura', 13: 'Ruido' }

// Formato de fecha y hora local (ej. 06/10/2026, 11:52:54)
var FORMATO_FECHA = {
  day: '2-digit', month: '2-digit', year: 'numeric',
  hour: '2-digit', minute: '2-digit', second: '2-digit',
}

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
//
// Para el usuario: el Timestamp se muestra como fecha y hora local y el
// TipoMedicion con su nombre (11 -> CO2). Un tipo desconocido se muestra
// con su número.
// ---------------------------------------------------------------------------
function mostrarMedicion(medicion) {
  const fechaLocal = new Date(medicion.Timestamp).toLocaleString('es-ES', FORMATO_FECHA)
  const nombreTipo = NOMBRES_TIPO_MEDICION[medicion.TipoMedicion] || medicion.TipoMedicion

  document.getElementById('UUID').textContent = medicion.UUID
  document.getElementById('Timestamp').textContent = fechaLocal
  document.getElementById('TipoMedicion').textContent = nombreTipo
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
