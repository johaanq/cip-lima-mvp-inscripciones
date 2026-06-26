CREATE TABLE evento_config (
    id              SMALLINT PRIMARY KEY DEFAULT 1,
    cupo_maximo     INTEGER      NOT NULL,
    cupo_ocupado    INTEGER      NOT NULL DEFAULT 0,
    sede_consejo    VARCHAR(100) NOT NULL,
    CONSTRAINT chk_evento_singleton CHECK (id = 1),
    CONSTRAINT chk_cupo_ocupado CHECK (cupo_ocupado >= 0 AND cupo_ocupado <= cupo_maximo)
);

CREATE TABLE solicitud_inscripcion (
    id                  BIGSERIAL PRIMARY KEY,
    dni_colegiado       VARCHAR(8)   NOT NULL,
    nombre_colegiado    VARCHAR(200) NOT NULL,
    dni_menor           VARCHAR(8)   NOT NULL,
    imagen_object_key   VARCHAR(500),
    estado              VARCHAR(20)  NOT NULL,
    motivo_rechazo      TEXT,
    origen_rechazo      VARCHAR(20),
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_estado CHECK (estado IN ('PENDIENTE', 'APROBADO', 'RECHAZADO')),
    CONSTRAINT chk_origen_rechazo CHECK (
        origen_rechazo IS NULL OR origen_rechazo IN ('AUTOMATICO', 'ADMIN')
    ),
    CONSTRAINT chk_motivo_rechazo CHECK (
        (estado = 'RECHAZADO' AND motivo_rechazo IS NOT NULL AND TRIM(motivo_rechazo) <> '')
        OR (estado <> 'RECHAZADO')
    )
);

CREATE INDEX idx_solicitud_estado ON solicitud_inscripcion (estado);
CREATE INDEX idx_solicitud_dni_colegiado ON solicitud_inscripcion (dni_colegiado);
CREATE INDEX idx_solicitud_created_at ON solicitud_inscripcion (created_at DESC);

INSERT INTO evento_config (id, cupo_maximo, cupo_ocupado, sede_consejo)
VALUES (1, 10, 0, 'Lima');
