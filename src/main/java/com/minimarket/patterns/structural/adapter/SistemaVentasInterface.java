package com.minimarket.patterns.structural.adapter;

import com.minimarket.models.Producto;
import com.minimarket.models.Usuario;
import com.minimarket.models.Boleta;
import java.util.List;

/**
 * Interfaz común para el sistema de ventas
 * Permite adaptar funcionalidades según el rol del usuario
 */
public interface SistemaVentasInterface {
    
    // Operaciones básicas disponibles para todos los roles
    List<Producto> consultarProductos();
    Producto buscarProductoPorCodigo(String codigo);
    boolean verificarStock(Long productoId, Integer cantidad);
    
    // Operaciones de venta
    Boleta registrarVenta(List<Producto> productos, List<Integer> cantidades, 
                         Boleta.MetodoPago metodoPago, Usuario cajera);
    
    // Operaciones que pueden variar según el rol
    boolean puedeModificarPrecios();
    boolean puedeAccederReportes();
    boolean puedeGestionarInventario();
    boolean puedeAnularVentas();
    boolean puedeModificarUsuarios();
    
    // Operaciones específicas según permisos
    void mostrarMenuDisponible();
    List<String> obtenerOpcionesMenu();
}
