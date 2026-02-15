package com.rodrigo.construccion.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Categorías de profesionales según su relación con la empresa
 */
public enum CategoriaProfesional {
    EMPLEADO("EMPLEADO", "Personal permanente de la empresa"),
    INDEPENDIENTE("INDEPENDIENTE", "Profesionales autónomos/freelancers contratados temporalmente"),
    CONTRATISTA("CONTRATISTA", "Empresas contratistas o cooperativas");

    private final String valor;
    private final String descripcion;

    CategoriaProfesional(String valor, String descripcion) {
        this.valor = valor;
        this.descripcion = descripcion;
    }

    /**
     * Devuelve el valor para serialización JSON y base de datos
     */
    @JsonValue
    public String getValor() {
        return valor;
    }

    /**
     * Devuelve la descripción legible de la categoría
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Busca el enum a partir del valor String
     * 
     * @param valor El String "EMPLEADO", "INDEPENDIENTE" o "CONTRATISTA"
     * @return El enum correspondiente
     * @throws IllegalArgumentException si no se encuentra el valor
     */
    public static CategoriaProfesional fromValor(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return EMPLEADO; // Default
        }
        
        for (CategoriaProfesional categoria : values()) {
            if (categoria.valor.equalsIgnoreCase(valor.trim())) {
                return categoria;
            }
        }
        
        throw new IllegalArgumentException(
            "Categoría de profesional no válida: " + valor + 
            ". Valores permitidos: EMPLEADO, INDEPENDIENTE, CONTRATISTA"
        );
    }

    /**
     * Verifica si un string es una categoría válida
     */
    public static boolean esValida(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return false;
        }
        
        for (CategoriaProfesional categoria : values()) {
            if (categoria.valor.equalsIgnoreCase(valor.trim())) {
                return true;
            }
        }
        
        return false;
    }

    @Override
    public String toString() {
        return valor;
    }
}
