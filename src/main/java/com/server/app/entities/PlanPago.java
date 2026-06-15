package com.server.app.entities;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.server.app.entities.enums.EstadoPlanPago;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "plan_pagos")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer numeroCuota;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal montoCapital;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal montoInteres;

    @Column(nullable = false)
    private LocalDate fechaVencimiento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoPlanPago estado = EstadoPlanPago.PENDIENTE;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "prestamo_id", nullable = false)
    // Evita re-serializar el usuario completo (con su rol y permisos) en cada cuota
    @JsonIgnoreProperties({ "usuario" })
    private Prestamo prestamo;
}