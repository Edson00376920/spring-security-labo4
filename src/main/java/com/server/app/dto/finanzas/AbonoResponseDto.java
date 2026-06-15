package com.server.app.dto.finanzas;

import com.server.app.entities.Abono;
import com.server.app.entities.PlanPago;


public record AbonoResponseDto(
        Abono abono,
        PlanPago planPago,
        boolean prestamoLiquidado,
        String mensaje
) {}