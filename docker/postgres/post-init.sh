#!/bin/bash
# ============================================================================
# Script post-inicio: Actualizar restricción de tipos de espacios
# ============================================================================
# Este script se ejecuta después de que la aplicación Spring Boot haya
# creado/actualizado las tablas con Hibernate.
#
# Se asegura de que la restricción spaces_type_check esté actualizada
# con todos los tipos de espacios del enum SpaceType.java
# ============================================================================

set -e

echo "🔧 Esperando a que la aplicación inicie y cree las tablas..."
sleep 10

echo "📋 Verificando/Actualizando restricción de tipos de espacios..."

# Ejecutar el script SQL de actualización en el contenedor de PostgreSQL
PGPASSWORD="${POSTGRES_PASSWORD}" psql -h postgres -U "${POSTGRES_USER}" -d "${POSTGRES_DB}" << 'EOF'

-- Verificar si la tabla existe
DO $$
DECLARE
    constraint_exists BOOLEAN;
    current_definition TEXT;
BEGIN
    -- Verificar si la tabla spaces existe
    IF EXISTS (SELECT FROM information_schema.tables 
               WHERE table_schema = 'public' 
               AND table_name = 'spaces') THEN
        
        -- Verificar si la restricción existe
        SELECT EXISTS (
            SELECT 1 FROM pg_constraint 
            WHERE conrelid = 'spaces'::regclass 
            AND conname = 'spaces_type_check'
        ) INTO constraint_exists;
        
        IF constraint_exists THEN
            -- Obtener definición actual
            SELECT pg_get_constraintdef(oid) 
            INTO current_definition
            FROM pg_constraint 
            WHERE conrelid = 'spaces'::regclass 
            AND conname = 'spaces_type_check';
            
            RAISE NOTICE 'Restricción actual: %', current_definition;
            
            -- Verificar si incluye todos los tipos necesarios
            IF current_definition NOT LIKE '%PARQUE%' OR
               current_definition NOT LIKE '%GIMNASIO%' OR
               current_definition NOT LIKE '%PISCINA%' OR
               current_definition NOT LIKE '%LABORATORIO%' OR
               current_definition NOT LIKE '%BIBLIOTECA%' OR
               current_definition NOT LIKE '%TEATRO%' THEN
                
                RAISE NOTICE '⚠️  Restricción incompleta, actualizando...';
                
                -- Eliminar restricción antigua
                EXECUTE 'ALTER TABLE spaces DROP CONSTRAINT spaces_type_check';
                
                -- Crear nueva restricción completa
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
                
                RAISE NOTICE '✅ Restricción actualizada correctamente';
            ELSE
                RAISE NOTICE '✅ Restricción ya está actualizada';
            END IF;
        ELSE
            RAISE NOTICE '⚠️  Restricción no existe, creando...';
            
            -- Crear restricción
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
            
            RAISE NOTICE '✅ Restricción creada correctamente';
        END IF;
    ELSE
        RAISE NOTICE 'ℹ️  Tabla spaces no existe todavía';
    END IF;
END
$$;

EOF

echo "✅ Verificación completada"
