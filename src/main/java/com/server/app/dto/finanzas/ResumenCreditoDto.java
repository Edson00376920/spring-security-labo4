package com.server.app.dto.finanzas;

import java.math.BigDecimal;
import java.util.List;

public record ResumenCreditoDto(
        int totalPrestamos,
        int prestamosActivos,
        BigDecimal capitalPendiente,
        BigDecimal interesPendiente,
        BigDecimal moraPendiente,
        BigDecimal moraPagadaHistorico,
        BigDecimal deudaTotal,
        int cuotasPendientes,
        int cuotasVencidas,
        List<PrestamoResumenDto> prestamos
) {}