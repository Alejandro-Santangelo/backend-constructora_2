package com.rodrigo.construccion.service;

import com.rodrigo.construccion.model.entity.Proveedor;

public interface IProveedorService {
    Proveedor obtenerPorIdYEmpresa(Long id, Long empresaId);
}
