package com.minimarket.gui;

import com.minimarket.gui.swing.DashboardFrame;
import javax.swing.*;

/**
 * Aplicación principal con Swing para el Sistema de Ventas del Minimarket
 * Dashboard moderno enfocado en demostrar los patrones de diseño
 */
public class MinimarketSwingApp {

    public static void main(String[] args) {
        // Configurar Look and Feel moderno
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
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
        SwingUtilities.invokeLater(() -> {
            try {
                DashboardFrame dashboard = new DashboardFrame();
                dashboard.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, 
                    "Error al iniciar la aplicación: " + e.getMessage(),
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
