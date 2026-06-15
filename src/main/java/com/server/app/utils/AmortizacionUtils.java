package com.server.app.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.server.app.entities.PlanPago;
import com.server.app.entities.Prestamo;
import com.server.app.entities.enums.EstadoPlanPago;


public final class AmortizacionUtils {

    private static final int ESCALA_MONTO = 2;
    private static final int ESCALA_TASA = 10;

    private AmortizacionUtils() {
    }

    public static List<PlanPago> generarTablaAmortizacion(Prestamo prestamo) {
        BigDecimal capital = prestamo.getCapitalSolicitado();
        int plazo = prestamo.getPlazoMeses();
        BigDecimal tasaMensual = tasaMensual(prestamo.getTasaInteresAnual());
        BigDecimal cuotaFija = calcularCuotaFija(capital, tasaMensual, plazo);

        List<PlanPago> plan = new ArrayList<>();
        BigDecimal saldo = capital;
        LocalDate fechaBase = prestamo.getFechaSolicitud().toLocalDate();

        for (int numero = 1; numero <= plazo; numero++) {
            BigDecimal interesCuota = saldo.multiply(tasaMensual).setScale(ESCALA_MONTO, RoundingMode.HALF_UP);
            BigDecimal capitalCuota;

            if (numero == plazo) {
                // En la última cuota se ajusta el capital para cancelar el saldo exacto
                // y absorber posibles diferencias de redondeo de las cuotas anteriores.
                capitalCuota = saldo.setScale(ESCALA_MONTO, RoundingMode.HALF_UP);
            } else {
                capitalCuota = cuotaFija.subtract(interesCuota).setScale(ESCALA_MONTO, RoundingMode.HALF_UP);
            }

            saldo = saldo.subtract(capitalCuota);

            plan.add(PlanPago.builder()
                    .numeroCuota(numero)
                    .montoCapital(capitalCuota)
                    .montoInteres(interesCuota)
                    .fechaVencimiento(fechaBase.plusMonths(numero))
                    .estado(EstadoPlanPago.PENDIENTE)
                    .prestamo(prestamo)
                    .build());
        }

        return plan;
    }

    public static BigDecimal tasaMensual(BigDecimal tasaAnual) {
        return tasaAnual
                .divide(BigDecimal.valueOf(100), ESCALA_TASA, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(12), ESCALA_TASA, RoundingMode.HALF_UP);
    }


    private static BigDecimal calcularCuotaFija(BigDecimal capital, BigDecimal tasaMensual, int plazo) {
        if (tasaMensual.compareTo(BigDecimal.ZERO) == 0) {
            return capital.divide(BigDecimal.valueOf(plazo), ESCALA_MONTO, RoundingMode.HALF_UP);
        }

        BigDecimal unoMasTasa = BigDecimal.ONE.add(tasaMensual);
        BigDecimal factor = unoMasTasa.pow(plazo);
        BigDecimal numerador = capital.multiply(tasaMensual).multiply(factor);
        BigDecimal denominador = factor.subtract(BigDecimal.ONE);

        return numerador.divide(denominador, ESCALA_MONTO, RoundingMode.HALF_UP);
    }
}