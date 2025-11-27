package com.minimarket.security;

import com.minimarket.config.DatabaseConnection;
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

    public static synchronized AuditoriaManager getInstance() {
        if (instance == null) {
            instance = new AuditoriaManager();
        }
        return instance;
    }

    public void registrarEvento(String usuario, String tipoEvento, String detalles) {
        Timestamp ahora = Timestamp.valueOf(LocalDateTime.now());
        
        // Intentar primero con auditoria_log, si falla usar logs_sistema
        if (!insertarEnAuditoriaLog(usuario, tipoEvento, detalles, ahora)) {
            insertarEnLogsSistema(usuario, tipoEvento, detalles, ahora);
        }
    }
    
    private boolean insertarEnAuditoriaLog(String usuario, String tipoEvento, String detalles, Timestamp fecha) {
        String sql = "INSERT INTO auditoria_log (usuario, tipo_evento, detalles, fecha_hora, ip_origen) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, usuario);
            pstmt.setString(2, tipoEvento);
            pstmt.setString(3, detalles);
            pstmt.setTimestamp(4, fecha);
            pstmt.setString(5, "127.0.0.1");
            pstmt.executeUpdate();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    private void insertarEnLogsSistema(String usuario, String tipoEvento, String detalles, Timestamp fecha) {
        String sql = "INSERT INTO logs_sistema (usuario, evento, descripcion, fecha_hora) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, usuario);
            pstmt.setString(2, tipoEvento);
            pstmt.setString(3, detalles);
            pstmt.setTimestamp(4, fecha);
            pstmt.executeUpdate();
        } catch (Exception e) {
            // Silencioso - no mostrar errores de auditoría al usuario
        }
    }
}