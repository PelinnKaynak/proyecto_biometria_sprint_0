// ---------------------------------------------------------------------------
// Fichero:     LogicaFakeNavegador.test.js
// Descripción: Tests automáticos (mocha) de la lógica fake del navegador
//              contra el servidor REST real (diseño en doc/web_design.md).
// Autora:      Pelin Kaynak
// Fecha:       29/09/2026
// Copyright:   (c) 2026 Pelin Kaynak - Proyecto Biometría y Medio Ambiente, EPSG-UPV
// ---------------------------------------------------------------------------

const assert = require('assert')
const fs = require('fs')
const os = require('os')
const path = require('path')
const { arrancarServidor, pararServidor } = require('../../api/servidorREST')
const { LogicaFakeNavegador } = require('../logicaFake/LogicaFakeNavegador')

describe('web: LogicaFakeNavegador', function () {
  let urlBase
  let ficheroTemporal

  beforeEach(async function () {
    ficheroTemporal = path.join(os.tmpdir(), `test-web-${Date.now()}-${Math.random()}.db`)
    const servidor = await arrancarServidor(0, ficheroTemporal)
    urlBase = `http://localhost:${servidor.address().port}`
  })

  afterEach(async function () {
    await pararServidor()
    fs.rmSync(ficheroTemporal, { force: true })
  })

  it('devuelve null cuando todavía no hay mediciones (404)', async function () {
    const laLogicaFake = new LogicaFakeNavegador(urlBase)
    assert.strictEqual(await laLogicaFake.recuperarUltimaMedicion(), null)
  })

  it('devuelve la última medición con todos sus campos', async function () {
    await fetch(`${urlBase}/guardarMedicion`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ uuid: 'EPSG-GTI-PROY-3A', tipoMedicion: 11, valorMedicion: 123 }),
    })

    const laLogicaFake = new LogicaFakeNavegador(urlBase)
    const medicion = await laLogicaFake.recuperarUltimaMedicion()

    assert.strictEqual(medicion.uuid, 'EPSG-GTI-PROY-3A')
    assert.strictEqual(medicion.tipoMedicion, 11)
    assert.strictEqual(medicion.valorMedicion, 123)
    assert.ok(medicion.timestamp)
  })

  it('produce un error cuando el servidor no está disponible', async function () {
    const laLogicaFake = new LogicaFakeNavegador('http://localhost:1') // puerto sin servidor
    await assert.rejects(laLogicaFake.recuperarUltimaMedicion(), /No se puede conectar/)
  })
})
