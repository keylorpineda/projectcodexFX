
SELECT conname, pg_get_constraintdef(oid) as constraint_definition
FROM pg_constraint
WHERE conrelid = 'spaces'::regclass
  AND conname = 'spaces_type_check';

ALTER TABLE spaces 
DROP CONSTRAINT IF EXISTS spaces_type_check;


ALTER TABLE spaces 
ADD CONSTRAINT spaces_type_check 
CHECK (type IN (
    'SALA',
    'CANCHA',
    'AUDITORIO',
    'GIMNASIO',
    'PISCINA',
    'PARQUE',
    'LABORATORIO',
    'BIBLIOTECA',
    'TEATRO'
));


SELECT conname, pg_get_constraintdef(oid) as constraint_definition
FROM pg_constraint
WHERE conrelid = 'spaces'::regclass
  AND conname = 'spaces_type_check';


SELECT DISTINCT type, COUNT(*) as cantidad
FROM spaces
GROUP BY type
ORDER BY type;
