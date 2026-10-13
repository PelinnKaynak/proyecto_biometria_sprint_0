// -*- mode: c++ -*-

// ---------------------------------------------------------------------------
// Fichero:     Publicador.h
// Descripción: Publica las mediciones en tramas iBeacon (major = tipo*256 + contador, minor = valor).
//              Diseño en doc/arduino_design.md.
// Autor:       Jordi Bataller i Mascarell (código original proporcionado)
// Aportación:  Pelin Kaynak - cabeceras de fichero y de función con el
//              diseño lógico (29/09/2026)
// Copyright:   (c) Jordi Bataller i Mascarell - EPSG-UPV
// ---------------------------------------------------------------------------

// --------------------------------------------------------------
// Jordi Bataller i Mascarell
// --------------------------------------------------------------

#ifndef PUBLICADOR_H_INCLUIDO
#define PUBLICADOR_H_INCLUIDO

// --------------------------------------------------------------
// --------------------------------------------------------------
// Publicador class: prepares the measurement (UUID with the sensor id, major, minor) and asks EmisoraBLE to send it.
class Publicador {

  // ............................................................
  // ............................................................
private:

  // (NUEVO) número del sensor de esta placa (0..65535). Cada placa del
  // proyecto tiene que tener uno distinto. Va en los 2 últimos bytes del UUID.
  static const uint16_t ID_SENSOR = 1;

  // (NUEVO) los 14 primeros bytes identifican las placas de este proyecto
  // ("EPSG-GTI-PELIN"; antes "EPSG-GTI-PROY-", igual para toda la clase);
  // los 2 últimos son ID_SENSOR (byte alto, byte bajo). Antes eran '3', 'A'.
  uint8_t beaconUUID[16] = { 
	'E', 'P', 'S', 'G', '-', 'G', 'T', 'I', 
	'-', 'P', 'E', 'L', 'I', 'N',
	(uint8_t) (ID_SENSOR >> 8), (uint8_t) (ID_SENSOR & 0xFF)
	};

  // ............................................................
  // ............................................................
public:
  EmisoraBLE laEmisora {
	"PelinIBeacon", //  nombre emisora  (NUEVO: antes "GTI3A-2025", igual para toda la clase)
	//"GTI-3A", //  OK
	  0x004c, // fabricanteID (Apple)
	  4 // txPower
	  };
  
  const int RSSI = -53; // por poner algo, de momento no lo uso

  // ............................................................
  // ............................................................
public:

  // ............................................................
  // ............................................................
  enum MedicionesID  {
	CO2 = 11,
	TEMPERATURA = 12,
	RUIDO = 13
  };

  // ------------------------------------------------------------
  // Publicador() -->
  // What it does: creates the publisher (its EmisoraBLE is created inside).
  // ------------------------------------------------------------
  Publicador( ) {
	// ATENCION: no hacerlo aquí. (*this).laEmisora.encenderEmisora();
	// Pondremos un método para llamarlo desde el setup() más tarde
  } // ()

  // ------------------------------------------------------------
  // encenderEmisora() -->
  // What it does: turns on Bluetooth through EmisoraBLE.
  // ------------------------------------------------------------
  void encenderEmisora() {
	(*this).laEmisora.encenderEmisora();
  } // ()

  // ------------------------------------------------------------
  // valorCO2: Z, contador: N, tiempoEspera: Z --> publicarCO2() -->
  // What it does: sends the CO2 value in iBeacon packets: major = (11 << 8) + contador,
  //               minor = valorCO2. Broadcasts for 'tiempoEspera' ms and then stops.
  // ------------------------------------------------------------
  void publicarCO2( int16_t valorCO2, uint8_t contador,
					long tiempoEspera ) {

	//
	// 1. empezamos anuncio
	//
	uint16_t major = (MedicionesID::CO2 << 8) + contador;
	(*this).laEmisora.emitirAnuncioIBeacon( (*this).beaconUUID, 
											major,
											valorCO2, // minor
											(*this).RSSI // rssi
									);

	/*
	Globales::elPuerto.escribir( "   publicarCO2(): valor=" );
	Globales::elPuerto.escribir( valorCO2 );
	Globales::elPuerto.escribir( "   contador=" );
	Globales::elPuerto.escribir( contador );
	Globales::elPuerto.escribir( "   todo="  );
	Globales::elPuerto.escribir( major );
	Globales::elPuerto.escribir( "\n" );
	*/

	//
	// 2. esperamos el tiempo que nos digan
	//
	esperar( tiempoEspera );

	//
	// 3. paramos anuncio
	//
	(*this).laEmisora.detenerAnuncio();
  } // ()

  // ------------------------------------------------------------
  // valorTemperatura: Z, contador: N, tiempoEspera: Z --> publicarTemperatura() -->
  // What it does: same as publicarCO2 but for temperature (type 12). Not used in Sprint 0.
  // ------------------------------------------------------------
  void publicarTemperatura( int16_t valorTemperatura,
							uint8_t contador, long tiempoEspera ) {

	uint16_t major = (MedicionesID::TEMPERATURA << 8) + contador;
	(*this).laEmisora.emitirAnuncioIBeacon( (*this).beaconUUID, 
											major,
											valorTemperatura, // minor
											(*this).RSSI // rssi
									);
	esperar( tiempoEspera );

	(*this).laEmisora.detenerAnuncio();
  } // ()
	
}; // class

// --------------------------------------------------------------
// --------------------------------------------------------------
// --------------------------------------------------------------
// --------------------------------------------------------------
#endif
