package com.server.app.dto.finanzas;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PrestamoCreateDto {

    @NotNull(message = "El capital solicitado es obligatorio")
    @DecimalMin(value = "0.01", message = "El capital solicitado debe ser mayor a 0")
    @Digits(integer = 13, fraction = 2, message = "El capital solicitado no tiene un formato válido")
    private BigDecimal capitalSolicitado;

    @NotNull(message = "La tasa de interés anual es obligatoria")
    @DecimalMin(value = "0.0", inclusive = false, message = "La tasa de interés debe ser mayor a 0")
    @DecimalMax(value = "100.0", message = "La tasa de interés no puede ser mayor a 100")
    private BigDecimal tasaInteresAnual;

    @NotNull(message = "El plazo en meses es obligatorio")
    @Min(value = 1, message = "El plazo debe ser de al menos 1 mes")
    @Max(value = 360, message = "El plazo no puede ser mayor a 360 meses")
    private Integer plazoMeses;
}