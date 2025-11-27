package com.minimarket.security;

import com.minimarket.patterns.creational.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Singleton para gestionar auditoría del sistema
 * Parte de la solución RBAC del sistema POS
 */
public class AuditoriaManager {
    private static AuditoriaManager instance;

    private AuditoriaManager() {
        // Constructor privado para Singleton
    }

    public static AuditoriaManager getInstance() {
        if (instance == null) {
            instance = new AuditoriaManager();
        }
        return instance;
    }

    public void registrarEvento(String usuario, String tipoEvento, String detalles) {
        // Intentar primero con auditoria_log, si falla usar logs_sistema
        String sql = "INSERT INTO auditoria_log (usuario, tipo_evento, detalles, fecha_hora, ip_origen) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, usuario);
            pstmt.setString(2, tipoEvento);
            pstmt.setString(3, detalles);
            pstmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setString(5, "127.0.0.1"); // IP de origen (simplificado para demo)
            pstmt.executeUpdate();
        } catch (Exception e) {
            // Si falla auditoria_log, intentar con logs_sistema (tabla existente)
            try {
                String sqlFallback = "INSERT INTO logs_sistema (usuario, evento, descripcion, fecha_hora) VALUES (?, ?, ?, ?)";
                try (Connection conn = DatabaseConnection.getInstance().getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sqlFallback)) {
                    pstmt.setString(1, usuario);
                    pstmt.setString(2, tipoEvento);
                    pstmt.setString(3, detalles);
                    pstmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                    pstmt.executeUpdate();
                }
            } catch (Exception e2) {
                System.err.println("Error al registrar evento de auditoría: " + e2.getMessage());
            }
        }
    }
}