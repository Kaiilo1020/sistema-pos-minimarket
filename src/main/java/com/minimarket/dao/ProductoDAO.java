package com.minimarket.dao;

import com.minimarket.model.Producto;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO responsable de toda la interacción con la tabla productos.
 */
public class ProductoDAO {

    private static final String SELECT_BASE = """
        SELECT id, codigo, nombre, descripcion, precio, stock, categoria,
               lote, fecha_vencimiento, requiere_lote, requiere_fecha_vencimiento, activo
        FROM productos
        WHERE activo = true AND stock > 0
        AND (fecha_vencimiento IS NULL OR fecha_vencimiento >= CURRENT_DATE)
        """;

    public List<Producto> listarActivos(Connection conn, String filtro) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT_BASE);
        List<Producto> productos = new ArrayList<>();

        if (filtro != null && !filtro.isBlank()) {
            sql.append(" AND (LOWER(nombre) LIKE LOWER(?) OR LOWER(descripcion) LIKE LOWER(?))");
        }

        sql.append(" ORDER BY nombre ASC");

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (filtro != null && !filtro.isBlank()) {
                String patron = "%" + filtro.trim() + "%";
                ps.setString(1, patron);
                ps.setString(2, patron);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    productos.add(mapearProducto(rs));
                }
            }
        }
        return productos;
    }

    public Producto obtenerPorId(Connection conn, Long id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BASE + " AND id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearProducto(rs);
                }
            }
        }
        return null;
    }

    public void descontarStock(Connection conn, long productoId, int cantidadVendida) throws SQLException {
        String sql = "UPDATE productos SET stock = stock - ? WHERE id = ? AND stock >= ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cantidadVendida);
            ps.setLong(2, productoId);
            ps.setInt(3, cantidadVendida);
            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new SQLException("Stock insuficiente para el producto " + productoId);
            }
        }
    }

    /**
     * Lista productos con alertas de vencimiento (próximos 30 días o vencidos)
     * Solo muestra productos activos (los desactivados ya fueron retirados)
     * IMPORTANTE: Siempre obtiene datos frescos de la BD (no usa caché)
     */
    public List<Producto> listarConAlertasVencimiento(Connection conn) throws SQLException {
        // Consulta que siempre obtiene datos actualizados de la BD
        String sql = """
            SELECT id, codigo, nombre, descripcion, precio, stock, categoria,
                   lote, fecha_vencimiento, requiere_lote, requiere_fecha_vencimiento, activo
            FROM productos
            WHERE activo = true 
            AND fecha_vencimiento IS NOT NULL 
            AND fecha_vencimiento <= CURRENT_DATE + INTERVAL '30 days'
            ORDER BY fecha_vencimiento ASC
        """;
        
        List<Producto> productos = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                // Mapear directamente desde el ResultSet (datos frescos de la BD)
                productos.add(mapearProducto(rs));
            }
        }
        return productos;
    }

    /**
     * Desactiva un producto específico por ID y pone su stock en 0
     */
    public void desactivarProducto(Connection conn, Long productoId) throws SQLException {
        String sql = "UPDATE productos SET activo = false, stock = 0 WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, productoId);
            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new SQLException("No se encontró el producto con ID: " + productoId);
            }
        }
    }
    
    /**
     * Retira (desactiva) productos vencidos y pone su stock en 0
     */
    public int retirarVencidos(Connection conn) throws SQLException {
        String sql = """
            UPDATE productos 
            SET activo = false, stock = 0 
            WHERE fecha_vencimiento < CURRENT_DATE 
            AND activo = true
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            return ps.executeUpdate();
        }
    }

    /**
     * Verifica si un código de producto ya existe
     * @param conn Conexión a la base de datos
     * @param codigo Código a verificar
     * @param excluirId ID a excluir de la búsqueda (para ediciones, null si es nuevo)
     * @return true si el código ya existe, false si está disponible
     */
    public boolean existeCodigo(Connection conn, String codigo, Long excluirId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM productos WHERE codigo = ? AND activo = true";
        if (excluirId != null) {
            sql += " AND id != ?";
        }
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, codigo);
            if (excluirId != null) {
                stmt.setLong(2, excluirId);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
    
    /**
     * Busca productos desactivados o con stock 0 que coincidan con el nombre (para autocompletado)
     * Si hay múltiples productos desactivados con el mismo nombre, solo muestra uno (el más reciente)
     * @param conn Conexión a la base de datos
     * @param textoBusqueda Texto a buscar (iniciales o parte del nombre)
     * @return Lista de productos desactivados o con stock 0 que coinciden (sin duplicados por nombre)
     */
    public List<Producto> buscarProductosDesactivadosPorNombre(Connection conn, String textoBusqueda) throws SQLException {
        List<Producto> productos = new ArrayList<>();
        
        if (textoBusqueda == null || textoBusqueda.trim().isEmpty()) {
            return productos;
        }
        
        // Usar DISTINCT ON para evitar duplicados por nombre (muestra solo el más reciente de cada nombre)
        String sql = """
            SELECT DISTINCT ON (nombre) 
                   id, codigo, nombre, descripcion, precio, stock, categoria,
                   lote, fecha_vencimiento, requiere_lote, requiere_fecha_vencimiento, activo
            FROM productos
            WHERE (activo = false OR stock = 0)
            AND LOWER(nombre) LIKE LOWER(?)
            ORDER BY nombre ASC, id DESC
            LIMIT 10
        """;
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, textoBusqueda.trim() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    productos.add(mapearProducto(rs));
                }
            }
        }
        return productos;
    }
    
    /**
     * Verifica si existe un producto activo con el mismo nombre (para evitar duplicados)
     * @param conn Conexión a la base de datos
     * @param nombre Nombre del producto a verificar
     * @param excluirId ID a excluir (para ediciones, null si es nuevo)
     * @return true si existe un producto activo con ese nombre, false si no existe
     */
    public boolean existeProductoActivoPorNombre(Connection conn, String nombre, Long excluirId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM productos WHERE LOWER(nombre) = LOWER(?) AND activo = true";
        if (excluirId != null) {
            sql += " AND id != ?";
        }
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre.trim());
            if (excluirId != null) {
                ps.setLong(2, excluirId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
    
    /**
     * Busca un producto desactivado por nombre exacto (para evitar duplicados)
     * @param conn Conexión a la base de datos
     * @param nombre Nombre exacto del producto
     * @return Producto desactivado o null si no existe
     */
    public Producto buscarProductoDesactivadoPorNombre(Connection conn, String nombre) throws SQLException {
        String sql = """
            SELECT id, codigo, nombre, descripcion, precio, stock, categoria,
                   lote, fecha_vencimiento, requiere_lote, requiere_fecha_vencimiento, activo
            FROM productos
            WHERE activo = false AND LOWER(nombre) = LOWER(?)
            LIMIT 1
        """;
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearProducto(rs);
                }
            }
        }
        return null;
    }
    
    /**
     * Reactiva y actualiza un producto desactivado (evita duplicados)
     * @param conn Conexión a la base de datos
     * @param productoId ID del producto a reactivar
     * @param nombre Nuevo nombre del producto
     * @param descripcion Nueva descripción
     * @param precio Nuevo precio
     * @param stock Nuevo stock
     * @param fechaVencimiento Nueva fecha de vencimiento
     */
    public void reactivarYActualizarProducto(Connection conn, Long productoId, String nombre,
                                             String descripcion, double precio, int stock, 
                                             Date fechaVencimiento) throws SQLException {
        // Verificar una vez más que no exista otro producto activo con el nombre NUEVO
        // (protección contra race conditions)
        if (existeProductoActivoPorNombre(conn, nombre, productoId)) {
            throw new SQLException("No se puede reactivar: ya existe otro producto activo con el nombre '" + nombre + "'.");
        }
        
        String sql = """
            UPDATE productos 
            SET nombre = ?, descripcion = ?, precio = ?, stock = ?, 
                fecha_vencimiento = ?, activo = true, updated_at = CURRENT_TIMESTAMP
            WHERE id = ? AND activo = false
        """;
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, descripcion);
            ps.setDouble(3, precio);
            ps.setInt(4, stock);
            if (fechaVencimiento != null) {
                ps.setDate(5, fechaVencimiento);
            } else {
                ps.setNull(5, Types.DATE);
            }
            ps.setLong(6, productoId);
            
            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new SQLException("No se encontró el producto desactivado con ID: " + productoId + 
                    " o ya está activo.");
            }
        }
    }

    private Producto mapearProducto(ResultSet rs) throws SQLException {
        Producto producto = new Producto();
        producto.setId(rs.getLong("id"));
        producto.setCodigo(rs.getString("codigo"));
        producto.setNombre(rs.getString("nombre"));
        producto.setDescripcion(rs.getString("descripcion"));
        producto.setPrecio(rs.getBigDecimal("precio"));
        producto.setStock(rs.getInt("stock"));
        producto.setCategoria(rs.getString("categoria"));
        producto.setLote(rs.getString("lote"));

        LocalDate fechaVencimiento = rs.getObject("fecha_vencimiento", LocalDate.class);
        producto.setFechaVencimiento(fechaVencimiento);
        producto.setRequiereLote(rs.getBoolean("requiere_lote"));
        producto.setRequiereFechaVencimiento(rs.getBoolean("requiere_fecha_vencimiento"));
        return producto;
    }
}

