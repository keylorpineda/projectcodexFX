package com.municipal.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Deserializador personalizado que convierte fechas UTC del backend a zona horaria de Costa Rica.
 */
public class UtcToCostaRicaDeserializer extends JsonDeserializer<LocalDateTime> {
    
    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String dateString = p.getText();
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        
        // Parsear la fecha como UTC
        LocalDateTime utcDateTime = LocalDateTime.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        
        // Convertir de UTC a Costa Rica
        return DateTimeUtils.utcToCostaRica(utcDateTime);
    }
}
