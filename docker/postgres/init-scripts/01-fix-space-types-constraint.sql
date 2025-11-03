-- ============================================================================
-- Script de inicialización: Actualizar restricción de tipos de espacios
-- ============================================================================
-- Este script se ejecuta automáticamente cuando se crea la base de datos
-- por primera vez o cuando el volumen de PostgreSQL se recrea.
--
-- PROPÓSITO:
-- Asegurar que la tabla 'spaces' permita todos los tipos definidos en el
-- enum SpaceType.java del backend
--
-- TIPOS PERMITIDOS:
-- - SALA
-- - CANCHA
-- - AUDITORIO
-- - GIMNASIO
-- - PISCINA
-- - PARQUE
-- - LABORATORIO
-- - BIBLIOTECA
-- - TEATRO
-- ============================================================================

-- Nota: Este script usa IF EXISTS para ser idempotente (puede ejecutarse múltiples veces)

-- Eliminar la restricción si existe
ALTER TABLE IF EXISTS spaces 
DROP CONSTRAINT IF EXISTS spaces_type_check;

-- Crear/Recrear la restricción con todos los tipos permitidos
DO $$
BEGIN
    -- Solo crear la restricción si la tabla existe
    IF EXISTS (SELECT FROM information_schema.tables 
               WHERE table_schema = 'public' 
               AND table_name = 'spaces') THEN
        
        -- Eliminar restricción antigua si existe
        EXECUTE 'ALTER TABLE spaces DROP CONSTRAINT IF EXISTS spaces_type_check';
        
        -- Crear nueva restricción
        EXECUTE 'ALTER TABLE spaces 
                 ADD CONSTRAINT spaces_type_check 
                 CHECK (type IN (
                     ''SALA'',
                     ''CANCHA'',
                     ''AUDITORIO'',
                     ''GIMNASIO'',
                     ''PISCINA'',
                     ''PARQUE'',
                     ''LABORATORIO'',
                     ''BIBLIOTECA'',
                     ''TEATRO''
                 ))';
        
        RAISE NOTICE 'Restricción spaces_type_check actualizada correctamente';
    ELSE
        RAISE NOTICE 'Tabla spaces no existe todavía, la restricción se creará con la tabla';
    END IF;
END
$$;
