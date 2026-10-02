-- ---------------------------------------------------------------------------
-- Fichero:     crearBaseDeDatos.sql
-- Descripción: Crea la tabla Mediciones de la base de datos del proyecto
--              (diseño en doc/basedatos_design.md).
-- Autora:      Pelin Kaynak
-- Fecha:       30/09/2026
-- Copyright:   (c) 2026 Pelin Kaynak - Proyecto Biometría y Medio Ambiente, EPSG-UPV
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS Mediciones (
    UUID          TEXT      NOT NULL,
    Timestamp     TIMESTAMP NOT NULL,
    TipoMedicion  INTEGER   NOT NULL CHECK (TipoMedicion IN (11, 12, 13)),
    ValorMedicion INTEGER   NOT NULL
);
