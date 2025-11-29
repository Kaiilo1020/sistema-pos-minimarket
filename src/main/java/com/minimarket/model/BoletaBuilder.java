package com.minimarket.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Builder explícito para construir boletas válidas antes de persistirlas.
 * Garantiza validaciones obligatorias (cliente, productos, totales, método de pago).
 */
public class BoletaBuilder {

    private final Boleta boleta;

    public BoletaBuilder() {
        this.boleta = new Boleta();
    }

    public BoletaBuilder numero(String numero) {
        boleta.setNumero(numero);
        return this;
    }

    public BoletaBuilder fecha(LocalDateTime fechaHora) {
        boleta.setFechaHora(fechaHora);
        return this;
    }

    public BoletaBuilder cajera(Usuario cajera) {
        boleta.setCajera(cajera);
        return this;
    }

    public BoletaBuilder metodoPago(Boleta.MetodoPago metodoPago) {
        boleta.setMetodoPago(metodoPago);
        return this;
    }

    public BoletaBuilder agregarDetalle(DetalleVenta detalle) {
        boleta.getDetalles().add(detalle);
        return this;
    }

    public BoletaBuilder observaciones(String observaciones) {
        boleta.setObservaciones(observaciones);
        return this;
    }

    public Boleta build() {
        validarCamposObligatorios();
        boleta.calcularTotales();
        if (!boleta.esValida()) {
            throw new IllegalStateException("La boleta no tiene todos los datos requeridos");
        }
        return boleta;
    }

    private void validarCamposObligatorios() {
        Objects.requireNonNull(boleta.getNumero(), "El número de boleta es obligatorio");
        Objects.requireNonNull(boleta.getCajera(), "Debe asignarse una cajera");
        Objects.requireNonNull(boleta.getMetodoPago(), "El método de pago es requerido");
        if (boleta.getDetalles().isEmpty()) {
            throw new IllegalStateException("La boleta debe contener al menos un detalle");
        }
        BigDecimal total = boleta.getDetalles().stream()
                .map(DetalleVenta::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("El total de la boleta debe ser mayor a cero");
        }
    }
}

