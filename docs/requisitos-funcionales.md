# Requisitos funcionales clave

## Desempeño
- **Tiempo de respuesta máximo de 2 segundos** para las operaciones críticas del sistema de reservas.

## Gestión y auditoría
- **RF13 - Registro de logs para auditoría:** almacenar eventos relevantes (reservas, aprobaciones, cancelaciones) con trazabilidad de usuario y marca de tiempo.
- **RF14 - Calificación de espacios post-uso:** permitir que las personas usuarias valoren el espacio una vez finalizada la reserva.
- **RF15 - Gestión de horarios personalizables por espacio:** cada espacio municipal debe definir bloques y excepciones de horario específicos.

## Reservas y cancelaciones
- **RF09 - Exportación de historial de reservas a Excel** para supervisores.
- **Historial personal exportable a Excel** para cada persona usuaria, limitado a sus propias reservas.
- **Cancelaciones con restricciones temporales:** solo se permiten cancelaciones dentro de las ventanas definidas por la municipalidad.

## Métricas y monitoreo
- **Métricas de uso y ocupación** disponibles para la administración, con indicadores de porcentaje de ocupación y cancelaciones.

## Gestión de espacios
- **CRUD completo de espacios** con los atributos: nombre, tipo, capacidad, horarios configurables y galería de imágenes.
- **Activación / inactivación temporal** de espacios para mantenimiento o indisponibilidad temporal.
