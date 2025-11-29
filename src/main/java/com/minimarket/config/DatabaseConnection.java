package com.minimarket.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton para conexión a base de datos PostgreSQL
 */
public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;
    
    // Configuración de la base de datos PostgreSQL
    private static final String URL = "jdbc:postgresql://localhost:5432/minimarket_db";
    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "postgres";
    
    // Constructor privado
    private DatabaseConnection() {
        inicializarDriver();
        reconectar();
    }
    
    /**
     * Obtiene la instancia única (thread-safe)
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
    
    /**
     * Obtiene la conexión actual
     */
    public synchronized Connection getConnection() {
        try {
            // Verificar si la conexión sigue activa
            if (connection == null || connection.isClosed()) {
                reconectar();
            }
        } catch (SQLException e) {
            // Error al verificar conexión
            throw new RuntimeException("Error de conexión a la base de datos", e);
        }
        return connection;
    }
    
    /**
     * Inicia una transacción ACID
     */
    public Connection beginTransaction() throws SQLException {
        Connection conn = getConnection();
        conn.setAutoCommit(false);
        return conn;
    }
    
    public void commit(Connection conn) {
        if (conn == null) {
            return;
        }
        try {
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo confirmar la transacción", e);
        } finally {
            restaurarAutoCommit(conn);
        }
    }
    
    public void rollback(Connection conn) {
        if (conn == null) {
            return;
        }
        try {
            conn.rollback();
        } catch (SQLException e) {
            // Evitar propagar un error adicional y registrar en logs centralizados
        } finally {
            restaurarAutoCommit(conn);
        }
    }
    
    private void restaurarAutoCommit(Connection conn) {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.setAutoCommit(true);
            }
        } catch (SQLException ignored) {
        }
    }
    
    /**
     * Prueba la conexión ejecutando una consulta simple
     */
    public boolean testConnection() {
        try {
            // Si la conexión no existe o está cerrada, intentar reconectar
            if (connection == null || connection.isClosed()) {
                // Reconectando...
                reconectar();
            }
            
            // Verificar que la conexión esté disponible
            if (connection == null || connection.isClosed()) {
                // No se pudo establecer conexión
                return false;
            }
            
            // Ejecutar consulta simple para probar
            try (var stmt = connection.createStatement();
                 var rs = stmt.executeQuery("SELECT 1")) {
                
                boolean hasResult = rs.next();
                
                if (hasResult) {
                    // Conexión verificada
                    return true;
                } else {
                    // La consulta de prueba no devolvió resultados
                    return false;
                }
            }
            
        } catch (SQLException e) {
            // Error al probar conexión
            return false;
        }
    }
    
    /**
     * Método para reconectar a la base de datos
     */
    private void reconectar() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
            
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                // Reconexión exitosa
            
        } catch (SQLException e) {
            throw new RuntimeException("Error al reconectar a la base de datos", e);
        }
    }
    
    private void inicializarDriver() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Driver de PostgreSQL no disponible", e);
        }
    }
}
