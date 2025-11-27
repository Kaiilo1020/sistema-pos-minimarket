package com.minimarket.patterns.creational;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Singleton para conexión a base de datos PostgreSQL
 */
public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;
    private static final Logger logger = Logger.getLogger(DatabaseConnection.class.getName());
    
    // Configuración de la base de datos PostgreSQL
    private static final String URL = "jdbc:postgresql://localhost:5432/minimarket_db";
    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "postgres";
    
    // Constructor privado
    private DatabaseConnection() {
        try {
            Class.forName("org.postgresql.Driver");
            this.connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            logger.info("Conexión a la base de datos establecida exitosamente");
        } catch (ClassNotFoundException | SQLException e) {
            logger.severe("Error al establecer conexión con la base de datos: " + e.getMessage());
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
                logger.info("Conexión reestablecida");
            }
        } catch (SQLException e) {
            logger.severe("Error al verificar conexión: " + e.getMessage());
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
            logger.warning("Error al verificar conexión: " + e.getMessage());
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
                logger.info("Reconectando a la base de datos...");
                reconectar();
            }
            
            // Verificar que la conexión esté disponible
            if (connection == null || connection.isClosed()) {
                logger.warning("No se pudo establecer conexión a la base de datos");
                return false;
            }
            
            // Ejecutar consulta simple para probar
            try (var stmt = connection.createStatement();
                 var rs = stmt.executeQuery("SELECT 1")) {
                
                boolean hasResult = rs.next();
                
                if (hasResult) {
                    logger.info("✅ Conexión a la base de datos verificada correctamente");
                    return true;
                } else {
                    logger.warning("❌ La consulta de prueba no devolvió resultados");
                    return false;
                }
            }
            
        } catch (SQLException e) {
            logger.severe("❌ Error al probar conexión: " + e.getMessage());
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
            logger.info("Reconexión exitosa a la base de datos");
            
        } catch (SQLException e) {
            logger.severe("Error al reconectar a la base de datos: " + e.getMessage());
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
            
            logger.info("🔍 Verificando estructura de la base de datos...");
            
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
                    logger.info("   ✅ Tabla encontrada: " + tabla);
                } else {
                    logger.warning("   ❌ Tabla faltante: " + tabla);
                }
                rs.close();
            }
            
            boolean estructuraCompleta = tablasEncontradas == tablasRequeridas.length;
            
            if (estructuraCompleta) {
                logger.info("✅ Estructura de base de datos verificada correctamente");
                
                // Verificar datos iniciales
                verifyInitialData();
            } else {
                logger.warning("❌ Estructura incompleta: " + tablasEncontradas + "/" + tablasRequeridas.length + " tablas encontradas");
            }
            
            return estructuraCompleta;
            
        } catch (SQLException e) {
            logger.severe("❌ Error al verificar estructura: " + e.getMessage());
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
            logger.info("   📊 Usuarios en sistema: " + usuarios);
            
            // Verificar productos
            rs = stmt.executeQuery("SELECT COUNT(*) FROM productos");
            rs.next();
            int productos = rs.getInt(1);
            logger.info("   📦 Productos en inventario: " + productos);
            
            // Verificar configuración
            rs = stmt.executeQuery("SELECT COUNT(*) FROM configuracion_sistema");
            rs.next();
            int configuraciones = rs.getInt(1);
            logger.info("   ⚙️ Parámetros de configuración: " + configuraciones);
            
            rs.close();
            stmt.close();
            
            if (usuarios >= 4 && productos >= 8 && configuraciones >= 8) {
                logger.info("✅ Datos iniciales verificados correctamente");
            } else {
                logger.warning("⚠️ Algunos datos iniciales pueden estar faltando");
            }
            
        } catch (SQLException e) {
            logger.warning("⚠️ Error al verificar datos iniciales: " + e.getMessage());
        }
    }
    
    /**
     * Cierra la conexión
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                logger.info("Conexión cerrada correctamente");
            }
        } catch (SQLException e) {
            logger.severe("Error al cerrar conexión: " + e.getMessage());
        }
    }
}
