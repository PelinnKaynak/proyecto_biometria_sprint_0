// -*- mode: c++ -*-
// ---------------------------------------------------------------------------
// Fichero:     pruebasHolaMundoIBeacon.cpp
// Descripción: Tests automáticos del programa de la placa, ejecutados en el
//              ordenador con el doble de prueba stubs/bluefruit.h (sin placa).
//              Diseño en doc/arduino_design.md.
// Autora:      Pelin Kaynak
// Fecha:       13/10/2026
// Copyright:   (c) 2026 Pelin Kaynak - Proyecto Biometría y Medio Ambiente, EPSG-UPV
// ---------------------------------------------------------------------------
#include <cstdio>

#include "../HolaMundoIBeacon/HolaMundoIBeacon.ino"

static int pruebasFallidas = 0;
static int comprobaciones = 0;

// ------------------------------------------------------------
// condicion: B, descripcion: Text --> comprobar() -->
// What it does: counts the check and prints FALLO if the condition is false.
// ------------------------------------------------------------
static void comprobar(bool condicion, const char * descripcion) {
  comprobaciones++;
  if (!condicion) {
    pruebasFallidas++;
    printf("  FALLO: %s\n", descripcion);
  }
}

// ------------------------------------------------------------
// reiniciarDoble()
// What it does: empties what the test double has recorded.
// ------------------------------------------------------------
static void reiniciarDoble() {
  Doble::tiempoEsperado = 0;
  Doble::escriturasPin.clear();
  Doble::salidaSerie.clear();
  Bluefruit.Advertising = AdvertisingDoble();
}

// ------------------------------------------------------------
// pruebaMedidor()
// What it does: checks the fake values of Medidor (123 and -12).
// ------------------------------------------------------------
static void pruebaMedidor() {
  printf("Medidor\n");
  Medidor medidor;
  medidor.iniciarMedidor();
  comprobar(medidor.medirCO2() == 123, "medirCO2() devuelve 123");
  comprobar(medidor.medirTemperatura() == -12, "medirTemperatura() devuelve -12");
}

// ------------------------------------------------------------
// pruebaLED()
// What it does: checks the LED turns on/off, toggles and blinks for the given time.
// ------------------------------------------------------------
static void pruebaLED() {
  printf("LED\n");
  reiniciarDoble();
  LED led(7);
  comprobar(Doble::escriturasPin.back() == std::make_pair(7, LOW), "el constructor deja el LED apagado");
  led.brillar(100);
  size_t n = Doble::escriturasPin.size();
  comprobar(Doble::escriturasPin[n - 2] == std::make_pair(7, HIGH), "brillar() enciende el LED");
  comprobar(Doble::escriturasPin[n - 1] == std::make_pair(7, LOW), "brillar() apaga el LED al final");
  comprobar(Doble::tiempoEsperado == 100, "brillar(100) espera 100 ms");
  led.encender();
  led.alternar();
  comprobar(Doble::escriturasPin.back() == std::make_pair(7, LOW), "alternar() apaga un LED encendido");
}

// ------------------------------------------------------------
// pruebaPuertoSerie()
// What it does: checks escribir() sends text and numbers to the serial port.
// ------------------------------------------------------------
static void pruebaPuertoSerie() {
  printf("PuertoSerie\n");
  reiniciarDoble();
  PuertoSerie puerto(115200);
  puerto.esperarDisponible();
  puerto.escribir("valor=");
  puerto.escribir(123);
  comprobar(Doble::salidaSerie == "valor=123", "escribir() envía texto y números al puerto serie");
}

// ------------------------------------------------------------
// pruebaEmisoraBLE()
// What it does: checks Bluetooth is turned on and the iBeacon is configured, started and stopped.
// ------------------------------------------------------------
static void pruebaEmisoraBLE() {
  printf("EmisoraBLE\n");
  reiniciarDoble();
  EmisoraBLE emisora("GTI3A-2025", 0x004c, 4);
  emisora.encenderEmisora();
  comprobar(Bluefruit.encendido, "encenderEmisora() enciende el Bluetooth");
  comprobar(!emisora.estaAnunciando(), "al encender no se está anunciando");

  uint8_t uuid[16] = { 'E','P','S','G','-','G','T','I','-','P','R','O','Y','-','3','A' };
  emisora.emitirAnuncioIBeacon(uuid, 0x0B05, 123, -53);
  comprobar(emisora.estaAnunciando(), "emitirAnuncioIBeacon() empieza a anunciar");
  comprobar(Bluefruit.nombre == "GTI3A-2025", "el nombre de la emisora es GTI3A-2025");
  comprobar(Bluefruit.potencia == 4, "txPower es 4");
  comprobar(Bluefruit.Advertising.ultimoBeacon.fabricante == 0x004c, "fabricante 0x004C (Apple)");
  comprobar(Bluefruit.Advertising.intervaloMin == 100 && Bluefruit.Advertising.intervaloMax == 100,
            "intervalo de anuncio 100 x 0,625 ms");

  emisora.detenerAnuncio();
  comprobar(!emisora.estaAnunciando(), "detenerAnuncio() deja de anunciar");
}

// ------------------------------------------------------------
// pruebaPublicador()
// What it does: checks major = (type << 8) + counter, minor = value, duration and stop.
// ------------------------------------------------------------
static void pruebaPublicador() {
  printf("Publicador\n");
  reiniciarDoble();
  Publicador publicador;
  publicador.encenderEmisora();

  publicador.publicarCO2(123, 5, 10000);
  BLEBeacon & beacon = Bluefruit.Advertising.ultimoBeacon;
  comprobar(memcmp(beacon.uuid, "EPSG-GTI-PROY-3A", 16) == 0, "UUID EPSG-GTI-PROY-3A");
  comprobar(beacon.major == ((11 << 8) + 5), "publicarCO2: major = (CO2 << 8) + contador");
  comprobar(beacon.minor == 123, "publicarCO2: minor = valor");
  comprobar(Doble::tiempoEsperado == 10000, "publicarCO2 anuncia durante tiempoEspera");
  comprobar(!Bluefruit.Advertising.isRunning(), "publicarCO2 detiene el anuncio al terminar");

  publicador.publicarTemperatura(-12, 255, 1000);
  comprobar(beacon.major == ((12 << 8) + 255), "publicarTemperatura: major = (TEMPERATURA << 8) + contador");
  comprobar((int16_t) beacon.minor == -12, "publicarTemperatura: minor = valor negativo");
}

// ------------------------------------------------------------
// pruebaUtilidades()
// What it does: checks alReves() and stringAUint8AlReves().
// ------------------------------------------------------------
static void pruebaUtilidades() {
  printf("alReves y stringAUint8AlReves\n");
  int numeros[4] = { 1, 2, 3, 4 };
  alReves(numeros, 4);
  comprobar(numeros[0] == 4 && numeros[1] == 3 && numeros[2] == 2 && numeros[3] == 1, "alReves invierte el array");

  uint8_t destino[4] = { 0, 0, 0, 0 };
  stringAUint8AlReves("ABC", destino, 4);
  comprobar(destino[3] == 'A' && destino[2] == 'B' && destino[1] == 'C', "stringAUint8AlReves copia al revés");
}

// ------------------------------------------------------------
// pruebaProgramaPrincipal()
// What it does: checks setup() and loop() of the real .ino file (counter, value and time).
// ------------------------------------------------------------
static void pruebaProgramaPrincipal() {
  printf("HolaMundoIBeacon.ino: setup() y loop()\n");
  reiniciarDoble();
  setup();
  comprobar(Bluefruit.encendido, "setup() enciende la emisora");
  comprobar(Doble::salidaSerie.find("setup(): fin") != std::string::npos, "setup() avisa por el puerto serie");

  reiniciarDoble();
  loop();
  BLEBeacon & beacon = Bluefruit.Advertising.ultimoBeacon;
  comprobar(Loop::cont == 1, "loop() incrementa el contador");
  comprobar(beacon.major == ((11 << 8) + 1), "loop() publica CO2 con el contador 1");
  comprobar(beacon.minor == 123, "loop() publica el valor 123");
  comprobar(Doble::tiempoEsperado == 3500 + 10000, "una vuelta = lucecitas (3,5 s) + anuncio (10 s)");

  loop();
  comprobar(beacon.major == ((11 << 8) + 2), "la segunda vuelta usa el contador 2");
}

// ------------------------------------------------------------
// main() --> Z
// What it does: runs all the tests and returns 0 if none failed.
// ------------------------------------------------------------
int main() {
  pruebaMedidor();
  pruebaLED();
  pruebaPuertoSerie();
  pruebaEmisoraBLE();
  pruebaPublicador();
  pruebaUtilidades();
  pruebaProgramaPrincipal();
  printf("%d comprobaciones, %d fallidas\n", comprobaciones, pruebasFallidas);
  return pruebasFallidas == 0 ? 0 : 1;
}
