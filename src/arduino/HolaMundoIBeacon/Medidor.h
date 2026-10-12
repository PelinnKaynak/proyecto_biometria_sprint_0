// -*- mode: c++ -*-

// ---------------------------------------------------------------------------
// Fichero:     Medidor.h
// Descripción: Obtiene las mediciones de los sensores (ficticias en el Sprint 0).
//              Diseño en doc/arduino_design.md.
// Autor:       Jordi Bataller i Mascarell (código original proporcionado)
// Aportación:  Pelin Kaynak - cabeceras de fichero y de función con el
//              diseño lógico (29/09/2026)
// Copyright:   (c) Jordi Bataller i Mascarell - EPSG-UPV
// ---------------------------------------------------------------------------

#ifndef MEDIDOR_H_INCLUIDO
#define MEDIDOR_H_INCLUIDO

// ------------------------------------------------------
// ------------------------------------------------------
// Medidor class: the 'sensor'. For now it returns fake fixed values.
class Medidor {

  // .....................................................
  // .....................................................
private:

public:

  // .....................................................
  // constructor
  // ------------------------------------------------------------
  // Medidor() -->
  // What it does: creates the meter. Nothing to prepare yet.
  // ------------------------------------------------------------
  Medidor(  ) {
  } // ()

  // ------------------------------------------------------------
  // iniciarMedidor() -->
  // What it does: prepares the sensor. Empty for now (no real sensor in Sprint 0).
  // ------------------------------------------------------------
  void iniciarMedidor() {
	// las cosas que no se puedan hacer en el constructor, if any
  } // ()

  // ------------------------------------------------------------
  // medirCO2() --> Z          (valor ficticio: 123)
  // What it does: returns the CO2 value. Always 123 for now (fake value).
  // ------------------------------------------------------------
  int medirCO2() {
	return 123;
  } // ()

  // ------------------------------------------------------------
  // medirTemperatura() --> Z  (valor ficticio: -12)
  // What it does: returns the temperature. Always -12 (fake value, not used in Sprint 0).
  // ------------------------------------------------------------
  int medirTemperatura() {
	return -12; // qué frío !
  } // ()
	
}; // class

// ------------------------------------------------------
// ------------------------------------------------------
// ------------------------------------------------------
// ------------------------------------------------------
#endif
