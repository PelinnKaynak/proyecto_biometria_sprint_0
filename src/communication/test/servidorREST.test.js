// ---------------------------------------------------------------------------
// Fichero:     servidorREST.test.js
// Descripción: Tests automáticos (mocha) de las rutas del servidor REST con
//              peticiones HTTP reales (diseño en doc/communication_design.md).
// Autora:      Pelin Kaynak
// Fecha:       29/09/2026
// Copyright:   (c) 2026 Pelin Kaynak - Proyecto Biometría y Medio Ambiente, EPSG-UPV
// ---------------------------------------------------------------------------

const assert = require('assert')
const fs = require('fs')
const os = require('os')
const path = require('path')
const { arrancarServidor, pararServidor } = require('../servidorREST')

const MEDICION_CO2 = { IdSensor: 1, TipoMedicion: 11, ValorMedicion: 123 }

// ---------------------------------------------------------------------------
// url: Text, cuerpo: Text --> enviarPOST() --> Response
//
// Response = respuesta HTTP de fetch (tipo externo: status y cuerpo)
// What it does: sends a POST with a JSON body, like the phone does.
// ---------------------------------------------------------------------------
function enviarPOST(url, cuerpo) {
  return fetch(url, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: cuerpo })
}

describe('communication: servidor REST', function () {
  let urlBase
  let ficheroTemporal

  beforeEach(async function () {
    ficheroTemporal = path.join(os.tmpdir(), `test-api-${Date.now()}-${Math.random()}.db`)
    const servidor = await arrancarServidor(0, ficheroTemporal) // puerto 0 = cualquier puerto libre
    urlBase = `http://localhost:${servidor.address().port}`
  })

  afterEach(async function () {
    await pararServidor()
    fs.rmSync(ficheroTemporal, { force: true })
  })

  it('POST /guardarMedicion correcto -> 201', async function () {
    const respuesta = await enviarPOST(`${urlBase}/guardarMedicion`, JSON.stringify(MEDICION_CO2))
    assert.strictEqual(respuesta.status, 201)
    assert.deepStrictEqual(await respuesta.json(), { resultado: 'ok' })
  })

  it('POST /guardarMedicion con datos que faltan -> 400', async function () {
    const respuesta = await enviarPOST(`${urlBase}/guardarMedicion`, JSON.stringify({ IdSensor: 1 }))
    assert.strictEqual(respuesta.status, 400)
    assert.ok((await respuesta.json()).error)
  })

  it('POST /guardarMedicion sin IdSensor (con UUID) o con IdSensor negativo -> 400', async function () {
    const conUUID = JSON.stringify({ UUID: 'EPSG-GTI-PROY-3A', TipoMedicion: 11, ValorMedicion: 123 })
    assert.strictEqual((await enviarPOST(`${urlBase}/guardarMedicion`, conUUID)).status, 400)

    const negativo = JSON.stringify({ ...MEDICION_CO2, IdSensor: -1 })
    assert.strictEqual((await enviarPOST(`${urlBase}/guardarMedicion`, negativo)).status, 400)
  })

  it('POST /guardarMedicion con tipo no válido -> 400', async function () {
    const cuerpo = JSON.stringify({ ...MEDICION_CO2, TipoMedicion: 99 })
    const respuesta = await enviarPOST(`${urlBase}/guardarMedicion`, cuerpo)
    assert.strictEqual(respuesta.status, 400)
  })

  it('POST /guardarMedicion con JSON mal formado -> 400', async function () {
    const respuesta = await enviarPOST(`${urlBase}/guardarMedicion`, '{esto no es json')
    assert.strictEqual(respuesta.status, 400)
    assert.ok((await respuesta.json()).error)
  })

  it('GET /recuperarUltimaMedicion con la tabla vacía -> 404', async function () {
    const respuesta = await fetch(`${urlBase}/recuperarUltimaMedicion`)
    assert.strictEqual(respuesta.status, 404)
    assert.ok((await respuesta.json()).error)
  })

  it('GET /recuperarUltimaMedicion después de un POST -> 200 con la misma medición y Timestamp', async function () {
    await enviarPOST(`${urlBase}/guardarMedicion`, JSON.stringify(MEDICION_CO2))

    const respuesta = await fetch(`${urlBase}/recuperarUltimaMedicion`)
    assert.strictEqual(respuesta.status, 200)

    const medicion = await respuesta.json()
    assert.strictEqual(medicion.IdSensor, MEDICION_CO2.IdSensor)
    assert.strictEqual(medicion.UUID, undefined)
    assert.strictEqual(medicion.TipoMedicion, MEDICION_CO2.TipoMedicion)
    assert.strictEqual(medicion.ValorMedicion, MEDICION_CO2.ValorMedicion)
    assert.ok(!isNaN(new Date(medicion.Timestamp)), 'debe incluir un timestamp válido')
  })

  it('no acepta Timestamp del cliente: lo genera el servidor', async function () {
    const cuerpo = JSON.stringify({ ...MEDICION_CO2, Timestamp: '1999-01-01T00:00:00.000Z' })
    await enviarPOST(`${urlBase}/guardarMedicion`, cuerpo)

    const medicion = await (await fetch(`${urlBase}/recuperarUltimaMedicion`)).json()
    assert.notStrictEqual(medicion.Timestamp, '1999-01-01T00:00:00.000Z')
  })

  it('sirve la GUI en / y la lógica fake en /frontend_business_logic/', async function () {
    const pagina = await fetch(`${urlBase}/`)
    assert.strictEqual(pagina.status, 200)
    assert.match(await pagina.text(), /<h1>Última Medición<\/h1>/)

    const logicaFake = await fetch(`${urlBase}/frontend_business_logic/LogicaFakeNavegador.js`)
    assert.strictEqual(logicaFake.status, 200)
    assert.match(await logicaFake.text(), /class LogicaFakeNavegador/)
  })
})
