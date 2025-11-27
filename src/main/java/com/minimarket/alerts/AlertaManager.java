package com.minimarket.alerts;

import com.minimarket.patterns.creational.DatabaseConnection;
import com.minimarket.security.AuditoriaManager;

import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

/**
 * Gestor de alertas del sistema POS
 * Parte de la solución de alertas reactivas
 */
public class AlertaManager {
    private static final Logger LOGGER = Logger.getLogger(AlertaManager.class.getName());
    private static final int UMBRAL_STOCK_CRITICO = 10;

    public void verificarStockCritico(Long productoId, int nuevoStock, String usuario) {
        if (nuevoStock <= UMBRAL_STOCK_CRITICO) {
            String nombreProducto = getNombreProducto(productoId);
            String mensaje = "ALERTA DE STOCK: El producto " + nombreProducto + " tiene " + nuevoStock + " unidades. Revisar Kardex.";
            mostrarAlertaStock(mensaje);
            AuditoriaManager.getInstance().registrarEvento(usuario, "ALERTA_STOCK_CRITICO", mensaje);
            LOGGER.warning(mensaje);
        }
    }

    private String getNombreProducto(Long productoId) {
        String sql = "SELECT nombre FROM productos WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, productoId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("nombre");
            }
        } catch (SQLException e) {
            LOGGER.severe("Error al obtener nombre del producto para alerta: " + e.getMessage());
        }
        return "Desconocido";
    }

    private void mostrarAlertaStock(String mensaje) {
        // Ejecutar en el Event Dispatch Thread de Swing
        SwingUtilities.invokeLater(() -> {
            JDialog dialog = new JDialog();
            dialog.setTitle("Alerta de Stock Crítico");
            dialog.setModal(false); // No bloquea la aplicación principal
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

            JLabel label = new JLabel("<html><p style='width: 250px; text-align: center;'>" + mensaje + "</p></html>", SwingConstants.CENTER);
            label.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
            label.setForeground(java.awt.Color.RED);
            dialog.add(label, java.awt.BorderLayout.CENTER);

            JButton okButton = new JButton("Entendido");
            okButton.addActionListener(e -> dialog.dispose());
            JPanel buttonPanel = new JPanel();
            buttonPanel.add(okButton);
            dialog.add(buttonPanel, java.awt.BorderLayout.SOUTH);

            dialog.pack();
            dialog.setLocationRelativeTo(null); // Centrar en pantalla
            dialog.setVisible(true);

            // Auto-cerrar después de 10 segundos
            Timer timer = new Timer(true);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (dialog.isVisible()) {
                        SwingUtilities.invokeLater(dialog::dispose);
                    }
                    timer.cancel();
                }
            }, 10000); // 10 segundos
        });
    }
}
