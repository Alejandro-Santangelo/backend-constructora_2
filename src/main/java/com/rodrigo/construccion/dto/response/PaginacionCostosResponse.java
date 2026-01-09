package com.rodrigo.construccion.dto.response;

import java.util.List;

public class PaginacionCostosResponse {
    public List<CostoResponseDTO> contenido;
    public PaginacionInfo paginacion;
    public boolean ultimo;
    public int totalElementos;
    public int totalPaginas;
    public int tamano;
    public int numero;
    public boolean primero;
    public int numeroElementos;
    public boolean vacio;
    public String mensaje;

    public static class PaginacionInfo {
        public int numeroPagina;
        public int tamanoPagina;
        public OrdenInfo orden;
        public int desplazamiento;
        public boolean paginado;
        public boolean noPaginado;
    }

    public static class OrdenInfo {
        public boolean vacio;
        public boolean ordenado;
        public boolean noOrdenado;
    }
}
