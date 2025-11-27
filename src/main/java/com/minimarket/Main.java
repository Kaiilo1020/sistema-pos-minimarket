package com.minimarket;

import com.minimarket.config.DatabaseConnection;
import com.minimarket.ui.swing.DashboardFrame;

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

        // Iniciar la aplicación
        iniciarAplicacion();
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
            
            // Configurar propiedades para mejor apariencia
            System.setProperty("awt.useSystemAAFontSettings", "on");
            System.setProperty("swing.aatext", "true");
        } catch (Exception e) {
            // Usar el look and feel por defecto si hay error
        }
    }

    private static boolean verificarConexionBD() {
        try {
            return DatabaseConnection.getInstance().testConnection();
        } catch (Exception e) {
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

    private static void iniciarAplicacion() {
        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                DashboardFrame dashboard = new DashboardFrame();
                dashboard.setVisible(true);
            } catch (Exception e) {
                javax.swing.JOptionPane.showMessageDialog(null, 
                    "❌ Error al iniciar la aplicación: " + e.getMessage(),
                    "Error - Sistema POS Minimarket", 
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
