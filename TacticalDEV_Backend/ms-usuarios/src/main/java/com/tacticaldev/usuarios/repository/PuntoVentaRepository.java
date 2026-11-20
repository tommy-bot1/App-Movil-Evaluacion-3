package com.tacticaldev.usuarios.repository;

import com.tacticaldev.usuarios.model.PuntoVenta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PuntoVentaRepository extends JpaRepository<PuntoVenta, Long> {
    boolean existsByNombre(String nombre);
}
