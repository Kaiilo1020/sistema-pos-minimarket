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
        try {
            Class.forName("org.postgresql.Driver");
            this.connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            // Conexión establecida silenciosamente
        } catch (ClassNotFoundException | SQLException e) {
            // Error de conexión
            throw new RuntimeException("No se pudo conectar a la base de datos", e);
        }
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
    public Connection getConnection() {
        try {
            // Verificar si la conexión sigue activa
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                // Conexión reestablecida
            }
        } catch (SQLException e) {
            // Error al verificar conexión
            throw new RuntimeException("Error de conexión a la base de datos", e);
        }
        return connection;
    }
    
    /**
     * Verifica si la conexión está activa
     */
    public boolean isConnected() {
        try {
            if (connection == null) {
                return false;
            }
            
            // Verificar si la conexión está cerrada
            if (connection.isClosed()) {
                return false;
            }
            
            // Verificar si la conexión es válida (con timeout más corto)
            return connection.isValid(2);
            
        } catch (SQLException e) {
            // Error al verificar conexión
            return false;
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
            // Error al reconectar
        }
    }
    
    /**
     * Obtiene información detallada de la conexión
     */
    public String getConnectionInfo() {
        try {
            if (connection == null) {
                return "❌ Conexión no inicializada";
            }
            
            if (connection.isClosed()) {
                return "❌ Conexión cerrada";
            }
            
            var metaData = connection.getMetaData();
            StringBuilder info = new StringBuilder();
            info.append("✅ INFORMACIÓN DE CONEXIÓN:\n");
            info.append("   URL: ").append(metaData.getURL()).append("\n");
            info.append("   Usuario: ").append(metaData.getUserName()).append("\n");
            info.append("   Base de datos: ").append(metaData.getDatabaseProductName()).append("\n");
            info.append("   Versión: ").append(metaData.getDatabaseProductVersion()).append("\n");
            info.append("   Driver: ").append(metaData.getDriverName()).append("\n");
            info.append("   Versión driver: ").append(metaData.getDriverVersion()).append("\n");
            info.append("   Estado: ").append(connection.isValid(5) ? "ACTIVA" : "INACTIVA").append("\n");
            
            return info.toString();
            
        } catch (SQLException e) {
            return "❌ Error al obtener información: " + e.getMessage();
        }
    }
    
    /**
     * Verifica la conexión y las tablas principales
     */
    public boolean verifyDatabaseStructure() {
        try {
            if (!testConnection()) {
                return false;
            }
            
            // Verificando estructura de la base de datos
            
            // Tablas que deben existir
            String[] tablasRequeridas = {
                "usuarios", "productos", "ventas", "detalle_ventas", 
                "categorias", "auditoria_precios", "alertas_inventario",
                "solicitudes_aprobacion", "configuracion_sistema", "logs_sistema"
            };
            
            var metaData = connection.getMetaData();
            int tablasEncontradas = 0;
            
            for (String tabla : tablasRequeridas) {
                var rs = metaData.getTables(null, null, tabla, new String[]{"TABLE"});
                if (rs.next()) {
                    tablasEncontradas++;
                    // Tabla encontrada
                } else {
                    // Tabla faltante
                }
                rs.close();
            }
            
            boolean estructuraCompleta = tablasEncontradas == tablasRequeridas.length;
            
            if (estructuraCompleta) {
                // Estructura verificada correctamente
                
                // Verificar datos iniciales
                verifyInitialData();
            } else {
                // Estructura incompleta
            }
            
            return estructuraCompleta;
            
        } catch (SQLException e) {
            // Error al verificar estructura
            return false;
        }
    }
    
    /**
     * Verifica que existan datos iniciales
     */
    private void verifyInitialData() {
        try {
            // Verificar usuarios
            var stmt = connection.createStatement();
            var rs = stmt.executeQuery("SELECT COUNT(*) FROM usuarios");
            rs.next();
            int usuarios = rs.getInt(1);
            // Usuarios en sistema
            
            // Verificar productos
            rs = stmt.executeQuery("SELECT COUNT(*) FROM productos");
            rs.next();
            int productos = rs.getInt(1);
            // Productos en inventario
            
            // Verificar configuración
            rs = stmt.executeQuery("SELECT COUNT(*) FROM configuracion_sistema");
            rs.next();
            int configuraciones = rs.getInt(1);
            // Parámetros de configuración
            
            rs.close();
            stmt.close();
            
            if (usuarios >= 4 && productos >= 8 && configuraciones >= 8) {
                // Datos iniciales verificados
            } else {
                // Algunos datos iniciales pueden estar faltando
            }
            
        } catch (SQLException e) {
            // Error al verificar datos iniciales
        }
    }
    
    /**
     * Cierra la conexión
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                // Conexión cerrada correctamente
            }
        } catch (SQLException e) {
            // Error al cerrar conexión
        }
    }
}
