package com.server.app.services;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.server.app.dto.finanzas.AbonoCreateDto;
import com.server.app.dto.finanzas.AbonoResponseDto;
import com.server.app.entities.Abono;
import com.server.app.entities.PlanPago;
import com.server.app.entities.Prestamo;
import com.server.app.entities.User;
import com.server.app.entities.enums.EstadoPlanPago;
import com.server.app.entities.enums.EstadoPrestamo;
import com.server.app.exceptions.BadRequestException;
import com.server.app.exceptions.ConfictException;
import com.server.app.exceptions.ForbiddenException;
import com.server.app.exceptions.NotFoundException;
import com.server.app.repositories.AbonoRepository;
import com.server.app.repositories.PlanPagoRepository;
import com.server.app.repositories.PrestamoRepository;
import com.server.app.utils.MoraUtils;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AbonoService {

    private final AbonoRepository abonoRepository;
    private final PlanPagoRepository planPagoRepository;
    private final PrestamoRepository prestamoRepository;

    /**
     * Registra el pago de una cuota del plan de pagos, calculando el
     * recargo por mora si la cuota se paga después de su vencimiento.
     */
    @Transactional
    public AbonoResponseDto registrar(AbonoCreateDto dto, User usuario) {

        PlanPago planPago = planPagoRepository.findById(dto.getPlanPagoId())
                .orElseThrow(() -> new NotFoundException("La cuota indicada no existe"));

        Prestamo prestamo = planPago.getPrestamo();

        if (prestamo.getUsuario().getId() != usuario.getId()) {
            throw new ForbiddenException("No tienes acceso a esta cuota");
        }

        if (planPago.getEstado() == EstadoPlanPago.PAGADO) {
            throw new ConfictException("La cuota " + planPago.getNumeroCuota() + " ya se encuentra pagada");
        }

        LocalDate fechaPago = dto.getFechaPago() != null ? dto.getFechaPago() : LocalDate.now();

        if (fechaPago.isAfter(LocalDate.now())) {
            throw new BadRequestException("La fecha de pago no puede ser futura");
        }

        BigDecimal montoCuota = planPago.getMontoCapital().add(planPago.getMontoInteres());
        BigDecimal recargoMora = MoraUtils.calcularRecargoMora(montoCuota, planPago.getFechaVencimiento(), fechaPago);
        BigDecimal montoEsperado = montoCuota.add(recargoMora);

        if (dto.getMonto().compareTo(montoEsperado) < 0) {
            throw new BadRequestException(
                    "El monto del abono es insuficiente. Se requiere al menos " + montoEsperado
                            + " (cuota: " + montoCuota + ", mora: " + recargoMora + ")");
        }

        Abono abono = Abono.builder()
                .monto(dto.getMonto())
                .fechaPago(fechaPago)
                .recargoMora(recargoMora)
                .planPago(planPago)
                .build();

        abono = abonoRepository.save(abono);

        planPago.setEstado(EstadoPlanPago.PAGADO);
        planPagoRepository.save(planPago);

        boolean prestamoLiquidado = actualizarEstadoPrestamo(prestamo);

        String mensaje = recargoMora.compareTo(BigDecimal.ZERO) > 0
                ? "Pago registrado con recargo por mora de " + recargoMora
                : "Pago registrado sin recargo por mora";

        return new AbonoResponseDto(abono, planPago, prestamoLiquidado, mensaje);
    }

    /**
     * Si ya no quedan cuotas pendientes, marca el préstamo como PAGADO.
     *
     * @return true si el préstamo quedó liquidado con este abono.
     */
    private boolean actualizarEstadoPrestamo(Prestamo prestamo) {
        long pendientes = planPagoRepository.countByPrestamoIdAndEstado(prestamo.getId(), EstadoPlanPago.PENDIENTE);

        if (pendientes == 0) {
            prestamo.setEstado(EstadoPrestamo.PAGADO);
            prestamoRepository.save(prestamo);
            return true;
        }

        return false;
    }
}