package com.server.app.dto.finanzas;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;


@Data
public class AbonoCreateDto {

    @NotNull(message = "El plan de pago (cuota) es obligatorio")
    @Positive(message = "El ID del plan de pago debe ser un número positivo")
    private Long planPagoId;

    @NotNull(message = "El monto del abono es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal monto;

    /**
     * Fecha en la que se realiza el pago. Si no se envía, se asume la fecha actual.
     * Se utiliza para calcular el recargo por mora si la cuota está vencida.
     */
    private LocalDate fechaPago;
}