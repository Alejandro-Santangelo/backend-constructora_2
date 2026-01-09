package com.rodrigo.construccion.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum RolEnObra {
    ROL_DIRECTOR("Director de Obra"),
    ROL_JEFE_OBRA("Jefe de Obra"),
    ROL_CAPATAZ("Capataz"),
    ROL_OFICIAL("Oficial"),
    ROL_OFICIAL_ALBANIL("Oficial Albañil"),
    ROL_OFICIAL_ELECTRICISTA("Oficial Electricista"),
    ROL_OFICIAL_PLOMERO("Oficial Plomero"),
    ROL_OFICIAL_CARPINTERO("Oficial Carpintero"),
    ROL_OFICIAL_PINTOR("Oficial Pintor"),
    ROL_OFICIAL_SOLDADOR("Oficial Soldador"),
    ROL_OFICIAL_GASISTA("Oficial Gasista"),
    ROL_MEDIO_OFICIAL("Medio Oficial"),
    ROL_MEDIO_OFICIAL_ALBANIL("Medio Oficial Albañil"),
    ROL_MEDIO_OFICIAL_ELECTRICISTA("Medio Oficial Electricista"),
    ROL_MEDIO_OFICIAL_PLOMERO("Medio Oficial Plomero"),
    ROL_MEDIO_OFICIAL_CARPINTERO("Medio Oficial Carpintero"),
    ROL_APRENDIZ("Aprendiz"),
    ROL_APRENDIZ_ALBANIL("Aprendiz Albañil"),
    ROL_APRENDIZ_ELECTRICISTA("Aprendiz Electricista"),
    ROL_APRENDIZ_PLOMERO("Aprendiz Plomero"),
    ROL_APRENDIZ_CARPINTERO("Aprendiz Carpintero"),
    ROL_PEON("Peón"),
    ROL_TRABAJADOR("Trabajador"),
    ROL_ESPECIALISTA("Especialista"), 
    ROL_CONSULTOR("Consultor");

    private final String displayName;

    RolEnObra(String displayName) {
        this.displayName = displayName;
    }

    /* Devuelve el nombre legible para mostrar en la UI. */
    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Busca el valor del Enum a partir de su nombre legible (displayName).
     * Ahora acepta variaciones flexibles para diferentes oficios.
     * 
     * @param displayName El String como "Oficial Albañil", "Trabajador", etc.
     * @return El Enum correspondiente.
     * @throws IllegalArgumentException si no se encuentra el valor.
     */
    public static RolEnObra fromDisplayName(String displayName) {
        if (displayName == null || displayName.trim().isEmpty()) {
            return ROL_OFICIAL; // Valor por defecto
        }
        
        String input = displayName.trim().toLowerCase();
        
        // Coincidencias exactas primero
        for (RolEnObra rolObra : RolEnObra.values()) {
            if (rolObra.getDisplayName().equalsIgnoreCase(displayName)) {
                return rolObra;
            }
        }
        
        // Mapeo flexible para variaciones comunes
        if (input.contains("director")) {
            return ROL_DIRECTOR;
        } else if (input.contains("jefe")) {
            return ROL_JEFE_OBRA;
        } else if (input.contains("capataz")) {
            return ROL_CAPATAZ;
        } else if (input.contains("trabajador")) {
            return ROL_TRABAJADOR;
        } else if (input.contains("aprendiz")) {
            if (input.contains("albañil")) return ROL_APRENDIZ_ALBANIL;
            if (input.contains("electricista")) return ROL_APRENDIZ_ELECTRICISTA;
            if (input.contains("plomero")) return ROL_APRENDIZ_PLOMERO;
            if (input.contains("carpintero")) return ROL_APRENDIZ_CARPINTERO;
            return ROL_APRENDIZ;
        } else if (input.contains("medio oficial")) {
            if (input.contains("albañil")) return ROL_MEDIO_OFICIAL_ALBANIL;
            if (input.contains("electricista")) return ROL_MEDIO_OFICIAL_ELECTRICISTA;
            if (input.contains("plomero")) return ROL_MEDIO_OFICIAL_PLOMERO;
            if (input.contains("carpintero")) return ROL_MEDIO_OFICIAL_CARPINTERO;
            return ROL_MEDIO_OFICIAL;
        } else if (input.contains("oficial")) {
            if (input.contains("albañil")) return ROL_OFICIAL_ALBANIL;
            if (input.contains("electricista")) return ROL_OFICIAL_ELECTRICISTA;
            if (input.contains("plomero")) return ROL_OFICIAL_PLOMERO;
            if (input.contains("carpintero")) return ROL_OFICIAL_CARPINTERO;
            if (input.contains("pintor")) return ROL_OFICIAL_PINTOR;
            if (input.contains("soldador")) return ROL_OFICIAL_SOLDADOR;
            if (input.contains("gasista")) return ROL_OFICIAL_GASISTA;
            return ROL_OFICIAL;
        } else if (input.contains("peón") || input.contains("peon")) {
            return ROL_PEON;
        } else if (input.contains("especialista") || input.contains("técnico") || input.contains("tecnico")) {
            return ROL_ESPECIALISTA;
        } else if (input.contains("consultor") || input.contains("asesor")) {
            return ROL_CONSULTOR;
        }
        
        // Si no coincide con nada, devolver OFICIAL por defecto
        return ROL_OFICIAL;
    }
}
