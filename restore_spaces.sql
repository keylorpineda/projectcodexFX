-- Script para restaurar espacios que fueron marcados como eliminados incorrectamente
-- Esto restaura los espacios que tienen deletedAt != null

-- Ver cuántos espacios están marcados como eliminados
SELECT COUNT(*) as espacios_eliminados 
FROM spaces 
WHERE deleted_at IS NOT NULL;

-- Ver los detalles de los espacios eliminados
SELECT id, name, type, active, deleted_at, created_at 
FROM spaces 
WHERE deleted_at IS NOT NULL;

-- Restaurar todos los espacios eliminados (quitar la marca de deletedAt)
UPDATE spaces 
SET deleted_at = NULL, 
    updated_at = NOW()
WHERE deleted_at IS NOT NULL;

-- Verificar que se restauraron correctamente
SELECT COUNT(*) as espacios_activos 
FROM spaces 
WHERE deleted_at IS NULL;

-- Ver todos los espacios después de la restauración
SELECT id, name, type, active, deleted_at 
FROM spaces 
ORDER BY id;
