# Ingeniería inversa: esqueleto WebApp en PHP con sesión (código de referencia)

**Autora del diseño:** Pelin Kaynak · **Código original:** proporcionado por el profesor (`EsqueletoWebAppEnPHPConSesion`)

> Este código **no** forma parte de `src/`: se usa solo como referencia de arquitectura. De él se toma la separación en tres capas (`logica/`, `rest/`, `ux/logicaFake/`) que se aplica en los componentes `logica`, `api` y `web` de este proyecto.

## Arquitectura

```text
ux/Aplicacion.html  -->  ux/logicaFake/*.js  --HTTP GET-->  rest/*.php  -->  logica/*.php
      (interfaz)          (misma firma que la lógica)       (traduce HTTP)    (lógica real)
```

## Lógica (`logica/`)

```text
nombre: Text, password: Text --> hacerLogin() --> B

usuario: Text --> diHola() --> ( nombre: Text, saludo: Text )
```

## API REST (`rest/`)

```text
GET rest/hacerLogin.php?nombre=<Text>&password=<Text>
    --> ( resultado: B, usuario: Text )     (guarda usuario en la sesión si es correcto)

GET rest/diHola.php                          (usuario implícito en la sesión)
    --> ( nombre: Text, saludo: Text, error: 0 ) | ( error: Text )
```

## Lógica fake (`ux/logicaFake/`)

Misma firma lógica que la lógica real; por dentro hace la petición REST (resultado devuelto por callback, que en la notación se omite):

```text
nombre: Text, password: Text --> hacerLogin() --> B

diHola() --> ( nombre: Text, saludo: Text ) | error: Text
```

## Qué se reutiliza en este proyecto

- La UX nunca llama a la API directamente: siempre pasa por la lógica fake.
- La lógica fake tiene los mismos nombres de funciones que la lógica real.
- La capa REST solo traduce: no contiene lógica del negocio.
- Diferencia: aquí el servidor es Node.js + Express (práctica 6 de Programación 2) en lugar de PHP, y no hace falta sesión porque en el Sprint 0 no hay login.
