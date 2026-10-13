// ---------------------------------------------------------------------------
// Fichero:     LogicaDeNegocio.js
// Descripción: Lógica del negocio del backend. Único componente que accede a
//              la base de datos SQLite (tabla Mediciones). No contiene código
//              HTTP/REST. Diseño en doc/business_logic_design.md.
// Autora:      Pelin Kaynak
// Fecha:       30/09/2026
// Copyright:   (c) 2026 Pelin Kaynak - Proyecto Biometría y Medio Ambiente, EPSG-UPV
// ---------------------------------------------------------------------------

const fs = require('fs')
const path = require('path')
const sqlite3 = require('sqlite3')

const SCRIPT_CREACION = path.join(__dirname, '..', 'database', 'crearBaseDeDatos.sql')
const TIPOS_VALIDOS = [11, 12, 13] // CO2, TEMPERATURA, RUIDO

// LogicaDeNegocio class: the only part of the server that reads and writes the database.
class LogicaDeNegocio {
  #laConexion = null
  #baseDeDatosAbierta = null

  // -------------------------------------------------------------------------
  // nombreBD: Text --> LogicaDeNegocio() -->
  // What it does: opens the database file (in the background) and remembers the 'ready' promise.
  // -------------------------------------------------------------------------
  constructor(nombreBD) {
    this.#baseDeDatosAbierta = this.#abrirBaseDeDatos(nombreBD)
  }

  // -------------------------------------------------------------------------
  // nombreBD: Text --> abrirBaseDeDatos() -->          (privado)
  //
  // Abre la conexión y crea la tabla Mediciones si no existe.
  // What it does: opens (or creates) the database file and runs crearBaseDeDatos.sql.
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
  // idSensor: N, tipoMedicion: N, valorMedicion: Z --> guardarMedicion() -->
  //
  // Genera el Timestamp (ISO 8601, UTC) y guarda la medición.
  // Falla si faltan datos o si el tipo no es 11, 12 o 13.
  // What it does: checks the data, adds the current time and INSERTs one row in Mediciones.
  // -------------------------------------------------------------------------
  async guardarMedicion(idSensor, tipoMedicion, valorMedicion) {
    if (!Number.isInteger(idSensor) || idSensor < 0) {
      throw new Error('IdSensor debe ser un número natural')
    }
    if (!TIPOS_VALIDOS.includes(tipoMedicion)) {
      throw new Error(`TipoMedicion no válido: ${tipoMedicion}`)
    }
    if (!Number.isInteger(valorMedicion)) {
      throw new Error('ValorMedicion debe ser un número entero')
    }

    await this.#baseDeDatosAbierta
    const timestamp = new Date().toISOString()

    return new Promise((resolver, rechazar) => {
      this.#laConexion.run(
        'INSERT INTO Mediciones (IdSensor, Timestamp, TipoMedicion, ValorMedicion) VALUES (?, ?, ?, ?)',
        [idSensor, timestamp, tipoMedicion, valorMedicion],
        (error) => (error ? rechazar(error) : resolver())
      )
    })
  }

  // -------------------------------------------------------------------------
  // recuperarUltimaMedicion() --> Medicion | vacío
  //
  // Medicion = ( IdSensor: N, Timestamp: Text, TipoMedicion: N, ValorMedicion: Z )
  // Devuelve null si la tabla está vacía.
  // What it does: returns the newest row of Mediciones, or null if the table is empty.
  // -------------------------------------------------------------------------
  async recuperarUltimaMedicion() {
    await this.#baseDeDatosAbierta

    return new Promise((resolver, rechazar) => {
      this.#laConexion.get(
        // id (clave primaria, creciente) desempata dos mediciones
        // guardadas en el mismo milisegundo.
        'SELECT IdSensor, Timestamp, TipoMedicion, ValorMedicion FROM Mediciones ORDER BY Timestamp DESC, id DESC LIMIT 1',
        (error, fila) => (error ? rechazar(error) : resolver(fila ?? null))
      )
    })
  }

  // -------------------------------------------------------------------------
  // cerrar() -->
  // What it does: closes the database connection (the file is not locked anymore).
  // -------------------------------------------------------------------------
  async cerrar() {
    await this.#baseDeDatosAbierta.catch(() => {})

    return new Promise((resolver, rechazar) => {
      this.#laConexion.close((error) => (error ? rechazar(error) : resolver()))
    })
  }
}

module.exports = { LogicaDeNegocio }
