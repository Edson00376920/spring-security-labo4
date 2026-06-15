package com.server.app.dto.finanzas;

import java.math.BigDecimal;

import com.server.app.entities.enums.EstadoPrestamo;

public record PrestamoResumenDto(
        Long prestamoId,
        BigDecimal capitalSolicitado,
        BigDecimal tasaInteresAnual,
        Integer plazoMeses,
        EstadoPrestamo estado,
        BigDecimal saldoPendiente,
        int cuotasPagadas,
        int cuotasPendientes,
        int cuotasVencidas
) {}