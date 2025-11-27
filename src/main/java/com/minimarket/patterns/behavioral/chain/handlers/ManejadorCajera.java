package com.minimarket.patterns.behavioral.chain.handlers;

import com.minimarket.patterns.behavioral.chain.*;
import com.minimarket.models.Usuario;
import com.minimarket.security.Rol;

/**
 * Manejador para aprobaciones de nivel Cajera
 * Maneja solicitudes básicas que no requieren supervisión
 */
public class ManejadorCajera extends ManejadorAprobacion {
    
    private Usuario cajera;
    
    public ManejadorCajera(Usuario cajera) {
        super(Rol.CAJERO);
        this.cajera = cajera;
    }
    
    @Override
    protected ResultadoAprobacion procesarAprobacion(SolicitudAprobacion solicitud) {
        System.out.println("🔍 Procesando solicitud en nivel CAJERA...");
        System.out.println("   Cajera: " + cajera.getNombreCompleto());
        System.out.println("   Solicitud: " + solicitud.getDescripcion());
        
        // Verificar que la cajera esté activa
        if (!cajera.isActivo()) {
            return ResultadoAprobacion.rechazado(
                "La cajera no está activa en el sistema", 
                "USUARIO_INACTIVO"
            );
        }
        
        // Procesar según el tipo de solicitud
        switch (solicitud.getTipo()) {
            case VENTA_SIN_LOTE:
                return procesarVentaSinLote(solicitud);
                
            case OVERRIDE_PRECIO:
                return procesarOverridePrecio(solicitud);
                
            default:
                // Para otros tipos, escalar al siguiente nivel
                return escalarSolicitud(solicitud);
        }
    }
    
    /**
     * Procesa solicitud de venta sin lote
     */
    private ResultadoAprobacion procesarVentaSinLote(SolicitudAprobacion solicitud) {
        String productoId = solicitud.getParametro("productoId", String.class);
        
        System.out.println("⚠️ Evaluando venta sin lote para producto: " + productoId);
        
        // Las cajeras pueden aprobar ventas sin lote solo para productos específicos
        // (por ejemplo, productos que por naturaleza no requieren lote)
        if (esProductoExentoLote(productoId)) {
            return ResultadoAprobacion.aprobado(
                "Venta sin lote aprobada - Producto exento de lote",
                cajera.getNombreCompleto()
            ).conObservacion("Producto no requiere lote por naturaleza");
        }
        
        // Si requiere lote, escalar al supervisor
        return escalarSolicitud(solicitud);
    }
    
    /**
     * Procesa solicitud de override de precio
     */
    private ResultadoAprobacion procesarOverridePrecio(SolicitudAprobacion solicitud) {
        java.math.BigDecimal nuevoPrecio = solicitud.getParametro("nuevoPrecio", java.math.BigDecimal.class);
        java.math.BigDecimal precioOriginal = solicitud.getParametro("precioOriginal", java.math.BigDecimal.class);
        
        if (nuevoPrecio == null || precioOriginal == null) {
            return ResultadoAprobacion.requiereInformacion(
                "Faltan datos de precios para evaluar la solicitud",
                "Se requiere precio original y nuevo precio"
            );
        }
        
        // Calcular porcentaje de descuento
        java.math.BigDecimal diferencia = precioOriginal.subtract(nuevoPrecio);
        java.math.BigDecimal porcentajeDescuento = diferencia.divide(precioOriginal, 4, 
            java.math.RoundingMode.HALF_UP).multiply(new java.math.BigDecimal("100"));
        
        System.out.println("💰 Evaluando override de precio:");
        System.out.println("   Precio original: S/ " + precioOriginal);
        System.out.println("   Nuevo precio: S/ " + nuevoPrecio);
        System.out.println("   Descuento: " + porcentajeDescuento + "%");
        
        // Las cajeras pueden aprobar descuentos menores al 5%
        if (porcentajeDescuento.compareTo(new java.math.BigDecimal("5")) <= 0) {
            return ResultadoAprobacion.aprobado(
                "Override de precio aprobado - Descuento menor al 5%",
                cajera.getNombreCompleto()
            ).conObservacion("Descuento aplicado: " + porcentajeDescuento + "%");
        }
        
        // Para descuentos mayores, escalar
        return escalarSolicitud(solicitud);
    }
    
    /**
     * Escala la solicitud al siguiente nivel
     */
    private ResultadoAprobacion escalarSolicitud(SolicitudAprobacion solicitud) {
        return ResultadoAprobacion.escalado(
            "La solicitud requiere aprobación de supervisor",
            "SUPERVISOR"
        ).conObservacion("Escalado desde nivel cajera: " + cajera.getNombreCompleto());
    }
    
    /**
     * Verifica si un producto está exento de requerir lote
     */
    private boolean esProductoExentoLote(String productoId) {
        // Lista de productos que no requieren lote (ejemplo)
        String[] productosExentos = {
            "BEBIDA_GASEOSA", "AGUA_MINERAL", "GALLETAS_SIMPLES", 
            "CARAMELOS", "CHICLES", "PRODUCTOS_LIMPIEZA_BASICOS"
        };
        
        for (String exento : productosExentos) {
            if (productoId.contains(exento)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Verifica las capacidades de aprobación de la cajera
     */
    public void mostrarCapacidades() {
        System.out.println("=== CAPACIDADES DE APROBACIÓN - CAJERA ===");
        System.out.println("Cajera: " + cajera.getNombreCompleto());
        System.out.println("Puede aprobar:");
        System.out.println("✅ Ventas sin lote (productos exentos)");
        System.out.println("✅ Override de precios (descuentos ≤ 5%)");
        System.out.println("\nRequiere escalamiento:");
        System.out.println("⬆️ Modificaciones de precio");
        System.out.println("⬆️ Anulación de ventas");
        System.out.println("⬆️ Descuentos > 5%");
        System.out.println("⬆️ Eliminación de productos");
    }
    
    public Usuario getCajera() {
        return cajera;
    }
}
