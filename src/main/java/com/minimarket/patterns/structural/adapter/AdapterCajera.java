package com.minimarket.patterns.structural.adapter;

import com.minimarket.models.Usuario;
import com.minimarket.security.Rol;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter para Cajeras - Restringe funcionalidades según el rol
 * Resuelve: Accesos errados de cajeras a funciones no autorizadas
 */
public class AdapterCajera extends SistemaVentasBase {
    
    private Usuario cajera;
    
    public AdapterCajera(Usuario cajera) {
        super();
        if (cajera.getRol() != Rol.CAJERO) {
            throw new IllegalArgumentException("Este adapter solo es para usuarios con rol CAJERA");
        }
        this.cajera = cajera;
    }
    
    @Override
    public boolean puedeModificarPrecios() {
        return false; // Las cajeras NO pueden modificar precios
    }
    
    @Override
    public boolean puedeAccederReportes() {
        return false; // Las cajeras NO pueden acceder a reportes
    }
    
    @Override
    public boolean puedeGestionarInventario() {
        return false; // Las cajeras NO pueden gestionar inventario
    }
    
    @Override
    public boolean puedeAnularVentas() {
        return false; // Las cajeras NO pueden anular ventas
    }
    
    @Override
    public boolean puedeModificarUsuarios() {
        return false; // Las cajeras NO pueden modificar usuarios
    }
    
    @Override
    public List<String> obtenerOpcionesMenu() {
        List<String> opciones = new ArrayList<>();
        opciones.add("Consultar productos");
        opciones.add("Buscar producto por código");
        opciones.add("Registrar venta");
        opciones.add("Verificar stock");
        opciones.add("Consultar mi perfil");
        return opciones;
    }
    
    @Override
    public void mostrarMenuDisponible() {
        System.out.println("=== MENÚ CAJERA - " + cajera.getNombreCompleto() + " ===");
        System.out.println("Funciones disponibles:");
        
        List<String> opciones = obtenerOpcionesMenu();
        for (int i = 0; i < opciones.size(); i++) {
            System.out.println((i + 1) + ". " + opciones.get(i));
        }
        
        System.out.println("\n⚠️  RESTRICCIONES:");
        System.out.println("- No puede modificar precios");
        System.out.println("- No puede acceder a reportes");
        System.out.println("- No puede gestionar inventario");
        System.out.println("- No puede anular ventas");
    }
    
    /**
     * Método específico para cajeras: validar producto antes de venta
     */
    public String validarProductoParaVenta(String codigoProducto) {
        var producto = buscarProductoPorCodigo(codigoProducto);
        
        if (producto == null) {
            return "❌ Producto no encontrado";
        }
        
        if (!producto.puedeVenderse()) {
            StringBuilder mensaje = new StringBuilder("❌ Producto no puede venderse:\n");
            
            if (producto.isRequiereLote() && (producto.getLote() == null || producto.getLote().trim().isEmpty())) {
                mensaje.append("- Falta información de lote\n");
            }
            
            if (producto.isRequiereFechaVencimiento() && producto.getFechaVencimiento() == null) {
                mensaje.append("- Falta fecha de vencimiento\n");
            }
            
            if (producto.getStock() <= 0) {
                mensaje.append("- Sin stock disponible\n");
            }
            
            mensaje.append("\n🔧 Contacte al supervisor para resolver este problema");
            return mensaje.toString();
        }
        
        StringBuilder info = new StringBuilder("✅ Producto disponible para venta:\n");
        info.append("Nombre: ").append(producto.getNombre()).append("\n");
        info.append("Precio: S/ ").append(producto.getPrecio()).append("\n");
        info.append("Stock: ").append(producto.getStock()).append(" unidades\n");
        
        if (producto.getLote() != null) {
            info.append("Lote: ").append(producto.getLote()).append("\n");
        }
        
        if (producto.getFechaVencimiento() != null) {
            info.append("Vence: ").append(producto.getFechaVencimiento()).append("\n");
        }
        
        // Alertas especiales
        if (producto.stockBajo()) {
            info.append("\n⚠️  ALERTA: Stock bajo (≤10 unidades)");
        }
        
        if (producto.proximoAVencer()) {
            info.append("\n⚠️  ALERTA: Producto próximo a vencer");
        }
        
        return info.toString();
    }
    
    /**
     * Obtiene información básica del usuario cajera
     */
    public Usuario getCajera() {
        return cajera;
    }
}
