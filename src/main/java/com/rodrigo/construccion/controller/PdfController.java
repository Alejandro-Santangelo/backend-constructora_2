package com.rodrigo.construccion.controller;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.util.List;

@RestController
@RequestMapping("/pdf")
@CrossOrigin(origins = "http://localhost:5173")
public class PdfController {

    @GetMapping
    @CrossOrigin(origins = "*", exposedHeaders = "Content-Disposition")
    public void generatePdf(HttpServletResponse response) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, baos);

            document.open();

            // Título centrado con fuente estándar
            BaseFont baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED);
            Font titleFont = new Font(baseFont, 18, Font.BOLD);
            Paragraph title = new Paragraph("Título del Documento", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            // Párrafo de texto con fuente estándar
            Font bodyFont = new Font(baseFont, 12, Font.NORMAL);
            Paragraph paragraph = new Paragraph("Este es un PDF que se abrirá en el navegador.", bodyFont);
            document.add(paragraph);

            document.close();

            // Configurar encabezados HTTP para abrir en el navegador
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename=documento.pdf");
            response.setContentLength(baos.size());
            
            // Escribir y cerrar inmediatamente
            response.getOutputStream().write(baos.toByteArray());
            response.getOutputStream().flush();
            response.getOutputStream().close();
            
            // IMPORTANTE: NO HACER NADA MÁS DESPUÉS DE ESTO
            return;
            
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try {
                response.getWriter().write("Error generando PDF: " + e.getMessage());
            } catch (Exception ex) {
                // Si ya no se puede escribir al response, no hacer nada
            }
        }
    }

    @PostMapping("/generar")
    public ResponseEntity<byte[]> generarPresupuestoPdf(@RequestBody PresupuestoRequest presupuesto) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();

            // Encabezado con logo o nombre de la empresa
            Paragraph header = new Paragraph("Nombre de la Empresa", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20));
            header.setAlignment(Element.ALIGN_CENTER);
            document.add(header);
            document.add(new Paragraph("\n"));

            // Datos del cliente y fecha
            document.add(new Paragraph("Cliente: " + presupuesto.getCliente()));
            document.add(new Paragraph("Dirección: " + presupuesto.getDireccion()));
            document.add(new Paragraph("Fecha: " + presupuesto.getFecha()));
            document.add(new Paragraph("\n"));

            // Tabla con materiales
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{4, 2, 2, 2});

            // Encabezados de la tabla
            table.addCell(new PdfPCell(new Phrase("Material")));
            table.addCell(new PdfPCell(new Phrase("Cantidad")));
            table.addCell(new PdfPCell(new Phrase("Precio Unitario")));
            table.addCell(new PdfPCell(new Phrase("Subtotal")));

            // Filas de la tabla
            for (PresupuestoRequest.Material material : presupuesto.getMateriales()) {
                table.addCell(material.getNombre());
                table.addCell(String.valueOf(material.getCantidad()));
                table.addCell(String.format("$%.2f", material.getPrecioUnitario()));
                table.addCell(String.format("$%.2f", material.getSubtotal()));
            }
            document.add(table);
            document.add(new Paragraph("\n"));

            // Totales
            document.add(new Paragraph("Mano de Obra: $" + presupuesto.getManoDeObra()));
            document.add(new Paragraph("Otros Gastos: $" + presupuesto.getOtrosGastos()));
            document.add(new Paragraph("Total: $" + presupuesto.getTotal()));
            document.add(new Paragraph("\n"));

            // Pie con firma o aclaración
            Paragraph footer = new Paragraph("Firma: __________________________", FontFactory.getFont(FontFactory.HELVETICA, 12));
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=presupuesto.pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                    .body(baos.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Clase interna para representar el objeto de solicitud
    public static class PresupuestoRequest {
        private String cliente;
        private String direccion;
        private String fecha;
        private List<Material> materiales;
        private double manoDeObra;
        private double otrosGastos;
        private double total;

        public String getCliente() {
            return cliente;
        }

        public void setCliente(String cliente) {
            this.cliente = cliente;
        }

        public String getDireccion() {
            return direccion;
        }

        public void setDireccion(String direccion) {
            this.direccion = direccion;
        }

        public String getFecha() {
            return fecha;
        }

        public void setFecha(String fecha) {
            this.fecha = fecha;
        }

        public List<Material> getMateriales() {
            return materiales;
        }

        public void setMateriales(List<Material> materiales) {
            this.materiales = materiales;
        }

        public double getManoDeObra() {
            return manoDeObra;
        }

        public void setManoDeObra(double manoDeObra) {
            this.manoDeObra = manoDeObra;
        }

        public double getOtrosGastos() {
            return otrosGastos;
        }

        public void setOtrosGastos(double otrosGastos) {
            this.otrosGastos = otrosGastos;
        }

        public double getTotal() {
            return total;
        }

        public void setTotal(double total) {
            this.total = total;
        }

        public static class Material {
            private String nombre;
            private int cantidad;
            private double precioUnitario;
            private double subtotal;

            public String getNombre() {
                return nombre;
            }

            public void setNombre(String nombre) {
                this.nombre = nombre;
            }

            public int getCantidad() {
                return cantidad;
            }

            public void setCantidad(int cantidad) {
                this.cantidad = cantidad;
            }

            public double getPrecioUnitario() {
                return precioUnitario;
            }

            public void setPrecioUnitario(double precioUnitario) {
                this.precioUnitario = precioUnitario;
            }

            public double getSubtotal() {
                return subtotal;
            }

            public void setSubtotal(double subtotal) {
                this.subtotal = subtotal;
            }
        }
    }
}