package com.minimarket;

import com.minimarket.config.DatabaseConnection;
import com.minimarket.ui.swing.LoginFrame;

/**
 * Clase Main principal del Sistema POS del Minimarket
 * Punto de entrada único - Abre directamente la interfaz gráfica
 */
public class Main {

    public static void main(String[] args) {
        // Configurar Look and Feel
        configurarLookAndFeel();
        
        // Verificar conexión a la base de datos
        if (!verificarConexionBD()) {
            mostrarErrorConexion();
            return;
        }

        // Mostrar pantalla de login
        mostrarLogin();
    }

    private static void configurarLookAndFeel() {
        try {
            // Configurar Look and Feel moderno Nimbus
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
            
            // Deshabilitar bordes de botones en Nimbus para estilo flat
            javax.swing.UIManager.put("Button.border", javax.swing.BorderFactory.createEmptyBorder());
            javax.swing.UIManager.put("nimbusBorder", javax.swing.BorderFactory.createEmptyBorder());
            
            // Configurar propiedades para mejor apariencia
            System.setProperty("awt.useSystemAAFontSettings", "on");
            System.setProperty("swing.aatext", "true");
        } catch (Exception e) {
            // Usar el look and feel por defecto si hay error
        }
    }

    private static boolean verificarConexionBD() {
        try {
            // Intentar obtener la conexión - si funciona, la BD está disponible
            DatabaseConnection.getInstance().getConnection();
            return true;
        } catch (Exception e) {
            // Si hay error, la conexión no está disponible
            return false;
        }
    }

    private static void mostrarErrorConexion() {
        javax.swing.SwingUtilities.invokeLater(() -> {
            javax.swing.JOptionPane.showMessageDialog(null, 
                "❌ Error: No se pudo conectar a la base de datos.\n\n" +
                "💡 Asegúrate de que PostgreSQL esté ejecutándose y ejecuta:\n" +
                "psql -U postgres -d minimarket_db -f database/actualizacion_pos_personalizada.sql",
                "Error de Conexión - Sistema POS Minimarket", 
                javax.swing.JOptionPane.ERROR_MESSAGE);
        });
    }

    private static void mostrarLogin() {
        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                new LoginFrame();
            } catch (Exception e) {
                javax.swing.JOptionPane.showMessageDialog(null, 
                    "❌ Error al iniciar la aplicación: " + e.getMessage(),
                    "Error - Sistema POS Minimarket", 
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
