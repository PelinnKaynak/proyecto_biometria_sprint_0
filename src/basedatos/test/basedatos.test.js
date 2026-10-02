// ---------------------------------------------------------------------------
// Fichero:     basedatos.test.js
// Descripción: Tests automáticos (mocha) del script crearBaseDeDatos.sql
//              y de los datos de prueba (diseño en doc/basedatos_design.md).
// Autora:      Pelin Kaynak
// Fecha:       29/09/2026
// Copyright:   (c) 2026 Pelin Kaynak - Proyecto Biometría y Medio Ambiente, EPSG-UPV
// ---------------------------------------------------------------------------

const assert = require('assert')
const fs = require('fs')
const os = require('os')
const path = require('path')
const sqlite3 = require('sqlite3')

const SCRIPT_CREACION = path.join(__dirname, '..', 'crearBaseDeDatos.sql')
const SCRIPT_DATOS_PRUEBA = path.join(__dirname, '..', 'datosDePrueba.sql')

// ---------------------------------------------------------------------------
// nombreFichero: Text --> abrir() --> ConexionSQLite
// ---------------------------------------------------------------------------
function abrir(nombreFichero) {
  return new Promise((resolver, rechazar) => {
    const conexion = new sqlite3.Database(nombreFichero, (error) => (error ? rechazar(error) : resolver(conexion)))
  })
}

// ---------------------------------------------------------------------------
// conexion: ConexionSQLite, sql: Text --> ejecutarScript() -->
// ---------------------------------------------------------------------------
function ejecutarScript(conexion, sql) {
  return new Promise((resolver, rechazar) => {
    conexion.exec(sql, (error) => (error ? rechazar(error) : resolver()))
  })
}

// ---------------------------------------------------------------------------
// conexion: ConexionSQLite, sql: Text, parametros: [ ? ] --> consultar() --> [ Fila ]
// ---------------------------------------------------------------------------
function consultar(conexion, sql, parametros = []) {
  return new Promise((resolver, rechazar) => {
    conexion.all(sql, parametros, (error, filas) => (error ? rechazar(error) : resolver(filas)))
  })
}

// ---------------------------------------------------------------------------
// conexion: ConexionSQLite --> cerrar() -->
// ---------------------------------------------------------------------------
function cerrar(conexion) {
  return new Promise((resolver) => conexion.close(() => resolver()))
}

describe('basedatos: script crearBaseDeDatos.sql', function () {
  let conexion
  let ficheroTemporal

  beforeEach(async function () {
    ficheroTemporal = path.join(os.tmpdir(), `test-basedatos-${Date.now()}-${Math.random()}.db`)
    conexion = await abrir(ficheroTemporal)
    await ejecutarScript(conexion, fs.readFileSync(SCRIPT_CREACION, 'utf8'))
  })

  afterEach(async function () {
    await cerrar(conexion)
    fs.rmSync(ficheroTemporal, { force: true })
  })

  it('crea la tabla Mediciones con las columnas del diseño', async function () {
    const columnas = await consultar(conexion, 'PRAGMA table_info(Mediciones)')
    const nombres = columnas.map((columna) => columna.name)
    assert.deepStrictEqual(nombres, ['UUID', 'Timestamp', 'TipoMedicion', 'ValorMedicion'])

    const tipos = columnas.map((columna) => columna.type)
    assert.deepStrictEqual(tipos, ['TEXT', 'TIMESTAMP', 'INTEGER', 'INTEGER'])
  })

  it('se puede ejecutar dos veces sin error (CREATE TABLE IF NOT EXISTS)', async function () {
    await ejecutarScript(conexion, fs.readFileSync(SCRIPT_CREACION, 'utf8'))
  })

  it('permite insertar y leer una medición', async function () {
    await ejecutarScript(conexion, fs.readFileSync(SCRIPT_DATOS_PRUEBA, 'utf8'))
    const filas = await consultar(conexion, 'SELECT * FROM Mediciones WHERE TipoMedicion = 11')
    assert.strictEqual(filas.length, 1)
    assert.strictEqual(filas[0].UUID, 'EPSG-GTI-PROY-3A')
    assert.strictEqual(filas[0].ValorMedicion, 123)
  })

  it('recupera las mediciones guardadas (3 datos de prueba)', async function () {
    await ejecutarScript(conexion, fs.readFileSync(SCRIPT_DATOS_PRUEBA, 'utf8'))
    const filas = await consultar(conexion, 'SELECT * FROM Mediciones ORDER BY Timestamp')
    assert.deepStrictEqual(filas.map((fila) => fila.TipoMedicion), [11, 12, 13])
  })

  it('guarda valores negativos (temperatura -12)', async function () {
    await ejecutarScript(conexion, fs.readFileSync(SCRIPT_DATOS_PRUEBA, 'utf8'))
    const filas = await consultar(conexion, 'SELECT ValorMedicion FROM Mediciones WHERE TipoMedicion = 12')
    assert.strictEqual(filas[0].ValorMedicion, -12)
  })

  it('rechaza un TipoMedicion no válido', async function () {
    await assert.rejects(
      ejecutarScript(conexion,
        "INSERT INTO Mediciones (UUID, Timestamp, TipoMedicion, ValorMedicion) VALUES ('x', '2026-01-01T00:00:00.000Z', 99, 1)"),
      /CHECK constraint failed/
    )
  })
})
