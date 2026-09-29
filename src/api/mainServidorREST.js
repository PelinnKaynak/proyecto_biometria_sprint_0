// ---------------------------------------------------------------------------
// Fichero:     mainServidorREST.js
// Descripción: Programa de arranque del servidor REST (npm run servidor).
//              Puerto y base de datos configurables con PUERTO y NOMBRE_BD.
//              Diseño en doc/api_design.md.
// Autora:      Pelin Kaynak
// Fecha:       29/09/2026
// Copyright:   (c) 2026 Pelin Kaynak - Proyecto Biometría y Medio Ambiente, EPSG-UPV
// ---------------------------------------------------------------------------

const { arrancarServidor, pararServidor } = require('./servidorREST')

const PUERTO = Number(process.env.PUERTO) || 8080
const NOMBRE_BD = process.env.NOMBRE_BD || 'mediciones.db'

// ---------------------------------------------------------------------------
// main() -->
// ---------------------------------------------------------------------------
async function main() {
  await arrancarServidor(PUERTO, NOMBRE_BD)
  console.log(`Servidor REST escuchando en http://localhost:${PUERTO} (base de datos: ${NOMBRE_BD})`)
  console.log('Pulsa Ctrl+C para pararlo.')

  process.on('SIGINT', async () => {
    await pararServidor()
    console.log('\nServidor parado.')
    process.exit(0)
  })
}

main()
