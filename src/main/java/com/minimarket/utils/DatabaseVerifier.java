package com.minimarket.utils;

import com.minimarket.patterns.creational.DatabaseConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Utilidad para verificar y diagnosticar la conexión a la base de datos
 */
public class DatabaseVerifier {
    
    private static final Logger logger = Logger.getLogger(DatabaseVerifier.class.getName());
    
    /**
     * Ejecuta una verificación completa de la base de datos
     */
    public static boolean verificarSistemaCompleto() {
        System.out.println("🔍 VERIFICACIÓN COMPLETA DEL SISTEMA DE BASE DE DATOS");
        System.out.println("=" .repeat(60));
        
        boolean todoOk = true;
        
        // 1. Verificar conexión básica
        System.out.println("\n📡 Paso 1: Verificando conexión básica...");
        if (verificarConexionBasica()) {
            System.out.println("✅ Conexión básica: OK");
        } else {
            System.out.println("❌ Conexión básica: FALLO");
            todoOk = false;
        }
        
        // 2. Verificar estructura de tablas
        System.out.println("\n🗃️ Paso 2: Verificando estructura de tablas...");
        if (verificarEstructuraTablas()) {
            System.out.println("✅ Estructura de tablas: OK");
        } else {
            System.out.println("❌ Estructura de tablas: FALLO");
            todoOk = false;
        }
        
        // 3. Verificar datos iniciales
        System.out.println("\n📊 Paso 3: Verificando datos iniciales...");
        if (verificarDatosIniciales()) {
            System.out.println("✅ Datos iniciales: OK");
        } else {
            System.out.println("⚠️ Datos iniciales: INCOMPLETOS");
            // No marcar como fallo crítico
        }
        
        // 4. Verificar permisos
        System.out.println("\n🔐 Paso 4: Verificando permisos...");
        if (verificarPermisos()) {
            System.out.println("✅ Permisos: OK");
        } else {
            System.out.println("❌ Permisos: FALLO");
            todoOk = false;
        }
        
        // 5. Verificar rendimiento básico
        System.out.println("\n⚡ Paso 5: Verificando rendimiento...");
        long tiempoRespuesta = verificarRendimiento();
        if (tiempoRespuesta > 0) {
            System.out.println("✅ Rendimiento: OK (" + tiempoRespuesta + "ms)");
        } else {
            System.out.println("⚠️ Rendimiento: No se pudo medir");
        }
        
        // Resultado final
        System.out.println("\n" + "=" .repeat(60));
        if (todoOk) {
            System.out.println("🎉 VERIFICACIÓN COMPLETADA: SISTEMA LISTO PARA USAR");
        } else {
            System.out.println("❌ VERIFICACIÓN FALLIDA: REVISAR CONFIGURACIÓN");
        }
        System.out.println("=" .repeat(60));
        
        return todoOk;
    }
    
    /**
     * Verificación rápida de conexión
     */
    public static boolean verificacionRapida() {
        System.out.println("⚡ VERIFICACIÓN RÁPIDA DE CONEXIÓN");
        System.out.println("-" .repeat(40));
        
        try {
            DatabaseConnection dbConnection = DatabaseConnection.getInstance();
            
            // Usar directamente testConnection() que es más confiable
            boolean connected = dbConnection.testConnection();
            
            if (connected) {
                System.out.println("✅ Estado: CONECTADO");
                System.out.println("✅ Prueba: EXITOSA");
                System.out.println("🎉 Base de datos lista para usar!");
                return true;
            } else {
                System.out.println("❌ Estado: DESCONECTADO");
                System.out.println("❌ Prueba: FALLIDA");
                System.out.println("💡 Verifica que PostgreSQL esté ejecutándose");
            }
            
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            System.out.println("💡 Verifica la configuración de la base de datos");
        }
        
        return false;
    }
    
    /**
     * Muestra información detallada de la conexión
     */
    public static void mostrarInformacionConexion() {
        System.out.println("📋 INFORMACIÓN DETALLADA DE CONEXIÓN");
        System.out.println("=" .repeat(50));
        
        try {
            DatabaseConnection dbConnection = DatabaseConnection.getInstance();
            String info = dbConnection.getConnectionInfo();
            System.out.println(info);
            
        } catch (Exception e) {
            System.out.println("❌ Error al obtener información: " + e.getMessage());
        }
    }
    
    // Métodos privados para verificaciones específicas
    
    private static boolean verificarConexionBasica() {
        try {
            DatabaseConnection dbConnection = DatabaseConnection.getInstance();
            return dbConnection.testConnection();
        } catch (Exception e) {
            System.out.println("   ❌ Error: " + e.getMessage());
            return false;
        }
    }
    
    private static boolean verificarEstructuraTablas() {
        try {
            DatabaseConnection dbConnection = DatabaseConnection.getInstance();
            return dbConnection.verifyDatabaseStructure();
        } catch (Exception e) {
            System.out.println("   ❌ Error: " + e.getMessage());
            return false;
        }
    }
    
    private static boolean verificarDatosIniciales() {
        try {
            Connection connection = DatabaseConnection.getInstance().getConnection();
            
            // Verificar usuarios
            var stmt = connection.createStatement();
            var rs = stmt.executeQuery("SELECT COUNT(*) FROM usuarios WHERE activo = true");
            rs.next();
            int usuarios = rs.getInt(1);
            System.out.println("   👥 Usuarios activos: " + usuarios);
            
            // Verificar productos
            rs = stmt.executeQuery("SELECT COUNT(*) FROM productos WHERE activo = true");
            rs.next();
            int productos = rs.getInt(1);
            System.out.println("   📦 Productos activos: " + productos);
            
            // Verificar categorías
            rs = stmt.executeQuery("SELECT COUNT(*) FROM categorias WHERE activo = true");
            rs.next();
            int categorias = rs.getInt(1);
            System.out.println("   🏷️ Categorías: " + categorias);
            
            // Verificar configuración
            rs = stmt.executeQuery("SELECT COUNT(*) FROM configuracion_sistema");
            rs.next();
            int configuraciones = rs.getInt(1);
            System.out.println("   ⚙️ Configuraciones: " + configuraciones);
            
            rs.close();
            stmt.close();
            
            return usuarios >= 4 && productos >= 8 && categorias >= 7 && configuraciones >= 8;
            
        } catch (SQLException e) {
            System.out.println("   ❌ Error: " + e.getMessage());
            return false;
        }
    }
    
    private static boolean verificarPermisos() {
        try {
            Connection connection = DatabaseConnection.getInstance().getConnection();
            
            // Probar SELECT
            var stmt = connection.createStatement();
            stmt.executeQuery("SELECT 1 FROM usuarios LIMIT 1").close();
            System.out.println("   ✅ Permiso SELECT: OK");
            
            // Probar INSERT (en tabla de logs)
            stmt.executeUpdate("INSERT INTO logs_sistema (nivel, mensaje, usuario) VALUES ('INFO', 'Prueba de permisos', 'SISTEMA')");
            System.out.println("   ✅ Permiso INSERT: OK");
            
            // Probar UPDATE
            stmt.executeUpdate("UPDATE logs_sistema SET mensaje = 'Prueba actualizada' WHERE mensaje = 'Prueba de permisos'");
            System.out.println("   ✅ Permiso UPDATE: OK");
            
            // Probar DELETE
            stmt.executeUpdate("DELETE FROM logs_sistema WHERE mensaje = 'Prueba actualizada'");
            System.out.println("   ✅ Permiso DELETE: OK");
            
            stmt.close();
            return true;
            
        } catch (SQLException e) {
            System.out.println("   ❌ Error de permisos: " + e.getMessage());
            return false;
        }
    }
    
    private static long verificarRendimiento() {
        try {
            Connection connection = DatabaseConnection.getInstance().getConnection();
            
            long inicio = System.currentTimeMillis();
            
            // Ejecutar consulta compleja
            var stmt = connection.createStatement();
            var rs = stmt.executeQuery(
                "SELECT u.nombre, COUNT(v.id) as ventas " +
                "FROM usuarios u " +
                "LEFT JOIN ventas v ON u.id = v.cajera_id " +
                "WHERE u.rol = 'CAJERA' " +
                "GROUP BY u.id, u.nombre"
            );
            
            int filas = 0;
            while (rs.next()) {
                filas++;
            }
            
            long fin = System.currentTimeMillis();
            long tiempo = fin - inicio;
            
            rs.close();
            stmt.close();
            
            System.out.println("   📊 Consulta ejecutada: " + filas + " filas en " + tiempo + "ms");
            
            return tiempo;
            
        } catch (SQLException e) {
            System.out.println("   ❌ Error de rendimiento: " + e.getMessage());
            return -1;
        }
    }
    
    /**
     * Diagnóstico de problemas comunes
     */
    public static void diagnosticarProblemas() {
        System.out.println("🔧 DIAGNÓSTICO DE PROBLEMAS COMUNES");
        System.out.println("=" .repeat(50));
        
        // Verificar driver
        try {
            Class.forName("org.postgresql.Driver");
            System.out.println("✅ Driver PostgreSQL: Encontrado");
        } catch (ClassNotFoundException e) {
            System.out.println("❌ Driver PostgreSQL: NO ENCONTRADO");
            System.out.println("   💡 Solución: Agregar postgresql-42.7.1.jar al classpath");
            System.out.println("   💡 O ejecutar: mvn clean install");
        }
        
        // Verificar configuración
        System.out.println("\n📋 Configuración actual:");
        System.out.println("   URL: jdbc:postgresql://localhost:5432/minimarket_db");
        System.out.println("   Usuario: postgres");
        System.out.println("   💡 Verifica que estos datos sean correctos");
        
        // Sugerencias
        System.out.println("\n💡 SOLUCIONES COMUNES:");
        System.out.println("   1. Verificar que PostgreSQL esté ejecutándose");
        System.out.println("   2. Verificar usuario y contraseña");
        System.out.println("   3. Verificar que la base de datos 'minimarket_db' exista");
        System.out.println("   4. Ejecutar: database/setup_database.bat");
        System.out.println("   5. Verificar firewall/puertos (5432)");
    }
    
    /**
     * Método principal para ejecutar verificaciones
     */
    public static void main(String[] args) {
        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "rapida":
                case "quick":
                    verificacionRapida();
                    break;
                case "info":
                    mostrarInformacionConexion();
                    break;
                case "diagnostico":
                case "diagnostic":
                    diagnosticarProblemas();
                    break;
                default:
                    verificarSistemaCompleto();
            }
        } else {
            verificarSistemaCompleto();
        }
    }
}
