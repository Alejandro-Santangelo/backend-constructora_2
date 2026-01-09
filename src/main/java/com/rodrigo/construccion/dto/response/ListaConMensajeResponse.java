package com.rodrigo.construccion.dto.response;

import java.util.List;

public class ListaConMensajeResponse<T> {
    public List<T> resultado;
    public String mensaje;

    public ListaConMensajeResponse(List<T> resultado, String mensaje) {
        this.resultado = resultado;
        this.mensaje = mensaje;
    }

    public ListaConMensajeResponse(List<T> resultado) {
        this.resultado = resultado;
        this.mensaje = null;
    }
}
