package com.municipal.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Serializ

ador personalizado que convierte fechas de Costa Rica a UTC antes de enviar al backend.
 */
public class CostaRicaToUtcSerializer extends JsonSerializer<LocalDateTime> {
    
    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        
        // Convertir de Costa Rica a UTC
        LocalDateTime utcDateTime = DateTimeUtils.costaRicaToUtc(value);
        
        // Escribir como string ISO
        gen.writeString(utcDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }
}
