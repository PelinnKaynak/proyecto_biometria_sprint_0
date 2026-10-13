// ---------------------------------------------------------------------------
// Fichero:     LogicaDeNegocio.test.js
// Descripción: Tests automáticos (mocha) de la clase LogicaDeNegocio
//              (diseño en doc/business_logic_design.md).
// Autora:      Pelin Kaynak
// Fecha:       29/09/2026
// Copyright:   (c) 2026 Pelin Kaynak - Proyecto Biometría y Medio Ambiente, EPSG-UPV
// ---------------------------------------------------------------------------

const assert = require('assert')
const fs = require('fs')
const os = require('os')
const path = require('path')
const { LogicaDeNegocio } = require('../LogicaDeNegocio')

const ID_SENSOR = 1
const CO2 = 11
const TEMPERATURA = 12

describe('business_logic: LogicaDeNegocio', function () {
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

  it('guarda una medición y la recupera como la última, con Timestamp generado', async function () {
    const antes = new Date()
    await laLogica.guardarMedicion(ID_SENSOR, CO2, 123)

    const medicion = await laLogica.recuperarUltimaMedicion()
    assert.strictEqual(medicion.IdSensor, ID_SENSOR)
    assert.strictEqual(medicion.TipoMedicion, CO2)
    assert.strictEqual(medicion.ValorMedicion, 123)

    const momento = new Date(medicion.Timestamp)
    assert.ok(!isNaN(momento), 'Timestamp debe ser una fecha ISO válida')
    assert.ok(momento >= new Date(antes.getTime() - 1000), 'Timestamp debe ser el momento de guardar')
  })

  it('con varias mediciones devuelve la más reciente', async function () {
    await laLogica.guardarMedicion(ID_SENSOR, CO2, 100)
    await laLogica.guardarMedicion(ID_SENSOR, CO2, 200)
    await laLogica.guardarMedicion(ID_SENSOR, TEMPERATURA, -12)

    const medicion = await laLogica.recuperarUltimaMedicion()
    assert.strictEqual(medicion.TipoMedicion, TEMPERATURA)
    assert.strictEqual(medicion.ValorMedicion, -12)
  })

  it('devuelve solo los campos de Medicion', async function () {
    await laLogica.guardarMedicion(ID_SENSOR, CO2, 123)
    const medicion = await laLogica.recuperarUltimaMedicion()
    assert.deepStrictEqual(Object.keys(medicion).sort(), ['IdSensor', 'Timestamp', 'TipoMedicion', 'ValorMedicion'])
  })

  it('rechaza un tipo de medición no válido', async function () {
    await assert.rejects(laLogica.guardarMedicion(ID_SENSOR, 99, 1), /TipoMedicion no válido/)
    assert.strictEqual(await laLogica.recuperarUltimaMedicion(), null)
  })

  it('rechaza datos que faltan', async function () {
    await assert.rejects(laLogica.guardarMedicion(undefined, CO2, 1), /IdSensor/)
    await assert.rejects(laLogica.guardarMedicion(-1, CO2, 1), /IdSensor/)
    await assert.rejects(laLogica.guardarMedicion(ID_SENSOR, CO2, undefined), /ValorMedicion/)
  })
})
