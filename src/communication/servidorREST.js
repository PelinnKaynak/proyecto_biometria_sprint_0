// ---------------------------------------------------------------------------
// Fichero:     servidorREST.js
// Descripción: Servidor REST (Express). Traduce peticiones HTTP a llamadas a
//              LogicaDeNegocio y sus resultados a JSON. También sirve la
//              página web de src/gui. Diseño en doc/communication_design.md.
// Autora:      Pelin Kaynak
// Fecha:       30/09/2026
// Copyright:   (c) 2026 Pelin Kaynak - Proyecto Biometría y Medio Ambiente, EPSG-UPV
// ---------------------------------------------------------------------------

// servidorREST module: receives the HTTP requests, checks them, calls LogicaDeNegocio
// and answers with a code and JSON. It also serves the web page (src/gui).
const path = require('path')
const express = require('express')
const { LogicaDeNegocio } = require('../business_logic/LogicaDeNegocio')

const CARPETA_GUI = path.join(__dirname, '..', 'gui')
const CARPETA_LOGICA_FAKE = path.join(__dirname, '..', 'frontend_business_logic')
const TIPOS_VALIDOS = [11, 12, 13]

let logica = null // instancia privada de LogicaDeNegocio (solo la usa este módulo)
let elServidorHTTP = null

// ---------------------------------------------------------------------------
// cuerpo: MedicionEntrante --> esMedicionEntranteValida() --> B
// MedicionEntrante = ( IdSensor: N, TipoMedicion: N, ValorMedicion: Z )
// What it does: returns true if the body has a natural IdSensor, a type 11/12/13 and an integer value.
// ---------------------------------------------------------------------------
function esMedicionEntranteValida(cuerpo) {
  return (
    cuerpo !== null &&
    typeof cuerpo === 'object' &&
    Number.isInteger(cuerpo.IdSensor) &&
    cuerpo.IdSensor >= 0 &&
    TIPOS_VALIDOS.includes(cuerpo.TipoMedicion) &&
    Number.isInteger(cuerpo.ValorMedicion)
  )
}

// ---------------------------------------------------------------------------
// peticion: PeticionHTTP --> rutaGuardarMedicion() --> EXITO | ERROR      (POST /guardarMedicion)
//
// peticion.body = MedicionEntrante = ( IdSensor: N, TipoMedicion: N, ValorMedicion: Z )
// EXITO = 201 { resultado: "ok" }
// ERROR = 400 (faltan datos o no son válidos) | 500 { error: Text }
// El Timestamp no se recibe: lo genera la lógica del negocio.
// What it does: (POST /guardarMedicion) checks the body and saves it: 201 ok, 400 bad data, 500 error.
// ---------------------------------------------------------------------------
async function rutaGuardarMedicion(peticion, respuesta) {
  const cuerpo = peticion.body
  if (!esMedicionEntranteValida(cuerpo)) {
    return respuesta.status(400).json({
      error: 'se necesita IdSensor (natural), TipoMedicion (11, 12 o 13) y ValorMedicion (entero)',
    })
  }

  try {
    await logica.guardarMedicion(cuerpo.IdSensor, cuerpo.TipoMedicion, cuerpo.ValorMedicion)
    respuesta.status(201).json({ resultado: 'ok' })
  } catch (error) {
    respuesta.status(500).json({ error: `no se pudo guardar la medición: ${error.message}` })
  }
}

// ---------------------------------------------------------------------------
// peticion: PeticionHTTP --> rutaRecuperarUltimaMedicion() --> Medicion | ERROR   (GET /recuperarUltimaMedicion)
//
// Medicion = ( IdSensor: N, Timestamp: Text, TipoMedicion: N, ValorMedicion: Z ), con código 200
// ERROR = 404 (todavía no hay mediciones) | 500 { error: Text }
// What it does: (GET /recuperarUltimaMedicion) returns the last measurement: 200, 404 if none, 500 error.
// ---------------------------------------------------------------------------
async function rutaRecuperarUltimaMedicion(peticion, respuesta) {
  try {
    const medicion = await logica.recuperarUltimaMedicion()
    if (medicion === null) {
      return respuesta.status(404).json({ error: 'todavía no hay mediciones' })
    }
    respuesta.status(200).json(medicion)
  } catch (error) {
    respuesta.status(500).json({ error: `no se pudo recuperar la medición: ${error.message}` })
  }
}

// ---------------------------------------------------------------------------
// error: Error, peticion: PeticionHTTP --> responderJSONMalFormado() --> ERROR
//
// Convierte un JSON mal formado en el cuerpo en una respuesta 400 en JSON.
// (siguiente es el callback de Express para pasar otros errores; se omite en la notación)
// What it does: if the body is broken JSON, answers 400 in JSON instead of an HTML error page.
// ---------------------------------------------------------------------------
function responderJSONMalFormado(error, peticion, respuesta, siguiente) {
  if (error.type === 'entity.parse.failed') {
    return respuesta.status(400).json({ error: 'el cuerpo no es un JSON válido' })
  }
  siguiente(error)
}

// ---------------------------------------------------------------------------
// puerto: N, nombreBD: Text --> arrancarServidor() --> ServidorHTTP
// What it does: creates the logic, sets the routes and the web folder, and listens on the port
//               on all network addresses (0.0.0.0) so the phone can connect.
// ---------------------------------------------------------------------------
function arrancarServidor(puerto, nombreBD) {
  logica = new LogicaDeNegocio(nombreBD)

  const aplicacion = express()
  aplicacion.use(express.json())
  aplicacion.post('/guardarMedicion', rutaGuardarMedicion)
  aplicacion.get('/recuperarUltimaMedicion', rutaRecuperarUltimaMedicion)
  aplicacion.use(express.static(CARPETA_GUI))
  aplicacion.use('/frontend_business_logic', express.static(CARPETA_LOGICA_FAKE))
  aplicacion.use(responderJSONMalFormado)

  return new Promise((resolver, rechazar) => {
    elServidorHTTP = aplicacion.listen(puerto, '0.0.0.0', () => resolver(elServidorHTTP))
    elServidorHTTP.on('error', rechazar)
  })
}

// ---------------------------------------------------------------------------
// pararServidor() -->
// What it does: stops listening and closes the database.
// ---------------------------------------------------------------------------
async function pararServidor() {
  if (elServidorHTTP) {
    await new Promise((resolver) => elServidorHTTP.close(() => resolver()))
    elServidorHTTP = null
  }
  if (logica) {
    await logica.cerrar()
    logica = null
  }
}

module.exports = { arrancarServidor, pararServidor }
