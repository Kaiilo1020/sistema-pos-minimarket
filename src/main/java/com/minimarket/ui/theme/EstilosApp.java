package com.minimarket.ui.theme;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;

/**
 * Centraliza la paleta cromática y el estilo general de controles reutilizables.
 * Mantiene la aplicación consistente y facilita futuros cambios de diseño.
 */
public final class EstilosApp {

    public static final Color COLOR_PRIMARIO = new Color(0x2E, 0xCC, 0x71);      // Verde #2ECC71
    public static final Color COLOR_SECUNDARIO = new Color(41, 128, 185);   // Azul acero
    public static final Color COLOR_ERROR = new Color(211, 47, 47);         // Rojo de advertencia
    public static final Color COLOR_NEUTRO = new Color(117, 117, 117);      // Gris para acciones suaves

    private EstilosApp() {
    }

    /**
     * Estilo por defecto (botón principal). Usa el color primario de la marca.
     */
    public static void estilizarBoton(JButton boton) {
        estilizarBoton(boton, COLOR_PRIMARIO);
    }

    public static void estilizarBotonSecundario(JButton boton) {
        estilizarBoton(boton, COLOR_SECUNDARIO);
    }

    public static void estilizarBotonError(JButton boton) {
        estilizarBoton(boton, COLOR_ERROR);
    }

    public static void estilizarBotonNeutro(JButton boton) {
        estilizarBoton(boton, COLOR_NEUTRO);
    }

    /**
     * Aplica el estilo Flat Moderno con el color solicitado.
     */
    public static void estilizarBoton(JButton boton, Color backgroundColor) {
        if (boton == null || backgroundColor == null) {
            return;
        }

        boton.setUI(new BasicButtonUI());
        boton.setContentAreaFilled(true);
        boton.setOpaque(true);
        boton.setBorderPainted(false);
        boton.setFocusPainted(false);
        boton.setRolloverEnabled(false);
        boton.setBorder(null);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setBackground(backgroundColor);
        boton.setForeground(Color.WHITE);
        boton.setFont(boton.getFont().deriveFont(Font.BOLD));
        boton.setMargin(new Insets(12, 28, 12, 28));

        Dimension preferred = boton.getPreferredSize();
        preferred.width += 20; // espacio adicional para evitar texto truncado
        preferred.height = Math.max(preferred.height, 42);
        boton.setPreferredSize(preferred);
        boton.setMinimumSize(preferred);

        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                boton.setBackground(backgroundColor.darker());
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                boton.setBackground(backgroundColor);
            }
        });
    }
}

