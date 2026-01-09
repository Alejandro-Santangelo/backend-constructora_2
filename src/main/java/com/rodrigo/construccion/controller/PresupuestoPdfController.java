package com.rodrigo.construccion.controller;

import com.rodrigo.construccion.dto.response.PresupuestoPdfResponseDTO;
import com.rodrigo.construccion.model.entity.PresupuestoNoCliente;
import com.rodrigo.construccion.model.entity.PresupuestoPdf;
import com.rodrigo.construccion.repository.PresupuestoNoClienteRepository;
import com.rodrigo.construccion.repository.PresupuestoPdfRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/presupuestos-no-cliente-pdf")
@CrossOrigin(origins = "http://localhost:8080")
public class PresupuestoPdfController {

    @Autowired
    private PresupuestoPdfRepository pdfRepository;

    @Autowired
    private PresupuestoNoClienteRepository presupuestoRepository;

    // 1. SUBIR PDF
    @PostMapping("/{id}/pdf")
    public ResponseEntity<?> subirPDF(
            @PathVariable Long id,
            @RequestParam Long empresaId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("nombre_archivo") String nombreArchivo,
            @RequestParam("version_presupuesto") Integer versionPresupuesto,
            @RequestParam("incluye_honorarios") String incluyeHonorariosStr,
            @RequestParam("incluye_configuracion") String incluyeConfiguracionStr) {

        try {
            // Validar que el presupuesto existe
            PresupuestoNoCliente presupuesto = presupuestoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Presupuesto no encontrado"));

            // Validar empresa
            if (!presupuesto.getEmpresa().getId().equals(empresaId)) {
                return ResponseEntity.status(403).body("No autorizado");
            }

            // Validar archivo
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("El archivo está vacío");
            }

            if (!"application/pdf".equals(file.getContentType())) {
                return ResponseEntity.badRequest().body("El archivo debe ser PDF");
            }

            if (file.getSize() > 50 * 1024 * 1024) { // 50MB
                return ResponseEntity.badRequest().body("El archivo excede el tamaño máximo (50MB)");
            }

            // Convertir strings a boolean
            Boolean incluyeHonorarios = Boolean.parseBoolean(incluyeHonorariosStr);
            Boolean incluyeConfiguracion = Boolean.parseBoolean(incluyeConfiguracionStr);

            // Crear entidad
            PresupuestoPdf pdf = new PresupuestoPdf();
            pdf.setPresupuestoId(id);
            pdf.setNombreArchivo(nombreArchivo);
            pdf.setContenidoPdf(file.getBytes());
            pdf.setTamanioBytes(file.getSize());
            pdf.setFechaGeneracion(LocalDateTime.now());
            pdf.setVersionPresupuesto(versionPresupuesto);
            pdf.setIncluyeHonorarios(incluyeHonorarios);
            pdf.setIncluyeConfiguracion(incluyeConfiguracion);

            // Guardar
            PresupuestoPdf pdfGuardado = pdfRepository.save(pdf);

            // Retornar DTO (sin el contenido binario)
            PresupuestoPdfResponseDTO dto = new PresupuestoPdfResponseDTO();
            dto.setId(pdfGuardado.getId());
            dto.setPresupuestoId(pdfGuardado.getPresupuestoId());
            dto.setNombreArchivo(pdfGuardado.getNombreArchivo());
            dto.setTamanioBytes(pdfGuardado.getTamanioBytes());
            dto.setFechaGeneracion(pdfGuardado.getFechaGeneracion());
            dto.setGeneradoPor(pdfGuardado.getGeneradoPor());
            dto.setVersionPresupuesto(pdfGuardado.getVersionPresupuesto());
            dto.setIncluyeHonorarios(pdfGuardado.getIncluyeHonorarios());
            dto.setIncluyeConfiguracion(pdfGuardado.getIncluyeConfiguracion());

            return ResponseEntity.status(HttpStatus.CREATED).body(dto);

        } catch (Exception e) {
            e.printStackTrace(); // Log completo del error
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error al guardar PDF");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // 2. LISTAR PDFs
    @GetMapping("/{id}/pdfs")
    public ResponseEntity<?> listarPDFs(
            @PathVariable Long id,
            @RequestParam Long empresaId) {

        try {
            // Validar presupuesto y empresa
            PresupuestoNoCliente presupuesto = presupuestoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Presupuesto no encontrado"));

            if (!presupuesto.getEmpresa().getId().equals(empresaId)) {
                return ResponseEntity.status(403).body("No autorizado");
            }

            // Obtener PDFs
            List<PresupuestoPdf> pdfs = pdfRepository.findByPresupuestoIdOrderByFechaGeneracionDesc(id);

            // Convertir a DTOs
            List<PresupuestoPdfResponseDTO> dtos = pdfs.stream().map(pdf -> {
                PresupuestoPdfResponseDTO dto = new PresupuestoPdfResponseDTO();
                dto.setId(pdf.getId());
                dto.setPresupuestoId(pdf.getPresupuestoId());
                dto.setNombreArchivo(pdf.getNombreArchivo());
                dto.setTamanioBytes(pdf.getTamanioBytes());
                dto.setFechaGeneracion(pdf.getFechaGeneracion());
                dto.setGeneradoPor(pdf.getGeneradoPor());
                dto.setVersionPresupuesto(pdf.getVersionPresupuesto());
                dto.setIncluyeHonorarios(pdf.getIncluyeHonorarios());
                dto.setIncluyeConfiguracion(pdf.getIncluyeConfiguracion());
                return dto;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(dtos);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al listar PDFs: " + e.getMessage());
        }
    }

    // 3. DESCARGAR PDF
    @GetMapping("/pdf/{pdfId}")
    public ResponseEntity<?> descargarPDF(
            @PathVariable Long pdfId,
            @RequestParam Long empresaId) {

        try {
            // Buscar PDF
            PresupuestoPdf pdf = pdfRepository.findById(pdfId)
                    .orElseThrow(() -> new RuntimeException("PDF no encontrado"));

            // Validar empresa
            PresupuestoNoCliente presupuesto = presupuestoRepository.findById(pdf.getPresupuestoId())
                    .orElseThrow(() -> new RuntimeException("Presupuesto no encontrado"));

            if (!presupuesto.getEmpresa().getId().equals(empresaId)) {
                return ResponseEntity.status(403).body("No autorizado");
            }

            // Retornar bytes del PDF
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(
                    ContentDisposition.builder("attachment")
                            .filename(pdf.getNombreArchivo())
                            .build()
            );
            headers.setContentLength(pdf.getTamanioBytes());

            return new ResponseEntity<>(pdf.getContenidoPdf(), headers, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al descargar PDF: " + e.getMessage());
        }
    }

    // 4. ELIMINAR PDF
    @DeleteMapping("/pdf/{pdfId}")
    public ResponseEntity<?> eliminarPDF(
            @PathVariable Long pdfId,
            @RequestParam Long empresaId) {

        try {
            // Buscar PDF
            PresupuestoPdf pdf = pdfRepository.findById(pdfId)
                    .orElseThrow(() -> new RuntimeException("PDF no encontrado"));

            // Validar empresa
            PresupuestoNoCliente presupuesto = presupuestoRepository.findById(pdf.getPresupuestoId())
                    .orElseThrow(() -> new RuntimeException("Presupuesto no encontrado"));

            if (!presupuesto.getEmpresa().getId().equals(empresaId)) {
                return ResponseEntity.status(403).body("No autorizado");
            }

            // Eliminar
            pdfRepository.deleteById(pdfId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "PDF eliminado exitosamente");
            response.put("id", pdfId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al eliminar PDF: " + e.getMessage());
        }
    }
}
