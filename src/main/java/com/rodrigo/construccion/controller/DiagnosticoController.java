package com.rodrigo.construccion.controller;

import com.rodrigo.construccion.config.TenantContext;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/diagnostico")
@CrossOrigin(origins = "*")
public class DiagnosticoController {

    @GetMapping("/tenant-info")
    public Map<String, Object> getTenantInfo(HttpServletRequest request) {
        Map<String, Object> info = new HashMap<>();
        
        // Info del TenantContext
        info.put("empresaIdEnContext", TenantContext.getTenantId());
        info.put("isSuperAdminEnContext", TenantContext.isSuperAdmin());
        
        // Headers recibidos
        Map<String, String> headers = new HashMap<>();
        headers.put("empresaId", request.getHeader("empresaId"));
        headers.put("X-Empresa-Id", request.getHeader("X-Empresa-Id"));
        headers.put("empresa-id", request.getHeader("empresa-id"));
        headers.put("X-Super-Admin", request.getHeader("X-Super-Admin"));
        headers.put("X-Tenant-ID", request.getHeader("X-Tenant-ID"));
        info.put("headers", headers);
        
        // Parámetros recibidos
        info.put("paramEmpresaId", request.getParameter("empresaId"));
        
        // URI
        info.put("uri", request.getRequestURI());
        info.put("method", request.getMethod());
        
        return info;
    }
}
