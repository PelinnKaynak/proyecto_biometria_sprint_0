-- ---------------------------------------------------------------------------
-- Fichero:     crearBaseDeDatos.sql
-- Descripción: Crea la tabla Mediciones de la base de datos del proyecto
--              (diseño en doc/basedatos_design.md).
-- Autora:      Pelin Kaynak
-- Fecha:       29/09/2026
-- Copyright:   (c) 2026 Pelin Kaynak - Proyecto Biometría y Medio Ambiente, EPSG-UPV
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS Mediciones (
    id            INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    uuid          TEXT    NOT NULL,
    timestamp     TEXT    NOT NULL,
    tipoMedicion  INTEGER NOT NULL CHECK (tipoMedicion IN (11, 12, 13)),
    valorMedicion INTEGER NOT NULL
);
