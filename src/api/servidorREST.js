// ---------------------------------------------------------------------------
// Fichero:     servidorREST.js
// Descripción: Servidor REST (Express). Traduce peticiones HTTP a llamadas a
//              LogicaDeNegocio y sus resultados a JSON. También sirve la
//              página web de src/web. Diseño en doc/api_design.md.
// Autora:      Pelin Kaynak
// Fecha:       30/09/2026
// Copyright:   (c) 2026 Pelin Kaynak - Proyecto Biometría y Medio Ambiente, EPSG-UPV
// ---------------------------------------------------------------------------

const path = require('path')
const express = require('express')
const { LogicaDeNegocio } = require('../logica/LogicaDeNegocio')

const CARPETA_WEB = path.join(__dirname, '..', 'web')
const TIPOS_VALIDOS = [11, 12, 13]

let logica = null // instancia privada de LogicaDeNegocio (solo la usa este módulo)
let elServidorHTTP = null

// ---------------------------------------------------------------------------
// cuerpo: JSON( UUID: Text, TipoMedicion: N, ValorMedicion: Z ) --> esMedicionEntranteValida() --> B
// ---------------------------------------------------------------------------
function esMedicionEntranteValida(cuerpo) {
  return (
    cuerpo !== null &&
    typeof cuerpo === 'object' &&
    typeof cuerpo.UUID === 'string' &&
    cuerpo.UUID.length > 0 &&
    TIPOS_VALIDOS.includes(cuerpo.TipoMedicion) &&
    Number.isInteger(cuerpo.ValorMedicion)
  )
}

// ---------------------------------------------------------------------------
// JSON( UUID: Text, TipoMedicion: N, ValorMedicion: Z ) --> POST /guardarMedicion --> EXITO | ERROR
//
// EXITO = 201 { resultado: "ok" }
// ERROR = 400 (faltan datos o no son válidos) | 500 { error: Text }
// El Timestamp no se recibe: lo genera la lógica del negocio.
// ---------------------------------------------------------------------------
async function rutaGuardarMedicion(peticion, respuesta) {
  const cuerpo = peticion.body
  if (!esMedicionEntranteValida(cuerpo)) {
    return respuesta.status(400).json({
      error: 'se necesita UUID (texto), TipoMedicion (11, 12 o 13) y ValorMedicion (entero)',
    })
  }

  try {
    await logica.guardarMedicion(cuerpo.UUID, cuerpo.TipoMedicion, cuerpo.ValorMedicion)
    respuesta.status(201).json({ resultado: 'ok' })
  } catch (error) {
    respuesta.status(500).json({ error: `no se pudo guardar la medición: ${error.message}` })
  }
}

// ---------------------------------------------------------------------------
// GET /recuperarUltimaMedicion --> JSON( UUID: Text, Timestamp: Text, TipoMedicion: N, ValorMedicion: Z )
//                                | ERROR
//
// JSON  = 200
// ERROR = 404 (todavía no hay mediciones) | 500 { error: Text }
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
// error, peticion, respuesta, siguiente --> responderJSONMalFormado() -->
//
// Convierte un JSON mal formado en el cuerpo en una respuesta 400 en JSON.
// ---------------------------------------------------------------------------
function responderJSONMalFormado(error, peticion, respuesta, siguiente) {
  if (error.type === 'entity.parse.failed') {
    return respuesta.status(400).json({ error: 'el cuerpo no es un JSON válido' })
  }
  siguiente(error)
}

// ---------------------------------------------------------------------------
// puerto: N, nombreBD: Text --> arrancarServidor() --> ServidorHTTP
// ---------------------------------------------------------------------------
function arrancarServidor(puerto, nombreBD) {
  logica = new LogicaDeNegocio(nombreBD)

  const aplicacion = express()
  aplicacion.use(express.json())
  aplicacion.post('/guardarMedicion', rutaGuardarMedicion)
  aplicacion.get('/recuperarUltimaMedicion', rutaRecuperarUltimaMedicion)
  aplicacion.use(express.static(CARPETA_WEB))
  aplicacion.use(responderJSONMalFormado)

  return new Promise((resolver, rechazar) => {
    elServidorHTTP = aplicacion.listen(puerto, '0.0.0.0', () => resolver(elServidorHTTP))
    elServidorHTTP.on('error', rechazar)
  })
}

// ---------------------------------------------------------------------------
// pararServidor() -->
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
