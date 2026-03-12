-- ============================================
-- MIGRACIÓN COMPLETA SCHEMA RAILWAY
-- Fecha: 2026-03-12
-- Descripción: Sincroniza el schema de Railway con los cambios locales
--              SOLO MODIFICA ESQUEMA, NO BORRA DATOS
-- ============================================

-- PARTE 1: Columnas en tabla descuentos_por_rubro
-- ------------------------------------------------
\echo ''
\echo '========== PARTE 1: descuentos_por_rubro =========='
\echo ''

-- Verificar si las columnas ya existen antes de agregarlas
DO $$
BEGIN
    -- Honorarios en descuentos_por_rubro
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'descuentos_por_rubro' 
                   AND column_name = 'honorarios_activo') THEN
        ALTER TABLE descuentos_por_rubro 
        ADD COLUMN honorarios_activo BOOLEAN NOT NULL DEFAULT false;
        RAISE NOTICE 'Columna honorarios_activo agregada';
    ELSE
        RAISE NOTICE 'Columna honorarios_activo ya existe';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'descuentos_por_rubro' 
                   AND column_name = 'honorarios_tipo') THEN
        ALTER TABLE descuentos_por_rubro 
        ADD COLUMN honorarios_tipo VARCHAR(20) NOT NULL DEFAULT 'PORCENTAJE';
        RAISE NOTICE 'Columna honorarios_tipo agregada';
    ELSE
        RAISE NOTICE 'Columna honorarios_tipo ya existe';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'descuentos_por_rubro' 
                   AND column_name = 'honorarios_valor') THEN
        ALTER TABLE descuentos_por_rubro 
        ADD COLUMN honorarios_valor NUMERIC(10,2);
        RAISE NOTICE 'Columna honorarios_valor agregada';
    ELSE
        RAISE NOTICE 'Columna honorarios_valor ya existe';
    END IF;

    -- Mayores Costos en descuentos_por_rubro
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'descuentos_por_rubro' 
                   AND column_name = 'mayores_costos_activo') THEN
        ALTER TABLE descuentos_por_rubro 
        ADD COLUMN mayores_costos_activo BOOLEAN NOT NULL DEFAULT false;
        RAISE NOTICE 'Columna mayores_costos_activo agregada';
    ELSE
        RAISE NOTICE 'Columna mayores_costos_activo ya existe';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'descuentos_por_rubro' 
                   AND column_name = 'mayores_costos_tipo') THEN
        ALTER TABLE descuentos_por_rubro 
        ADD COLUMN mayores_costos_tipo VARCHAR(20) NOT NULL DEFAULT 'PORCENTAJE';
        RAISE NOTICE 'Columna mayores_costos_tipo agregada';
    ELSE
        RAISE NOTICE 'Columna mayores_costos_tipo ya existe';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'descuentos_por_rubro' 
                   AND column_name = 'mayores_costos_valor') THEN
        ALTER TABLE descuentos_por_rubro 
        ADD COLUMN mayores_costos_valor NUMERIC(10,2);
        RAISE NOTICE 'Columna mayores_costos_valor agregada';
    ELSE
        RAISE NOTICE 'Columna mayores_costos_valor ya existe';
    END IF;
END $$;


-- PARTE 2: Columnas en tabla mayores_costos_por_rubro
-- ----------------------------------------------------
\echo ''
\echo '========== PARTE 2: mayores_costos_por_rubro =========='
\echo ''

DO $$
BEGIN
    -- Honorarios en mayores_costos_por_rubro
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'mayores_costos_por_rubro' 
                   AND column_name = 'honorarios_activo') THEN
        ALTER TABLE mayores_costos_por_rubro 
        ADD COLUMN honorarios_activo BOOLEAN NOT NULL DEFAULT true;
        RAISE NOTICE 'Columna honorarios_activo agregada';
    ELSE
        RAISE NOTICE 'Columna honorarios_activo ya existe';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'mayores_costos_por_rubro' 
                   AND column_name = 'honorarios_tipo') THEN
        ALTER TABLE mayores_costos_por_rubro 
        ADD COLUMN honorarios_tipo VARCHAR(20) NOT NULL DEFAULT 'porcentaje';
        RAISE NOTICE 'Columna honorarios_tipo agregada';
    ELSE
        RAISE NOTICE 'Columna honorarios_tipo ya existe';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'mayores_costos_por_rubro' 
                   AND column_name = 'honorarios_valor') THEN
        ALTER TABLE mayores_costos_por_rubro 
        ADD COLUMN honorarios_valor NUMERIC(10,2);
        RAISE NOTICE 'Columna honorarios_valor agregada';
    ELSE
        RAISE NOTICE 'Columna honorarios_valor ya existe';
    END IF;
END $$;


-- PARTE 3: Columnas en tabla presupuesto_no_cliente
-- --------------------------------------------------
\echo ''
\echo '========== PARTE 3: presupuesto_no_cliente =========='
\echo ''

DO $$
BEGIN
    -- Totales por rubro
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'presupuesto_no_cliente' 
                   AND column_name = 'total_mayores_costos_por_rubro') THEN
        ALTER TABLE presupuesto_no_cliente 
        ADD COLUMN total_mayores_costos_por_rubro DECIMAL(15,2) DEFAULT 0.00;
        RAISE NOTICE 'Columna total_mayores_costos_por_rubro agregada';
    ELSE
        RAISE NOTICE 'Columna total_mayores_costos_por_rubro ya existe';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'presupuesto_no_cliente' 
                   AND column_name = 'total_descuentos_por_rubro') THEN
        ALTER TABLE presupuesto_no_cliente 
        ADD COLUMN total_descuentos_por_rubro DECIMAL(15,2) DEFAULT 0.00;
        RAISE NOTICE 'Columna total_descuentos_por_rubro agregada';
    ELSE
        RAISE NOTICE 'Columna total_descuentos_por_rubro ya existe';
    END IF;

    -- Mayores costos honorarios
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'presupuesto_no_cliente' 
                   AND column_name = 'mayores_costos_honorarios_activo') THEN
        ALTER TABLE presupuesto_no_cliente 
        ADD COLUMN mayores_costos_honorarios_activo BOOLEAN DEFAULT false;
        RAISE NOTICE 'Columna mayores_costos_honorarios_activo agregada';
    ELSE
        RAISE NOTICE 'Columna mayores_costos_honorarios_activo ya existe';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'presupuesto_no_cliente' 
                   AND column_name = 'mayores_costos_honorarios_tipo') THEN
        ALTER TABLE presupuesto_no_cliente 
        ADD COLUMN mayores_costos_honorarios_tipo VARCHAR(20);
        RAISE NOTICE 'Columna mayores_costos_honorarios_tipo agregada';
    ELSE
        RAISE NOTICE 'Columna mayores_costos_honorarios_tipo ya existe';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'presupuesto_no_cliente' 
                   AND column_name = 'mayores_costos_honorarios_valor') THEN
        ALTER TABLE presupuesto_no_cliente 
        ADD COLUMN mayores_costos_honorarios_valor DECIMAL(15,2);
        RAISE NOTICE 'Columna mayores_costos_honorarios_valor agregada';
    ELSE
        RAISE NOTICE 'Columna mayores_costos_honorarios_valor ya existe';
    END IF;

    -- Descuentos honorarios
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'presupuesto_no_cliente' 
                   AND column_name = 'descuentos_honorarios_activo') THEN
        ALTER TABLE presupuesto_no_cliente 
        ADD COLUMN descuentos_honorarios_activo BOOLEAN DEFAULT false;
        RAISE NOTICE 'Columna descuentos_honorarios_activo agregada';
    ELSE
        RAISE NOTICE 'Columna descuentos_honorarios_activo ya existe';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'presupuesto_no_cliente' 
                   AND column_name = 'descuentos_honorarios_tipo') THEN
        ALTER TABLE presupuesto_no_cliente 
        ADD COLUMN descuentos_honorarios_tipo VARCHAR(20);
        RAISE NOTICE 'Columna descuentos_honorarios_tipo agregada';
    ELSE
        RAISE NOTICE 'Columna descuentos_honorarios_tipo ya existe';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'presupuesto_no_cliente' 
                   AND column_name = 'descuentos_honorarios_valor') THEN
        ALTER TABLE presupuesto_no_cliente 
        ADD COLUMN descuentos_honorarios_valor DECIMAL(15,2);
        RAISE NOTICE 'Columna descuentos_honorarios_valor agregada';
    ELSE
        RAISE NOTICE 'Columna descuentos_honorarios_valor ya existe';
    END IF;

    -- Descuentos mayores costos
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'presupuesto_no_cliente' 
                   AND column_name = 'descuentos_mayores_costos_activo') THEN
        ALTER TABLE presupuesto_no_cliente 
        ADD COLUMN descuentos_mayores_costos_activo BOOLEAN DEFAULT false;
        RAISE NOTICE 'Columna descuentos_mayores_costos_activo agregada';
    ELSE
        RAISE NOTICE 'Columna descuentos_mayores_costos_activo ya existe';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'presupuesto_no_cliente' 
                   AND column_name = 'descuentos_mayores_costos_tipo') THEN
        ALTER TABLE presupuesto_no_cliente 
        ADD COLUMN descuentos_mayores_costos_tipo VARCHAR(20);
        RAISE NOTICE 'Columna descuentos_mayores_costos_tipo agregada';
    ELSE
        RAISE NOTICE 'Columna descuentos_mayores_costos_tipo ya existe';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'presupuesto_no_cliente' 
                   AND column_name = 'descuentos_mayores_costos_valor') THEN
        ALTER TABLE presupuesto_no_cliente 
        ADD COLUMN descuentos_mayores_costos_valor DECIMAL(15,2);
        RAISE NOTICE 'Columna descuentos_mayores_costos_valor agregada';
    ELSE
        RAISE NOTICE 'Columna descuentos_mayores_costos_valor ya existe';
    END IF;
END $$;


-- PARTE 4: Columnas en tabla trabajo_extra  
-- -----------------------------------------
\echo ''
\echo '========== PARTE 4: trabajo_extra =========='
\echo ''

DO $$
BEGIN
    -- Mayores costos honorarios en trabajo_extra
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'trabajo_extra' 
                   AND column_name = 'mayores_costos_honorarios_activo') THEN
        ALTER TABLE trabajo_extra 
        ADD COLUMN mayores_costos_honorarios_activo BOOLEAN DEFAULT false;
        RAISE NOTICE 'Columna mayores_costos_honorarios_activo agregada';
    ELSE
        RAISE NOTICE 'Columna mayores_costos_honorarios_activo ya existe';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'trabajo_extra' 
                   AND column_name = 'mayores_costos_honorarios_valor') THEN
        ALTER TABLE trabajo_extra 
        ADD COLUMN mayores_costos_honorarios_valor DECIMAL(15,2);
        RAISE NOTICE 'Columna mayores_costos_honorarios_valor agregada';
    ELSE
        RAISE NOTICE 'Columna mayores_costos_honorarios_valor ya existe';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'trabajo_extra' 
                   AND column_name = 'mayores_costos_honorarios_tipo') THEN
        ALTER TABLE trabajo_extra 
        ADD COLUMN mayores_costos_honorarios_tipo VARCHAR(20);
        RAISE NOTICE 'Columna mayores_costos_honorarios_tipo agregada';
    ELSE
        RAISE NOTICE 'Columna mayores_costos_honorarios_tipo ya existe';
    END IF;
END $$;


-- PARTE 5: Tabla pagos_parciales_rubros
-- --------------------------------------
\echo ''
\echo '========== PARTE 5: pagos_parciales_rubros =========='
\echo ''

-- Crear tabla si no existe
CREATE TABLE IF NOT EXISTS pagos_parciales_rubros (
    id BIGSERIAL PRIMARY KEY,
    presupuesto_id BIGINT NOT NULL,
    empresa_id BIGINT NOT NULL,
    
    -- Identificación del rubro e item
    nombre_rubro VARCHAR(255) NOT NULL,
    tipo_item VARCHAR(50) NOT NULL,
    
    -- Datos del pago
    monto DECIMAL(15,2) NOT NULL,
    metodo_pago VARCHAR(50) DEFAULT 'EFECTIVO',
    observaciones TEXT,
    fecha_pago TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Metadata
    usuario_registro VARCHAR(100),
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_pagos_monto_positivo CHECK (monto > 0),
    CONSTRAINT chk_pagos_tipo_item CHECK (tipo_item IN ('JORNALES', 'MATERIALES', 'GASTOS_GENERALES'))
);

-- Crear índices si no existen
CREATE INDEX IF NOT EXISTS idx_pagos_parciales_presupuesto ON pagos_parciales_rubros(presupuesto_id);
CREATE INDEX IF NOT EXISTS idx_pagos_parciales_empresa ON pagos_parciales_rubros(empresa_id);
CREATE INDEX IF NOT EXISTS idx_pagos_parciales_rubro ON pagos_parciales_rubros(presupuesto_id, nombre_rubro);
CREATE INDEX IF NOT EXISTS idx_pagos_parciales_rubro_item ON pagos_parciales_rubros(presupuesto_id, nombre_rubro, tipo_item);

-- Comentarios
COMMENT ON TABLE pagos_parciales_rubros IS 'Registra pagos parciales sobre items (jornales/materiales/gastos) de rubros del presupuesto';

\echo 'Tabla pagos_parciales_rubros verificada/creada con 4 índices'
\echo ''


-- PARTE 6: Verificación final
-- ----------------------------
\echo ''
\echo '========== VERIFICACIÓN FINAL =========='
\echo ''

\echo 'Columnas en descuentos_por_rubro relacionadas con honorarios y mayores costos:'
SELECT column_name, data_type, column_default, is_nullable
FROM information_schema.columns
WHERE table_name = 'descuentos_por_rubro'
  AND column_name LIKE '%honorarios%' OR column_name LIKE '%mayores_costos%'
ORDER BY column_name;

\echo ''
\echo 'Columnas en mayores_costos_por_rubro relacionadas con honorarios:'
SELECT column_name, data_type, column_default, is_nullable
FROM information_schema.columns
WHERE table_name = 'mayores_costos_por_rubro'
  AND column_name LIKE '%honorarios%'
ORDER BY column_name;

\echo ''
\echo 'Columnas nuevas en presupuesto_no_cliente:'
SELECT column_name, data_type, column_default, is_nullable
FROM information_schema.columns
WHERE table_name = 'presupuesto_no_cliente'
  AND (column_name LIKE '%por_rubro%' 
       OR column_name LIKE '%mayores_costos_honorarios%'
       OR column_name LIKE '%descuentos_honorarios%'
       OR column_name LIKE '%descuentos_mayores_costos%')
ORDER BY column_name;

\echo ''
\echo 'Columnas nuevas en trabajo_extra:'
SELECT column_name, data_type, column_default, is_nullable
FROM information_schema.columns
WHERE table_name = 'trabajo_extra'
  AND column_name LIKE '%mayores_costos_honorarios%'
ORDER BY column_name;

\echo ''
\echo 'Tabla pagos_parciales_rubros:'
SELECT 
    column_name, 
    data_type, 
    column_default, 
    is_nullable
FROM information_schema.columns
WHERE table_name = 'pagos_parciales_rubros'
ORDER BY ordinal_position;

\echo ''
\echo 'Índices en pagos_parciales_rubros:'
SELECT indexname 
FROM pg_indexes 
WHERE tablename = 'pagos_parciales_rubros'
ORDER BY indexname;

\echo ''
\echo '========== MIGRACIÓN COMPLETADA =========='
\echo 'Ahora el schema de Railway debe estar sincronizado con el código Java'
\echo 'Incluye: descuentos_por_rubro, mayores_costos_por_rubro, presupuesto_no_cliente, trabajo_extra, pagos_parciales_rubros'
\echo ''
