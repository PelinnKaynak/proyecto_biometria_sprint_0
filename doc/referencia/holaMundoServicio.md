# Ingeniería inversa: HolaMundoServicio2021 (código de referencia)

**Autora del diseño:** Pelin Kaynak · **Código original:** proporcionado por el profesor (`HolaMundoServicio2021`)

> Este código **no** forma parte de `src/` en el Sprint 0: el escaneo BLE se hace desde `MainActivity` (ver `android_design.md`). Se documenta porque en sprints posteriores el escaneo pasará a un servicio en segundo plano.

## Clase ServicioEscucharBeacons (IntentService)

```text
                               --------- ServicioEscucharBeacons ----------------
                               |
                               | ETIQUETA_LOG: Text
                               | tiempoDeEspera: Z
                               | seguir: B
                               |
                               |
                               ServicioEscucharBeacons() -->
                               |
                               |
                               parar() -->
                               |
                               |
                               onDestroy() -->
                               |
                               |
  intent: Intent           --> onHandleIntent() -->
                               |
                               --------------------------------------------------
```

## Clase MainActivity (de HolaMundoServicio)

```text
                               --------- MainActivity ---------------------------
                               |
                               | ETIQUETA_LOG: Text
                               | elIntentDelServicio: Intent
                               |
                               |    savedInstanceState: Bundle --> onCreate() -->
                               |
                               |
  v: View                  --> botonArrancarServicioPulsado() -->
                               |
                               |
  v: View                  --> botonDetenerServicioPulsado() -->
                               |
                               --------------------------------------------------
```

## Aclaraciones

- `onHandleIntent()` se ejecuta en un hilo aparte y repite un bucle mientras `seguir` sea `true`, esperando `tiempoDeEspera` milisegundos en cada vuelta.
- `parar()` pone `seguir := false` y detiene el servicio.
- En el código original la clase se llama `ServicioEscuharBeacons` (falta una "c"); aquí se escribe correctamente.
