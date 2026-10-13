// ---------------------------------------------------------------------------
// Fichero:     arduino.test.js
// Descripción: Lanza desde mocha los tests C++ del programa de la placa
//              (pruebasHolaMundoIBeacon.cpp): los compila con g++ y los
//              ejecuta. Si el ordenador no tiene g++, el test se omite.
//              Diseño en doc/arduino_design.md.
// Autora:      Pelin Kaynak
// Fecha:       13/10/2026
// Copyright:   (c) 2026 Pelin Kaynak - Proyecto Biometría y Medio Ambiente, EPSG-UPV
// ---------------------------------------------------------------------------

const assert = require('assert')
const os = require('os')
const path = require('path')
const { spawnSync } = require('child_process')

const CARPETA_TEST = __dirname
const EJECUTABLE = path.join(os.tmpdir(), `pruebasHolaMundoIBeacon-${process.pid}${process.platform === 'win32' ? '.exe' : ''}`)

// ------------------------------------------------------------
// hayCompilador() --> B
// What it does: returns true if g++ can be run on this computer.
// ------------------------------------------------------------
function hayCompilador() {
  return spawnSync('g++', ['--version']).status === 0
}

describe('arduino: programa de la placa (C++ con doble de prueba de Bluefruit)', function () {
  this.timeout(60000)

  it('compila el código de la placa y pasa todas las comprobaciones', function () {
    if (!hayCompilador()) this.skip()

    const compilacion = spawnSync('g++', ['-std=c++17', '-I', path.join(CARPETA_TEST, 'stubs'),
      '-o', EJECUTABLE, path.join(CARPETA_TEST, 'pruebasHolaMundoIBeacon.cpp')], { encoding: 'utf8' })
    assert.strictEqual(compilacion.status, 0, compilacion.stderr)

    const ejecucion = spawnSync(EJECUTABLE, { encoding: 'utf8' })
    assert.strictEqual(ejecucion.status, 0, ejecucion.stdout)
    assert.match(ejecucion.stdout, /, 0 fallidas/)
  })
})
