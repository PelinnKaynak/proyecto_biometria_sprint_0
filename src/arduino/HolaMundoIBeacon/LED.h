// -*- mode: c++ -*-

// ---------------------------------------------------------------------------
// Fichero:     LED.h
// Descripción: Control de un LED de la placa (encender, apagar, parpadear).
//              Diseño en doc/arduino_design.md.
// Autor:       Jordi Bataller i Mascarell (código original proporcionado)
// Aportación:  Pelin Kaynak - cabeceras de fichero y de función con el
//              diseño lógico (29/09/2026)
// Copyright:   (c) Jordi Bataller i Mascarell - EPSG-UPV
// ---------------------------------------------------------------------------

#ifndef LED_H_INCLUIDO
#define LED_H_INCLUIDO

// ----------------------------------------------------------
// Jordi Bataller i Mascarell
// 2019-07-07
// ----------------------------------------------------------

// ------------------------------------------------------------
// tiempo: Z --> esperar() -->
// ------------------------------------------------------------
void esperar (long tiempo) {
  delay (tiempo);
}

// ----------------------------------------------------------
// ----------------------------------------------------------
class LED {
private:
  int numeroLED;
  bool encendido;
public:

  // ------------------------------------------------------------
  // numero: Z --> LED() -->
  // ------------------------------------------------------------
  LED (int numero)
	: numeroLED (numero), encendido(false)
  {
	pinMode(numeroLED, OUTPUT);
	apagar ();
  }

  // ------------------------------------------------------------
  // encender() -->
  // ------------------------------------------------------------
  void encender () {
	digitalWrite(numeroLED, HIGH); 
	encendido = true;
  }

  // ------------------------------------------------------------
  // apagar() -->
  // ------------------------------------------------------------
  void apagar () {
	  digitalWrite(numeroLED, LOW);
	  encendido = false;
  }

  // ------------------------------------------------------------
  // alternar() -->
  // ------------------------------------------------------------
  void alternar () {
	if (encendido) {
	  apagar();
	} else {
	  encender ();
	}
  } // ()

  // ------------------------------------------------------------
  // tiempo: Z --> brillar() -->
  // ------------------------------------------------------------
  void brillar (long tiempo) {
	encender ();
	esperar(tiempo); 
	apagar ();
  }
}; // class

// ----------------------------------------------------------
// ----------------------------------------------------------
// ----------------------------------------------------------
// ----------------------------------------------------------
#endif
