package com.minimarket.patterns.behavioral.chain.handlers;

import com.minimarket.patterns.behavioral.chain.*;
import com.minimarket.models.Usuario;
import com.minimarket.security.Rol;
import com.minimarket.patterns.creational.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Manejador para aprobaciones de nivel Supervisor
 * Maneja solicitudes de nivel intermedio que requieren supervisión
 */
public class ManejadorSupervisor extends ManejadorAprobacion {
    
    private Usuario supervisor;
    private Connection connection;
    
    public ManejadorSupervisor(Usuario supervisor) {
        super(Rol.SUPERVISOR);
        this.supervisor = supervisor;
        this.connection = DatabaseConnection.getInstance().getConnection();
    }
    
    @Override
    protected ResultadoAprobacion procesarAprobacion(SolicitudAprobacion solicitud) {
        System.out.println("🔍 Procesando solicitud en nivel SUPERVISOR...");
        System.out.println("   Supervisor: " + supervisor.getNombreCompleto());
        System.out.println("   Solicitud: " + solicitud.getDescripcion());
        
        // Verificar que el supervisor esté activo
        if (!supervisor.isActivo()) {
            return ResultadoAprobacion.rechazado(
                "El supervisor no está activo en el sistema", 
                "USUARIO_INACTIVO"
            );
        }
        
        // Procesar según el tipo de solicitud
        switch (solicitud.getTipo()) {
            case MODIFICAR_PRECIO:
                return procesarModificacionPrecio(solicitud);
                
            case ANULAR_VENTA:
                return procesarAnulacionVenta(solicitud);
                
            case DESCUENTO_ESPECIAL:
                return procesarDescuentoEspecial(solicitud);
                
            case VENTA_SIN_LOTE:
                return procesarVentaSinLote(solicitud);
                
            case OVERRIDE_PRECIO:
                return procesarOverridePrecio(solicitud);
                
            case ELIMINAR_PRODUCTO:
                return procesarEliminacionProducto(solicitud);
                
            case ACCESO_REPORTES:
                return procesarAccesoReportes(solicitud);
                
            default:
                // Para otros tipos, escalar al administrador
                return escalarSolicitud(solicitud);
        }
    }
    
    /**
     * Procesa solicitud de modificación de precio
     */
    private ResultadoAprobacion procesarModificacionPrecio(SolicitudAprobacion solicitud) {
        String productoId = solicitud.getParametro("productoId", String.class);
        java.math.BigDecimal nuevoPrecio = solicitud.getParametro("nuevoPrecio", java.math.BigDecimal.class);
        
        System.out.println("💰 Evaluando modificación de precio:");
        System.out.println("   Producto ID: " + productoId);
        System.out.println("   Nuevo precio: S/ " + nuevoPrecio);
        
        // Validar que el precio sea razonable
        if (nuevoPrecio.compareTo(new java.math.BigDecimal("0.10")) < 0) {
            return ResultadoAprobacion.rechazado(
                "El precio es demasiado bajo (menor a S/ 0.10)",
                "PRECIO_INVALIDO"
            );
        }
        
        if (nuevoPrecio.compareTo(new java.math.BigDecimal("1000")) > 0) {
            return ResultadoAprobacion.rechazado(
                "El precio es demasiado alto (mayor a S/ 1000) - Requiere aprobación de administrador",
                "PRECIO_EXCESIVO"
            );
        }
        
        // Registrar la modificación para auditoría
        registrarModificacionPrecio(productoId, nuevoPrecio, solicitud.getUsuarioSolicitante().getUsername());
        
        return ResultadoAprobacion.aprobado(
            "Modificación de precio aprobada por supervisor",
            supervisor.getNombreCompleto()
        ).conObservacion("Precio actualizado a S/ " + nuevoPrecio);
    }
    
    /**
     * Procesa solicitud de anulación de venta
     */
    private ResultadoAprobacion procesarAnulacionVenta(SolicitudAprobacion solicitud) {
        String numeroBoleta = solicitud.getParametro("numeroBoleta", String.class);
        String motivo = solicitud.getParametro("motivo", String.class);
        
        System.out.println("🚫 Evaluando anulación de venta:");
        System.out.println("   Boleta: " + numeroBoleta);
        System.out.println("   Motivo: " + motivo);
        
        // Validar que se proporcione un motivo válido
        if (motivo == null || motivo.trim().length() < 10) {
            return ResultadoAprobacion.requiereInformacion(
                "Se requiere un motivo detallado para la anulación",
                "Motivo debe tener al menos 10 caracteres"
            );
        }
        
        // Verificar que la venta existe y puede anularse
        if (!puedeAnularseVenta(numeroBoleta)) {
            return ResultadoAprobacion.rechazado(
                "La venta no puede anularse (muy antigua o ya anulada)",
                "VENTA_NO_ANULABLE"
            );
        }
        
        // Registrar la anulación
        registrarAnulacionVenta(numeroBoleta, motivo, supervisor.getUsername());
        
        return ResultadoAprobacion.aprobado(
            "Anulación de venta aprobada por supervisor",
            supervisor.getNombreCompleto()
        ).conObservacion("Motivo: " + motivo);
    }
    
    /**
     * Procesa solicitud de descuento especial
     */
    private ResultadoAprobacion procesarDescuentoEspecial(SolicitudAprobacion solicitud) {
        java.math.BigDecimal porcentajeDescuento = solicitud.getParametro("porcentajeDescuento", java.math.BigDecimal.class);
        String justificacion = solicitud.getParametro("justificacion", String.class);
        
        System.out.println("🎯 Evaluando descuento especial:");
        System.out.println("   Porcentaje: " + porcentajeDescuento + "%");
        System.out.println("   Justificación: " + justificacion);
        
        // Los supervisores pueden aprobar descuentos hasta 25%
        if (porcentajeDescuento.compareTo(new java.math.BigDecimal("25")) <= 0) {
            return ResultadoAprobacion.aprobado(
                "Descuento especial aprobado por supervisor",
                supervisor.getNombreCompleto()
            ).conObservacion("Descuento: " + porcentajeDescuento + "%")
             .conObservacion("Justificación: " + justificacion);
        }
        
        // Para descuentos mayores, escalar
        return escalarSolicitud(solicitud);
    }
    
    /**
     * Procesa solicitud de venta sin lote
     */
    private ResultadoAprobacion procesarVentaSinLote(SolicitudAprobacion solicitud) {
        String productoId = solicitud.getParametro("productoId", String.class);
        
        System.out.println("📦 Evaluando venta sin lote:");
        System.out.println("   Producto ID: " + productoId);
        
        // Los supervisores pueden aprobar ventas sin lote con justificación
        return ResultadoAprobacion.aprobado(
            "Venta sin lote aprobada por supervisor",
            supervisor.getNombreCompleto()
        ).conObservacion("Autorización especial de supervisor")
         .conObservacion("Producto: " + productoId);
    }
    
    /**
     * Procesa solicitud de override de precio
     */
    private ResultadoAprobacion procesarOverridePrecio(SolicitudAprobacion solicitud) {
        java.math.BigDecimal porcentajeDescuento = solicitud.getParametro("porcentajeDescuento", java.math.BigDecimal.class);
        
        // Los supervisores pueden aprobar overrides hasta 30%
        if (porcentajeDescuento.compareTo(new java.math.BigDecimal("30")) <= 0) {
            return ResultadoAprobacion.aprobado(
                "Override de precio aprobado por supervisor",
                supervisor.getNombreCompleto()
            ).conObservacion("Descuento autorizado: " + porcentajeDescuento + "%");
        }
        
        return escalarSolicitud(solicitud);
    }
    
    /**
     * Procesa solicitud de eliminación de producto
     */
    private ResultadoAprobacion procesarEliminacionProducto(SolicitudAprobacion solicitud) {
        String productoId = solicitud.getParametro("productoId", String.class);
        String motivo = solicitud.getParametro("motivo", String.class);
        
        System.out.println("🗑️ Evaluando eliminación de producto:");
        System.out.println("   Producto ID: " + productoId);
        System.out.println("   Motivo: " + motivo);
        
        // Verificar que no tenga stock o ventas recientes
        if (tieneStockOVentasRecientes(productoId)) {
            return ResultadoAprobacion.rechazado(
                "El producto tiene stock o ventas recientes - No puede eliminarse",
                "PRODUCTO_CON_MOVIMIENTO"
            );
        }
        
        return ResultadoAprobacion.aprobado(
            "Eliminación de producto aprobada por supervisor",
            supervisor.getNombreCompleto()
        ).conObservacion("Motivo: " + motivo);
    }
    
    /**
     * Procesa solicitud de acceso a reportes
     */
    private ResultadoAprobacion procesarAccesoReportes(SolicitudAprobacion solicitud) {
        String tipoReporte = solicitud.getParametro("tipoReporte", String.class);
        
        return ResultadoAprobacion.aprobado(
            "Acceso a reportes aprobado por supervisor",
            supervisor.getNombreCompleto()
        ).conObservacion("Tipo de reporte: " + tipoReporte);
    }
    
    /**
     * Escala la solicitud al administrador
     */
    private ResultadoAprobacion escalarSolicitud(SolicitudAprobacion solicitud) {
        return ResultadoAprobacion.escalado(
            "La solicitud requiere aprobación de administrador",
            "ADMINISTRADOR"
        ).conObservacion("Escalado desde nivel supervisor: " + supervisor.getNombreCompleto());
    }
    
    // Métodos auxiliares para validaciones
    private void registrarModificacionPrecio(String productoId, java.math.BigDecimal nuevoPrecio, String usuario) {
        // Implementación para registrar en auditoría
        System.out.println("📝 Registrando modificación de precio en auditoría...");
    }
    
    private void registrarAnulacionVenta(String numeroBoleta, String motivo, String supervisor) {
        // Implementación para registrar anulación
        System.out.println("📝 Registrando anulación de venta en auditoría...");
    }
    
    private boolean puedeAnularseVenta(String numeroBoleta) {
        // Verificar si la venta puede anularse (ejemplo: menos de 24 horas)
        return true; // Simplificado para el ejemplo
    }
    
    private boolean tieneStockOVentasRecientes(String productoId) {
        // Verificar stock y ventas recientes
        return false; // Simplificado para el ejemplo
    }
    
    /**
     * Muestra las capacidades de aprobación del supervisor
     */
    public void mostrarCapacidades() {
        System.out.println("=== CAPACIDADES DE APROBACIÓN - SUPERVISOR ===");
        System.out.println("Supervisor: " + supervisor.getNombreCompleto());
        System.out.println("Puede aprobar:");
        System.out.println("✅ Modificaciones de precio (hasta S/ 1000)");
        System.out.println("✅ Anulación de ventas");
        System.out.println("✅ Descuentos especiales (hasta 25%)");
        System.out.println("✅ Ventas sin lote");
        System.out.println("✅ Override de precios (hasta 30%)");
        System.out.println("✅ Eliminación de productos");
        System.out.println("✅ Acceso a reportes");
        System.out.println("\nRequiere escalamiento:");
        System.out.println("⬆️ Creación de usuarios");
        System.out.println("⬆️ Modificación de configuración");
        System.out.println("⬆️ Descuentos > 25%");
        System.out.println("⬆️ Precios > S/ 1000");
    }
    
    public Usuario getSupervisor() {
        return supervisor;
    }
}
