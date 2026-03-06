package com.rodrigo.construccion.controller;

import com.rodrigo.construccion.dto.request.EmpresaRequestDTO;
import com.rodrigo.construccion.dto.response.EmpresaEstadisticasDTO;
import com.rodrigo.construccion.dto.response.EmpresaEstadoResponseDTO;
import com.rodrigo.construccion.dto.response.EmpresaResponseDTO;
import com.rodrigo.construccion.dto.response.ValidacionResponseDTO;
import com.rodrigo.construccion.service.IEmpresaService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empresas")
@RequiredArgsConstructor
@Slf4j
public class EmpresaController {

    private final IEmpresaService empresaService;

    /* Obtener todas las empresas */
    @GetMapping
    @Operation(summary = "Listar todas las empresas", description = "Obtiene la lista completa de todas las empresas activas e inactivas del sistema.")
    public ResponseEntity<List<EmpresaResponseDTO>> obtenerTodasLasEmpresas() {
        List<EmpresaResponseDTO> empresas = empresaService.obtenerTodasLasEmpresas();
        return ResponseEntity.ok(empresas);
    }

    /* Crear una nueva empresa */
    @PostMapping
    @Operation(summary = "Registrar nueva empresa (tenant)", description = "Crea una nueva empresa en el sistema multi-tenant. Cada empresa funciona como un tenant independiente "
            + "con sus propios datos aislados. Campos requeridos: nombreEmpresa. Campos opcionales: CUIT, dirección, " +
            "teléfono, email, representante legal.")
    public ResponseEntity<EmpresaResponseDTO> crearEmpresa(@Valid @RequestBody EmpresaRequestDTO empresaRequestDTO) {
        EmpresaResponseDTO empresaCreada = empresaService.crearEmpresa(empresaRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(empresaCreada);
    }

    /* Actualizar empresa */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar empresa (id | cuit | nombre)", description = "Actualiza los datos de una empresa existente. El parámetro 'id' acepta: ID numérico, CUIT o nombre de la empresa.")
    public ResponseEntity<EmpresaResponseDTO> actualizarEmpresa(@PathVariable("id") String id, @Valid @RequestBody EmpresaRequestDTO empresaRequestDTO) {
        Long resolvedId = empresaService.buscarPorIdentificador(id).getId();
        EmpresaResponseDTO empresaDtoActualizada = empresaService.actualizarEmpresa(resolvedId, empresaRequestDTO);
        return ResponseEntity.ok(empresaDtoActualizada);
    }

    /* Eliminar (soft) empresa por identificador universal */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar empresa (id | cuit | nombre)", description = "Desactiva una empresa (soft delete). El parámetro 'id' acepta: ID numérico, CUIT o nombre de la empresa.")
    public ResponseEntity<String> eliminarEmpresa(@PathVariable("id") String id) {
        Long resolvedId = empresaService.buscarPorIdentificador(id).getId();
        empresaService.desactivarEmpresa(resolvedId);
        return ResponseEntity.noContent().build();
    }

    /* Obtener lista simple de empresas (sin metadatos de paginación) */
    @GetMapping("/simple")
    @Operation(summary = "Lista simple de todas las empresas", description = "Obtiene una lista simple de TODAS las empresas (activas e inactivas) sin metadatos de paginación. Usado por el selector de empresas en el frontend.")
    public ResponseEntity<List<EmpresaResponseDTO>> obtenerEmpresasSimple() {
        List<EmpresaResponseDTO> todasLasEmpresas = empresaService.obtenerTodasLasEmpresas();
        return ResponseEntity.ok(todasLasEmpresas);
    }

    /* Desactivar empresa */
    @PatchMapping("/{id}/desactivar")
    @Operation(summary = "Desactivar empresa (id | cuit | nombre)", description = "Desactiva una empresa (soft delete). El parámetro 'id' acepta: ID numérico, CUIT o nombre de la empresa. Los datos se mantienen pero no será accesible.")
    public ResponseEntity<String> desactivarEmpresa(@PathVariable("id") String id) {
        Long resolvedId = empresaService.buscarPorIdentificador(id).getId();
        empresaService.desactivarEmpresa(resolvedId);
        return ResponseEntity.ok().body("Empresa desactivada correctamente.");
    }

    /* Activar empresa */
    @PatchMapping("/{id}/activar")
    @Operation(summary = "Activar empresa (id | cuit | nombre)", description = "Activa una empresa que había sido desactivada. El parámetro 'id' acepta: ID numérico, CUIT o nombre de la empresa.")
    public ResponseEntity<String> activarEmpresa(@PathVariable("id") String id) {
        Long resolvedId = empresaService.buscarPorIdentificador(id).getId();
        empresaService.activarEmpresa(resolvedId);
        return ResponseEntity.ok().body("Empresa activada correctamente.");
    }

    /* CONSULTAS ESPECIALIZADAS */

    /* Obtener empresas activas solamente */
    @GetMapping("/activas")
    @Operation(summary = "Empresas activas", description = "Obtiene solo las empresas que están activas")
    public ResponseEntity<List<EmpresaResponseDTO>> obtenerEmpresasActivas() {
        List<EmpresaResponseDTO> listaEmpresasActivas = empresaService.obtenerEmpresasActivas();
        return ResponseEntity.ok(listaEmpresasActivas);
    }

    /* Obtener empresas con clientes */
    @GetMapping("/con-clientes")
    @Operation(summary = "Empresas con clientes", description = "Obtiene empresas que tienen al menos un cliente registrado")
    public ResponseEntity<List<EmpresaResponseDTO>> obtenerEmpresasConClientes() {
        List<EmpresaResponseDTO> empresasClientes = empresaService.obtenerEmpresasConClientes();
        return ResponseEntity.ok(empresasClientes);
    }

    /* Obtener estadísticas de empresas */
    @GetMapping("/estadisticas")
    @Operation(summary = "Estadísticas de empresas", description = "Obtiene estadísticas detalladas de todas las empresas")
    public ResponseEntity<List<EmpresaEstadisticasDTO>> obtenerEstadisticas() {
        List<EmpresaEstadisticasDTO> estadisticas = empresaService.obtenerEstadisticasEmpresas();
        return ResponseEntity.ok(estadisticas);
    }

    /* UTILIDADES Y VALIDACIONES */

    /* Verificar disponibilidad de CUIT */
    @GetMapping("/validar-cuit/{cuit}")
    @Operation(summary = "Validar CUIT", description = "Verifica si un CUIT está disponible para usar, que no este siendo usado por otra empresa en la bd.")
    public ResponseEntity<ValidacionResponseDTO> validarCuit(@PathVariable String cuit) {
        boolean disponible = empresaService.esCuitDisponible(cuit);
        return ResponseEntity.ok(new ValidacionResponseDTO(disponible));
    }

    /* Verificar estado de empresa */
    @GetMapping("/{id}/estado")
    @Operation(summary = "Estado de empresa (id | cuit | nombre)", description = "Verifica si una empresa existe y está activa. El parámetro 'id' acepta: ID numérico, CUIT o nombre de la empresa.")
    public ResponseEntity<EmpresaEstadoResponseDTO> verificarEstado(@PathVariable("id") String id) {
        Long resolvedId = empresaService.buscarPorIdentificador(id).getId();
        EmpresaEstadoResponseDTO estadoDto = empresaService.verificarEstado(resolvedId);
        return ResponseEntity.ok(estadoDto);
    }

    /* BÚSQUEDA UNIVERSAL: Buscar empresa por ID, CUIT o nombre de forma inteligente  */
    @GetMapping("/buscar")
    public ResponseEntity<EmpresaResponseDTO> buscarEmpresa(@RequestParam(name = "q") String identificador) {
        EmpresaResponseDTO empresa = empresaService.buscarPorIdentificador(identificador.trim());
        return ResponseEntity.ok(empresa);
    }
}