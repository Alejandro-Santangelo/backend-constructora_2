package com.rodrigo.construccion.controller;

import com.rodrigo.construccion.dto.request.CambiarPinRequest;
import com.rodrigo.construccion.dto.request.LoginPinRequest;
import com.rodrigo.construccion.dto.response.LoginResponse;
import com.rodrigo.construccion.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(originPatterns = "*")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    
    private final AuthService authService;

    /**
     * Login con PIN de 4 dígitos
     * POST /api/auth/login-pin
     * Body: { "pin": "1111" }
     * Respuesta: Usuario con lista de empresas permitidas
     */
    @PostMapping("/login-pin")
    public ResponseEntity<?> loginConPin(@RequestBody LoginPinRequest request) {
        try {
            log.info("🔐 POST /api/auth/login-pin - Intento de login");
            
            if (request.getPin() == null || request.getPin().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El PIN es obligatorio");
            }

            LoginResponse response = authService.loginConPin(request);
            
            log.info("✅ Login exitoso para usuario: {}", response.getNombre());
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            log.error("❌ Error en login: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("PIN incorrecto o usuario inactivo");
        } catch (Exception e) {
            log.error("❌ Error inesperado en login", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor");
        }
    }

    /**
     * Cambiar PIN de un usuario
     * PUT /api/auth/cambiar-pin/{userId}
     * Body: { "pinActual": "1111", "pinNuevo": "9999" }
     * Header: X-Super-Admin: true (opcional - omite validación de PIN actual)
     */
    @PutMapping("/cambiar-pin/{userId}")
    public ResponseEntity<?> cambiarPin(
            @PathVariable Long userId,
            @RequestBody CambiarPinRequest request,
            @RequestHeader(value = "X-Super-Admin", required = false) String superAdminHeader
    ) {
        try {
            log.info("🔐 PUT /api/auth/cambiar-pin/{} - Cambio de PIN solicitado", userId);
            
            boolean isSuperAdmin = "true".equalsIgnoreCase(superAdminHeader);
            authService.cambiarPin(userId, request, isSuperAdmin);
            
            log.info("✅ PIN cambiado exitosamente para usuario ID {}", userId);
            return ResponseEntity.ok("PIN actualizado exitosamente");
            
        } catch (RuntimeException e) {
            log.error("❌ Error cambiando PIN: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("❌ Error inesperado cambiando PIN", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor");
        }
    }

    /**
     * Verificar si un usuario tiene permiso para una empresa
     * GET /api/auth/verificar-permiso?userId=1&empresaId=2
     */
    @GetMapping("/verificar-permiso")
    public ResponseEntity<Boolean> verificarPermiso(
            @RequestParam Long userId,
            @RequestParam Long empresaId
    ) {
        try {
            boolean tienePermiso = authService.tienePermisoEmpresa(userId, empresaId);
            return ResponseEntity.ok(tienePermiso);
        } catch (Exception e) {
            log.error("❌ Error verificando permiso", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

    /**
     * Health check
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth service OK");
    }
}
