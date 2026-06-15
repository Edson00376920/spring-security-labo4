package com.server.app.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.server.app.entities.PlanPago;
import com.server.app.entities.enums.EstadoPlanPago;

public interface PlanPagoRepository extends JpaRepository<PlanPago, Long> {

    List<PlanPago> findByPrestamoIdOrderByNumeroCuotaAsc(Long prestamoId);

    List<PlanPago> findByPrestamoIdAndEstadoOrderByNumeroCuotaAsc(Long prestamoId, EstadoPlanPago estado);

    long countByPrestamoIdAndEstado(Long prestamoId, EstadoPlanPago estado);
}