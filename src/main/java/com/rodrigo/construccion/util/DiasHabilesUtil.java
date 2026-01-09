package com.rodrigo.construccion.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Utilidad para cálculo de días hábiles en Argentina
 */
public class DiasHabilesUtil {

    // Feriados fijos de Argentina (MM-DD)
    private static final List<String> FERIADOS_FIJOS = Arrays.asList(
            "01-01", // Año Nuevo
            "24-03", // Día Nacional de la Memoria por la Verdad y la Justicia
            "02-04", // Día del Veterano y de los Caídos en la Guerra de Malvinas
            "01-05", // Día del Trabajador
            "25-05", // Día de la Revolución de Mayo
            "20-06", // Paso a la Inmortalidad del General Manuel Belgrano
            "09-07", // Día de la Independencia
            "08-12", // Inmaculada Concepción de María
            "25-12"  // Navidad
    );

    /**
     * Verifica si una fecha es día hábil (no es fin de semana ni feriado)
     */
    public static boolean esDiaHabil(LocalDate fecha) {
        // Verificar fin de semana
        DayOfWeek diaSemana = fecha.getDayOfWeek();
        if (diaSemana == DayOfWeek.SATURDAY || diaSemana == DayOfWeek.SUNDAY) {
            return false;
        }

        // Verificar feriado fijo
        String mesdia = fecha.format(DateTimeFormatter.ofPattern("MM-dd"));
        return !FERIADOS_FIJOS.contains(mesdia);
    }

    /**
     * Calcula todos los días hábiles entre dos fechas (inclusive)
     */
    public static List<LocalDate> calcularDiasHabiles(LocalDate fechaInicio, LocalDate fechaFin) {
        List<LocalDate> diasHabiles = new ArrayList<>();
        LocalDate fecha = fechaInicio;

        while (!fecha.isAfter(fechaFin)) {
            if (esDiaHabil(fecha)) {
                diasHabiles.add(fecha);
            }
            fecha = fecha.plusDays(1);
        }

        return diasHabiles;
    }

    /**
     * Calcula días hábiles desde una fecha de inicio durante N semanas
     */
    public static List<LocalDate> calcularDiasHabilesPorSemanas(LocalDate fechaInicio, int semanas) {
        LocalDate fechaFin = fechaInicio.plusWeeks(semanas);
        return calcularDiasHabiles(fechaInicio, fechaFin);
    }

    /**
     * Obtiene el código de semana ISO (formato: YYYY-Www)
     */
    public static String obtenerSemanaIso(LocalDate fecha) {
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int year = fecha.get(weekFields.weekBasedYear());
        int week = fecha.get(weekFields.weekOfWeekBasedYear());
        return String.format("%d-W%02d", year, week);
    }
}
