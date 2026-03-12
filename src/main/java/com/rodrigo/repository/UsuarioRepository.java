package com.rodrigo.repository;

import com.rodrigo.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    Optional<Usuario> findByEmail(String email);
    
    Optional<Usuario> findByIdEmpresa(Long idEmpresa);
    
    // Buscar por email O idEmpresa (para login flexible)
    Optional<Usuario> findByEmailOrIdEmpresa(String email, Long idEmpresa);
    
    boolean existsByEmail(String email);
}
