// ---------------------------------------------------------------------------
// Fichero:     LogicaDeNegocio.js
// Descripción: Lógica del negocio del backend. Único componente que accede a
//              la base de datos SQLite (tabla Mediciones). No contiene código
//              HTTP/REST. Diseño en doc/logica_design.md.
// Autora:      Pelin Kaynak
// Fecha:       30/09/2026
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
  // UUID: Text, TipoMedicion: N, ValorMedicion: Z --> guardarMedicion() -->
  //
  // Genera el Timestamp (ISO 8601, UTC) y guarda la medición.
  // Falla si faltan datos o si el tipo no es 11, 12 o 13.
  // -------------------------------------------------------------------------
  async guardarMedicion(UUID, TipoMedicion, ValorMedicion) {
    if (typeof UUID !== 'string' || UUID.length === 0) {
      throw new Error('UUID debe ser un texto no vacío')
    }
    if (!TIPOS_VALIDOS.includes(TipoMedicion)) {
      throw new Error(`TipoMedicion no válido: ${TipoMedicion}`)
    }
    if (!Number.isInteger(ValorMedicion)) {
      throw new Error('ValorMedicion debe ser un número entero')
    }

    await this.#baseDeDatosAbierta
    const Timestamp = new Date().toISOString()

    return new Promise((resolver, rechazar) => {
      this.#laConexion.run(
        'INSERT INTO Mediciones (UUID, Timestamp, TipoMedicion, ValorMedicion) VALUES (?, ?, ?, ?)',
        [UUID, Timestamp, TipoMedicion, ValorMedicion],
        (error) => (error ? rechazar(error) : resolver())
      )
    })
  }

  // -------------------------------------------------------------------------
  // recuperarUltimaMedicion() --> Medicion | vacío
  //
  // Medicion = ( UUID: Text, Timestamp: Text, TipoMedicion: N, ValorMedicion: Z )
  // Devuelve null si la tabla está vacía.
  // -------------------------------------------------------------------------
  async recuperarUltimaMedicion() {
    await this.#baseDeDatosAbierta

    return new Promise((resolver, rechazar) => {
      this.#laConexion.get(
        // rowid es interno de SQLite (no es un campo de la tabla): desempata
        // dos mediciones guardadas en el mismo milisegundo.
        'SELECT UUID, Timestamp, TipoMedicion, ValorMedicion FROM Mediciones ORDER BY Timestamp DESC, rowid DESC LIMIT 1',
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
