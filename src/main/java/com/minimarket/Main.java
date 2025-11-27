package com.minimarket;

import com.minimarket.patterns.creational.DatabaseConnection;

/**
 * Clase Main principal del Sistema POS del Minimarket
 * Abre directamente la interfaz gráfica Swing
 */
public class Main {

    public static void main(String[] args) {
        // Verificar conexión a la base de datos silenciosamente
        if (!verificarConexionBD()) {
            // Si no hay conexión, mostrar error en la GUI
            javax.swing.SwingUtilities.invokeLater(() -> {
                javax.swing.JOptionPane.showMessageDialog(null, 
                    "❌ Error: No se pudo conectar a la base de datos.\n\n" +
                    "💡 Asegúrate de que PostgreSQL esté ejecutándose y ejecuta:\n" +
                    "psql -U postgres -d minimarket_db -f database/actualizacion_pos_rbac.sql",
                    "Error de Conexión - Sistema POS Minimarket", 
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            });
            return;
        }

        // Abrir directamente la interfaz gráfica
        abrirInterfazGrafica();
    }

    private static boolean verificarConexionBD() {
        try {
            return DatabaseConnection.getInstance().testConnection();
        } catch (Exception e) {
            System.out.println("Error de conexión: " + e.getMessage());
            return false;
        }
    }


    private static void abrirInterfazGrafica() {
        try {
            // Configurar Look and Feel moderno
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // Usar el look and feel por defecto si hay error
        }

        // Configurar propiedades del sistema para mejor apariencia
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Ejecutar en el hilo de eventos de Swing
        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                com.minimarket.gui.swing.DashboardFrame dashboard = new com.minimarket.gui.swing.DashboardFrame();
                dashboard.setVisible(true);
            } catch (Exception e) {
                javax.swing.JOptionPane.showMessageDialog(null, 
                    "❌ Error al abrir la interfaz gráfica: " + e.getMessage(),
                    "Error - Sistema POS Minimarket", 
                    javax.swing.JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        });
    }
}
