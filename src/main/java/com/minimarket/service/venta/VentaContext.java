package com.minimarket.service.venta;

import com.minimarket.model.Boleta;
import com.minimarket.model.DetalleVenta;
import com.minimarket.model.Usuario;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Contexto que viaja a través del Command + Chain of Responsibility.
 */
public class VentaContext {

    public enum TipoComprobante {
        BOLETA, FACTURA
    }

    private String numeroBoleta;
    private TipoComprobante tipoComprobante = TipoComprobante.BOLETA; // Por defecto BOLETA (venta rápida)
    private String nombreCliente;
    private String documentoCliente;
    private Boleta.MetodoPago metodoPago;
    private Usuario cajero;
    private final List<DetalleVenta> detalles = new ArrayList<>();
    private double total;
    private boolean requiereAutorizacionSupervisor;
    private boolean descuentoManual;
    private String observaciones;

    public String getNumeroBoleta() {
        return numeroBoleta;
    }

    public void setNumeroBoleta(String numeroBoleta) {
        this.numeroBoleta = numeroBoleta;
    }

    public TipoComprobante getTipoComprobante() {
        return tipoComprobante;
    }

    public void setTipoComprobante(TipoComprobante tipoComprobante) {
        this.tipoComprobante = tipoComprobante;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getDocumentoCliente() {
        return documentoCliente;
    }

    public void setDocumentoCliente(String documentoCliente) {
        this.documentoCliente = documentoCliente;
    }

    public Boleta.MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(Boleta.MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }

    public Usuario getCajero() {
        return cajero;
    }

    public void setCajero(Usuario cajero) {
        this.cajero = cajero;
    }

    public List<DetalleVenta> getDetalles() {
        return Collections.unmodifiableList(detalles);
    }

    public void agregarDetalle(DetalleVenta detalle) {
        this.detalles.add(detalle);
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public boolean isRequiereAutorizacionSupervisor() {
        return requiereAutorizacionSupervisor;
    }

    public void setRequiereAutorizacionSupervisor(boolean requiereAutorizacionSupervisor) {
        this.requiereAutorizacionSupervisor = requiereAutorizacionSupervisor;
    }

    public boolean isDescuentoManual() {
        return descuentoManual;
    }

    public void setDescuentoManual(boolean descuentoManual) {
        this.descuentoManual = descuentoManual;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}

