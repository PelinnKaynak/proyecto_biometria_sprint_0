// ---------------------------------------------------------------------------
// Fichero:     LogicaFakeNavegador.test.js
// Descripción: Tests automáticos (mocha) de la lógica fake del navegador
//              contra el servidor REST real (diseño en doc/web_design.md).
// Autora:      Pelin Kaynak
// Fecha:       30/09/2026
// Copyright:   (c) 2026 Pelin Kaynak - Proyecto Biometría y Medio Ambiente, EPSG-UPV
// ---------------------------------------------------------------------------

const assert = require('assert')
const fs = require('fs')
const os = require('os')
const path = require('path')
const { arrancarServidor, pararServidor } = require('../../api/servidorREST')
const { LogicaFakeNavegador } = require('../logicaFake/LogicaFakeNavegador')

const fetchOriginal = globalThis.fetch

// ---------------------------------------------------------------------------
// urlBase: Text --> simularMismoOrigen() -->
//
// En el navegador la URL relativa /recuperarUltimaMedicion va al servidor que
// sirvió la página. En Node se simula anteponiendo urlBase a la ruta.
// ---------------------------------------------------------------------------
function simularMismoOrigen(urlBase) {
  globalThis.fetch = (ruta, opciones) => fetchOriginal(urlBase + ruta, opciones)
}

describe('web: LogicaFakeNavegador', function () {
  let urlBase
  let ficheroTemporal

  beforeEach(async function () {
    ficheroTemporal = path.join(os.tmpdir(), `test-web-${Date.now()}-${Math.random()}.db`)
    const servidor = await arrancarServidor(0, ficheroTemporal)
    urlBase = `http://localhost:${servidor.address().port}`
    simularMismoOrigen(urlBase)
  })

  afterEach(async function () {
    globalThis.fetch = fetchOriginal
    await pararServidor()
    fs.rmSync(ficheroTemporal, { force: true })
  })

  it('recuperarMedicion() hace GET /recuperarUltimaMedicion', async function () {
    const rutasPedidas = []
    const fetchQueEspia = globalThis.fetch
    globalThis.fetch = (ruta, opciones) => {
      rutasPedidas.push(ruta)
      return fetchQueEspia(ruta, opciones)
    }
    await new LogicaFakeNavegador().recuperarMedicion()
    assert.deepStrictEqual(rutasPedidas, ['/recuperarUltimaMedicion'])
  })

  it('devuelve null cuando todavía no hay mediciones (404)', async function () {
    assert.strictEqual(await new LogicaFakeNavegador().recuperarMedicion(), null)
  })

  it('devuelve el JSON con UUID, Timestamp, TipoMedicion y ValorMedicion', async function () {
    await fetchOriginal(`${urlBase}/guardarMedicion`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ UUID: 'EPSG-GTI-PROY-3A', TipoMedicion: 11, ValorMedicion: 123 }),
    })

    const medicion = await new LogicaFakeNavegador().recuperarMedicion()

    assert.strictEqual(medicion.UUID, 'EPSG-GTI-PROY-3A')
    assert.strictEqual(medicion.TipoMedicion, 11)
    assert.strictEqual(medicion.ValorMedicion, 123)
    assert.ok(medicion.Timestamp)
  })

  it('produce un error cuando el servidor no está disponible', async function () {
    simularMismoOrigen('http://localhost:1') // puerto sin servidor
    await assert.rejects(new LogicaFakeNavegador().recuperarMedicion(), /No se puede conectar/)
  })

  it('produce un error si el servidor responde con otro código (500)', async function () {
    globalThis.fetch = async () => ({ status: 500 })
    await assert.rejects(new LogicaFakeNavegador().recuperarMedicion(), /código 500/)
  })
})
