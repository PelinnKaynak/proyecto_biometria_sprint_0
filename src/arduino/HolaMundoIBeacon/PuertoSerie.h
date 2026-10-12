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
// PuertoSerie class: lets the board write messages to the PC (Serial Monitor).
class PuertoSerie  {

public:
  // ------------------------------------------------------------
  // baudios: Z --> PuertoSerie() -->
  // What it does: opens the serial port at 'baudios' speed (115200).
  // ------------------------------------------------------------
  PuertoSerie (long baudios) {
	Serial.begin( baudios );
	// mejor no poner esto aquí: while ( !Serial ) delay(10);   
  } // ()

  // ------------------------------------------------------------
  // esperarDisponible() -->
  // What it does: waits until the serial port is ready, so no message is lost.
  // ------------------------------------------------------------
  void esperarDisponible() {

	while ( !Serial ) {
	  delay(10);   
	}

  } // ()

  // ------------------------------------------------------------
  // mensaje: T --> escribir() -->
  // What it does: writes 'mensaje' (text or number) on the Serial Monitor.
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
