package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.TrabajoExtraGastoGeneral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para TrabajoExtraGastoGeneral.
 */
@Repository
public interface TrabajoExtraGastoGeneralRepository extends JpaRepository<TrabajoExtraGastoGeneral, Long> {
    
    List<TrabajoExtraGastoGeneral> findByItemCalculadoraId(Long itemCalculadoraId);
    
    List<TrabajoExtraGastoGeneral> findByItemCalculadoraIdOrderByOrdenAsc(Long itemCalculadoraId);
    
    void deleteByItemCalculadoraId(Long itemCalculadoraId);
}
