package com.server.app.controllers;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.server.app.dto.finanzas.AbonoCreateDto;
import com.server.app.dto.finanzas.AbonoResponseDto;
import com.server.app.dto.finanzas.PrestamoAmortizacionDto;
import com.server.app.dto.finanzas.PrestamoCreateDto;
import com.server.app.dto.finanzas.ResumenCreditoDto;
import com.server.app.dto.response.Pagination;
import com.server.app.dto.response.PaginationMeta;
import com.server.app.entities.PlanPago;
import com.server.app.entities.Prestamo;
import com.server.app.entities.User;
import com.server.app.services.AbonoService;
import com.server.app.services.PrestamoService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/finanzas")
public class FinanzasController {

    private final PrestamoService prestamoService;
    private final AbonoService abonoService;

    public FinanzasController(PrestamoService prestamoService, AbonoService abonoService) {
        this.prestamoService = prestamoService;
        this.abonoService = abonoService;
    }

    /** GET /api/finanzas/prestamos -> Lista el historial de créditos solicitados. */
    @GetMapping("/prestamos")
    public ResponseEntity<Pagination<Prestamo>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User usuario
    ) {
        Page<Prestamo> p = prestamoService.findAll(page, size, usuario);
        return ResponseEntity.ok(new Pagination<>(
                p.getContent(),
                new PaginationMeta(
                        p.getNumber(),
                        p.getSize(),
                        p.getTotalPages(),
                        p.getTotalElements()
                )
        ));
    }

    /** POST /api/finanzas/prestamos -> Solicita un préstamo y genera la tabla de amortización. */
    @PostMapping("/prestamos")
    public ResponseEntity<PrestamoAmortizacionDto> solicitar(
            @Valid @RequestBody PrestamoCreateDto dto,
            @AuthenticationPrincipal User usuario
    ) {
        return ResponseEntity.ok(prestamoService.solicitar(dto, usuario));
    }

    /** GET /api/finanzas/prestamos/{id}/planes-pago -> Consulta las cuotas pendientes de un préstamo. */
    @GetMapping("/prestamos/{id}/planes-pago")
    public ResponseEntity<List<PlanPago>> planesPago(
            @PathVariable Long id,
            @AuthenticationPrincipal User usuario
    ) {
        return ResponseEntity.ok(prestamoService.findPlanesPagoPendientes(id, usuario));
    }

    /** POST /api/finanzas/abonos -> Registra un pago de cuota y calcula mora si aplica. */
    @PostMapping("/abonos")
    public ResponseEntity<AbonoResponseDto> registrarAbono(
            @Valid @RequestBody AbonoCreateDto dto,
            @AuthenticationPrincipal User usuario
    ) {
        return ResponseEntity.ok(abonoService.registrar(dto, usuario));
    }

    /** GET /api/finanzas/resumen-credito -> Obtiene el estado consolidado de deuda del cliente. */
    @GetMapping("/resumen-credito")
    public ResponseEntity<ResumenCreditoDto> resumenCredito(@AuthenticationPrincipal User usuario) {
        return ResponseEntity.ok(prestamoService.resumenCredito(usuario));
    }
}