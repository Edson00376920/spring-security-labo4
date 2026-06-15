package com.server.app.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;


public final class MoraUtils {

    public static final BigDecimal TASA_MORA_DIARIA = new BigDecimal("0.001");

    private MoraUtils() {
    }


    public static BigDecimal calcularRecargoMora(BigDecimal montoCuota, LocalDate fechaVencimiento, LocalDate fechaPago) {
        if (fechaPago == null || fechaVencimiento == null || !fechaPago.isAfter(fechaVencimiento)) {
            return BigDecimal.ZERO;
        }

        long diasMora = ChronoUnit.DAYS.between(fechaVencimiento, fechaPago);

        return montoCuota
                .multiply(TASA_MORA_DIARIA)
                .multiply(BigDecimal.valueOf(diasMora))
                .setScale(2, RoundingMode.HALF_UP);
    }
}