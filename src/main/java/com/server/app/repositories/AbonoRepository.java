package com.server.app.repositories;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.server.app.entities.Abono;

public interface AbonoRepository extends JpaRepository<Abono, Long> {

    @Query("SELECT COALESCE(SUM(a.recargoMora), 0) FROM Abono a " +
            "WHERE a.planPago.prestamo.usuario.id = :usuarioId")
    BigDecimal sumRecargoMoraByUsuarioId(@Param("usuarioId") int usuarioId);
}