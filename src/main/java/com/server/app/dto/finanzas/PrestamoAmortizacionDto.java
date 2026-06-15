package com.server.app.dto.finanzas;

import java.util.List;

import com.server.app.entities.PlanPago;
import com.server.app.entities.Prestamo;

public record PrestamoAmortizacionDto(
        Prestamo prestamo,
        List<PlanPago> tablaAmortizacion
) {}