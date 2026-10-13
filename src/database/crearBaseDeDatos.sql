-- ---------------------------------------------------------------------------
-- Fichero:     crearBaseDeDatos.sql
-- Descripción: Crea la tabla Mediciones de la base de datos del proyecto
--              (diseño en doc/database_design.md).
-- Autora:      Pelin Kaynak
-- Fecha:       30/09/2026
-- Copyright:   (c) 2026 Pelin Kaynak - Proyecto Biometría y Medio Ambiente, EPSG-UPV
-- ---------------------------------------------------------------------------

-- What it does: creates the Mediciones table (only if it does not exist yet, so old data is kept).
-- Columns: id (row number, primary key), UUID (who), Timestamp (when, set by the server), TipoMedicion (11 CO2, 12 temperature, 13 noise)
-- and ValorMedicion (the value). No column can be empty and the type must be 11, 12 or 13.
CREATE TABLE IF NOT EXISTS Mediciones (
    id            INTEGER   NOT NULL PRIMARY KEY AUTOINCREMENT,
    UUID          TEXT      NOT NULL,
    Timestamp     TIMESTAMP NOT NULL,
    TipoMedicion  INTEGER   NOT NULL CHECK (TipoMedicion IN (11, 12, 13)),
    ValorMedicion INTEGER   NOT NULL
);
