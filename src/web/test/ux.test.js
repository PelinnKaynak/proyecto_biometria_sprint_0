// ---------------------------------------------------------------------------
// Fichero:     ux.test.js
// Descripción: Tests automáticos (mocha + jsdom) de la UX del navegador. La
//              lógica fake se sustituye por un doble de prueba.
//              Diseño en doc/web_design.md.
// Autora:      Pelin Kaynak
// Fecha:       29/09/2026
// Copyright:   (c) 2026 Pelin Kaynak - Proyecto Biometría y Medio Ambiente, EPSG-UPV
// ---------------------------------------------------------------------------

const assert = require('assert')
const fs = require('fs')
const path = require('path')
const { JSDOM } = require('jsdom')

const HTML = fs.readFileSync(path.join(__dirname, '..', 'index.html'), 'utf8')
const CODIGO_UX = fs.readFileSync(path.join(__dirname, '..', 'ux.js'), 'utf8')

const MEDICION = { uuid: 'EPSG-GTI-PROY-3A', timestamp: '2026-09-29T10:15:30.000Z', tipoMedicion: 11, valorMedicion: 123 }

// ---------------------------------------------------------------------------
// respuestaDelDoble: Medicion | vacío | Error --> crearPagina() --> ( ventana, llamadas: [ N ]_1 )
//
// Crea la página con un doble de LogicaFakeNavegador que cuenta las llamadas
// a recuperarUltimaMedicion() y devuelve (o lanza) respuestaDelDoble.
// ---------------------------------------------------------------------------
function crearPagina(respuestaDelDoble) {
  const ventana = new JSDOM(HTML, { runScripts: 'outside-only' }).window
  const llamadas = { recuperarUltimaMedicion: 0 }

  const esError = respuestaDelDoble instanceof Error
  ventana.respuestaDelDoble = esError ? null : respuestaDelDoble
  ventana.mensajeErrorDelDoble = esError ? respuestaDelDoble.message : null
  ventana.llamadas = llamadas
  ventana.eval(`
    class LogicaFakeNavegador {
      async recuperarUltimaMedicion() {
        window.llamadas.recuperarUltimaMedicion++
        if (window.mensajeErrorDelDoble) throw new Error(window.mensajeErrorDelDoble)
        return window.respuestaDelDoble
      }
    }
    window.LogicaFakeNavegador = LogicaFakeNavegador
  `)
  ventana.eval(CODIGO_UX)
  return { ventana, llamadas }
}

// ---------------------------------------------------------------------------
// esperarPromesasPendientes() -->
// ---------------------------------------------------------------------------
function esperarPromesasPendientes() {
  return new Promise((resolver) => setImmediate(resolver))
}

describe('web: UX del navegador', function () {
  it('al cargar la página llama una vez a recuperarUltimaMedicion()', async function () {
    const { llamadas } = crearPagina(MEDICION)
    await esperarPromesasPendientes()
    assert.strictEqual(llamadas.recuperarUltimaMedicion, 1)
  })

  it('pulsar el botón llama a recuperarUltimaMedicion()', async function () {
    const { ventana, llamadas } = crearPagina(MEDICION)
    await esperarPromesasPendientes()

    ventana.document.getElementById('botonActualizar').click()
    await esperarPromesasPendientes()

    assert.strictEqual(llamadas.recuperarUltimaMedicion, 2)
  })

  it('muestra los datos de la medición en los párrafos correctos', async function () {
    const { ventana } = crearPagina(MEDICION)
    await esperarPromesasPendientes()
    const documento = ventana.document

    assert.strictEqual(documento.getElementById('uuid').textContent, 'EPSG-GTI-PROY-3A')
    assert.strictEqual(documento.getElementById('timestamp').textContent, '2026-09-29T10:15:30.000Z')
    assert.strictEqual(documento.getElementById('tipoMedicion').textContent, '11 (CO2)')
    assert.strictEqual(documento.getElementById('valorMedicion').textContent, '123')
    assert.strictEqual(documento.getElementById('mensajeError').hidden, true)
  })

  it('muestra un mensaje de error al usuario si falla la petición', async function () {
    const { ventana } = crearPagina(new Error('No se puede conectar con el servidor'))
    await esperarPromesasPendientes()
    const parrafoError = ventana.document.getElementById('mensajeError')

    assert.strictEqual(parrafoError.hidden, false)
    assert.strictEqual(parrafoError.textContent, 'No se puede conectar con el servidor')
  })

  it('avisa si todavía no hay mediciones', async function () {
    const { ventana } = crearPagina(null)
    await esperarPromesasPendientes()
    const parrafoError = ventana.document.getElementById('mensajeError')

    assert.strictEqual(parrafoError.hidden, false)
    assert.strictEqual(parrafoError.textContent, 'Todavía no hay mediciones')
  })
})
