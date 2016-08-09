package com.resinet.views;

import com.resinet.util.Strings;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Fenster zum Anzeigen des Hilfetextes.
 */
public class HelpFrame {

    private HelpFrame() {
        JFrame frame = new JFrame(Strings.getLocalizedString("help"));

        JPanel panel = new JPanel(new BorderLayout());
        frame.setContentPane(panel);

        JTextPane aboutPane = new JTextPane();
        aboutPane.setText(Strings.getLocalizedString("help.frame.text"));
        aboutPane.setEditable(false);
        aboutPane.setFocusable(false);
        aboutPane.setOpaque(false);
        aboutPane.setBorder(null);
        panel.add(aboutPane, BorderLayout.CENTER);

        JButton closeBtn = new JButton(Strings.getLocalizedString("close"));
        closeBtn.addActionListener(e -> frame.setVisible(false));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(closeBtn);
        panel.add(buttonPanel, BorderLayout.PAGE_END);

        panel.setBorder(new EmptyBorder(5, 5, 5, 5));

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void show() {
        new HelpFrame();
    }
}
