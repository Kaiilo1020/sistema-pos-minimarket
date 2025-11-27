package com.minimarket.patterns.structural.adapter;

import com.minimarket.models.*;
import com.minimarket.patterns.creational.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Implementación base del sistema de ventas
 * Contiene la lógica común para todas las funcionalidades
 */
public class SistemaVentasBase implements SistemaVentasInterface {
    
    protected Connection connection;
    protected static final Logger logger = Logger.getLogger(SistemaVentasBase.class.getName());
    
    public SistemaVentasBase() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }
    
    @Override
    public List<Producto> consultarProductos() {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT * FROM productos WHERE activo = true";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Producto producto = new Producto();
                producto.setId(rs.getLong("id"));
                producto.setCodigo(rs.getString("codigo"));
                producto.setNombre(rs.getString("nombre"));
                producto.setDescripcion(rs.getString("descripcion"));
                producto.setPrecio(rs.getBigDecimal("precio"));
                producto.setStock(rs.getInt("stock"));
                producto.setCategoria(rs.getString("categoria"));
                producto.setLote(rs.getString("lote"));
                
                if (rs.getDate("fecha_vencimiento") != null) {
                    producto.setFechaVencimiento(rs.getDate("fecha_vencimiento").toLocalDate());
                }
                
                producto.setRequiereLote(rs.getBoolean("requiere_lote"));
                producto.setRequiereFechaVencimiento(rs.getBoolean("requiere_fecha_vencimiento"));
                
                productos.add(producto);
            }
            
        } catch (SQLException e) {
            logger.severe("Error al consultar productos: " + e.getMessage());
            throw new RuntimeException("Error al acceder a la base de datos", e);
        }
        
        return productos;
    }
    
    @Override
    public Producto buscarProductoPorCodigo(String codigo) {
        String sql = "SELECT * FROM productos WHERE codigo = ? AND activo = true";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, codigo);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Producto producto = new Producto();
                    producto.setId(rs.getLong("id"));
                    producto.setCodigo(rs.getString("codigo"));
                    producto.setNombre(rs.getString("nombre"));
                    producto.setDescripcion(rs.getString("descripcion"));
                    producto.setPrecio(rs.getBigDecimal("precio"));
                    producto.setStock(rs.getInt("stock"));
                    producto.setCategoria(rs.getString("categoria"));
                    producto.setLote(rs.getString("lote"));
                    
                    if (rs.getDate("fecha_vencimiento") != null) {
                        producto.setFechaVencimiento(rs.getDate("fecha_vencimiento").toLocalDate());
                    }
                    
                    producto.setRequiereLote(rs.getBoolean("requiere_lote"));
                    producto.setRequiereFechaVencimiento(rs.getBoolean("requiere_fecha_vencimiento"));
                    
                    return producto;
                }
            }
            
        } catch (SQLException e) {
            logger.severe("Error al buscar producto por código: " + e.getMessage());
            throw new RuntimeException("Error al acceder a la base de datos", e);
        }
        
        return null;
    }
    
    @Override
    public boolean verificarStock(Long productoId, Integer cantidad) {
        String sql = "SELECT stock FROM productos WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, productoId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int stockActual = rs.getInt("stock");
                    return stockActual >= cantidad;
                }
            }
            
        } catch (SQLException e) {
            logger.severe("Error al verificar stock: " + e.getMessage());
            throw new RuntimeException("Error al acceder a la base de datos", e);
        }
        
        return false;
    }
    
    @Override
    public Boleta registrarVenta(List<Producto> productos, List<Integer> cantidades, 
                                Boleta.MetodoPago metodoPago, Usuario cajera) {
        
        // Validar que todos los productos puedan venderse
        for (int i = 0; i < productos.size(); i++) {
            Producto producto = productos.get(i);
            Integer cantidad = cantidades.get(i);
            
            if (!producto.puedeVenderse()) {
                throw new IllegalArgumentException(
                    "El producto " + producto.getNombre() + " no puede venderse. " +
                    "Verifique lote y fecha de vencimiento.");
            }
            
            if (!verificarStock(producto.getId(), cantidad)) {
                throw new IllegalArgumentException(
                    "Stock insuficiente para el producto: " + producto.getNombre());
            }
        }
        
        // Construir la boleta usando el patrón Builder
        Boleta.Builder boletaBuilder = new Boleta.Builder()
            .setNumero(generarNumeroBoleta())
            .setCajera(cajera)
            .setMetodoPago(metodoPago);
        
        // Agregar productos a la boleta
        for (int i = 0; i < productos.size(); i++) {
            Producto producto = productos.get(i);
            Integer cantidad = cantidades.get(i);
            boletaBuilder.agregarProducto(producto, cantidad, producto.getPrecio());
        }
        
        Boleta boleta = boletaBuilder.build();
        
        // Guardar la venta en la base de datos
        guardarVentaEnBD(boleta);
        
        // Actualizar stock
        actualizarStock(productos, cantidades);
        
        logger.info("Venta registrada exitosamente: " + boleta.getNumero());
        return boleta;
    }
    
    // Métodos por defecto que serán sobrescritos por los adaptadores
    @Override
    public boolean puedeModificarPrecios() { return false; }
    
    @Override
    public boolean puedeAccederReportes() { return false; }
    
    @Override
    public boolean puedeGestionarInventario() { return false; }
    
    @Override
    public boolean puedeAnularVentas() { return false; }
    
    @Override
    public boolean puedeModificarUsuarios() { return false; }
    
    @Override
    public void mostrarMenuDisponible() {
        System.out.println("=== MENÚ DEL SISTEMA ===");
        List<String> opciones = obtenerOpcionesMenu();
        for (int i = 0; i < opciones.size(); i++) {
            System.out.println((i + 1) + ". " + opciones.get(i));
        }
    }
    
    @Override
    public List<String> obtenerOpcionesMenu() {
        List<String> opciones = new ArrayList<>();
        opciones.add("Consultar productos");
        opciones.add("Registrar venta");
        return opciones;
    }
    
    // Métodos auxiliares privados
    private String generarNumeroBoleta() {
        return "BOL-" + System.currentTimeMillis();
    }
    
    private void guardarVentaEnBD(Boleta boleta) {
        // Implementación para guardar en base de datos
        String sql = "INSERT INTO ventas (numero, fecha_hora, cajera_id, metodo_pago, subtotal, igv, total) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, boleta.getNumero());
            stmt.setTimestamp(2, java.sql.Timestamp.valueOf(boleta.getFechaHora()));
            stmt.setLong(3, boleta.getCajera().getId());
            stmt.setString(4, boleta.getMetodoPago().name());
            stmt.setBigDecimal(5, boleta.getSubtotal());
            stmt.setBigDecimal(6, boleta.getIgv());
            stmt.setBigDecimal(7, boleta.getTotal());
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            logger.severe("Error al guardar venta: " + e.getMessage());
            throw new RuntimeException("Error al guardar la venta", e);
        }
    }
    
    private void actualizarStock(List<Producto> productos, List<Integer> cantidades) {
        String sql = "UPDATE productos SET stock = stock - ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < productos.size(); i++) {
                stmt.setInt(1, cantidades.get(i));
                stmt.setLong(2, productos.get(i).getId());
                stmt.addBatch();
            }
            
            stmt.executeBatch();
            
        } catch (SQLException e) {
            logger.severe("Error al actualizar stock: " + e.getMessage());
            throw new RuntimeException("Error al actualizar el stock", e);
        }
    }
}
