-- ---------------------------------------------------------------------------
-- Fichero:     datosDePrueba.sql
-- Descripción: Mediciones ficticias para los tests de la base de datos.
-- Autora:      Pelin Kaynak
-- Fecha:       29/09/2026
-- Copyright:   (c) 2026 Pelin Kaynak - Proyecto Biometría y Medio Ambiente, EPSG-UPV
-- ---------------------------------------------------------------------------

INSERT INTO Mediciones (UUID, Timestamp, TipoMedicion, ValorMedicion)
VALUES ('EPSG-GTI-PROY-3A', '2026-09-29T10:00:00.000Z', 11, 123);

INSERT INTO Mediciones (UUID, Timestamp, TipoMedicion, ValorMedicion)
VALUES ('EPSG-GTI-PROY-3A', '2026-09-29T10:00:01.000Z', 12, -12);

INSERT INTO Mediciones (UUID, Timestamp, TipoMedicion, ValorMedicion)
VALUES ('EPSG-GTI-PROY-3A', '2026-09-29T10:00:02.000Z', 13, 45);
