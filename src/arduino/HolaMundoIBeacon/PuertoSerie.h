// -*- mode: c++ -*-

// ---------------------------------------------------------------------------
// Fichero:     PuertoSerie.h
// Descripción: Escritura de mensajes por el puerto serie (depuración).
//              Diseño en doc/arduino_design.md.
// Autor:       Jordi Bataller i Mascarell (código original proporcionado)
// Aportación:  Pelin Kaynak - cabeceras de fichero y de función con el
//              diseño lógico (29/09/2026)
// Copyright:   (c) Jordi Bataller i Mascarell - EPSG-UPV
// ---------------------------------------------------------------------------

// ----------------------------------------------------------
// Jordi Bataller i Mascarell
// 2019-07-07
// ----------------------------------------------------------

#ifndef PUERTO_SERIE_H_INCLUIDO
#define PUERTO_SERIE_H_INCLUIDO

// ----------------------------------------------------------
// ----------------------------------------------------------
class PuertoSerie  {

public:
  // ------------------------------------------------------------
  // baudios: Z --> PuertoSerie() -->
  // ------------------------------------------------------------
  PuertoSerie (long baudios) {
	Serial.begin( baudios );
	// mejor no poner esto aquí: while ( !Serial ) delay(10);   
  } // ()

  // ------------------------------------------------------------
  // esperarDisponible() -->
  // ------------------------------------------------------------
  void esperarDisponible() {

	while ( !Serial ) {
	  delay(10);   
	}

  } // ()

  // ------------------------------------------------------------
  // mensaje: T --> escribir() -->
  // ------------------------------------------------------------
  template<typename T>
  void escribir (T mensaje) {
	Serial.print( mensaje );
  } // ()
  
}; // class PuertoSerie

// ----------------------------------------------------------
// ----------------------------------------------------------
// ----------------------------------------------------------
// ----------------------------------------------------------
#endif
