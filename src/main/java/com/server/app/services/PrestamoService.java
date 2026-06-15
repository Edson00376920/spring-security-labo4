package com.server.app.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.server.app.dto.finanzas.PrestamoAmortizacionDto;
import com.server.app.dto.finanzas.PrestamoCreateDto;
import com.server.app.dto.finanzas.PrestamoResumenDto;
import com.server.app.dto.finanzas.ResumenCreditoDto;
import com.server.app.entities.PlanPago;
import com.server.app.entities.Prestamo;
import com.server.app.entities.User;
import com.server.app.entities.enums.EstadoPlanPago;
import com.server.app.entities.enums.EstadoPrestamo;
import com.server.app.exceptions.ForbiddenException;
import com.server.app.exceptions.NotFoundException;
import com.server.app.repositories.AbonoRepository;
import com.server.app.repositories.PlanPagoRepository;
import com.server.app.repositories.PrestamoRepository;
import com.server.app.utils.AmortizacionUtils;
import com.server.app.utils.MoraUtils;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class PrestamoService {

    private final PrestamoRepository prestamoRepository;
    private final PlanPagoRepository planPagoRepository;
    private final AbonoRepository abonoRepository;

    @Transactional
    public PrestamoAmortizacionDto solicitar(PrestamoCreateDto dto, User usuario) {
        Prestamo prestamo = Prestamo.builder()
                .capitalSolicitado(dto.getCapitalSolicitado())
                .tasaInteresAnual(dto.getTasaInteresAnual())
                .plazoMeses(dto.getPlazoMeses())
                .estado(EstadoPrestamo.APROBADO)
                .usuario(usuario)
                .build();

        prestamo = prestamoRepository.save(prestamo);

        List<PlanPago> tablaAmortizacion = AmortizacionUtils.generarTablaAmortizacion(prestamo);
        planPagoRepository.saveAll(tablaAmortizacion);

        return new PrestamoAmortizacionDto(prestamo, tablaAmortizacion);
    }

    /** Lista el historial de créditos solicitados por el usuario autenticado. */
    public Page<Prestamo> findAll(int page, int size, User usuario) {
        return prestamoRepository.findByUsuarioId(usuario.getId(), PageRequest.of(page, size));
    }

    public Prestamo findById(Long id, User usuario) {
        Prestamo prestamo = prestamoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Préstamo no encontrado"));

        if (prestamo.getUsuario().getId() != usuario.getId()) {
            throw new ForbiddenException("No tienes acceso a este préstamo");
        }

        return prestamo;
    }

    /** Consulta las cuotas pendientes de un préstamo del usuario autenticado. */
    public List<PlanPago> findPlanesPagoPendientes(Long prestamoId, User usuario) {
        Prestamo prestamo = findById(prestamoId, usuario);
        return planPagoRepository.findByPrestamoIdAndEstadoOrderByNumeroCuotaAsc(prestamo.getId(), EstadoPlanPago.PENDIENTE);
    }

    /** Obtiene el estado consolidado de deuda del usuario autenticado. */
    public ResumenCreditoDto resumenCredito(User usuario) {

        List<Prestamo> prestamos = prestamoRepository.findByUsuarioId(usuario.getId());

        BigDecimal capitalPendiente = BigDecimal.ZERO;
        BigDecimal interesPendiente = BigDecimal.ZERO;
        BigDecimal moraPendiente = BigDecimal.ZERO;
        int cuotasPendientes = 0;
        int cuotasVencidas = 0;
        int prestamosActivos = 0;

        List<PrestamoResumenDto> resumenes = new ArrayList<>();
        LocalDate hoy = LocalDate.now();

        for (Prestamo prestamo : prestamos) {
            List<PlanPago> cuotas = planPagoRepository.findByPrestamoIdOrderByNumeroCuotaAsc(prestamo.getId());

            BigDecimal saldoPrestamo = BigDecimal.ZERO;
            int pagadas = 0;
            int pendientes = 0;
            int vencidas = 0;

            for (PlanPago cuota : cuotas) {
                if (cuota.getEstado() == EstadoPlanPago.PAGADO) {
                    pagadas++;
                    continue;
                }

                BigDecimal montoCuota = cuota.getMontoCapital().add(cuota.getMontoInteres());
                saldoPrestamo = saldoPrestamo.add(montoCuota);
                capitalPendiente = capitalPendiente.add(cuota.getMontoCapital());
                interesPendiente = interesPendiente.add(cuota.getMontoInteres());
                pendientes++;
                cuotasPendientes++;

                if (cuota.getFechaVencimiento().isBefore(hoy)) {
                    vencidas++;
                    cuotasVencidas++;

                    BigDecimal mora = MoraUtils.calcularRecargoMora(montoCuota, cuota.getFechaVencimiento(), hoy);
                    moraPendiente = moraPendiente.add(mora);
                    saldoPrestamo = saldoPrestamo.add(mora);
                }
            }

            if (prestamo.getEstado() != EstadoPrestamo.PAGADO) {
                prestamosActivos++;
            }

            resumenes.add(new PrestamoResumenDto(
                    prestamo.getId(),
                    prestamo.getCapitalSolicitado(),
                    prestamo.getTasaInteresAnual(),
                    prestamo.getPlazoMeses(),
                    prestamo.getEstado(),
                    saldoPrestamo,
                    pagadas,
                    pendientes,
                    vencidas
            ));
        }

        BigDecimal moraPagadaHistorico = abonoRepository.sumRecargoMoraByUsuarioId(usuario.getId());
        BigDecimal deudaTotal = capitalPendiente.add(interesPendiente).add(moraPendiente);

        return new ResumenCreditoDto(
                prestamos.size(),
                prestamosActivos,
                capitalPendiente,
                interesPendiente,
                moraPendiente,
                moraPagadaHistorico,
                deudaTotal,
                cuotasPendientes,
                cuotasVencidas,
                resumenes
        );
    }
}