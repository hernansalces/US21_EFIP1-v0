package com.trainup;

import com.trainup.view.VentanaLogin;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Usar look and feel del sistema operativo
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Si falla, usa el look and feel por defecto de Swing
        }

        SwingUtilities.invokeLater(() -> new VentanaLogin().setVisible(true));
    }
}
