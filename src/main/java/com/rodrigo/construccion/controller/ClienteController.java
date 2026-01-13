package com.rodrigo.construccion.controller;

import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rodrigo.construccion.dto.request.ClienteRequestDTO;
import com.rodrigo.construccion.dto.response.ClienteResponseDTO;
import com.rodrigo.construccion.service.IClienteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "Gestión de clientes del sistema multi-tenant")
public class ClienteController {

    private final IClienteService clienteService;

    /* Crear un nuevo cliente */
    @PostMapping
    @Operation(summary = "Registrar nuevo cliente", description = "Registra un nuevo cliente asociado a una empresa específica en el sistema multi-tenant. "
            + "Cada cliente pertenece exclusivamente a una empresa (tenant). Campos requeridos: nombre, empresa. Campos opcionales: teléfono, email, dirección, tipo de cliente.")
    public ResponseEntity<ClienteResponseDTO> crearCliente(@RequestParam Long empresaId, @Valid @RequestBody ClienteRequestDTO clienteRequestDTO) {
        ClienteResponseDTO clienteResponseDto = clienteService.crearCliente(clienteRequestDTO, Collections.singletonList(empresaId));
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteResponseDto);
    }

    /* Actualizar cliente */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar cliente (id | cuit | nombre)", description = "Actualiza los datos de un cliente existente. El parámetro 'id' acepta: ID numérico, CUIT/CUIL o nombre del cliente.")
    public ResponseEntity<ClienteResponseDTO> actualizarCliente(
            @Parameter(description = "Identificador del cliente: id | cuit | nombre") @PathVariable("id") String id,
            @Parameter(description = "ID de la empresa para el contexto de la búsqueda, si el identificador no es numérico") @RequestParam(required = false) Long empresaId,
            @Valid @RequestBody ClienteRequestDTO clienteRequestDTO) {

        Long resolvedId = clienteService.resolveIdentifierToId(id, empresaId);
        ClienteResponseDTO responseDTO = clienteService.actualizarCliente(resolvedId, clienteRequestDTO);
        return ResponseEntity.ok(responseDTO);
    }

    /* Eliminar cliente */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar cliente (id | cuit | nombre)", description = "Elimina un cliente del sistema (soft delete). El parámetro 'id' acepta: ID numérico, CUIT/CUIL o nombre del cliente. Requiere empresaId para validar pertenencia.")
    public ResponseEntity<String> eliminarCliente(
            @Parameter(description = "Identificador del cliente: id | cuit | nombre") @PathVariable("id") String idCliente,
            @Parameter(description = "ID de la empresa") @RequestParam(required = false) Long empresaId) {

        clienteService.eliminarCliente(idCliente, empresaId);
        return ResponseEntity.ok().body("Cliente eliminado exitosamente.");
    }

    /* ------------ CONSULTAS ESPECIALIZADAS ------------ */

    /* Obtener cliente por ID y tambien por empresa */
    @GetMapping("/{idCliente}")
    @Operation(summary = "Obtener cliente por ID", description = "Obtiene un cliente específico por su ID dentro de una empresa.")
    public ResponseEntity<ClienteResponseDTO> obtenerClientePorId(
            @Parameter(description = "ID del cliente") @PathVariable Long idCliente,
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {

        ClienteResponseDTO responseDTO = clienteService.obtenerPorIdYEmpresa(idCliente, empresaId);
        return ResponseEntity.ok(responseDTO);
    }

    /* Busqueda Universal de clientes */
    @GetMapping("/buscar")
    @Operation(summary = "Buscar cliente (id | cuit | nombre)", description = "Busca un cliente por ID, CUIT/CUIL o nombre dentro de una empresa. El parámetro 'q' acepta: ID numérico, CUIT/CUIL o nombre completo/parcial del cliente.")
    public ResponseEntity<ClienteResponseDTO> buscarCliente(
            @Parameter(description = "Identificador universal: id, nombre o cuit/cuil") @RequestParam("q") String q,
            @Parameter(description = "ID de la empresa (opcional, requerido para buscar por nombre)") @RequestParam(required = false) Long empresaId) {

        ClienteResponseDTO clienteEncontrado = clienteService.buscarYMapearPorIdentificador(q.trim(), empresaId);
        return ResponseEntity.ok(clienteEncontrado);
    }

    /* Obtener todos los clientes sin paginación */
    @GetMapping("/todos")
    @Operation(summary = "Listar todos los clientes de una empresa", description = "Obtiene todos los clientes de una empresa sin paginación.")
    public ResponseEntity<List<ClienteResponseDTO>> obtenerTodosLosClientes(
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {

        List<ClienteResponseDTO> clientesDto = clienteService.obtenerTodosPorEmpresa(empresaId);
        return ResponseEntity.ok(clientesDto);
    }

    /* Listar todos los clientes de todas las empresas */
    @GetMapping("/empresas")
    @Operation(summary = "Listar todos los clientes de todas las empresas", description = "Obtiene todos los clientes registrados en el sistema, sin importar la empresa.")
    public ResponseEntity<List<ClienteResponseDTO>> obtenerTodosLosClientesDeTodasLasEmpresas() {
        List<ClienteResponseDTO> clientesDto = clienteService.obtenerTodos();
        return ResponseEntity.ok(clientesDto);
    }

    /* Obtener todos los clientes con paginación */
    @GetMapping
    @Operation(summary = "Listar clientes formato paginacion", description = "Obtiene todos los clientes de una empresa con paginación. Campos válidos para ordenar: id, nombre, cuitCuil, direccion, telefono, email, fechaCreacion")
    @Parameters({
            @Parameter(name = "empresaId", description = "ID de la empresa", required = true, in = ParameterIn.QUERY),
            @Parameter(name = "page", description = "Número de página (0-based)", in = ParameterIn.QUERY),
            @Parameter(name = "size", description = "Tamaño de página", in = ParameterIn.QUERY),
            @Parameter(name = "sort", description = "Campo de ordenamiento. Formato: campo,asc|desc. Ej: nombre,desc", in = ParameterIn.QUERY)
    })
    public ResponseEntity<Page<ClienteResponseDTO>> obtenerClientes(
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId,
            @Parameter(hidden = true) @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {

        Page<ClienteResponseDTO> clienteDTOPage = clienteService.obtenerPorEmpresaConPaginacion(empresaId, pageable);
        return ResponseEntity.ok(clienteDTOPage);
    }
}
