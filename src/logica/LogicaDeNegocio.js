// ---------------------------------------------------------------------------
// Fichero:     LogicaDeNegocio.js
// Descripción: Lógica del negocio del backend. Único componente que accede a
//              la base de datos SQLite (tabla Mediciones). No contiene código
//              HTTP/REST. Diseño en doc/logica_design.md.
// Autora:      Pelin Kaynak
// Fecha:       29/09/2026
// Copyright:   (c) 2026 Pelin Kaynak - Proyecto Biometría y Medio Ambiente, EPSG-UPV
// ---------------------------------------------------------------------------

const fs = require('fs')
const path = require('path')
const sqlite3 = require('sqlite3')

const SCRIPT_CREACION = path.join(__dirname, '..', 'basedatos', 'crearBaseDeDatos.sql')
const TIPOS_VALIDOS = [11, 12, 13] // CO2, TEMPERATURA, RUIDO

class LogicaDeNegocio {
  #laConexion = null
  #baseDeDatosAbierta = null

  // -------------------------------------------------------------------------
  // nombreBD: Text --> LogicaDeNegocio() -->
  // -------------------------------------------------------------------------
  constructor(nombreBD) {
    this.#baseDeDatosAbierta = this.#abrirBaseDeDatos(nombreBD)
  }

  // -------------------------------------------------------------------------
  // nombreBD: Text --> abrirBaseDeDatos() -->          (privado)
  //
  // Abre la conexión y crea la tabla Mediciones si no existe.
  // -------------------------------------------------------------------------
  #abrirBaseDeDatos(nombreBD) {
    return new Promise((resolver, rechazar) => {
      this.#laConexion = new sqlite3.Database(nombreBD, (errorAlAbrir) => {
        if (errorAlAbrir) return rechazar(errorAlAbrir)
        const sqlCreacion = fs.readFileSync(SCRIPT_CREACION, 'utf8')
        this.#laConexion.exec(sqlCreacion, (errorAlCrear) => (errorAlCrear ? rechazar(errorAlCrear) : resolver()))
      })
    })
  }

  // -------------------------------------------------------------------------
  // uuid: Text, tipoMedicion: N, valorMedicion: Z --> guardarMedicion() -->
  //
  // Genera el timestamp (ISO 8601, UTC) y guarda la medición.
  // Falla si faltan datos o si el tipo no es 11, 12 o 13.
  // -------------------------------------------------------------------------
  async guardarMedicion(uuid, tipoMedicion, valorMedicion) {
    if (typeof uuid !== 'string' || uuid.length === 0) {
      throw new Error('uuid debe ser un texto no vacío')
    }
    if (!TIPOS_VALIDOS.includes(tipoMedicion)) {
      throw new Error(`tipoMedicion no válido: ${tipoMedicion}`)
    }
    if (!Number.isInteger(valorMedicion)) {
      throw new Error('valorMedicion debe ser un número entero')
    }

    await this.#baseDeDatosAbierta
    const timestamp = new Date().toISOString()

    return new Promise((resolver, rechazar) => {
      this.#laConexion.run(
        'INSERT INTO Mediciones (uuid, timestamp, tipoMedicion, valorMedicion) VALUES (?, ?, ?, ?)',
        [uuid, timestamp, tipoMedicion, valorMedicion],
        (error) => (error ? rechazar(error) : resolver())
      )
    })
  }

  // -------------------------------------------------------------------------
  // recuperarUltimaMedicion() --> Medicion | vacío
  //
  // Medicion = ( uuid: Text, timestamp: Text, tipoMedicion: N, valorMedicion: Z )
  // Devuelve null si la tabla está vacía.
  // -------------------------------------------------------------------------
  async recuperarUltimaMedicion() {
    await this.#baseDeDatosAbierta

    return new Promise((resolver, rechazar) => {
      this.#laConexion.get(
        'SELECT uuid, timestamp, tipoMedicion, valorMedicion FROM Mediciones ORDER BY id DESC LIMIT 1',
        (error, fila) => (error ? rechazar(error) : resolver(fila ?? null))
      )
    })
  }

  // -------------------------------------------------------------------------
  // cerrar() -->
  // -------------------------------------------------------------------------
  async cerrar() {
    await this.#baseDeDatosAbierta.catch(() => {})

    return new Promise((resolver, rechazar) => {
      this.#laConexion.close((error) => (error ? rechazar(error) : resolver()))
    })
  }
}

module.exports = { LogicaDeNegocio }
