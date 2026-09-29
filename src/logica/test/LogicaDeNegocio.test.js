// ---------------------------------------------------------------------------
// Fichero:     LogicaDeNegocio.test.js
// Descripción: Tests automáticos (mocha) de la clase LogicaDeNegocio
//              (diseño en doc/logica_design.md).
// Autora:      Pelin Kaynak
// Fecha:       29/09/2026
// Copyright:   (c) 2026 Pelin Kaynak - Proyecto Biometría y Medio Ambiente, EPSG-UPV
// ---------------------------------------------------------------------------

const assert = require('assert')
const fs = require('fs')
const os = require('os')
const path = require('path')
const { LogicaDeNegocio } = require('../LogicaDeNegocio')

const UUID_BEACON = 'EPSG-GTI-PROY-3A'
const CO2 = 11
const TEMPERATURA = 12

describe('logica: LogicaDeNegocio', function () {
  let laLogica
  let ficheroTemporal

  beforeEach(function () {
    ficheroTemporal = path.join(os.tmpdir(), `test-logica-${Date.now()}-${Math.random()}.db`)
    laLogica = new LogicaDeNegocio(ficheroTemporal)
  })

  afterEach(async function () {
    await laLogica.cerrar()
    fs.rmSync(ficheroTemporal, { force: true })
  })

  it('abre la base de datos y la tabla empieza vacía (recuperarUltimaMedicion -> null)', async function () {
    assert.strictEqual(await laLogica.recuperarUltimaMedicion(), null)
    assert.ok(fs.existsSync(ficheroTemporal), 'debe crearse el fichero de la base de datos')
  })

  it('guarda una medición y la recupera como la última, con timestamp generado', async function () {
    const antes = new Date()
    await laLogica.guardarMedicion(UUID_BEACON, CO2, 123)

    const medicion = await laLogica.recuperarUltimaMedicion()
    assert.strictEqual(medicion.uuid, UUID_BEACON)
    assert.strictEqual(medicion.tipoMedicion, CO2)
    assert.strictEqual(medicion.valorMedicion, 123)

    const momento = new Date(medicion.timestamp)
    assert.ok(!isNaN(momento), 'timestamp debe ser una fecha ISO válida')
    assert.ok(momento >= new Date(antes.getTime() - 1000), 'timestamp debe ser el momento de guardar')
  })

  it('con varias mediciones devuelve la más reciente', async function () {
    await laLogica.guardarMedicion(UUID_BEACON, CO2, 100)
    await laLogica.guardarMedicion(UUID_BEACON, CO2, 200)
    await laLogica.guardarMedicion(UUID_BEACON, TEMPERATURA, -12)

    const medicion = await laLogica.recuperarUltimaMedicion()
    assert.strictEqual(medicion.tipoMedicion, TEMPERATURA)
    assert.strictEqual(medicion.valorMedicion, -12)
  })

  it('no devuelve el id (solo los campos de Medicion)', async function () {
    await laLogica.guardarMedicion(UUID_BEACON, CO2, 123)
    const medicion = await laLogica.recuperarUltimaMedicion()
    assert.deepStrictEqual(Object.keys(medicion).sort(), ['timestamp', 'tipoMedicion', 'uuid', 'valorMedicion'])
  })

  it('rechaza un tipo de medición no válido', async function () {
    await assert.rejects(laLogica.guardarMedicion(UUID_BEACON, 99, 1), /tipoMedicion no válido/)
    assert.strictEqual(await laLogica.recuperarUltimaMedicion(), null)
  })

  it('rechaza datos que faltan', async function () {
    await assert.rejects(laLogica.guardarMedicion('', CO2, 1), /uuid/)
    await assert.rejects(laLogica.guardarMedicion(UUID_BEACON, CO2, undefined), /valorMedicion/)
  })
})
