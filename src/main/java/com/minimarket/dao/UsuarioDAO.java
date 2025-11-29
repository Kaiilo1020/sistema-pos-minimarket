package com.minimarket.dao;

import com.minimarket.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para operaciones de usuarios en la base de datos.
 */
public class UsuarioDAO {

    /**
     * Obtiene un usuario por username
     */
    public Usuario obtenerPorUsername(Connection conn, String username) throws SQLException {
        String sql = "SELECT id, username, email, rol, activo, password, nombre, apellido " +
                    "FROM usuarios WHERE username = ? AND activo = true";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearUsuario(rs);
                }
            }
        }
        return null;
    }

    /**
     * Obtiene un usuario por ID
     */
    public Usuario obtenerPorId(Connection conn, Long id) throws SQLException {
        String sql = "SELECT id, username, email, rol, activo, password, nombre, apellido " +
                    "FROM usuarios WHERE id = ? AND activo = true";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearUsuario(rs);
                }
            }
        }
        return null;
    }

    /**
     * Lista todos los usuarios activos con información de ventas
     */
    public List<UsuarioInfo> listarConVentas(Connection conn) throws SQLException {
        List<UsuarioInfo> usuarios = new ArrayList<>();
        
        String sql = """
            SELECT u.id, u.username, u.rol, 
                   COUNT(v.id) as total_ventas
            FROM usuarios u
            LEFT JOIN ventas v ON u.id = v.cajera_id AND v.estado = 'ACTIVA'
            WHERE u.activo = true
            GROUP BY u.id, u.username, u.rol
            ORDER BY u.username
        """;
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                UsuarioInfo info = new UsuarioInfo();
                info.id = rs.getLong("id");
                info.username = rs.getString("username");
                info.rol = rs.getString("rol");
                info.totalVentas = rs.getInt("total_ventas");
                usuarios.add(info);
            }
        }
        
        return usuarios;
    }

    /**
     * Verifica si un username ya existe
     */
    public boolean existeUsername(Connection conn, String username, Long excluirId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE username = ? AND activo = true";
        if (excluirId != null) {
            sql += " AND id != ?";
        }
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
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
     * Verifica si un email ya existe
     */
    public boolean existeEmail(Connection conn, String email, Long excluirId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE email = ? AND activo = true";
        if (excluirId != null) {
            sql += " AND id != ?";
        }
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
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
     * Verifica si un usuario tiene ventas registradas
     */
    public boolean tieneVentas(Connection conn, Long usuarioId) throws SQLException {
        String sql = "SELECT COUNT(*) as total_ventas FROM ventas WHERE cajera_id = ? AND estado = 'ACTIVA'";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, usuarioId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total_ventas") > 0;
                }
            }
        }
        return false;
    }

    /**
     * Crea un nuevo usuario
     */
    public void crear(Connection conn, String username, String password, String nombre, 
                     String apellido, String email, String rol) throws SQLException {
        String sql = """
            INSERT INTO usuarios (username, password, nombre, apellido, email, rol, activo) 
            VALUES (?, ?, ?, ?, ?, ?, true)
        """;
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, nombre);
            pstmt.setString(4, apellido);
            pstmt.setString(5, email);
            pstmt.setString(6, rol);
            pstmt.executeUpdate();
        }
    }

    /**
     * Actualiza un usuario existente
     */
    public void actualizar(Connection conn, Long id, String username, String password, 
                           String nombre, String apellido, String email, String rol, 
                           boolean actualizarPassword) throws SQLException {
        String sql;
        if (actualizarPassword) {
            sql = """
                UPDATE usuarios SET username = ?, password = ?, nombre = ?, apellido = ?, 
                       email = ?, rol = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
            """;
        } else {
            sql = """
                UPDATE usuarios SET username = ?, nombre = ?, apellido = ?, email = ?, rol = ?, 
                       updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
            """;
        }
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int paramIndex = 1;
            pstmt.setString(paramIndex++, username);
            if (actualizarPassword) {
                pstmt.setString(paramIndex++, password);
            }
            pstmt.setString(paramIndex++, nombre);
            pstmt.setString(paramIndex++, apellido);
            pstmt.setString(paramIndex++, email);
            pstmt.setString(paramIndex++, rol);
            pstmt.setLong(paramIndex, id);
            pstmt.executeUpdate();
        }
    }

    /**
     * Elimina (desactiva) un usuario
     */
    public void eliminar(Connection conn, Long id) throws SQLException {
        String sql = "UPDATE usuarios SET activo = false WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        }
    }

    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getLong("id"));
        usuario.setUsername(rs.getString("username"));
        usuario.setEmail(rs.getString("email"));
        usuario.setRol(com.minimarket.security.Rol.valueOf(rs.getString("rol")));
        usuario.setNombre(rs.getString("nombre"));
        usuario.setApellido(rs.getString("apellido"));
        return usuario;
    }

    /**
     * Clase auxiliar para información de usuario con ventas
     */
    public static class UsuarioInfo {
        public Long id;
        public String username;
        public String rol;
        public int totalVentas;
    }
}

