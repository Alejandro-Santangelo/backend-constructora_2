    // ================== MAPPER COMPLETO Y ROBUSTO ==================
    
    private ObraResponseDTO mapToResponseDTO(Obra obra) {
        if (obra == null) return null;
        
        ObraResponseDTO dto = new ObraResponseDTO();
        
        // Campos básicos
        dto.setId(obra.getId());
        dto.setNombre(obra.getNombre());
        dto.setEstado(EstadoObra.fromDisplayName(obra.getEstado()));
        dto.setDescripcion(obra.getDescripcion());
        dto.setObservaciones(obra.getObservaciones());
        dto.setFechaInicio(obra.getFechaInicio());
        dto.setFechaFin(obra.getFechaFin());
        dto.setPresupuestoEstimado(obra.getPresupuestoEstimado());
        dto.setFechaCreacion(obra.getFechaCreacion());
        dto.setEmpresaId(obra.getEmpresaId());
        dto.setEsObraManual(obra.getEsObraManual());
        dto.setEsObraTrabajoExtra(obra.getEsObraTrabajoExtra());
        
        // Dirección (6 campos)
        dto.setDireccionObraCalle(obra.getDireccionObraCalle());
        dto.setDireccionObraAltura(obra.getDireccionObraAltura());
        dto.setDireccionObraBarrio(obra.getDireccionObraBarrio());
        dto.setDireccionObraTorre(obra.getDireccionObraTorre());
        dto.setDireccionObraPiso(obra.getDireccionObraPiso());
        dto.setDireccionObraDepartamento(obra.getDireccionObraDepartamento());
        
        // Presupuesto base (4 categorías)
        dto.setPresupuestoJornales(obra.getPresupuestoJornales());
        dto.setPresupuestoMateriales(obra.getPresupuestoMateriales());
        dto.setImporteGastosGeneralesObra(obra.getImporteGastosGeneralesObra());
        dto.setPresupuestoMayoresCostos(obra.getPresupuestoMayoresCostos());
        
        // Honorarios individuales (8 campos - 4 categorías x 2)
        dto.setHonorarioJornalesObra(obra.getHonorarioJornalesObra());
        dto.setTipoHonorarioJornalesObra(obra.getTipoHonorarioJornalesObra());
        dto.setHonorarioMaterialesObra(obra.getHonorarioMaterialesObra());
        dto.setTipoHonorarioMaterialesObra(obra.getTipoHonorarioMaterialesObra());
        dto.setHonorarioGastosGeneralesObra(obra.getHonorarioGastosGeneralesObra());
        dto.setTipoHonorarioGastosGeneralesObra(obra.getTipoHonorarioGastosGeneralesObra());
        dto.setHonorarioMayoresCostosObra(obra.getHonorarioMayoresCostosObra());
        dto.setTipoHonorarioMayoresCostosObra(obra.getTipoHonorarioMayoresCostosObra());
        
        // Descuentos sobre importes base (8 campos)
        dto.setDescuentoJornalesObra(obra.getDescuentoJornalesObra());
        dto.setTipoDescuentoJornalesObra(obra.getTipoDescuentoJornalesObra());
        dto.setDescuentoMaterialesObra(obra.getDescuentoMaterialesObra());
        dto.setTipoDescuentoMaterialesObra(obra.getTipoDescuentoMaterialesObra());
        dto.setDescuentoGastosGeneralesObra(obra.getDescuentoGastosGeneralesObra());
        dto.setTipoDescuentoGastosGeneralesObra(obra.getTipoDescuentoGastosGeneralesObra());
        dto.setDescuentoMayoresCostosObra(obra.getDescuentoMayoresCostosObra());
        dto.setTipoDescuentoMayoresCostosObra(obra.getTipoDescuentoMayoresCostosObra());
        
        // Descuentos sobre honorarios (8 campos)
        dto.setDescuentoHonorarioJornalesObra(obra.getDescuentoHonorarioJornalesObra());
        dto.setTipoDescuentoHonorarioJornalesObra(obra.getTipoDescuentoHonorarioJornalesObra());
        dto.setDescuentoHonorarioMaterialesObra(obra.getDescuentoHonorarioMaterialesObra());
        dto.setTipoDescuentoHonorarioMaterialesObra(obra.getTipoDescuentoHonorarioMaterialesObra());
        dto.setDescuentoHonorarioGastosGeneralesObra(obra.getDescuentoHonorarioGastosGeneralesObra());
        dto.setTipoDescuentoHonorarioGastosGeneralesObra(obra.getTipoDescuentoHonorarioGastosGeneralesObra());
        dto.setDescuentoHonorarioMayoresCostosObra(obra.getDescuentoHonorarioMayoresCostosObra());
        dto.setTipoDescuentoHonorarioMayoresCostosObra(obra.getTipoDescuentoHonorarioMayoresCostosObra());
        
        // Relaciones
        dto.setIdCliente(obra.getCliente() != null ? obra.getCliente().getId() : null);
        dto.setPresupuestoId(obra.getPresupuestoId());
        dto.setObraOrigenId(obra.getObraOrigenId());
        
        // Datos del cliente
        if (obra.getCliente() != null) {
            dto.setNombreSolicitante(obra.getCliente().getNombreSolicitante());
            dto.setTelefono(obra.getCliente().getTelefono());
            dto.setMail(obra.getCliente().getMail());
            dto.setDireccionParticular(obra.getCliente().getDireccionParticular());
        }
        
        return dto;
    }
    
    // Métodos auxiliares para listas
    private List<ObraResponseDTO> mapToResponseDTOList(List<Obra> obras) {
        if (obras == null || obras.isEmpty()) {
            return List.of();
        }
        return obras.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    private List<ObraSimpleDTO> mapToSimpleDTOList(List<Obra> obras) {
        if (obras == null || obras.isEmpty()) {
            return List.of();
        }
        return obras.stream()
                .map(this::mapToSimpleDTO)
                .collect(Collectors.toList());
    }